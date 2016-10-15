package com.simit;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by liuchun on 16/10/15.
 */

public class ReaderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // init fresco
        Fresco.initialize(this);
    }
}
