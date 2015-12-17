package com.simit.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
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

import com.simit.database.DBManager;
import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.network.NetworkMonitor;
import com.simit.network.SneezeClient;
import com.simit.network.SneezePageResponseHandler;
import com.simit.sneezereader.R;
import com.simit.sneezereader.UpdateService;

/**
 * Created by liuchun on 2015/12/12.
 */
public class DetailFragment extends Fragment {
    private static final String DAPENTI_HOST = "dapenti.com";
    // rootView
    private View rootView;  //缓存根View,防止重复渲染
    // component
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private WebSettings webSettings;
    // article info
    private Article article;
    // network state
    private int networkState;
    // 加载的是本地文件还是远程文件
    private boolean location;

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
        networkState = NetworkMonitor.getNetWorkState(getActivity());
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
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            webSettings.setDisplayZoomControls(false);
        }

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //
                Uri uri = Uri.parse(url);
                if (uri.getHost().equals(DAPENTI_HOST)) {
                    // dapenti
                    return false;
                }

                // Otherwise, the link
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                Context context = getActivity();
                if (context != null) {
                    networkState = NetworkMonitor.getNetWorkState(context);
                }
                // 3g条件下,本地加载失败,重新加载远程数据,可能本地缓存被删除了
                if (networkState >= NetworkMonitor.WIFI && location == false) {
                    String remote_url = article.getDescription();
                    mWebView.loadUrl(remote_url);
                    location = true;
                }
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

        displayArticle();
    }

    /**
     * 根据网络状态从不同路径加载文章
     */
    public void displayArticle(){
        // 首先判断是否连接wifi，wifi条件下加载远程链接，无wifi条件下优先加载本地缓存资源
        Context context = getActivity();
        if(context != null){
            networkState = NetworkMonitor.getNetWorkState(context);
        }

        Log.d("DetailFragment", "current network state: " + networkState);
        //
        String remote_url = article.getDescription();
        String local_url = article.getLocal_link();
        if(local_url.isEmpty()){
            local_url = DBManager.getInstance(getActivity()).getLocalUrl(remote_url);
            // 更新链接
            if(!local_url.isEmpty()){
                article.setLocal_link(local_url);
            }
        }

        Log.d("DetailFragment", "remote_url: "  + remote_url);
        Log.d("DetailFragment", "local_url: " + local_url);
        //
        if(networkState == NetworkMonitor.WIFI){
            //加载远程连接
            mWebView.loadUrl(remote_url);
            location = true;
            //wifi状态下获取页面源码,如果还没有本地缓存, 则缓存该页面
            if(local_url.isEmpty()){
                SneezeClient client = SneezeClient.getInstance(context);
                //client.getPageContent(remote_url, new SneezePageResponseHandler(context, remote_url));
                DataManager.getInstance().putLink(remote_url);
            }
        }else if(networkState > NetworkMonitor.WIFI){
            // 3g网络优先加载本地连接
            if(!local_url.isEmpty()){
                // 本地文件存储非空
                mWebView.loadUrl(local_url);
                location = false;
            }else{
                // 加载远程链接
                mWebView.loadUrl(remote_url);
                location = true;
            }
        }else {

            // 没有网络,应该加载本地文件
            if(!local_url.isEmpty()){
                mWebView.loadUrl(local_url);
                location = false;
            }else{
                // 理应加载错误提示页面
                mWebView.loadUrl(local_url);
                location = false;
            }
        }

        Log.d("DetailFragment", "webview is loading data...");
    }

    public boolean goBack(){
        // 能够回退到上一页
        if(mWebView.canGoBack()){
            mWebView.goBack();
            return  true;
        }

        return false;
    }
}
