package com.example.network;

import android.content.Context;
import android.util.Log;

import com.example.database.DBManager;
import com.example.datamodel.Datainfo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liuchun on 2015/7/17.
 */
public class SneezeClient{
    private static final Object LOCK = new Object();
    private static SneezeClient instance = null;
    //
    private AsyncHttpClient client;
    private PersistentCookieStore cookieStore;
    private Context context;
    private List<Datainfo>  urlQueue;   //待请求URL队列
    private List<Datainfo>  dataSet;  //新的数据集合

    private SneezeClient(Context context){
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);

        this.context = context;

        urlQueue = new ArrayList<Datainfo>();
        dataSet = new ArrayList<Datainfo>();
    }

    /**
     * 单例模式
     * @param context
     * @return
     */
    public static SneezeClient getInstance(Context context){
        if(instance == null){
            synchronized (LOCK){
                if(instance == null){
                    instance = new SneezeClient(context);
                }
            }
        }

        return instance;
    }

    /**
     * 向入口页面发起请求
     * @param type   页面入口类型
     * @param handler  回调函数
     */
    public void getEntryPage(int type, AsyncHttpResponseHandler handler){
        client.get(SneezeRules.PAGE_ENTRY[type], handler);
    }

    /**
     * 向内容页面发起请求
     * @param url     页面地址
     * @param type    页面分类
     * @param handler  回调
     */
    public void getContentPage(String url, int type, AsyncHttpResponseHandler handler){
        client.get(url, handler);
    }

    /**
     * 待请求队列是否为空
     * @return
     */
    public boolean urlQueueEmpty(){

        return urlQueue.size() == 0;
    }

    /**
     * 从请求队列拿出一条URL进行处理
     * @return
     */
    public Datainfo getUrlFront(){
        Datainfo datainfo = null;

        synchronized (urlQueue){
            if(urlQueue.size() > 0){
                datainfo = urlQueue.get(0);
                urlQueue.remove(0);  //出队
            }
        }

        return datainfo;
    }

    /**
     * 数据集是否为空
     * @return
     */
    public boolean dataSetEmpty(){
        return dataSet.size() == 0;
    }

    /**
     * 把数据集中的数据拷贝出来
     * @return
     */
    public List<Datainfo> getDataSet(){
        List<Datainfo> datainfos = new ArrayList<Datainfo>();

        synchronized (dataSet){
            for(Datainfo datainfo : dataSet){
                datainfos.add(datainfo);
            }
            dataSet.clear();  //数据取走之后,清空
        }

        return datainfos;
    }
    /**
     * 根据返回来的页面源码解析出有效的URL
     * @param page  页面源码
     * @param type  页面分类
     * @return
     */
    public boolean getPageLinks(String page, int type){
        List<String> urlList = SneezeRules.getRemoteUrl(page, type);   //获取该页内的超链接
        boolean hasUpdateLink = false;    //是否有新的链接

        DBManager dbm = DBManager.getInstance(context);
        for(String url : urlList){
            //数据库中没有这一条URL
            if(!dbm.hasRemoteUrl(url)){
                //加入待请求队列
                Datainfo datainfo = new Datainfo();
                datainfo.setRemote_url(url);
                datainfo.setType(type);
                //向队列添加数据需要加锁同步
                synchronized (urlQueue){
                    urlQueue.add(datainfo);
                }

                hasUpdateLink = true;   //有新的链接

                Log.d("SneezeClient", url + " " + type);
            }
        }

        return hasUpdateLink;
    }

    /**
     * 提取内容页面的数据
     * @param url   页面地址
     * @param page  页面源码
     * @param type  页面分类
     */
    public void getPageContent(String url, String page, int type){
        Datainfo datainfo = SneezeRules.getData(page, type);
        datainfo.setRemote_url(url);
        //新数据存储到内存
        dataSet.add(datainfo);
    }
}
