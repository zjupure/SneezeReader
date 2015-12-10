package com.example.fragment;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.datamodel.Article;
import com.example.datamodel.DataManager;
import com.example.sneezereader.R;

import java.util.List;


/**
 * Created by liuchun on 2015/7/16.
 */
public class YituFragment extends Fragment implements View.OnTouchListener{
    private static final int LEFT_SWIPE = 1;
    private static final int RIGHT_SWIPE = 2;

    private View rootView;
    private WebView mWebView;

    private Article article;
    private int position = 0;

    private float xDown;
    private float yDown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.yitu_page_layout, container, false);
        }
        //缓存的rootView已经被加载过parent,需要移除
        ViewGroup parent = (ViewGroup)rootView.getParent();
        if(parent != null){
            parent.removeView(rootView);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }


    private void init(){
        // webView
        mWebView = (WebView) rootView.findViewById(R.id.yitu_content);
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
        // 设置滑动手势监听
        mWebView.setOnTouchListener(this);

        WebSettings webSettings = mWebView.getSettings();
        //webSettings.setJavaScriptEnabled(true);   // 通过禁用js来禁用广告
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(true);

        // article
        List<Article> articles = DataManager.getInstance().getData(Article.YITU);
        if(articles.size() > 0){
            article = articles.get(position);

            // description is the subscription url
            String url = article.getDescription();
            mWebView.loadUrl(url);
        }
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

        mWebView.getSettings().setBlockNetworkImage(true);
        String url = article.getDescription();
        mWebView.loadUrl(url);
    }
}
