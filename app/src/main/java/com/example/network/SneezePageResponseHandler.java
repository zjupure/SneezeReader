package com.example.network;

import android.content.Context;
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
        Toast.makeText(context, "Get Page Source Failed!", Toast.LENGTH_SHORT);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString) {
        FileManager fileManager = FileManager.getInstance();
        DBManager dbManager = DBManager.getInstance(context);

        String[] paths = remote_link.split("?");
        String filename = paths[paths.length - 1];

        fileManager.writeHTML(filename, responseString);
        dbManager.updateLocalLink(remote_link, fileManager.getAbsolutPath(filename));
    }
}
