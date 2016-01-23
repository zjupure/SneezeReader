package com.simit.sneezereader;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.fragment.ShareDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liuchun on 2015/12/27.
 */
public class BaseActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    // share component
    private TextView mWeiboPhoto;
    private TextView mWeixinPhoto;
    private TextView mWeixinFriendPhoto;
    private TextView mShareCancel;
    //private RelativeLayout mShareMask;
    // 弹窗
    private PopupWindow popupWindow;
    private Article article;
    //
    private SneezeApplication app;
    protected boolean mNightMode;

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
            //
            a.recycle();
        }else{
            int[] attrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.global_background};
            TypedArray a = getTheme().obtainStyledAttributes(attrs);
            int navigationColor = a.getColor(0, 0xffffff);
            int statusColor = a.getColor(1, 0xffffff);
            int bgColor = a.getColor(2, 0xffffff);
            ColorDrawable drawable = new ColorDrawable(bgColor);
            getWindow().setBackgroundDrawable(drawable);
            //
            a.recycle();
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

    /**
     * 设置图标颜色
     * @param item
     */
    public void setFavoriteIcon(MenuItem item, boolean curFavorite) {
        if(item == null){
            return;
        }
        // item存在则进行设置
        Drawable iconDrawable = getResources().getDrawable(R.mipmap.ic_favorite);
        Drawable wrappedDrawable = DrawableCompat.wrap(iconDrawable);
        //boolean isFavorite = article.isFavorite();
        int color = getResources().getColor(R.color.favorite_color_nor);  // while
        if (curFavorite) {
            color = getResources().getColor(R.color.favorite_color);
        }
        DrawableCompat.setTint(wrappedDrawable, color);
        item.setIcon(wrappedDrawable);
    }

    /**
     * 改变收藏状态
     */
    public void changeFavoriteState(MenuItem item, Article article){
        // 更新数据库
        DBManager dbManager = DBManager.getInstance(this);
        // 改变状态
        boolean preFavorite = article.isFavorite();
        boolean curFavorite = !preFavorite;
        article.setIsFavorite(curFavorite);
        setFavoriteIcon(item, curFavorite);
        //
        if(curFavorite){
            // 添加收藏
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            String curTime = sdf.format(new Date());
            dbManager.insertFavorite(article, app.getUsername(), curTime);
            // Toast
            Toast.makeText(this, "添加收藏成功", Toast.LENGTH_SHORT).show();
        }else{
            // 删除收藏
            dbManager.deleteFavorite(article.getId(), app.getUsername());
            // Toast
            Toast.makeText(this, "删除收藏成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 弹出PopupWindow进行分享
     * @param article
     */
    public void shareArticle(Article article){
        this.article = article;
        // 构造PopupWindow
        View popView = LayoutInflater.from(this).inflate(R.layout.share_popup, null);
        mWeiboPhoto = (TextView) popView.findViewById(R.id.share_weibo);
        mWeixinPhoto = (TextView) popView.findViewById(R.id.share_weixin);
        mWeixinFriendPhoto = (TextView)popView.findViewById(R.id.share_weixin_friend);
        mShareCancel = (TextView) popView.findViewById(R.id.share_cancel);
        //mShareMask = (RelativeLayout) popView.findViewById(R.id.share_mask);
        // 设置监听
        mWeiboPhoto.setOnClickListener(mListener);
        mWeixinPhoto.setOnClickListener(mListener);
        mWeixinFriendPhoto.setOnClickListener(mListener);
        mShareCancel.setOnClickListener(mListener);
        //mShareMask.setOnClickListener(mListener);
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
                lp.alpha = 0.7f;
                getWindow().setAttributes(lp);
            }
        }, 1000);
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
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

    public void shareArticleDialog(Article article){
        ShareDialogFragment fragment = ShareDialogFragment.newInstance(article);
        FragmentManager fm = getSupportFragmentManager();
        fragment.show(fm, "ShareDialog");
    }
}
