package com.simit.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liuchun on 2015/12/14.
 */
public class Constants {
    /*********** Message Constants for Handler Start *************/
    public static final int MSG_NETWORK_SUCCESS = 0;      //网络请求成功
    public static final int MSG_NETWORK_ERROR = 1;        //网络请求失败
    public static final int MSG_LOCAL_LOAD_SUCCESS = 2;   //本地数据加载成功
    public static final int MSG_LOCAL_LOAD_FAIL = 3;      //本地数据加载失败
    public static final int MSG_NEW_ARTICLE_ARRIVAL = 4;  //新的图卦消息
    public static final int MSG_NO_NEW_ARTICLE = 5;       //网络上没有新的文章了
    public static final int MSG_NO_MORE_ARTICLE = 6;      //本地没有更多数据了
    public static final int MSG_GET_PAGE_SOURCE = 7;      //需要获取该页面的源码
    /*********** Message Constants for Handler End *************/


    public static final String TIME_FORMAT_REFRESH = "上次更新: yyyy年MM月dd日 HH:mm";
    public static final String TIME_FORMAT_LOAD = "上次加载: yyyy年MM月dd日 HH:mm";

    // weibo api key
    public static final String WEIBO_APP_KEY = "2994830099";
    public static final String WEIBO_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String WEIBO_SCOPE = "";

    // weixin api key
    public static final String WEIXIN_APP_KEY = "wxf92a176150a2a179";

    // thread pool
    public static final ExecutorService localFetcherPool = Executors.newCachedThreadPool();
}
