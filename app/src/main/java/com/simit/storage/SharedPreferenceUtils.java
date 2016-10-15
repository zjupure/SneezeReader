package com.simit.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by liuchun on 16/9/4.
 */
public class SharedPreferenceUtils {
    public static final String APP_PREF_NAME = "appConfig";
    // cache the data in memory
    private static final HashMap<String, Object> spCache = new HashMap<>();

    /**
     * 写全局sp
     * @param context
     * @param name
     * @param value
     */
    public static <T> void put(Context context, String name, T value){
        SharedPreferences preferences = context.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(value instanceof String){
            editor.putString(name, (String)value);
        }else if(value instanceof Boolean){
            editor.putBoolean(name, (Boolean)value);
        }else if(value instanceof Integer){
            editor.putInt(name, (Integer)value);
        }else if(value instanceof Long){
            editor.putLong(name, (Long)value);
        }else if(value instanceof Float){
            editor.putFloat(name, (Float)value);
        }else {
            throw new IllegalArgumentException("unsupported type");
        }

        spCache.put(name, value);
        editor.apply();
    }


    /**
     * 从全局sp获取String
     * @param context
     * @param name
     * @param defValue
     */
    public static <T> T get(Context context, String name, T defValue){
        Object result = spCache.get(name);

        if(result != null){
            return (T)result;
        }

        SharedPreferences preferences = context.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);

        if(defValue instanceof String){
            result = preferences.getString(name, (String)defValue);
        }else if(defValue instanceof Boolean){
            result = preferences.getBoolean(name, (Boolean)defValue);
        }else if(defValue instanceof Integer){
            result = preferences.getInt(name, (Integer)defValue);
        }else if(defValue instanceof Long){
            result = preferences.getLong(name, (Long)defValue);
        }else if(defValue instanceof Float){
            result = preferences.getFloat(name, (Float)defValue);
        }else {
            throw new IllegalArgumentException("unsupported type");
        }

        return (T)result;
    }




    /**
     * 添加String到Activity私有sp
     * @param activity
     * @param name
     * @param value
     */
    public static <T> void putLocal(Activity activity, String name, T value){
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();


        if(value instanceof String){
            editor.putString(name, (String)value);
        }else if(value instanceof Boolean){
            editor.putBoolean(name, (Boolean)value);
        }else if(value instanceof Integer){
            editor.putInt(name, (Integer)value);
        }else if(value instanceof Long){
            editor.putLong(name, (Long)value);
        }else if(value instanceof Float){
            editor.putFloat(name, (Float)value);
        }else {
            throw new IllegalArgumentException("unsupported type");
        }

        editor.apply();
    }

    /**
     * 从activity私有sp获取string
     * @param activity
     * @param name
     * @param defValue
     * @return
     */
    public static <T> T getLocal(Activity activity, String name, T defValue){
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        Object result = null;

        if(defValue instanceof String){
            result = preferences.getString(name, (String)defValue);
        }else if(defValue instanceof Boolean){
            result = preferences.getBoolean(name, (Boolean)defValue);
        }else if(defValue instanceof Integer){
            result = preferences.getInt(name, (Integer)defValue);
        }else if(defValue instanceof Long){
            result = preferences.getLong(name, (Long)defValue);
        }else if(defValue instanceof Float){
            result = preferences.getFloat(name, (Float)defValue);
        }else {
            throw new IllegalArgumentException("unsupported type");
        }

        return (T)result;
    }
}
