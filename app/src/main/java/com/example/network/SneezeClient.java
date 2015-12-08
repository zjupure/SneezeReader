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
    public static final String DOWNLOAD_SPLASH_PATH = "?s=/Home/api/loading_pic";
    //private static final String UPDATE_APP_PATH = "s=/Home/api/upgrade.html";
    public static final String TUGUA_PATH = "?s=/Home/api/tugua";  // GET
    public static final String LEHUO_PATH = "?s=/Home/api/lehuo";  // POST
    public static final String YITU_PATH = "?s=/Home/api/yitu";    // POST
    public static final String DUANZI_PATH = "?s=/Home/api/duanzi";  // POST
    //
    //public static final String[] APP_PATHS = new String[]{TUGUA_PATH, LEHUO_PATH, YITU_PATH, DUANZI_PATH};
    // parameters: p=1&limit=10
    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_LIMIT_NUMER = 10;

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

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * 图卦是GET请求
     * @param responseHandler
     */
    public void getTugua(AsyncHttpResponseHandler responseHandler){
        RequestParams params = new RequestParams("s", "/Home/api/tugua");
        params.add("p", Integer.toString(page_num));
        params.add("limit", Integer.toString(limit_num));
        //String tu_url = BASE_URL + TUGUA_PATH + "&p=1&limit=10";
        client.get(BASE_URL, params, responseHandler);
    }

    /**
     * 乐活, 意图, 段子是POST请求
     * @param url
     * @param responseHandler
     */
    public void getArticle(String url, AsyncHttpResponseHandler responseHandler){
        RequestParams params = new RequestParams();
        params.add("p", Integer.toString(page_num));
        params.add("limit", Integer.toString(limit_num));

        client.post(getAbsoluteUrl(url), responseHandler);
    }

    public void getPageContent(String url, AsyncHttpResponseHandler responseHandler){
        client.get(url, responseHandler);
    }

    public String getAbsoluteUrl(String url){
        return BASE_URL + url;
    }

    public void setRequestParams(int page_num, int limit_num){
        this.page_num = page_num;
        this.limit_num = limit_num;
    }

    public RequestParams getRequestParams(int page_num, int limit_num){
        RequestParams params = new RequestParams();
        params.put("p", Integer.toString(page_num));
        params.put("limit", Integer.toString(limit_num));

        return params;
    }
}
