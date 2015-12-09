package com.example.sneezereader;

import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.datamodel.Article;
import com.example.datamodel.DataManager;

import java.util.List;


/**
 * Created by liuchun on 2015/12/6.
 */
public class DetailActivity extends AppCompatActivity implements View.OnTouchListener{
    private static final int LEFT_SWIPE = 1;
    private static final int RIGHT_SWIPE = 2;


    private Toolbar mToolBar;
    private ProgressBar mProgressBar;
    private WebView mWebView;

    private Article article;
    private int position;

    private float xDown;
    private float yDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail_layout);


        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("detail");
        article = bundle.getParcelable("article");
        position = intent.getIntExtra("position", 0);

        Log.d("DetailActivity", "position: " + position);
        Log.d("DetailActivity", article.getDescription());
        Log.d("DetailActivity", article.getRemote_link());

        initView();
    }


    private void initView(){
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        //mToolBar.setTitle(R.string.app_title);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ProgressBar
        mProgressBar = (ProgressBar) findViewById(R.id.loading_bar);

        // mWebView
        mWebView = (WebView) findViewById(R.id.article_detail_container);

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);   // 点击链接覆盖窗口
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel();  //默认处理方式,WebView变空白页
                handler.proceed();  //接受证书
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebView.getSettings().setBlockNetworkImage(false);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // update progress bar
                mProgressBar.setProgress(newProgress);

                if (newProgress >= mProgressBar.getMax()) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        // 设置滑动手势监听
        mWebView.setOnTouchListener(this);

        WebSettings webSettings = mWebView.getSettings();
        //webSettings.setJavaScriptEnabled(false);   // 通过禁用js来禁用广告
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(true);

        // description is the subscription url
        String url = article.getDescription();
        mWebView.loadUrl(url);
        //String url = "http://www.baidu.com";
        //mWebView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.getSettings().setBlockNetworkImage(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                yDown = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if(event.getRawX() - xDown >= 50 && Math.abs(event.getRawY() - yDown) < 50){
                    // right swipe
                    updateArticle(RIGHT_SWIPE);
                    return true;  // 拦截
                }else if(event.getRawX() - xDown <= -50 && Math.abs(event.getRawY() - yDown) < 50){
                    // left swipe
                    updateArticle(LEFT_SWIPE);
                    return true;  // 拦截
                }
                break;
            default:break;
        }

        return false;
    }

    private void updateArticle(int direction){
        int type = article.getType();
        List<Article> articles = DataManager.getInstance().getData(type);

        if(direction == LEFT_SWIPE){
            if(position < articles.size() - 1){
                article = articles.get(++position);
            }
        }else if(direction == RIGHT_SWIPE){
            if(position > 0){
                article = articles.get(--position);
            }
        }

        String url = article.getDescription();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0); // reset
        mWebView.loadUrl(url);
    }
}
