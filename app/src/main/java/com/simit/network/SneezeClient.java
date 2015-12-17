package com.simit.network;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

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
    private static final int DEFAULT_LIMIT_NUMER = 100;  //每次启动取100条记录
    //接口最大数目：图卦30条，乐活30条，意图50条，段子100条
    // singleton
    private static SneezeClient instance;

    private int page_num = DEFAULT_PAGE_NUMBER;
    private int limit_num = DEFAULT_LIMIT_NUMER;
    private int[] limits = {10, 30, 30, 30};
    private boolean isUpdated = false;
    // httpclient
    private AsyncHttpClient client = new AsyncHttpClient();
    private PersistentCookieStore cookieStore;

    private SneezeClient(Context context){

        cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
        // 信任所有的主机
        client.setSSLSocketFactory(SSLSocketFactoryEx.getFixedSocketFactory());
        // 使用assets目录下的特定证书
        //client.setSSLSocketFactory(getSpecSocketFactory(context));
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

        if(isUpdated){
            // 进入了主界面
            params.put("limit", Integer.toString(limits[type]));
        }else{
            params.put("limit", Integer.toString(limit_num));
        }

        client.get(BASE_URL, params, handler);
    }

    /**
     * 获取图卦页面更新
     * @param handler
     */
    public void getTugua(AsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("s", TUGUA_PARAM);
        params.put("p", Integer.toString(page_num));
        params.put("limit", Integer.toString(5));

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

    public void setLimitNum(int limit){
        limit_num = limit;
    }

    public void setUpdated(boolean updated){
        isUpdated = updated;
    }

    public SSLSocketFactory getSpecSocketFactory(Context context){
        SSLSocketFactory socketFactory;
        InputStream is;
        try{
            is = context.getAssets().open("dapenti.crt");
            CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
            Certificate cert = cerFactory.generateCertificate(is);
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust", cert);

            socketFactory = new SSLSocketFactoryEx(keyStore);
        }catch (Exception e){
            e.printStackTrace();
            socketFactory = SSLSocketFactory.getSocketFactory();
        }

        return socketFactory;
    }
}