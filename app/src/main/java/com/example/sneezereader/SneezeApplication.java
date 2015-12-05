package com.example.sneezereader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.database.DBManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuchun on 2015/7/18.
 */
public class SneezeApplication extends Application {
    private String lastUpdateDate;   //上次更新的日期
    private boolean updateToday;    //今天是否更新过

    @Override
    public void onCreate() {
        super.onCreate();
        //应用启动是应该去读上次更新的日期
        lastUpdateDate = readLastUpdateDate();
        //获取系统当前日期
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = sdf.format(date);

        //当前时间比记录的更新时间早,说明今天还没有更新过
        if(curDate.compareTo(lastUpdateDate) > 0){
            updateToday = false;
            setUpdateFlag(updateToday);
        }else{
            updateToday = readUpdateFlag();   //否则,更新过,直接读取配置信息
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 读取标记信息
     * @return
     */
    public boolean getUpdateFlag(){
        return  updateToday;
    }

    /**
     * 读取今天是否已更新过的标记
     * @return
     */
    public boolean readUpdateFlag(){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        //读取数据
        boolean update = sp.getBoolean("isUpdate", false);

        return update;
    }

    /**
     *  写入已更新标记
     */
    public void setUpdateFlag(boolean flag){
        updateToday = flag;
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //写入数据
        editor.putBoolean("isUpdate", updateToday);
        editor.commit();
    }

    /**
     * 读取上次更新时间
     * @return
     */
    public String readLastUpdateDate(){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        //读取数据
        String updateDate = sp.getString("updateDate", "2015-07-18");

        return updateDate;
    }

    /**
     * 设置上次更新时间
     */
    public void setLastUpdateDate(String date){
        lastUpdateDate = date;
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //写入数据
        editor.putString("updateDate", lastUpdateDate);
        editor.commit();
    }

    /**
     * 设置已更新
     */
    public void setUpdate(){
        setUpdateFlag(true);   //今天已更新

        //获取系统当前日期
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = sdf.format(date);

        setLastUpdateDate(curDate);
    }
}
