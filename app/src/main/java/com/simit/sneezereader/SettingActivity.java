package com.simit.sneezereader;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.simit.storage.FileManager;

/**
 * Created by liuchun on 2015/12/18.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    // Component
    private Toolbar mToolBar;
    private CheckBox mNight, mAdvertise;
    private View mClearCache, mCheckUpdate, mFeedBack;
    private View mGoRank, mGoShare;
    private TextView mCacheSize, mVersion;
    //
    SneezeApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //
        app = (SneezeApplication) getApplication();
        initView();
    }

    private void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        mToolBar.setTitle(R.string.action_setting);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNight = (CheckBox) findViewById(R.id.night_mode);
        mAdvertise = (CheckBox) findViewById(R.id.advertise_mode);
        mClearCache = findViewById(R.id.clear_cache);
        mCheckUpdate = findViewById(R.id.check_update);
        mFeedBack = findViewById(R.id.go_feedback);
        mGoRank = findViewById(R.id.go_rank);
        mGoShare = findViewById(R.id.go_share);

        // 监听checkbox
        mNight.setChecked(app.getNightMode());
        mNight.setOnCheckedChangeListener(this);
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
        String version = getAppVersionName();
        mVersion.setText(version);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()){
            case R.id.night_mode:
                app.setNightMode(isChecked);
                break;
            case R.id.advertise_mode:
                if(isChecked == true){
                    displayWarning(buttonView);
                }
                app.setAdMode(isChecked);
                break;
            default:break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clear_cache:
                break;
            case R.id.check_update:
                break;
            case R.id.go_feedback:
                break;
            case R.id.go_rank:
                try{
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
                break;
            case R.id.go_share:
                Uri uri = Uri.parse("www.baidu.com");
                Intent intent = new Intent(Intent.ACTION_SEND, uri);
                intent.setType("text/plain");
                startActivity(intent);
                break;
            default:break;
        }
    }

    private void displayWarning(final CompoundButton button){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_warning);
        builder.setMessage(R.string.dialog_msg);
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
                button.setChecked(false);
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
}
