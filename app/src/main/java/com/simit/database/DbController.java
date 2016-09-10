package com.simit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  liuchun on 2015/7/17.
 */
public class DbController {
    private static final String TAG = "DbController";
    /** 单例 */
    private static DbController instance = null;
    /** 同步锁 */
    private static final Object LOCK = new Object();

    private SQLiteDatabase db;

    private DbController(Context context){
        DbOpenHelper helper = new DbOpenHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 关闭数据库
     */
    public void close(){

        if(db != null && db.isOpen()){
            db.close();
        }

    }

    /**
     * 单例模式
     * @param context
     * @return
     */
    public static DbController getInstance(Context context){
        if(instance == null){
            synchronized (DbController.class){
                if(instance == null){
                    instance = new DbController(context);
                }
            }
        }

        return instance;
    }


    /**
     * 插入单条记录
     * @param article
     */
    private void insertRecord(Article article){
        ContentValues cv = new ContentValues();
        cv.put("type", article.getType());
        cv.put("title", article.getTitle());
        cv.put("remote_link", article.getLink());
        cv.put("author", article.getAuthor());
        cv.put("pubDate", article.getPubDate());
        cv.put("description", article.getDescription());
        cv.put("imgurl", article.getImgUrl());
        cv.put("local_link", article.getLocalLink());

        synchronized (LOCK) {
            db.insert(DbOpenHelper.ARTICLE_TABLE_NAME, null, cv);   //插入一条数据
        }

        Log.d(TAG, "insert: write data to database successfully");
    }

    /**
     * 连续插入多条记录
     * @param articles
     */
    public void insertMultiRecords(List<Article> articles){

        db.beginTransaction();   //开始事务
        try{
            for(Article article: articles){

                ContentValues cv = new ContentValues();
                cv.put("type", article.getType());
                cv.put("title", article.getTitle());
                cv.put("remote_link", article.getLink());
                cv.put("author", article.getAuthor());
                cv.put("pubDate", article.getPubDate());
                cv.put("description", article.getDescription());
                cv.put("imgurl", article.getImgUrl());
                cv.put("local_link", article.getLocalLink());

                synchronized (LOCK){
                    db.insert(DbOpenHelper.ARTICLE_TABLE_NAME, null, cv);
                }
            }
            db.setTransactionSuccessful();   //设置事务完成
        } finally {
            db.endTransaction();  //结束事务

            Log.d(TAG, "insert: write multiple data to database successfully");
        }

    }

    /**
     * 根据description更新local_link
     * 图卦、乐活、意图的description就是rssLink
     * @param rssLink
     * @param local_link
     */
    public void updateLocalLink(String rssLink, String local_link){
        ContentValues cv = new ContentValues();
        cv.put("local_link", local_link);

        String[] args = {rssLink};
        db.update(DbOpenHelper.ARTICLE_TABLE_NAME, cv, "description=?", args);
    }

    /**
     * 根据description获取本地连接
     * 图卦、乐活、意图的description就是rssLink
     * @param rssLink
     * @return
     */
    public String getLocalUrl(String rssLink){
        // 根据sql查询
        /*String sql = "SELECT local_link FROM articles WHERE description = '%s'";

        sql = String.format(sql, rssLink);
        Cursor cursor = db.rawQuery(sql, null);
        String local_link = "";

        if(cursor.moveToFirst()){
            local_link = cursor.getString(cursor.getColumnIndex("local_link"));
        }
        cursor.close();*/

        String[] columns = {"local_link"};
        String[] args = {rssLink};
        Cursor cursor = db.query(DbOpenHelper.ARTICLE_TABLE_NAME, columns, "description=?", args, null, null, null);
        String local_link = "";

        if(cursor.moveToFirst()){
            local_link = cursor.getString(cursor.getColumnIndex("local_link"));
        }
        cursor.close();

        return local_link;
    }

    /**
     * 根据description判断是否已经存在数据库中
     * @param description
     * @return
     */
    public boolean isExist(String description){
        /*
        String sql = "SELECT id FROM articles WHERE description = '%s'";

        sql = String.format(sql, description);
        Cursor cursor = db.rawQuery(sql, null);
        boolean exist = cursor.moveToFirst();
        cursor.close();*/

        String[] columns = {"id"};
        String[] args = {description};
        Cursor cursor = db.query(DbOpenHelper.ARTICLE_TABLE_NAME, columns, "description=?", args, null, null, null);
        boolean exist = cursor.moveToFirst();
        cursor.close();

        return exist;
    }

    /**
     * 根据图片地址判断是否是重复的
     * @param imgurl
     * @return
     */
    public boolean isDuplicateImg(String imgurl){
        /*
        String sql = "SELECT id FROM articles WHERE imgurl = '%s'";

        sql = String.format(sql, imgurl);
        Cursor cursor = db.rawQuery(sql, null);
        boolean exist = cursor.moveToFirst();
        cursor.close();*/

        String[] columns = {"id"};
        String[] args = {imgurl};
        Cursor cursor = db.query(DbOpenHelper.ARTICLE_TABLE_NAME, columns, "imgurl=?", args, null, null, null);
        boolean exist = cursor.moveToFirst();
        cursor.close();

        return exist;
    }


    /**
     * 本地缓存文件被清除时,
     * 同时清除数据库中的记录
     */
    public void clearLocalLink(){
        ContentValues cv = new ContentValues();
        cv.put("local_link", "");

        String[] args = {""};
        db.update("articles", cv, "local_link!=?", args);
    }

    /**
     * 查询特定类型的数据填充到数据管理器
     * @param type
     * @param limit
     * @return
     */
    public List<Article> getData(int type, int limit){
        //按照type查询所有列,根据发表时间降序排列,取前LIMIT项
        String sql_art = "SELECT * FROM articles WHERE type = %d ORDER BY pubDate DESC ";
        List<Article> datainfos;

        if(limit == 0){
            //查询出所有符合条件的数据
            sql_art = String.format(sql_art, type);
        }else{
            sql_art = String.format(sql_art, type) + " LIMIT " + limit;   //格式化
        }

        datainfos = query(sql_art);

        return datainfos;
    }

    public List<Article> getData(int type, int limit, String username){
        List<Article> articles = getData(type, limit);
        List<Article> datainfos = new ArrayList<>();

        for(Article article: articles){
            boolean isFavorite = getFavoriteState(article.getId(), username);
            article.setFavorite(isFavorite);
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
            data.setLink(cursor.getString(cursor.getColumnIndex("remote_link")));
            data.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            data.setPubDate(cursor.getString(cursor.getColumnIndex("pubDate")));
            data.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            data.setImgUrl(cursor.getString(cursor.getColumnIndex("imgurl")));
            data.setLocalLink(cursor.getString(cursor.getColumnIndex("local_link")));
            data.setFavorite(false);
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
        cursor.close();
        // 收藏列表已经存在
        if(exist){
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

    public boolean getFavoriteState(int article_id, String user){
        String sql_fav = "SELECT article_id FROM favorites where article_id = %d AND user = '%s'";

        sql_fav = String.format(sql_fav, article_id, user);

        Cursor cursor = db.rawQuery(sql_fav, null);
        boolean isFavorite = cursor.moveToFirst();
        cursor.close();

        return isFavorite;
    }

    /**
     * 根据类型获取收藏文章信息
     * @param type
     * @param limit
     * @return
     */
    public List<Article> getFavorites(int type, int limit, String user){
        //按照type查询所有列,根据添加时间降序排列,取前LIMIT项
        String sql_fav = "SELECT article_id FROM favorites WHERE type = %d AND user = '%s' ORDER BY add_time DESC ";
        String sql_art = "SELECT * FROM articles WHERE id = %d ";
        List<Article> articles = new ArrayList<>();

        if(limit == 0){
            //查询出所有符合条件的数据
            sql_fav = String.format(sql_fav, type, user);
        }else{
            sql_fav = String.format(sql_fav, type, user) + " LIMIT " + limit;   //格式化
        }
        // 从收藏列表查询到article_id
        Cursor cursor = db.rawQuery(sql_fav, null);
        //处理返回数据
        while (cursor.moveToNext()){
            int article_id = cursor.getInt(cursor.getColumnIndex("article_id"));
            String sql = String.format(sql_art, article_id);
            List<Article> results = query(sql);
            for(Article article : results){
                article.setFavorite(true);
                articles.add(article);
            }
        }
        cursor.close();

        return articles;
    }
}
