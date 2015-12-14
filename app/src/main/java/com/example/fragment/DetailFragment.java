package com.example.fragment;

import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.database.DBManager;
import com.example.datamodel.Article;
import com.example.network.NetworkMonitor;
import com.example.network.SneezeClient;
import com.example.network.SneezePageResponseHandler;
import com.example.sneezereader.R;

/**
 * Created by liuchun on 2015/12/12.
 */
public class DetailFragment extends Fragment {
    // rootView
    private View rootView;  //缓存根View,防止重复渲染
    // component
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private WebSettings webSettings;
    // article info
    private Article article;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.article_datail_frag, container, false);
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

        article = getArguments().getParcelable("article");
        //初始化界面View
        initWebView();
    }


    private void initWebView(){
        // ProgressBar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_bar);
        // WebView
        mWebView = (WebView) rootView.findViewById(R.id.article_detail_container);
        webSettings = mWebView.getSettings();
        //
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        //webSettings.setJavaScriptEnabled(true);   // 通过禁用js来禁用广告
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);   // 点击链接覆盖窗口
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                //Log.d("FlingWebView", "onRecivedError");
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel();  //默认处理方式,WebView变空白页
                handler.proceed();  //接受证书
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webSettings.setBlockNetworkImage(false);
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

        // 首先判断是否连接wifi，wifi条件下加载远程链接，无wifi条件下优先加载本地缓存资源
        String local_url = article.getLocal_link();
        String remote_url = article.getDescription();
        Context context = getActivity();
        int networkState = NetworkMonitor.getNetWorkState(context);
        if(networkState == NetworkMonitor.WIFI){
            //加载远程连接
            mWebView.loadUrl(remote_url);
            //wifi状态下获取页面源码,如果还没有本地缓存, 则缓存该页面
            if(local_url.isEmpty()){
                SneezeClient client = SneezeClient.getInstance(context);
                client.getPageContent(remote_url, new SneezePageResponseHandler(context, remote_url));
            }
        }else if(networkState > NetworkMonitor.WIFI){
            //
            if(!local_url.isEmpty()){
                // 本地文件存储非空
                mWebView.loadUrl(local_url);
            }else{
                // 加载远程链接
                mWebView.loadUrl(remote_url);
            }
        }else {
            // 无网络链接，去数据库查询
            local_url = DBManager.getInstance(getActivity()).getLocalUrl(remote_url);
            mWebView.loadUrl(local_url);
        }
    }
}
