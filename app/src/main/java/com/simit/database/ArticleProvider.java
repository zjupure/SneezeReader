package com.simit.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by liuchun on 16/9/1.
 */
public class ArticleProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher;

    private static final int TABLE_ARTICLE = 1;
    private static final int TABLE_FAVORITE = 2;
    private static final String AUTHORITY = "com.simit.database.ArticleProvider";
    private static final Uri CONTENT_URI_ARTICLE = Uri.parse("content://" + AUTHORITY + "/article");
    private static final Uri CONTENT_URI_FAVORITE = Uri.parse("content://" + AUTHORITY + "/favorite");


    private DbOpenHelper mDbHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "article", TABLE_ARTICLE);
        sUriMatcher.addURI(AUTHORITY, "favorite", TABLE_FAVORITE);
    }

    @Override
    public boolean onCreate() {

        mDbHelper = new DbOpenHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)){
            case TABLE_ARTICLE:
                queryBuilder.setTables(DbOpenHelper.ARTICLE_TABLE_NAME);
                break;
            case TABLE_FAVORITE:
                queryBuilder.setTables(DbOpenHelper.FAVORITE_TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("unknown uri: " + uri);
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long rowId = 0;
        switch (sUriMatcher.match(uri)){
            case TABLE_ARTICLE:
                rowId = db.insert(DbOpenHelper.ARTICLE_TABLE_NAME, null, values);
                if(rowId > 0){
                    return ContentUris.withAppendedId(CONTENT_URI_ARTICLE, rowId);
                }
                break;
            case TABLE_FAVORITE:
                rowId = db.insert(DbOpenHelper.FAVORITE_TABLE_NAME, null, values);
                if(rowId > 0){
                    return ContentUris.withAppendedId(CONTENT_URI_FAVORITE, rowId);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown uri: " + uri);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int count = 0;
        switch (sUriMatcher.match(uri)){
            case TABLE_ARTICLE:
                count = db.delete(DbOpenHelper.ARTICLE_TABLE_NAME, selection, selectionArgs);
                break;
            case TABLE_FAVORITE:
                count = db.delete(DbOpenHelper.FAVORITE_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unknown uri: " + uri);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int count  = 0;
        switch (sUriMatcher.match(uri)){
            case TABLE_ARTICLE:
                count = db.update(DbOpenHelper.ARTICLE_TABLE_NAME, values, selection, selectionArgs);
                break;
            case TABLE_FAVORITE:
                count = db.update(DbOpenHelper.FAVORITE_TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unknown uri: " + uri);
        }

        Context context = getContext();
        if(context != null){
            context.getContentResolver().notifyChange(uri, null);
        }


        return count;
    }
}
