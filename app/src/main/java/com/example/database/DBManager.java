package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.datamodel.Datainfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by  liuchun on 2015/7/17.
 */
public class DBManager {
    public static final int READ_ONLY = 0;
    public static final int WRITE_ONLY = 1;
    public static final int READ_WRITE = 2;
    private static final Object LOCK = new Object();
    private static DBManager instance = null;
    private DBHelper helper;
    private SQLiteDatabase db;

    private DBManager(Context context){
        helper = new DBHelper(context);
    }

    /**
     * 单例模式
     * @param context
     * @return
     */
    public static DBManager getInstance(Context context){
        if(instance == null){
            synchronized (LOCK){
                if(instance == null){
                    instance = new DBManager(context);
                }
            }
        }

        return instance;
    }

    /**
     * 打开数据库,只读或读写模式
     * @param mode
     */
    public void open(int mode){
        if(mode == READ_ONLY)
            db = helper.getReadableDatabase();
        else
            db = helper.getWritableDatabase();
    }

    /**
     * 关闭数据库
     */
    public void close(){
        if(db.isOpen()){
            db.close();
        }
    }

    /**
     * 插入操作
     * @param datainfo
     */
    private void addSingleRecord(Datainfo datainfo){
        ContentValues cv = new ContentValues();
        cv.put("title", datainfo.getTitle());
        cv.put("type", datainfo.getType());
        cv.put("author", datainfo.getAuthor());
        Date date = datainfo.getPublishdate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cv.put("publishdate", sdf.format(date));
        cv.put("content", datainfo.getContent());
        cv.put("remote_url", datainfo.getRemote_url());
        cv.put("local_url", datainfo.getLocal_url());

        synchronized (LOCK) {
            db.insert("paper", null, cv);   //插入一条数据
        }

        Log.d("Database Insert", "write data to database successful\n");
    }

    /**
     * 连续插入多条记录
     * @param datainfos
     */
    public void addMultiRecords(List<Datainfo> datainfos){

        db.beginTransaction();   //开始事务
        try{
            for(Datainfo datainfo: datainfos){
                addSingleRecord(datainfo);
            }
            db.setTransactionSuccessful();   //设置事务完成
        } finally {
            db.endTransaction();  //结束事务
        }

    }

    /**
     * 判断数据库中是否已有该远程地址链接
     * @param url
     * @return
     */
    public boolean hasRemoteUrl(String url){
        String sql = "SELECT remote_url FROM paper WHERE remote_url = '%s'";
        boolean isOk = false;

        sql = String.format(sql, url);

        Cursor cursor =  db.rawQuery(sql, null);

        if(cursor.getCount() > 0){
            isOk = true;
        }

        return isOk;
    }

    /**
     * 查询特定类型的数据填充到数据管理器
     * @param type
     * @return
     */
    public List<Datainfo> getData(int type, int limit){
        //按照type查询所有列,根据发表时间降序排列,取前LIMIT项
        String sql = "SELECT * FROM paper WHERE type = %d";
        List<Datainfo> datainfos;

        if(limit == 0){
            //查询出所有符合条件的数据
            sql = String.format(sql, type);
        }else{
            sql = String.format(sql, type) + " LIMIT " + limit;   //格式化
        }

        datainfos = query(sql);

        return datainfos;
    }

    /**
     * 获取所有类型的数据
     * @param limit
     * @return
     */
    public List<Datainfo> getAllData(int limit){
        String sql = "SELECT * FROM paper";
        List<Datainfo> datainfos;

        if(limit > 0){
            sql += " LIMIT " + limit;
        }

        datainfos = query(sql);

        return  datainfos;
    }

    /**
     * 执行查询指令
     * @param sql
     * @return
     */
    public List<Datainfo> query(String sql){
        List<Datainfo> datainfos = new ArrayList<Datainfo>();

        Cursor cursor = db.rawQuery(sql, null);

        //处理返回数据
        while(cursor.moveToNext()){
            Datainfo data = new Datainfo();
            data.setId(cursor.getInt(cursor.getColumnIndex("id")));
            data.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            data.setType(cursor.getInt(cursor.getColumnIndex("type")));
            data.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            String date = cursor.getString(cursor.getColumnIndex("publishdate"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                data.setPublishdate(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            data.setContent(cursor.getString(cursor.getColumnIndex("content")));
            data.setRemote_url(cursor.getString(cursor.getColumnIndex("remote_url")));
            data.setLocal_url(cursor.getString(cursor.getColumnIndex("local_url")));
        }
        cursor.close();

        return datainfos;
    }
}
