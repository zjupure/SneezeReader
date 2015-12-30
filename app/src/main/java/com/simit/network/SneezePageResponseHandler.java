package com.simit.network;

import android.content.Context;
import android.util.Log;

import com.simit.database.DBManager;
import com.simit.storage.FileManager;
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

        String filePath = fileManager.writeHTML(filename, responseString);
        if(!filePath.isEmpty()){
            String uriPath = "file://" + filePath;
            dbManager.updateLocalLink(remote_link, uriPath);

            Log.d("PageResponse", uriPath);
        }else {
            Log.d("PageResponse", "write file failed");
        }
    }
}
