package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.datamodel.Article;

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
    private static Object LOCK = new Object();
    private static DBManager instance = null;
    private DBHelper helper;
    private SQLiteDatabase db;

    private DBManager(Context context){
        helper = new DBHelper(context);
        open(READ_WRITE);
    }

    /**
     * 单例模式
     * @param context
     * @return
     */
    public static DBManager getInstance(Context context){
        if(instance == null){
            synchronized (DBManager.class){
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
    private void insertRecord(Article datainfo){
        ContentValues cv = new ContentValues();
        cv.put("type", datainfo.getType());
        cv.put("title", datainfo.getTitle());
        cv.put("remote_link", datainfo.getRemote_link());
        cv.put("author", datainfo.getAuthor());
        cv.put("pubDate", datainfo.getPubDate());
        cv.put("description", datainfo.getDescription());
        cv.put("imgurl", datainfo.getImgurl());
        cv.put("local_link", datainfo.getLocal_link());

        synchronized (LOCK) {
            db.insert("articles", null, cv);   //插入一条数据
        }

        Log.d("Database Insert", "write data to database successful\n");
    }

    /**
     * 连续插入多条记录
     * @param datainfos
     */
    public void insertMultiRecords(List<Article> datainfos){

        db.beginTransaction();   //开始事务
        try{
            for(Article datainfo: datainfos){
                insertRecord(datainfo);
            }
            db.setTransactionSuccessful();   //设置事务完成
        } finally {
            db.endTransaction();  //结束事务
        }

    }

    public void updateLocalLink(String description, String local_link){
        ContentValues cv = new ContentValues();
        cv.put("local_link", local_link);

        String[] args = {description};
        db.update("articles", cv, "description=?", args);
    }

    public String getLocalUrl(String description){
        // 根据sql查询
        String sql = "SELECT local_link FROM articles WHERE description = '%s'";

        sql = String.format(sql, description);
        Cursor cursor = db.rawQuery(sql, null);
        String local_link = "";

        if(cursor.moveToFirst()){
            local_link = cursor.getString(cursor.getColumnIndex("local_link"));
        }

        return local_link;
    }
    /**
     * 查询特定类型的数据填充到数据管理器
     * @param type
     * @return
     */
    public List<Article> getData(int type, int limit){
        //按照type查询所有列,根据发表时间降序排列,取前LIMIT项
        String sql = "SELECT * FROM articles WHERE type = %d ORDER BY pubDate DESC";
        List<Article> datainfos;

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
     * 根据remote_link判断是否已经存在数据库中
     * @param remote_link
     * @return
     */
    public boolean isExist(String remote_link){

        String sql = "SELECT id FROM articles WHERE remote_link = '%s'";

        sql = String.format(sql, remote_link);
        Cursor cursor = db.rawQuery(sql, null);


        return cursor.moveToFirst() == true;
    }

    /**
     * 执行查询指令
     * @param sql
     * @return
     */
    public List<Article> query(String sql){
        List<Article> datainfos = new ArrayList<>();

        Cursor cursor = db.rawQuery(sql, null);

        //处理返回数据
        while(cursor.moveToNext()){
            Article data = new Article();

            data.setId(cursor.getInt(cursor.getColumnIndex("id")));
            data.setType(cursor.getInt(cursor.getColumnIndex("type")));
            data.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            data.setRemote_link(cursor.getString(cursor.getColumnIndex("remote_link")));
            data.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            data.setPubDate(cursor.getString(cursor.getColumnIndex("pubDate")));
            data.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            data.setImgurl(cursor.getString(cursor.getColumnIndex("imgurl")));
            data.setLocal_link(cursor.getString(cursor.getColumnIndex("local_link")));

            datainfos.add(data);
        }
        cursor.close();

        return datainfos;
    }
}
