package com.simit.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.simit.database.DbController;
import com.simit.fragment.ShareDialogFragment;
import com.simit.database.Article;
import com.simit.storage.SharedPreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by liuchun on 2015/12/27.
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 页面顶部的Toolbar
     */
    protected Toolbar mToolBar;
    // 弹窗
    private PopupWindow popupWindow;
    /**
     * 是否夜间模式
     */
    protected boolean mNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set theme
        mNightMode = SharedPreferenceUtils.get(this, "nightMode", false);
        setCurrentTheme(mNightMode);
        // set layout
        setContentView(getLayoutId());
        // handle intent
        handleIntent(getIntent());
        initView();

        Log.d("BaseActivity", this.getClass().getName() + "-->onCreate() called");
    }


    protected void setCurrentTheme(boolean mNightMode){
        setCurrentTheme(mNightMode, false);
    }

    /**
     * 设置当前的主题风格
     * 日间模式/夜间模式
     * @param mNightMode
     */
    protected void setCurrentTheme(boolean mNightMode, boolean shouldRecreate){
        SharedPreferenceUtils.put(this, "nightMode", mNightMode);

        if(mNightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if(shouldRecreate){
            restartActivity();
        }
    }

    /**
     * 由子类复写返回布局文件id
     * @return
     */
    protected abstract int getLayoutId();


    /**
     * 处理intent
     * @param intent
     */
    protected void handleIntent(Intent intent){

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
            // 默认设置向上箭头
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }



    /**
     * 重启当前Activity
     */
    protected void restartActivity(){

        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
        //    recreate();
        //}else {
            overridePendingTransition(0, 0); // 不设置进入退出动画
            finish();
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            overridePendingTransition(0, 0);
            startActivity(intent);
        //}
    }

    /**
     * 设置Toolbar的标题
     * @param title
     */
    protected void setToolBarTitle(CharSequence title){
        if(mToolBar != null){
            mToolBar.setTitle(title);
        }
    }

    /**
     * 使用资源文件设置Toolbar的标题
     * @param resId
     */
    protected void setToolBarTitle(int resId){
        if(mToolBar != null){
            mToolBar.setTitle(resId);
        }
    }


    /**
     * 刷新当前的收藏状态
     * @param article
     */
    protected void refreshFavoriteState(Article article){

        boolean isFavorite = !article.isFavorite();
        article.setFavorite(isFavorite);

        DbController dbHelper = DbController.getInstance(this);
        String username = SharedPreferenceUtils.get(this, "username", "any");
        if(isFavorite){
            //添加收藏
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String addTime = sdf.format(new Date());
            dbHelper.insertFavorite(article, username, addTime);
            // Toast
            Toast.makeText(this, "添加收藏成功", Toast.LENGTH_SHORT).show();
        }else {
            //删除收藏
            dbHelper.deleteFavorite(article.getId(), username);
            // Toast
            Toast.makeText(this, "删除收藏成功", Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu(); //刷新菜单

    }

    /**
     * 设置图标颜色
     * @param item
     */
    public void setFavoriteIcon(MenuItem item, boolean isFavorite) {
        if(item == null){
            return;
        }
        // item存在则进行设置
        Drawable iconDrawable = getResources().getDrawable(R.mipmap.ic_favorite);
        Drawable wrappedDrawable = DrawableCompat.wrap(iconDrawable);

        int color = 0xffffff;  // while
        if (isFavorite) {
            color = getResources().getColor(R.color.favorite_color);
        }else {
            color = getResources().getColor(R.color.favorite_color_nor);
        }
        DrawableCompat.setTint(wrappedDrawable, color);
        item.setIcon(wrappedDrawable);
    }




    /**
     * 弹出PopupWindow进行分享
     * @param article
     */
    public void shareArticle(final Article article){

        // 构造PopupWindow
        View popView = LayoutInflater.from(this).inflate(R.layout.share_popup, null);
        TextView mWeiboPhoto = (TextView) popView.findViewById(R.id.share_weibo);
        TextView mWeixinPhoto = (TextView) popView.findViewById(R.id.share_weixin);
        TextView mWeixinFriendPhoto = (TextView)popView.findViewById(R.id.share_weixin_friend);
        TextView mShareCancel = (TextView) popView.findViewById(R.id.share_cancel);

        View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                // 跳转Activity
                switch (v.getId()){
                    case R.id.share_weibo:
                        startShareActivity(article, "weibo");
                        break;
                    case R.id.share_weixin:
                        startShareActivity(article, "weixin");
                        break;
                    case R.id.share_weixin_friend:
                        startShareActivity(article, "weixinfriend");
                        break;
                    case R.id.share_cancel:
                        break;
                    default:break;
                }
            }
        };

        // 设置监听
        mWeiboPhoto.setOnClickListener(mListener);
        mWeixinPhoto.setOnClickListener(mListener);
        mWeixinFriendPhoto.setOnClickListener(mListener);
        mShareCancel.setOnClickListener(mListener);
        // 设置弹窗
        popupWindow = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        // 初始化窗体
        int share_bgcolor = getResources().getColor(R.color.share_bgcolor);
        popupWindow.setBackgroundDrawable(new ColorDrawable(share_bgcolor));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow = null;
                LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        popupWindow.update();
        // 显示窗体
        View parent = getWindow().getDecorView().getRootView();
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        // 背景Activity变暗
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.6f;
                getWindow().setAttributes(lp);
            }
        }, 1000);
    }



    /**
     * 启动分享Activity
     * @param article
     * @param from
     */
    public void startShareActivity(Article article, String from){
        Intent intent = new Intent(this, ShareActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("article", article);
        bundle.putString("from", from);
        intent.putExtra("share", bundle);
        // 启动分享Activity
        startActivity(intent);
    }
}
