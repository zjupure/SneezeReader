package com.simit.sneezereader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.jsonparser.JsonParserUtil;
import com.simit.network.SneezeClient;
import com.simit.network.SneezeJsonResponseHandler;
import com.simit.network.SneezePageResponseHandler;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.TextHttpResponseHandler;


import java.util.List;

import cz.msebera.android.httpclient.Header;


/**
 * Created by liuchun on 2015/7/18.
 */
public class SplashActivity extends Activity {
    //闪屏页
    private static final int SPLASH_LONG = 1500;
    private static final int SPLASH_SHORT = 500;
    //
    private Handler handler = new Handler();
    //
    private SimpleDraweeView splash;
    private String imgUrl = "";

    private int appStartNum = 0;     // app启动次数
    private boolean isFinished;

    private SneezeClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_layout);
        //
        splash = (SimpleDraweeView) findViewById(R.id.splash_image);
        //
        imgUrl = loadImgUrl();
        if(!imgUrl.isEmpty()){
            Uri uri = Uri.parse(imgUrl);
            splash.setImageURI(uri);
        }
        // exec Network request
        execNetworkRequest();
        // load data from database
        loadData();
        // 延时进入主页
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //entry the main activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // destroy itself
                finish();
                isFinished = true;
            }
        }, SPLASH_SHORT);
    }

    private void execNetworkRequest(){
        // request for the new picture
        client = SneezeClient.getInstance(this);
        //
        appStartNum = loadAppStartNum();
        if(appStartNum == 0){
            // 首次启动,请求100条数据
            client.setLimitNum(100);
        }
        // appStartNum increase
        saveAppStartNum(appStartNum + 1);
        // 请求文章
        client.getArticle(Article.TUGUA, new SneezeJsonResponseHandler(this, Article.TUGUA));
        client.getArticle(Article.LEHUO, new SneezeJsonResponseHandler(this, Article.LEHUO));
        client.getArticle(Article.YITU, new SneezeJsonResponseHandler(this, Article.YITU));
        client.getArticle(Article.DUANZI, new SneezeJsonResponseHandler(this, Article.DUANZI));
        // 请求首页展示图片
        client.getSplashImage(new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("SplashActivity", "Load Image Failed!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                // parser the url from json data
                String loadUrl = JsonParserUtil.JsonLoadUrlParser(responseString);
                // update the splash image url
                if(!loadUrl.equals(imgUrl)){
                    // new url
                    saveImgUrl(loadUrl);
                    if(isFinished == false){
                        // this activity is active
                        Uri uri = Uri.parse(loadUrl);
                        splash.setImageURI(uri);
                    }
                }
            }
        });
    }

    private void loadData(){
        List<Article> datainfos;
        for(int i = 0; i < 4; i++){
            datainfos = DBManager.getInstance(this).getData(i, 30);
            DataManager.getInstance().addDataset(datainfos);
        }
    }

    private void saveImgUrl(String imgUrl){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        editor.putString("imgUrl", imgUrl);
        editor.commit();
    }

    private String loadImgUrl(){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);

        String imgUrl = preference.getString("imgUrl", "");

        return imgUrl;
    }

    private void saveAppStartNum(int appStartNum){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        editor.putInt("appStartNum", appStartNum);
        editor.commit();
    }

    private int loadAppStartNum(){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);

        int appStartNum = preference.getInt("appStartNum", 0);

        return appStartNum;
    }
}
