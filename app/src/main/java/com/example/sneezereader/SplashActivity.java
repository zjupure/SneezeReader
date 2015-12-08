package com.example.sneezereader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.datamodel.Article;
import com.example.jsonparser.JsonParserUtil;
import com.example.liuchun.sneezereader.R;
import com.example.network.SneezeClient;
import com.example.network.SneezeJsonResponseHandler;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


/**
 * Created by liuchun on 2015/7/18.
 */
public class SplashActivity extends Activity {
    //闪屏页
    private static final int SPLASH_LONG = 1500;
    private SimpleDraweeView splash;

    private String oldUrl;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_layout);
        splash = (SimpleDraweeView) findViewById(R.id.splash_image);

        /*
        oldUrl = restoreLoadUrl();
        if(!oldUrl.isEmpty()){
            Uri uri = Uri.parse(oldUrl);
            splash.setImageURI(uri);
        }*/
        // request for the new picture
        SneezeClient client = SneezeClient.getInstance(this);
        client.get(SneezeClient.DOWNLOAD_SPLASH_PATH, null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(SplashActivity.this, "ImageDownLoad Failed", Toast.LENGTH_SHORT);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String loadUrl = JsonParserUtil.JsonLoadUrlParser(responseString);
                // update the splash image url
                /*
                if(!loadUrl.equals(oldUrl)){
                    oldUrl = loadUrl;
                    saveLoadUrl(loadUrl);

                    Uri uri = Uri.parse(loadUrl);
                    splash.setImageURI(uri);
                }*/
                saveLoadUrl(loadUrl);

                Uri uri = Uri.parse(loadUrl);
                splash.setImageURI(uri);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //entry the main activity
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        SplashActivity.this.startActivity(intent);

                        finish();
                    }
                }, SPLASH_LONG);
            }
        });

        client.getTugua(new SneezeJsonResponseHandler(this, Article.TUGUA));

        //client.getArticle(SneezeClient.LEHUO_PATH, new SneezeJsonResponseHandler(this, Article.LEHUO));
        //client.getArticle(SneezeClient.YITU_PATH, new SneezeJsonResponseHandler(this, Article.YITU));
        //client.getArticle(SneezeClient.DUANZI_PATH, new SneezeJsonResponseHandler(this, Article.DUANZI));
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
