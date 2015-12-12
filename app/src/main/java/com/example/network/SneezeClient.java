package com.example.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;


/**
 * Created by liuchun on 2015/7/17.
 */
public class SneezeClient{

    public static final String BASE_URL = "http://appb.dapenti.com/index.php";
    public static final String SPLASH_IMAGE_PARAM = "/Home/api/loading_pic";
    //private static final String UPDATE_APP_PATH = "s=/Home/api/upgrade.html";
    public static final String TUGUA_PARAM = "/Home/api/tugua";  // GET
    public static final String LEHUO_PARAM = "/Home/api/lehuo";  // GET
    public static final String YITU_PARAM = "/Home/api/yitu";    // GET
    public static final String DUANZI_PARAM = "/Home/api/duanzi";  // GET
    //
    public static final String[] APP_PARAMS = new String[]{TUGUA_PARAM, LEHUO_PARAM, YITU_PARAM, DUANZI_PARAM};
    // parameters: p=1&limit=10
    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_LIMIT_NUMER = 100;  //一页最多50条
    //接口最大数目：图卦30条，乐活30条，意图50条，段子100条
    // singleton
    private static SneezeClient instance;

    private int page_num = DEFAULT_PAGE_NUMBER;
    private int limit_num = DEFAULT_LIMIT_NUMER;

    // httpclient
    private AsyncHttpClient client = new AsyncHttpClient();
    private PersistentCookieStore cookieStore;

    private SneezeClient(Context context){

        cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
    }

    public static SneezeClient getInstance(Context context){
        if(instance == null){
            synchronized (SneezeClient.class){
                if(instance == null){
                    instance = new SneezeClient(context);
                }
            }
        }

        return instance;
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler handler){
        client.get(url, params, handler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler handler){
        client.post(url, params, handler);
    }

    /**
     * 文章所有请求都采用GET方式
     * @param type
     * @param handler
     */
    public void getArticle(int type, AsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("s", APP_PARAMS[type]);
        params.put("p", Integer.toString(page_num));
        params.put("limit", Integer.toString(limit_num));

        client.get(BASE_URL, params, handler);
    }

    /**
     * 获取Splash图片
     * @param handler
     */
    public void getSplashImage(AsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams("s", SPLASH_IMAGE_PARAM);
        client.get(BASE_URL, params, handler);
    }

    /**
     * 获取页面Html源码
     * @param url
     * @param responseHandler
     */
    public void getPageContent(String url, AsyncHttpResponseHandler responseHandler){
        client.get(url, responseHandler);
    }

    public void setRequestParams(int page_num, int limit_num){
        this.page_num = page_num;
        this.limit_num = limit_num;
    }

    public void setLimitNum(int limit_num){
        this.limit_num = limit_num;
    }
}
