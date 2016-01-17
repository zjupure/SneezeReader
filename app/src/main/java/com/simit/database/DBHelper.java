package com.simit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by liuchun on 2015/7/17.
 */
public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "sneeze.db";
    private static final int DATABASE_VERSION = 2;
    //创建文章数据表命令
    private static final String CREATE_ARTICLE_TABLE = "CREATE TABLE IF NOT EXISTS articles" +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "title VARCHAR(200) NOT NULL, " +
            "remote_link TEXT, " +
            "author CHAR(10), " +
            "pubDate DATE, " +
            "description TEXT, " +
            "imgurl TEXT, " +
            "local_link TEXT);";
    //创建收藏数据表命令
    private static final String CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS favorites" +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "article_id INTEGER NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "user VARCHAR(200) NOT NULL, " +
            "add_time DATE, " +
            "FOREIGN KEY(article_id) REFERENCES articles(id)" +
            ");";
    //在type+pubDate上创建索引
    private static final String CREATE_INDEX_TYPEDATE = "CREATE INDEX typeDate on articles (type, pubDate)";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 数据库第一次被创建时调用
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ARTICLE_TABLE);
        db.execSQL(CREATE_INDEX_TYPEDATE);
        // version 2
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(CREATE_FAVORITE_TABLE);
    }

    /**
     * 数据库版本号更新时调用
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeVersion = oldVersion;

        if(upgradeVersion == 1){
            // verison1 --> version2
            db.execSQL("PRAGMA foreign_keys = ON");
            db.execSQL(CREATE_FAVORITE_TABLE);
            upgradeVersion = 2;
        }

        if(upgradeVersion != oldVersion){
            // Drop tables
            db.execSQL("DROP TABLE IF EXISTS articles;");
            db.execSQL("DROP TABLE IF EXISTS favorites;");
            // Create tables
            onCreate(db);
        }
    }
}
