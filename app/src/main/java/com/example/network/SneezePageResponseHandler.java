package com.example.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.database.DBManager;
import com.example.datamodel.DataManager;
import com.example.storage.FileManager;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by liuchun on 2015/12/6.
 */
public class SneezePageResponseHandler extends TextHttpResponseHandler {
    private Context context;
    private String remote_link;

    public SneezePageResponseHandler(Context context, String remote_link){
        this.context = context;
        this.remote_link = remote_link;
    }
    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        Log.d("PageResponse", "page source download failed!");
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString) {
        Log.d("PageResponse", "page source download success!");

        FileManager fileManager = FileManager.getInstance(context);
        DBManager dbManager = DBManager.getInstance(context);

        String[] paths = remote_link.split("[?]");
        String filename = paths[paths.length - 1];
        paths = filename.split("[&]");
        filename = paths[paths.length - 1];
        filename = filename.replace('=', '_');
        filename += ".html";

        fileManager.writeHTML(filename, responseString);
        String path = "file://" + fileManager.getAbsolutPath(filename);
        dbManager.updateLocalLink(remote_link, path);

        Log.d("PageResponse", path);
    }
}
