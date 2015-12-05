package com.example.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.database.DBManager;
import com.example.datamodel.DataManager;
import com.example.datamodel.Datainfo;
import com.example.network.SneezeClient;
import com.example.network.SneezeRules;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liuchun on 2015/7/20.
 */
public class MessageService extends Service {
    //消息号
    public static final int IDLE_TASK = 98;           //空闲状态
    public static final int URL_LINKS_ARRIVAL = 99;
    public static final int REC_PAGE_LINKS = 100;   //收到页面源码
    public static final int REC_PAGE_CONTENT = 101;  //收到页面内容

    private SneezeClient client;
    private NetworkHandler handler;
    private ExecutorService threadpool;
    private DBManager dbm;
    @Override
    public void onCreate() {
        super.onCreate();

        client = SneezeClient.getInstance(this);
        handler = new NetworkHandler(getMainLooper());
        //threadpool = Executors.newFixedThreadPool(5);  //开5个线程池
        dbm = DBManager.getInstance(this);
        //dbm.open(DBManager.READ_WRITE);  //打开数据库

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //boolean post = false, cur = false;
                //线程循环检测URL队列是否为空,轮询机制
                while(true){
                    //post = cur;  //存储前一个状态
                    //cur = false;

                    if(!client.urlQueueEmpty()){
                        handler.sendEmptyMessage(URL_LINKS_ARRIVAL);  //非空,发一条消息通知主线程去处理
                        //cur = true;
                    }

                    //数据集不为空,取出来写入数据库和DataManager
                    if(!client.dataSetEmpty()){
                        List<Datainfo> datainfos = client.getDataSet();
                        dbm.addMultiRecords(datainfos);
                        //加入数据管理器
                        DataManager dm = DataManager.getInstance();
                        dm.addDataset(datainfos);

                        //cur = true;
                    }

                    //连续两次没有接收到数据
                    /*
                    if(post == false || cur == false){
                        handler.sendEmptyMessage(IDLE_TASK);   // 发送空闲消息,让Service停掉
                        break;   //跳出线程,中止
                    } */

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        //threadpool.execute(thread);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbm.open(DBManager.READ_WRITE);  //打开数据库

        if(intent == null)
            return START_STICKY;

        int type = intent.getIntExtra("page", -1);

        //只请求单个栏目
        if(type >= 0){
            requestEntryPage(type);
        }else{
            //4个栏目同时请求
            for(int page = 0; page < 4; page++){
                requestEntryPage(page);
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        dbm.close();
    }

    /**
     * 请求入口页面
     * @param type
     */
    public void requestEntryPage(final int type){
        //设置Handler的编码方式
        client.getEntryPage(type, new TextHttpResponseHandler(SneezeRules.WEB_CHARSET) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final String responseString) {
                //responseString默认是UTF-8编码的,需要设置编码方式为GBK
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.getPageLinks(responseString, type);
                        handler.sendEmptyMessage(REC_PAGE_LINKS);  //收到页面源码

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                //threadpool.execute(thread);  //加入线程池
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("MessageService", "entry page access failure!\n");
            }
        });
    }

    /**
     * 请求内容页面
     * @param url  页面远程地址
     * @param type 页面类型
     */
    public void requestContentPage(final String url, final int type){
        //设置Handler的编码方式
        client.getContentPage(url, type, new TextHttpResponseHandler(SneezeRules.WEB_CHARSET) {
            @Override
            public void onSuccess(int statusCode, final Header[] headers, final String responseString) {
                //responseString默认是UTF-8编码的,需要设置编码方式为GBK
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.getPageContent(url, responseString, type);
                        handler.sendEmptyMessage(REC_PAGE_CONTENT);   //收到页面内容

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                //threadpool.execute(thread);  //加入线程池
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("MessageService", "content page access failure!\n" + url);
            }
        });
    }

    /**
     * 处理URL队列
     */
    public void dealUrlQueue() {
        Datainfo datainfo = null;

        //每次处理一条URL
        if (!client.urlQueueEmpty()){
            datainfo = client.getUrlFront();
            String url = datainfo.getRemote_url();
            int type = datainfo.getType();

            requestContentPage(url, type);
            //是否需要休眠等待
        }
    }

    /**
     * 处理网络消息
     */
    public class NetworkHandler extends Handler{

        public NetworkHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case IDLE_TASK:
                    //dbm.close();   //停止服务前,关闭数据库
                    //stopSelf();
                    break;
                case URL_LINKS_ARRIVAL:
                    dealUrlQueue();
                    break;
                case REC_PAGE_LINKS:
                    break;
                case REC_PAGE_CONTENT:
                    break;
                default:break;
            }
        }
    }
}
