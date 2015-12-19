package com.simit.sneezereader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.simit.database.DBManager;
import com.simit.network.SneezeClient;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuchun on 2015/7/18.
 */
public class SneezeApplication extends Application {
    // basic settings
    private boolean night_mode;
    private boolean advertise_mode;

    @Override
    public void onCreate() {
        super.onCreate();
        // initial components
        Fresco.initialize(this);
        SneezeClient.getInstance(this);
        DBManager.getInstance(this);

        night_mode = restoreConfigBoolean("night_mode");
        advertise_mode = restoreConfigBoolean("ad_mode");
    }

    public boolean getNightMode(){
        return night_mode;
    }

    public boolean getAdMode(){
        return advertise_mode;
    }

    public void setNightMode(boolean value){
        night_mode = value;
        saveConfigBoolean("night_mode", value);
    }

    public void setAdMode(boolean value){
        advertise_mode = value;
        saveConfigBoolean("ad_mode", value);
    }

    public void saveConfigString(String key, String value){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public String restoreConfigString(String key){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        String value = preferences.getString(key, "");
        return value;
    }

    public void saveConfigBoolean(String key, boolean value){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean restoreConfigBoolean(String key){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        boolean value = preferences.getBoolean(key, false);
        return value;
    }
}
