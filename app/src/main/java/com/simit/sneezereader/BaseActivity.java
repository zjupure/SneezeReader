package com.simit.sneezereader;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.network.SneezeClient;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liuchun on 2015/12/27.
 */
public class BaseActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    // weibo share component
    private IWeiboShareAPI mWeiboShareAPI;
    // weibo share response
    private IWeiboHandler.Response mWeiboResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置主题
        setupTheme();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(mWeiboShareAPI != null){
            mWeiboShareAPI.handleWeiboResponse(intent, mWeiboResponse);
        }
    }

    protected void setupTheme(){
        SneezeApplication app = (SneezeApplication) getApplication();
        boolean mNightMode = app.getNightMode();

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

    /**
     * 初始化分享组件
     */
    protected void initShareConponents(Bundle bundle){
        // 先注册,后分享
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constant.WEIBO_APP_KEY);
        mWeiboShareAPI.registerApp();
        // 初始化Response接口
        mWeiboResponse = new IWeiboHandler.Response() {
            @Override
            public void onResponse(BaseResponse baseResponse) {
                //接收微博分享后的返回数据
                switch (baseResponse.errCode){
                    default:break;
                }
            }
        };

        if(bundle != null){
            mWeiboShareAPI.handleWeiboResponse(getIntent(), mWeiboResponse);
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
        // 改变状态
        boolean preFavorite = article.isFavorite();
        boolean curFavorite = !preFavorite;
        article.setIsFavorite(curFavorite);
        setFavoriteIcon(item, curFavorite);
        // 更新数据库
        DBManager dbManager = DBManager.getInstance(this);
        if(curFavorite){
            // 添加收藏
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            String curTime = sdf.format(new Date());
            dbManager.insertFavorite(article, "anonymous", curTime);
            // Toast
            Toast.makeText(this, "添加收藏成功", Toast.LENGTH_SHORT).show();
        }else{
            // 删除收藏
            dbManager.deleteFavorite(article.getId(), "anonymous");
            // Toast
            Toast.makeText(this, "删除收藏成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 分享文章信息
     * @param article
     */
    public void shareArticle(Article article){
        // 发起分享
        shareToWeibo(article);
    }

    /**
     * 分享到微博
     * @param article
     */
    public void shareToWeibo(Article article){
        if(mWeiboShareAPI != null){
            // 分享文章的链接
            WeiboMultiMessage message = new WeiboMultiMessage();
            TextObject textObject = new TextObject();
            // 文本内容
            String content = article.getTitle() + article.getDescription();
            textObject.text = content;
            message.textObject = textObject;
            // 初始化Request
            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
            request.transaction = String.valueOf(System.currentTimeMillis());
            request.multiMessage = message;
            // 发起分享Request
            mWeiboShareAPI.sendRequest(this, request);
        }
    }
}
