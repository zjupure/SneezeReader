package com.simit.activity;

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
import android.text.format.Formatter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simit.database.DbController;
import com.simit.storage.FileUtils;
import com.simit.storage.SharedPreferenceUtils;

/**
 * Created by liuchun on 2015/12/18.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private static final String UPDATE_URL = "http://gracesite.applinzi.com/update";
    private static final int[]  IDS = {R.id.base_setting, R.id.notification, R.id.night, R.id.comments,
            R.id.more_setting, R.id.clear_cache, R.id.check_update, R.id.go_feedback, R.id.go_rank, R.id.go_share};
    // Component
    private CheckBox mNight, mNotify, mComments;
    private View mClearCache, mCheckUpdate, mFeedBack;
    private View mGoRank, mGoShare;
    private TextView mCacheSize, mVersion;
    //
    private AlertDialog mDownDialog;
    private ProgressDialog mUpdateDialog;
    private ProgressBar mUpdateProgress;
    //
    private String version;
    private boolean notifyMode = true;
    private boolean commentOpen = false;
    //
    private DbController dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //

    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);

        dbHelper = DbController.getInstance(this);
    }

    @Override
    protected void initView(){
        super.initView();
        setToolBarTitle(R.string.action_setting);
        //
        mNight = (CheckBox) findViewById(R.id.night_mode);
        mNotify = (CheckBox) findViewById(R.id.notify_mode);
        mComments = (CheckBox) findViewById(R.id.comments_mode);
        mClearCache = findViewById(R.id.clear_cache);
        mCheckUpdate = findViewById(R.id.check_update);
        mFeedBack = findViewById(R.id.go_feedback);
        mGoRank = findViewById(R.id.go_rank);
        mGoShare = findViewById(R.id.go_share);

        // 监听checkbox
        mNight.setChecked(mNightMode);
        mNight.setOnCheckedChangeListener(this);

        notifyMode = SharedPreferenceUtils.get(this, "notify", true);
        mNotify.setChecked(notifyMode);
        mNotify.setOnCheckedChangeListener(this);

        commentOpen = SharedPreferenceUtils.get(this, "comments", false);
        mComments.setChecked(commentOpen);
        mComments.setOnCheckedChangeListener(this);
        // 监听其他设置按键
        mClearCache.setOnClickListener(this);
        mCheckUpdate.setOnClickListener(this);
        mFeedBack.setOnClickListener(this);
        mGoRank.setOnClickListener(this);
        mGoShare.setOnClickListener(this);

        // 设置文本
        mCacheSize = (TextView) findViewById(R.id.cache_cap_tv);
        mVersion = (TextView) findViewById(R.id.version_tv);

        long cacheSize = FileUtils.getCacheDirSize(this);
        mCacheSize.setText(Formatter.formatFileSize(this, cacheSize));
        version = getAppVersionName();
        mVersion.setText(version);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()){
            case R.id.night_mode:
                mNightMode = !mNightMode;
                setCurrentTheme(mNightMode, true);
                break;
            case R.id.notify_mode:
                notifyMode = isChecked;
                SharedPreferenceUtils.put(this, "notify", isChecked);
                break;
            case R.id.comments_mode:
                if(isChecked){
                    displayCommentsWarning();
                }
                commentOpen = isChecked;
                SharedPreferenceUtils.put(this, "comments", isChecked);
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
                FileUtils.clearCache(SettingActivity.this);
                dbHelper.clearLocalLink();
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
                mComments.setChecked(false);
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
        //mUpdateDialog.show();
        //网络请求

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

    }
}
