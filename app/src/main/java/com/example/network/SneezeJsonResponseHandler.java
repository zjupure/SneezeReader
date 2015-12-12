package com.example.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.database.DBManager;
import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.jsonparser.ArticleData;
import com.example.jsonparser.JsonParserUtil;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by liuchun on 2015/12/6.
 */
public class SneezeJsonResponseHandler extends TextHttpResponseHandler {
    private Context context;
    private int type;  // page indicator
    private int limit;
    private List<Article> articles;
    private DBManager dbManager;
    private SneezeClient client;

    public SneezeJsonResponseHandler(Context context, int type){
        this(context, type, 30);
    }

    public SneezeJsonResponseHandler(Context context, int type, int limit){
        this.context = context;
        this.type = type;
        this.limit = limit;

        articles = new ArrayList<>();
        dbManager = DBManager.getInstance(context);
        client = SneezeClient.getInstance(context);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

        Log.d("JsonResponse", "network error");
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString) {

        boolean isUpdated = false;

        ArticleData[] datas = JsonParserUtil.JsonArticleParser(responseString);

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
            // insert the new record into database
            dbManager.insertMultiRecords(articles);
            // get the latest articles from database
            List<Article> datainfos = dbManager.getData(type, limit);
            // update the dataset
            DataManager.getInstance().setDataset(type, datainfos);
        }

        //DataManager.getInstance().addDataset(articles);

        if(type != Article.DUANZI){
            downLoadPages(articles);
        }

    }

    /**
     * 发起下载页面源码请求
     * @param articles
     */
    private void downLoadPages(List<Article> articles){

        for(Article article : articles){
            String remote_url = article.getDescription(); // subscribe url
            client.getPageContent(remote_url, new SneezePageResponseHandler(context, remote_url));
        }
    }
}
