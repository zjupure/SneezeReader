package com.simit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.simit.network.HttpManager;
import com.facebook.drawee.view.SimpleDraweeView;
import com.simit.storage.SharedPreferenceUtils;



/**
 * Created by liuchun on 2015/7/18.
 */
public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    // 闪屏页
    private static final int SPLASH_TIME = 1000;  // 1s
    // 启动屏图片
    private SimpleDraweeView splash;
    // 当前Activity是否结束
    private boolean isFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initial fresco
        Fresco.initialize(this);
        //
        setContentView(R.layout.splash_layout);
        //
        splash = (SimpleDraweeView) findViewById(R.id.splash_image);
        //
        //String imgUrl = loadImgUrl();
        String imgUrl = SharedPreferenceUtils.get(this, "imgUrl", "");
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
            public void onSuccess(final String data) {

                if(!TextUtils.isEmpty(data)){
                    //saveImgUrl(data);
                    SharedPreferenceUtils.put(SplashActivity.this, "imgUrl", data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //转到UI线程执行, 判断当前activity是否已经销毁
                            if(!isFinished && splash != null){
                                splash.setImageURI(data);
                            }
                        }
                    });

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
}
