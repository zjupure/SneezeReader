package com.simit.common;

/**
 * Created by liuchun on 2015/12/14.
 */
public class Constants {
    /*********** Message Constants for Handler Start *************/
    public static final int MSG_NETWORK_SUCCESS = 10;      //网络请求成功
    public static final int MSG_NETWORK_ERROR = 11;        //网络请求失败
    public static final int MSG_LOCAL_LOAD_SUCCESS = 12;   //本地数据加载成功
    public static final int MSG_LOCAL_LOAD_FAIL = 13;      //本地数据加载失败
    public static final int MSG_NEW_ARTICLE_ARRIVAL = 14;  //新的图卦消息
    public static final int MSG_NO_NEW_ARTICLE = 15;       //网络上没有新的文章了
    public static final int MSG_NO_MORE_ARTICLE = 16;      //本地没有更多数据了
    public static final int MSG_GET_PAGE_SOURCE = 17;      //需要获取该页面的源码
    /*********** Message Constants for Handler End *************/

    // Message
    public static final int NEW_ARTICLE_ARRIVAL = 1;
    public static final int NO_NEW_ARTICLE = 2;
    public static final int LOAD_MORE_ARTICLE = 3;
    public static final int NO_MORE_ARTICLE = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int GET_PAGE_SOURCE = 6;
    public static final int GET_TUGUA_UPDATE = 7;
    public static final int CHECK_DATABASE = 8;

    public static final int SOURCE_INTERVAL = 5000;  // 5s
    public static final int CHECK_DATABASE_INTERVAL = 5000; // 5s
    public static final int UPDATE_INTERVAL = 10*60*1000; // 5 min

    public static final String DATASET_UPDATED_ACTION = "com.simit.fragment";
    public static final String TIME_FORMAT_REFRESH = "上次更新: yyyy年MM月dd日 HH:mm";
    public static final String TIME_FORMAT_LOAD = "上次加载: yyyy年MM月dd日 HH:mm";

    // weibo api key
    public static final String WEIBO_APP_KEY = "2994830099";
    public static final String WEIBO_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String WEIBO_SCOPE = "";

    // weixin api key
    public static final String WEIXIN_APP_KEY = "wxf92a176150a2a179";
}
