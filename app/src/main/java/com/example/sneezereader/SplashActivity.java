package com.example.sneezereader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.datamodel.Article;
import com.example.jsonparser.JsonParserUtil;
import com.example.network.SneezeClient;
import com.example.network.SneezeJsonResponseHandler;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.TextHttpResponseHandler;


import cz.msebera.android.httpclient.Header;


/**
 * Created by liuchun on 2015/7/18.
 */
public class SplashActivity extends Activity {
    //闪屏页
    private static final int SPLASH_LONG = 1500;
    private static final int SPLASH_SHORT = 500;

    //private String oldUrl;
    private Handler handler = new Handler();

    private SimpleDraweeView splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_layout);
        splash = (SimpleDraweeView) findViewById(R.id.splash_image);


        // request for the new picture
        SneezeClient client = SneezeClient.getInstance(this);

        client.getSplashImage(new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                Log.d("SplashActivity", "Splash Image Download Failed!");

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                Log.d("SplashActivity", "Splash Image Download Successful!");

                String loadUrl = JsonParserUtil.JsonLoadUrlParser(responseString);
                // update the splash image url

                //saveLoadUrl(loadUrl);

                Uri uri = Uri.parse(loadUrl);
                splash.setImageURI(uri);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //entry the main activity
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    }
                }, SPLASH_SHORT);
            }
        });

        client.getArticle(Article.TUGUA, new SneezeJsonResponseHandler(this, Article.TUGUA));
        client.getArticle(Article.LEHUO, new SneezeJsonResponseHandler(this, Article.LEHUO));
        client.getArticle(Article.YITU, new SneezeJsonResponseHandler(this, Article.YITU));
        client.getArticle(Article.DUANZI, new SneezeJsonResponseHandler(this, Article.DUANZI));

    }


    private void saveLoadUrl(String loadUrl){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        editor.putString("loadUrl", loadUrl);
        editor.commit();
    }

    private String restoreLoadUrl(){
        SharedPreferences preference = getSharedPreferences("config", Context.MODE_PRIVATE);

        String loadUrl = preference.getString("loadUrl", "");

        return loadUrl;
    }
}
