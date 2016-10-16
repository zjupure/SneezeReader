package com.simit.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.simit.database.DbController;
import com.simit.database.Article;
import com.simit.network.NetworkMonitor;
import com.simit.activity.R;
import com.simit.service.UpdateService;
import com.simit.storage.SharedPreferenceUtils;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liuchun on 2015/12/12.
 */
public class DetailFragment extends Fragment {
    private static final String TAG = "DetailFragment";

    private static final String DAPENTI_HOST = "dapenti.com";
    private static final String DAPENTI_IMG_HOST = "ptimg.org";
    private static final String DAPENTI_PIC_HOST = "pic.yupoo.com";
    private static final String[] AD_KEYWORDS = {"google", "show_ads","ad", "taobao", "alibaba",
            "tmall", "tianmao", "jd", "jingdong", "mougujie", "weidian", "360buy", "baidu"};
    private static final String DAY_THEME_CSS = "file:///android_asset/css/day.css";
    private static final String NIGHT_THEME_CSS = "file:///android_asset/css/night.css";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.0.3; zh-cn; M032 Build/IML74K) UC AppleWebKit/534.31 (KHTML, like Gecko) Mobile Safari/534.31";
    // rootView
    private View rootView;  //缓存根View,防止重复渲染
    // component
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private WebSettings webSettings;
    // article info
    private Article article;
    // 加载的是本地文件还是远程文件
    private boolean isLoadLocal;
    // 注入js
    private String wholeJS;
    //
    private Activity activity;


    /**
     * 静态工厂方法
     * @param bundle
     * @return
     */
    public static Fragment newInstance(Bundle bundle){

        Fragment fragment = new DetailFragment();
        fragment.setArguments(bundle);

        return fragment;
    }


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

        activity = getActivity();
        //初始化界面View
        initWebView();
        //
        if(!TextUtils.isEmpty(article.getImgUrl())){
            Uri uri = Uri.parse(article.getImgUrl());
            prefetchToBitmapCache(uri);
        }
    }


    /**
     * 初始化WebView
     */
    private void initWebView(){
        // ProgressBar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_bar);
        // WebView
        mWebView = (WebView) rootView.findViewById(R.id.article_detail_container);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // 垂直滑动栏
        // Websettings
        webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);  // 开启js
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        //webSettings.setUserAgentString(USER_AGENT);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            webSettings.setDisplayZoomControls(false);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            // allow https domain to load http content
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //
                Uri uri = Uri.parse(url);
                Log.i(TAG, "shouldOverrideUriLoading>>>" + uri.getHost());
                if (uri.getHost().equals(DAPENTI_HOST)) {
                    // dapenti
                    return false;
                }
                // Otherwise, the link
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                WebResourceResponse response = new WebResourceResponse("text/plain", "UTF-8", null);
                //站内请求,无需过滤
                Log.w(TAG, "shouldInterceptRequest>>" + url);
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                Log.i(TAG, "request host: " + host);

                if(host.equals(DAPENTI_HOST) || host.equals(DAPENTI_IMG_HOST) ||
                        host.equals(DAPENTI_PIC_HOST)){
                    //主站域名和意图的域名
                    Log.i(TAG, "webview load url: " + url);

                    return null;
                }

                //拦截百度域名
                if(host.equals("pos.baidu.com") || host.equals("eclick.baidu.com")){
                    Log.w(TAG, "webview intercept baidu url: " + url);

                    return response;
                }

                //主站资源不拦截
                if(url.contains("dapenti") || url.contains("penti")){
                    Log.i(TAG, "webview load url: " + url);

                    return null;
                }

                //评论js,根据要求看是否覆盖
                boolean commentOpen = SharedPreferenceUtils.get(activity, "comments", false);
                //评论系统采用的是搜狐的畅言
                if(host.equals("changyan.sohu.com")){
                    if(commentOpen){
                        //开启评论
                        Log.i(TAG, "webview load url: " + url);
                        return null;
                    }else {
                        //关闭评论
                        Log.w(TAG, "webview intercept url: " + url);
                        return response;
                    }
                }

                //其他站外请求,根据关键词过滤
                return filterADs(url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.i(TAG, "onLoadResource>>" + url);
                if(url.contains(DAPENTI_PIC_HOST) || url.contains(DAPENTI_IMG_HOST)){
                    Log.i(TAG, "onLoadResource>>Image resource");
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // 3g条件下,本地加载失败,重新加载远程数据,可能本地缓存被删除了
                if (NetworkMonitor.isMobileConnected(activity) && isLoadLocal) {
                    String remote_url = article.getDescription();
                    mWebView.loadUrl(remote_url);
                    isLoadLocal = false;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel();  //默认处理方式,WebView变空白页
                handler.proceed();   //接受证书
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 注入js代码
                wholeJS = loadThemeJs();
                if(!TextUtils.isEmpty(wholeJS)){
                    // js文件非空
                    mWebView.loadUrl("javascript:" + wholeJS);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinished>>" + url);
                //执行js脚本
                String jsCmd = "";
                boolean mNightMode = SharedPreferenceUtils.get(activity, "nightMode", false);
                if(mNightMode){
                    jsCmd = "javascript:setTheme('night')";
                    mWebView.loadUrl(jsCmd);
                }else{
                    jsCmd = "javascript:setTheme('day')";
                    mWebView.loadUrl(jsCmd);
                }
                Log.w(TAG, "webview current theme: " + (mNightMode ? "nightMode" : "dayMode"));

                // 最后加载图片
                webSettings.setBlockNetworkImage(false);
            }
        });
        // 设置进度条
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
        // 根据网络状态加载URL
        displayArticle();
    }

    @Override
    public void onResume() {
        super.onResume();
        //重新注入js
        if(!TextUtils.isEmpty(wholeJS)){
            // js文件非空
            mWebView.loadUrl("javascript:" + wholeJS);
        }

        String jsCmd = "";
        boolean mNightMode = SharedPreferenceUtils.get(activity, "nightMode", false);
        if(mNightMode){
            jsCmd = "javascript:setTheme('night')";
            mWebView.loadUrl(jsCmd);
        }else{
            jsCmd = "javascript:setTheme('day')";
            mWebView.loadUrl(jsCmd);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            mWebView.onPause();
        }
        //重新加载页面,停止视频和声音
        mWebView.reload();
    }


    /**
     * 加载assert目录下的js文件
     * @return
     */
    private String loadThemeJs(){
        InputStream is;
        try{
            is = activity.getAssets().open("js/theme.js");
            StringBuilder sb = new StringBuilder();
            String line;
            byte[] buffer = new byte[1024];
            int len;
            while((len = is.read(buffer, 0, 1024)) > 0){
                line = new String(buffer, 0, len);
                sb.append(line);
            }
            String content = sb.toString();

            return content;
        }catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 过滤广告,禁用js
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private WebResourceResponse filterADs(String url){
        // API 11
        WebResourceResponse response =  new WebResourceResponse("text/plain", "UTF-8", null);

        //判断是否含有广告关键词
        boolean exist = false;
        for(String keywords: AD_KEYWORDS){
            if(url.contains(keywords)){
                exist = true;
                break;
            }
        }

        // 过滤js请求
        if((url.endsWith(".js") || url.contains("javascript") || url.contains("script")) && exist){
            Log.w(TAG, "webview intercept js url: " + url);

            return response;
        }

        // 过滤图片请求
        if((url.endsWith(".img") || url.endsWith(".jpg") || url.endsWith(".png")) && exist){
            Log.w(TAG, "webview intercept img url: " + url);

            return response;
        }

        // 过滤百度统计相关请求
        if(url.contains("pos.baidu.com") || url.contains("baidustatic")){
            Log.w(TAG, "webview intercept baidu url: " + url);

            return response;
        }


        Log.i(TAG, "webview load url: " + url);
        return null;
    }

    /**
     * 根据网络状态从不同路径加载文章
     */
    public void displayArticle(){
        // 首先判断是否连接wifi，wifi条件下加载远程链接，无wifi条件下优先加载本地缓存资源
        Context context = getActivity();
        if(context == null){
            return;  // 脱离父主Activity
        }

        //Log.d("DetailFragment", "current network state: " + networkState);
        //

        String remoteUrl = article.getDescription();
        String localUrl = article.getLocalLink();
        if(TextUtils.isEmpty(localUrl)){
            //判断数据库中是否存在
            localUrl = DbController.getInstance(activity).getLocalUrl(remoteUrl);
            if(!TextUtils.isEmpty(localUrl)){
                article.setLocalLink(localUrl);
            }
        }

        //
        int networkState = NetworkMonitor.getNetworkType(context);
        if(networkState == NetworkMonitor.NETWORK_WIFI){
            //加载远程连接
            mWebView.loadUrl(remoteUrl);
            isLoadLocal = false;
            //wifi状态下获取页面源码,如果还没有本地缓存, 则缓存该页面
            if(localUrl.isEmpty()){

                Intent intent = new Intent(activity, UpdateService.class);
                intent.putExtra("link", article.getDescription());
                activity.startService(intent);
            }
        }else if((networkState & NetworkMonitor.NETWORK_MOBILE_MASK) == NetworkMonitor.NETWORK_MOBILE){
            // 3g网络优先加载本地连接
            if(!localUrl.isEmpty()){
                // 本地文件存储非空
                mWebView.loadUrl(localUrl);
                isLoadLocal = true;
            }else{
                // 加载远程链接
                mWebView.loadUrl(remoteUrl);
                isLoadLocal = false;
            }
        }else {

            // 没有网络,应该加载本地文件
            if(!localUrl.isEmpty()){
                mWebView.loadUrl(localUrl);
                isLoadLocal = true;
            }else{
                // 理应加载错误提示页面
                mWebView.loadUrl(localUrl);
                isLoadLocal = false;
            }
        }
    }


    /**
     * 返回上级页面
     * @return
     */
    public boolean goBack(){
        // 能够回退到上一页
        if(mWebView.canGoBack()){
            mWebView.goBack();
            return  true;
        }

        return false;
    }

    /**
     *  预加载该页的图片, 用于分享
     */
    private void prefetchToBitmapCache(Uri uri){


        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(uri)
                .build();

        imagePipeline.prefetchToBitmapCache(imageRequest, this);
    }
}
