package com.simit.sneezereader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.simit.database.DBManager;
import com.simit.network.SneezeClient;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuchun on 2015/7/18.
 */
public class SneezeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // initial components
        SneezeClient.getInstance(this);
        DBManager.getInstance(this);

        Fresco.initialize(this);
    }
}
