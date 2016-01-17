package com.simit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.simit.datamodel.Article;

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

    /**
     * 根据description更新local_link
     * @param description
     * @param local_link
     */
    public void updateLocalLink(String description, String local_link){
        ContentValues cv = new ContentValues();
        cv.put("local_link", local_link);

        String[] args = {description};
        db.update("articles", cv, "description=?", args);
    }

    /**
     * 根据description获取本地连接
     * @param description
     * @return
     */
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
     * 根据description判断是否已经存在数据库中
     * @param description
     * @return
     */
    public boolean isExist(String description){

        String sql = "SELECT id FROM articles WHERE description = '%s'";

        sql = String.format(sql, description);
        Cursor cursor = db.rawQuery(sql, null);

        return cursor.moveToFirst() == true;
    }

    public List<String> getRemoteLinks(int type, int limit){
        //按照type查询所有列,根据发表时间降序排列,取前LIMIT项
        String sql = "SELECT description, local_link FROM articles WHERE type = %d ORDER BY pubDate DESC LIMIT %d";
        List<String> links = new ArrayList<>();
        String description, local_link;

        sql = String.format(sql, type, limit);
        Cursor cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext()){
            description = cursor.getString(cursor.getColumnIndex("description"));
            local_link = cursor.getString(cursor.getColumnIndex("local_link"));

            if(local_link.isEmpty()){
                links.add(description);
            }
        }

        return links;
    }

    // 清除所有的本地缓存连接
    public void clearLocalLink(){
        ContentValues cv = new ContentValues();
        cv.put("local_link", "");

        String[] args = {""};
        db.update("articles", cv, "local_link!=?", args);
    }

    /**
     * 查询特定类型的数据填充到数据管理器
     * @param type
     * @return
     */
    public List<Article> getData(int type, int limit){
        //按照type查询所有列,根据发表时间降序排列,取前LIMIT项
        String sql_art = "SELECT * FROM articles WHERE type = %d ORDER BY pubDate DESC ";
        String sql_fav_format = "SELECT article_id FROM favorites where article_id = %d";
        List<Article> datainfos = new ArrayList<>();

        if(limit == 0){
            //查询出所有符合条件的数据
            sql_art = String.format(sql_art, type);
        }else{
            sql_art = String.format(sql_art, type) + " LIMIT " + limit;   //格式化
        }

        List<Article> articles = query(sql_art);
        // 去收藏列表查询
        for(Article article : articles){
            int article_id = article.getId();
            String sql_fav = String.format(sql_fav_format, article_id);
            Cursor cursor = db.rawQuery(sql_fav, null);
            if(cursor.moveToFirst()){
                article.setIsFavorite(true);
            }else {
                article.setIsFavorite(false);
            }
            cursor.close();
            //
            datainfos.add(article);
        }

        return datainfos;
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
            // 查询到的数据集
            datainfos.add(data);
        }
        cursor.close();

        return datainfos;
    }

    /**
     * 向收藏列表插入一条记录
     * @param article
     * @param username
     * @param addtime
     */
    public void insertFavorite(Article article, String username, String addtime){
        String sql = "SELECT article_id FROM favorites WHERE article_id = %d AND user = '%s'";

        sql = String.format(sql, article.getId(), username);

        Cursor cursor = db.rawQuery(sql, null);

        boolean exist = cursor.moveToFirst();

        // 收藏列表已经存在
        if(exist == true){
            return;
        }
        // 收藏列表不存在则插入
        ContentValues cv = new ContentValues();
        cv.put("article_id", article.getId());
        cv.put("type", article.getType());
        cv.put("user", username);
        cv.put("add_time", addtime);

        synchronized (LOCK) {
            db.insert("favorites", null, cv);   //插入一条数据
        }
    }

    /**
     * 从收藏列表删除一条记录
     * @param article_id
     * @param username
     */
    public void deleteFavorite(int article_id, String username){
        String whereClause = "article_id = ? AND user = ?";
        String[] whereArgs = {Integer.toString(article_id), username};
        db.delete("favorites", whereClause, whereArgs);
    }

    /**
     * 根据类型获取收藏文章信息
     * @param type
     * @param limit
     * @return
     */
    public List<Article> getFavorites(int type, int limit){
        //按照type查询所有列,根据添加时间降序排列,取前LIMIT项
        String sql_fav = "SELECT article_id FROM favorites WHERE type = %d ORDER BY add_time DESC ";
        String sql_art = "SELECT * FROM articles WHERE id = %d ";
        List<Article> datainfos = new ArrayList<>();

        if(limit == 0){
            //查询出所有符合条件的数据
            sql_fav = String.format(sql_fav, type);
        }else{
            sql_fav = String.format(sql_fav, type) + " LIMIT " + limit;   //格式化
        }
        // 从收藏列表查询到article_id
        Cursor cursor = db.rawQuery(sql_fav, null);
        //处理返回数据
        while (cursor.moveToNext()){
            int article_id = cursor.getInt(cursor.getColumnIndex("article_id"));
            String sql = String.format(sql_art, article_id);
            List<Article> articles = query(sql);
            for(Article article : articles){
                article.setIsFavorite(true);
                datainfos.add(article);
            }
        }
        cursor.close();

        return datainfos;
    }
}
