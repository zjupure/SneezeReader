package com.example.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.database.DBManager;
import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.fragment.ItemFragment;
import com.example.jsonparser.ArticleData;
import com.example.jsonparser.JsonParserUtil;
import com.example.sneezereader.DetailActivity;
import com.example.sneezereader.MainActivity;
import com.example.sneezereader.R;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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
            handler.sendEmptyMessage(ItemFragment.NETWORK_ERROR);
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

            String remote_url = data.getLink();
            if(dbManager.isExist(remote_url)){
                continue;  // exist in the database
            }

            isUpdated = true;

            Article article = new Article();
            article.setType(type);
            article.setTitle(data.getTitle());
            article.setRemote_link(data.getLink());
            article.setAuthor(data.getAuthor());
            article.setPubDate(data.getPubDate());
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
                message.what = ItemFragment.NEW_ARTICLE_ARRIVAL;
                message.arg1 = articles.size();
                handler.sendMessage(message);
            }else{
                // get the latest articles from database
                List<Article> datainfos = dbManager.getData(type, 30);
                // update the dataset
                DataManager.getInstance().updateDataset(type, datainfos);
                // send broadcast
                Intent intent = new Intent(ItemFragment.DATASET_UPDATED_ACTION);
                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                broadcastManager.sendBroadcast(intent);
            }
        }else{
            if(handler != null){
                // 没有新的数据
                handler.sendEmptyMessage(ItemFragment.NO_NEW_ARTICLE);
            }
        }

        if(type != Article.DUANZI){
            Log.d("PageResponse", "start to get page source");
            // 放到子线程去处理
            downLoadPages(articles);
        }

    }

    private void notifyNewArticle(Article article){
        Context app = context.getApplicationContext();
        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        // init notification
        int icon = R.mipmap.logo;
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

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.logo)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setTicker(tickerText)
                .setWhen(when)
                .setAutoCancel(true);

        Notification notification = builder.build();
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
