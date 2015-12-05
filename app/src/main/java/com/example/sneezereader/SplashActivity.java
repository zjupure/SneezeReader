package com.example.sneezereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.example.liuchun.sneezereader.R;
import com.example.service.MessageService;


/**
 * Created by liuchun on 2015/7/18.
 */
public class SplashActivity extends Activity {
    //闪屏页
    private static final int SPLASH_LONG = 2000;
    private ImageView splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_layout);
        splash = (ImageView)findViewById(R.id.splash_image);


        Intent intent = new Intent(this, MessageService.class);
        intent.putExtra("page", -1);
        startService(intent);   //启动后台服务
        //延时2s,做一些数据初始化工作
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //启动新的Activity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);

                finish();
            }
        }, SPLASH_LONG);

    }
}
