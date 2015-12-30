package com.simit.sneezereader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.simit.database.DBManager;
import com.simit.network.SneezeClient;
import com.facebook.drawee.backends.pipeline.Fresco;


/**
 * Created by liuchun on 2015/7/18.
 */
public class SneezeApplication extends Application {
    // basic settings
    private boolean night_mode;
    private boolean notify_mode;
    private boolean advertise_mode;
    @Override
    public void onCreate() {
        super.onCreate();
        // initial components
        Fresco.initialize(this);
        SneezeClient.getInstance(this);
        DBManager.getInstance(this);

        night_mode = restoreConfigBoolean("night_mode", false);
        notify_mode = restoreConfigBoolean("notify_mode", true);
        advertise_mode = restoreConfigBoolean("ad_mode", false);

    }

    public boolean getNightMode(){
        return night_mode;
    }

    public boolean getNotifyMode(){
        return notify_mode;
    }

    public boolean getAdMode(){
        return advertise_mode;
    }

    public void setNightMode(boolean value){
        night_mode = value;
        saveConfigBoolean("night_mode", value);
    }

    public void setNotifyMode(boolean value){
        notify_mode = value;
        saveConfigBoolean("notify_mode", value);
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

    public boolean restoreConfigBoolean(String key, boolean defValue){
        SharedPreferences preferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        boolean value = preferences.getBoolean(key, defValue);
        return value;
    }
}
