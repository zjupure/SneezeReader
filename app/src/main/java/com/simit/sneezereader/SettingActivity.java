package com.simit.sneezereader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.simit.database.DBManager;
import com.simit.jsonparser.JsonParserUtil;
import com.simit.jsonparser.JsonUpdateLink;
import com.simit.network.SneezeClient;
import com.simit.storage.FileManager;

import cz.msebera.android.httpclient.Header;

/**
 * Created by liuchun on 2015/12/18.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private static final String UPDATE_URL = "http://gracesite.applinzi.com/update";
    private static final int[]  IDS = {R.id.base_setting, R.id.notification, R.id.night, R.id.comments,
            R.id.more_setting, R.id.clear_cache, R.id.check_update, R.id.go_feedback, R.id.go_rank, R.id.go_share};
    // Component
    private CheckBox mNight, mNotify, mAdvertise;
    private View mClearCache, mCheckUpdate, mFeedBack;
    private View mGoRank, mGoShare;
    private TextView mCacheSize, mVersion;
    //
    private AlertDialog mDownDialog;
    private ProgressDialog mUpdateDialog;
    private ProgressBar mUpdateProgress;
    //
    private String version;
    //
    private FileManager fileManager;
    private DBManager dbManager;
    private SneezeApplication app;
    private SneezeClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //
        fileManager = FileManager.getInstance(this);
        dbManager = DBManager.getInstance(this);
        app = (SneezeApplication) getApplication();
        client = SneezeClient.getInstance(this);
        initView();
    }

    @Override
    protected void initView(){
        super.initView();
        setToolBarTitle(R.string.action_setting);
        //
        mNight = (CheckBox) findViewById(R.id.night_mode);
        mNotify = (CheckBox) findViewById(R.id.notify_mode);
        mAdvertise = (CheckBox) findViewById(R.id.advertise_mode);
        mClearCache = findViewById(R.id.clear_cache);
        mCheckUpdate = findViewById(R.id.check_update);
        mFeedBack = findViewById(R.id.go_feedback);
        mGoRank = findViewById(R.id.go_rank);
        mGoShare = findViewById(R.id.go_share);

        // 监听checkbox
        mNight.setChecked(app.getNightMode());
        mNight.setOnCheckedChangeListener(this);
        mNotify.setChecked(app.getNotifyMode());
        mNotify.setOnCheckedChangeListener(this);
        mAdvertise.setChecked(app.getAdMode());
        mAdvertise.setOnCheckedChangeListener(this);
        // 监听其他设置按键
        mClearCache.setOnClickListener(this);
        mCheckUpdate.setOnClickListener(this);
        mFeedBack.setOnClickListener(this);
        mGoRank.setOnClickListener(this);
        mGoShare.setOnClickListener(this);

        // 设置文本
        mCacheSize = (TextView) findViewById(R.id.cache_cap_tv);
        mVersion = (TextView) findViewById(R.id.version_tv);

        String cache_size = FileManager.getInstance(this).getStoreDirSize();
        mCacheSize.setText(cache_size);
        version = getAppVersionName();
        mVersion.setText(version);
        // 设置背景
        for(int ids : IDS){
            View v = findViewById(ids);
            if(app.getNightMode()){
                v.setBackgroundResource(R.drawable.item_background_night);
            }else {
                v.setBackgroundResource(R.drawable.item_background);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()){
            case R.id.night_mode:
                app.setNightMode(isChecked);
                updateTheme();
                break;
            case R.id.notify_mode:
                app.setNotifyMode(isChecked);
                break;
            case R.id.advertise_mode:
                /*
                if(isChecked == true){
                    displayCommentsWarning();
                }*/
                app.setAdMode(isChecked);
                break;
            default:break;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.clear_cache:
                displayCacheWarning();
                break;
            case R.id.check_update:
                checkUpdate();
                break;
            case R.id.go_feedback:
                intent = new Intent(this, FeedBackActivity.class);
                startActivity(intent);
                break;
            case R.id.go_rank:
                try{
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
                break;
            case R.id.go_share:
                //Uri uri = Uri.parse("www.baidu.com");
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "www.baidu.com");
                startActivity(intent);
                break;
            default:break;
        }
    }

    private void displayCacheWarning(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_warning);
        builder.setMessage(R.string.dialog_cache_msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                fileManager.clearStoreDir();
                dbManager.clearLocalLink();
                mCacheSize.setText("0KB");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void displayCommentsWarning(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_warning);
        builder.setMessage(R.string.dialog_comments_msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mAdvertise.setChecked(false);
            }
        });

        builder.create().show();
    }

    private String getAppVersionName()
    {
        String version = "1.0.8";

        try {
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packInfo.versionName;
        }catch (Exception e){
            e.printStackTrace();
        }

        return version;
    }

    private void checkUpdate(){
        //圆形进度条
        mUpdateDialog = new ProgressDialog(this);
        mUpdateDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // 圆形转动进度条
        mUpdateDialog.setCancelable(true);  // 设置可以点击Back键取消
        mUpdateDialog.setCanceledOnTouchOutside(false);  // 设置点击Dialog外部是否可以取消Dialog
        mUpdateDialog.setTitle("正在检查更新...");
        mUpdateDialog.show();
        //网络请求
        client.get(UPDATE_URL, null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(mUpdateDialog != null){
                    mUpdateDialog.dismiss();
                    Toast.makeText(SettingActivity.this, "网络出错,请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if(mUpdateDialog != null){
                    mUpdateDialog.dismiss();
                }
                // 处理数据
                JsonUpdateLink updateLink = JsonParserUtil.JsonUpdateLinkParser(responseString);
                String updateVersion = updateLink.getVersion();
                String link = updateLink.getLink();
                if(updateVersion.compareTo(version) > 0){
                    //发现版本更新
                    displayDownLoad(link);
                }else{
                    Toast.makeText(SettingActivity.this, "暂时没有更新", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 弹出对话框提示下载
     * @param link
     */
    private void displayDownLoad(final String link){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("准备下载更新");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 发起下载网络请求
                mUpdateProgress.setVisibility(View.VISIBLE);
                getUpdateApk(link);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mUpdateProgress = new ProgressBar(this);
        mUpdateProgress.setIndeterminate(false);
        Drawable drawable = getResources().getDrawable(android.R.drawable.progress_horizontal);
        mUpdateProgress.setProgressDrawable(drawable);
        mUpdateProgress.setProgress(0);
        mUpdateProgress.setVisibility(View.GONE);
        builder.setView(mUpdateProgress);
        //
        mDownDialog = builder.create();
        mDownDialog.show();
    }

    /**
     * 实现apk下载功能
     * @param url
     */
    public void getUpdateApk(final String url){
        client.get(url, null, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                // 更新进度条
                String[] paths = url.split("/");
                String filename = paths[paths.length - 1];
                fileManager.writeUpdateApk(filename, binaryData);
                //
                mDownDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                Toast.makeText(SettingActivity.this, "网络出错,请稍后再试", Toast.LENGTH_SHORT).show();
                mDownDialog.dismiss();
            }
        });
    }
}
