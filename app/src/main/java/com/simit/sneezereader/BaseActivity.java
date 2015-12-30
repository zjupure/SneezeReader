package com.simit.sneezereader;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by liuchun on 2015/12/27.
 */
public class BaseActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private boolean mNightMode;

    private SneezeApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置主题
        setupTheme();
    }

    protected void setupTheme(){
        app = (SneezeApplication) getApplication();
        mNightMode = app.getNightMode();

        if(mNightMode){
            setTheme(R.style.AppTheme_Dark);
        }else{
            setTheme(R.style.AppTheme_Light);
        }
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
        //
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int[] attrs = {android.R.attr.colorPrimary, android.R.attr.colorPrimaryDark, R.attr.global_background};
            TypedArray a = getTheme().obtainStyledAttributes(attrs);
            int navigationColor = a.getColor(0, 0xffffff);
            int statusColor = a.getColor(1, 0xffffff);
            int bgColor = a.getColor(2, 0xffffff);
            ColorDrawable drawable = new ColorDrawable(bgColor);
            getWindow().setBackgroundDrawable(drawable);
            getWindow().setNavigationBarColor(navigationColor);
            getWindow().setStatusBarColor(statusColor);
        }else{
            int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.global_background};
            TypedArray a = getTheme().obtainStyledAttributes(attrs);
            int navigationColor = a.getColor(0, 0xffffff);
            int statusColor = a.getColor(1, 0xffffff);
            int bgColor = a.getColor(2, 0xffffff);
            ColorDrawable drawable = new ColorDrawable(bgColor);
            getWindow().setBackgroundDrawable(drawable);
        }
    }

    /**
     * 初始化界面
     */
    protected void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        if(mToolBar != null){
            mToolBar.setTitle(R.string.app_title);
            setSupportActionBar(mToolBar);
            String clazz = getClass().getName();
            // 不是主Activity设置向上的箭头
            if(!clazz.equals("com.simit.sneezereader.MainActivity")){
                ActionBar actionBar = getSupportActionBar();
                if(actionBar != null){
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }

        }
    }

    protected void updateTheme(){
        // 重启Activity
        overridePendingTransition(0, 0); // 不设置进入退出动画
        finish();
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    protected Toolbar getToolBar(){
        return mToolBar;
    }


    protected void setToolBarTitle(CharSequence title){
        if(mToolBar != null){
            mToolBar.setTitle(title);
        }
    }

    protected void setToolBarTitle(int resid){
        if(mToolBar != null){
            mToolBar.setTitle(resid);
        }
    }
}
