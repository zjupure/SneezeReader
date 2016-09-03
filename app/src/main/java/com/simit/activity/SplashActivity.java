package com.simit.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.simit.database.DbController;
import com.simit.model.Article;
import com.simit.model.DataManager;
import com.simit.json.ParserUtils;
import com.simit.network.HttpManager;
import com.simit.network.SneezeClient;
import com.simit.network.SneezeJsonResponseHandler;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.TextHttpResponseHandler;


import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.TextUtils;


/**
 * Created by liuchun on 2015/7/18.
 */
public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    //闪屏页
    private static final int SPLASH_TIME = 1000;  // 1s
    //
    private SimpleDraweeView splash;
    // 当前Activity是否结束
    private boolean isFinished;

    //private SneezeClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_layout);
        //
        splash = (SimpleDraweeView) findViewById(R.id.splash_image);
        //
        String imgUrl = loadImgUrl();
        if(!TextUtils.isEmpty(imgUrl)){
            // 先设置成上次的图片
            splash.setImageURI(imgUrl);
        }
        // 请求新的Splash图片
        HttpManager.getInstance(this).getSplashImage(new HttpManager.INetworkCallback<String>() {
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "get Splash Image Url failed");
            }

            @Override
            public void onSuccess(String data) {

                if(!TextUtils.isEmpty(data)){
                    saveImgUrl(data);
                    // 判断当前activity是否已经销毁
                    if(!isFinished && splash != null){
                        splash.setImageURI(data);
                    }
                }
            }
        });

        // 延时1s进入主页
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //entry the main activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // destroy itself
                finish();
                isFinished = true;
            }
        }, SPLASH_TIME);
    }

    /*
    private void execNetworkRequest(){
        // request for the new picture
        client = SneezeClient.getInstance(this);

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
                String loadUrl = ParserUtils.parseSplashImgUrl(responseString);
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
    }*/

    /*
    private void loadData(){
        List<Article> datainfos;
        SneezeApplication app = (SneezeApplication) getApplication();
        String username = app.getUsername();
        for(int i = 0; i < 4; i++){
            datainfos = DbController.getInstance(this).getData(i, 30, username);
            DataManager.getInstance().addDataset(datainfos);
        }
    }*/


    /**
     * 将Splash图片的远程地址存储到sp
     * @param imgUrl
     */
    private void saveImgUrl(String imgUrl){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        editor.putString("imgUrl", imgUrl);
        editor.apply();
    }

    /**
     * 从sp中读取上次的Splash图片地址
     * @return
     */
    private String loadImgUrl(){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);

        return preference.getString("imgUrl", "");
    }
}
