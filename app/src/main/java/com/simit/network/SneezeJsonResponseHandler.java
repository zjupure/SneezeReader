package com.simit.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.fragment.ItemFragment;
import com.simit.jsonparser.ArticleData;
import com.simit.jsonparser.JsonParserUtil;
import com.simit.sneezereader.Config;
import com.simit.sneezereader.DetailActivity;
import com.simit.sneezereader.MainActivity;
import com.simit.sneezereader.R;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * Created by liuchun on 2015/12/6.
 */
public class SneezeJsonResponseHandler extends TextHttpResponseHandler {
    private static final int NEW_ARTICLE_ARRIVAL = 10; // notification id
    //
    private Context context;
    private int type;  // page indicator
    private Handler handler;  // send message to main activity
    private DBManager dbManager;
    private SneezeClient client;

    public SneezeJsonResponseHandler(Context context, int type){
        this(context, type, null);
    }

    public SneezeJsonResponseHandler(Context context, int type, Handler handler){
        this.context = context;
        this.type = type;
        this.handler = handler;

        dbManager = DBManager.getInstance(context);
        client = SneezeClient.getInstance(context);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

        Log.d("JsonResponse", "fetch data failed!");
        if(handler != null){
            handler.sendEmptyMessage(Config.NETWORK_ERROR);
        }
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString) {

        boolean isUpdated = false;

        ArticleData[] datas = JsonParserUtil.JsonArticleParser(responseString);
        List<Article> articles = new ArrayList<>();

        for(ArticleData data : datas){
            if(data.getTitle().equals("AD")){
                continue;  // filter advertisement
            }

            String description = data.getDescription();
            if(dbManager.isExist(description)){
                String local_url = dbManager.getLocalUrl(description);
                // 虽然在数据库中存在, 但是页面源码还未获取
                int networkState = NetworkMonitor.getNetWorkState(context);
                if(type != Article.DUANZI && local_url.isEmpty()
                         && networkState == NetworkMonitor.WIFI){
                    // 去请求获取页面源码
                    downLoadPages(articles);
                }
                continue;  // exist in the database
            }

            isUpdated = true;

            // fix the pubDate bug
            String title = data.getTitle();
            String pubDate = data.getPubDate(); // 2015-11-26 14:27:00
            String date = pubDate.substring(0, 10);
            date = date.replace("-", "");
            Pattern pattern = Pattern.compile("\\d{8}"); // ^[\u3010].*\d{8}[\u3011]$ 匹配图卦标题
            Matcher matcher = pattern.matcher(title);
            if(matcher.find()){
                String realDate = matcher.group();
                if(!realDate.equals(date)){
                    pubDate = realDate.substring(0, 4) + "-" + realDate.substring(4, 6) +
                            "-" + realDate.substring(6, 8) + pubDate.substring(10, pubDate.length());

                    Log.d("JsonResponse", pubDate);
                }
            }

            Article article = new Article();
            article.setType(type);
            article.setTitle(data.getTitle());
            article.setRemote_link(data.getLink());
            article.setAuthor(data.getAuthor());
            article.setPubDate(pubDate);
            article.setDescription(data.getDescription());
            article.setImgurl(data.getImgurl());
            article.setLocal_link("");

            articles.add(article);
        }

        if(isUpdated){
            Article article = articles.get(0);
            if(article.getType() == Article.TUGUA){
                notifyNewArticle(article);
            }
            // insert the new record into database
            dbManager.insertMultiRecords(articles);

            if(handler != null){
                Message message = handler.obtainMessage();
                message.what = Config.NEW_ARTICLE_ARRIVAL;
                message.arg1 = articles.size();
                handler.sendMessage(message);
            }else{
                // get the latest articles from database
                List<Article> datainfos = dbManager.getData(type, 30);
                // update the dataset
                DataManager.getInstance().updateDataset(type, datainfos);
                // send broadcast
                Intent intent = new Intent(Config.DATASET_UPDATED_ACTION);
                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                broadcastManager.sendBroadcast(intent);
            }

            // 新的页面,加载页面源码,只有wifi状态下才加载源码
            int networkState = NetworkMonitor.getNetWorkState(context);
            if(type != Article.DUANZI && networkState == NetworkMonitor.WIFI){
                Log.d("PageResponse", "start to get page source");
                // 去请求获取页面源码
                downLoadPages(articles);
            }
        }else{
            if(handler != null){
                // 没有新的数据
                handler.sendEmptyMessage(Config.NO_NEW_ARTICLE);
            }
        }
    }

    private void notifyNewArticle(Article article){
        Context app = context.getApplicationContext();
        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        // init notification
        CharSequence tickerText = article.getTitle();
        long when = System.currentTimeMillis();

        CharSequence contentTitle = article.getTitle();
        CharSequence contentText = article.getPubDate();
        Intent intent = new Intent(context, DetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        intent.putExtra("detail", bundle);
        intent.putExtra("position", 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.logo)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(pendingIntent)
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setAutoCancel(true);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                notification = builder.build();
            }else {
                notification = builder.getNotification();
            }

        }else{
            notification = new Notification();
            notification.icon = R.mipmap.logo;
            notification.tickerText = tickerText;
            notification.when = when;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.contentIntent = pendingIntent;

        }
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;
        nm.notify(NEW_ARTICLE_ARRIVAL, notification);
    }

    /**
     * 发起下载页面源码请求
     * @param articles
     */
    private void downLoadPages(List<Article> articles){
        // 遍历下载源码
        for(Article article : articles){
            String remote_url = article.getDescription(); // subscribe url
            client.getPageContent(remote_url, new SneezePageResponseHandler(context, remote_url));
        }
    }
}
