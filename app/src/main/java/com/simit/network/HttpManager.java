package com.simit.network;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.simit.database.Article;
import com.simit.json.ParserUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liuchun on 16/9/2.
 */
public class HttpManager {
    public static final String TAG = "HttpManager";
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

    private static final long CACHE_SIZE = 10 * 1024 * 1024;  // 10M

    /** 单例模式 */
    private static volatile HttpManager instance;

    private int pageNum = DEFAULT_PAGE_NUMBER;
    private int limitNum = DEFAULT_LIMIT_NUMER;

    private final OkHttpClient httpClient;

    /**
     * 私有方法,构造OkHttpClient
     * @param context
     */
    private HttpManager(Context context){


        Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);

        httpClient = new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }

    /**
     * DCL获取单例
     * @param context
     * @return
     */
    public static HttpManager getInstance(Context context){

        if(instance == null){
            synchronized (HttpManager.class){
                if(instance == null){
                    instance = new HttpManager(context);
                }
            }
        }

        return instance;
    }

    public interface INetworkCallback<T>{

        /**
         * 网络请求异常的返回
         * @param e
         */
        public void onError(Exception e);

        /**
         * 网络请求成功的返回
         * @param data
         */
        public void onSuccess(T data);
    }


    /**
     * get方法
     * @param url
     * @param callback
     */
    private void get(String url, ArrayMap<String,String> params, Callback callback){

        String getUrl = buildUrl(url, params);
        Request request = new Request.Builder()
                .url(getUrl)
                .build();

        Log.d(TAG, "url: " + getUrl);

        httpClient.newCall(request).enqueue(callback);
    }


    /**
     * 构造Get请求的URL
     * @param baseUrl
     * @param params
     * @return
     */
    private static String buildUrl(String baseUrl, ArrayMap<String, String> params){

        StringBuilder sb = new StringBuilder(baseUrl);
        if(params.size() > 0){
            sb.append("?");
            for(Map.Entry<String, String> entry : params.entrySet()){
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append("&");
            }
            // delete last "&"
            sb.delete(sb.length()-1, sb.length());
        }

        return sb.toString();
    }


    /**
     * 获取文章信息
     * @param type
     * @param callback
     */
    public void getArticle(final int type, final INetworkCallback<List<Article>> callback){

        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("s", APP_PARAMS[type]);
        params.put("p", String.valueOf(pageNum));
        params.put("limit", String.valueOf(limitNum));

        get(BASE_URL, params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /**TODO 异常信息处理 **/

                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /**TODO 数据解析处理成Bean对象 **/
                String resp = response.body().string();
                Article[] articles = ParserUtils.parseArticles(resp);

                ArrayList<Article> list = new ArrayList<Article>();
                String lastPubDate = "";
                for(Article article : articles){
                    if(article.getTitle().equalsIgnoreCase("AD")){
                        continue;  // filter advertisement
                    }
                    // fix the pubDate bug
                    String title = article.getTitle();
                    String pubDate = article.getPubDate(); // 2015-11-26 14:27:00
                    String date = pubDate.substring(0, 10);
                    date = date.replace("-", "");
                    Pattern pattern = Pattern.compile("\\d{8}"); // ^[\u3010].*\d{8}[\u3011]$ 匹配图卦标题
                    Matcher matcher = pattern.matcher(title);
                    if(matcher.find()){
                        String realDate = matcher.group();
                        if(!realDate.equals(date)){
                            pubDate = realDate.substring(0, 4) + "-" + realDate.substring(4, 6) +
                                    "-" + realDate.substring(6, 8) + pubDate.substring(10, pubDate.length());
                            //Log.d("JsonResponse", pubDate);
                        }
                    }
                    // fix "1970-01-01 08:00:00" pubDate bug
                    if(pubDate.contains("1970-01-01")){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        try{
                            Date lastDate = sdf.parse(lastPubDate);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(lastDate);
                            // 回退5分钟
                            calendar.add(Calendar.MINUTE, -5);
                            Date curDate = calendar.getTime();
                            //
                            pubDate = sdf.format(curDate);
                        }catch (ParseException e){
                            Log.e(TAG, "ParseException: " + e.getMessage());
                        }
                    }
                    // record last pubDate and update article
                    lastPubDate = pubDate;
                    article.setPubDate(pubDate);

                    list.add(article);
                }
                // update the article type
                for(Article article : list){
                    article.setType(type);
                }

                callback.onSuccess(list);
            }

        });
    }

    /**
     * 获取Splash的图片地址
     * @param callback
     */
    public void getSplashImage(final INetworkCallback<String> callback){

        ArrayMap<String, String> arrayMap = new ArrayMap<>();
        arrayMap.put("s", SPLASH_IMAGE_PARAM);

        get(BASE_URL, arrayMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /**TODO 异常处理*/
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();

                Log.e("ParserUtils", resp);

                String imgUrl = ParserUtils.parseSplashImgUrl(resp);

                callback.onSuccess(imgUrl);
            }
        });
    }


    /**
     * 获取页面html源码
     * @param url
     * @param callback
     */
    public void getPageSource(String url, final INetworkCallback<InputStream> callback){

        final Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //
                InputStream is = response.body().byteStream();
                callback.onSuccess(is);
            }
        });
    }

}
