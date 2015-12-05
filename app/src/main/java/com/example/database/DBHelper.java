package com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by liuchun on 2015/7/17.
 */
public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "sneeze.db";
    private static final int DATABASE_VERSION = 1;
    //创建表命令
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS paper" +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "title VARCHAR(200) NOT NULL, " +
            "type TINYINT NOT NULL, " +
            "author CHAR(10), " +
            "publishdate DATE, " +
            "content TEXT, " +
            "remote_url TEXT, " +
            "local_url TEXT NOT NULL)";
    //在remote_url上创建索引
    private static final String CREATE_INDEX_URL = "CREATE INDEX paper_url on paper (remote_url)";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 数据库第一次被创建时调用
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_INDEX_URL);
    }

    /**
     * 数据库版本号更新时调用
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
