package com.simit.sneezereader;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.simit.datamodel.Article;
import com.simit.datamodel.DataManager;
import com.simit.network.NetworkMonitor;
import com.simit.network.SneezeClient;
import com.simit.network.SneezeJsonResponseHandler;
import com.simit.network.SneezePageResponseHandler;

/**
 * Created by liuchun on 2015/12/17.
 */
public class UpdateService extends Service {
    //
    private DataManager dataManager;
    private SneezeClient client;
    private Context context;
    //网络状态监听
    private BroadcastReceiver connReceiver;
    //
    private boolean netWorkAvaible;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        dataManager = DataManager.getInstance();
        client = SneezeClient.getInstance(this);
        // 注册广播接收器
        connReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if(networkInfo == null){
                    // 无网络链接
                    netWorkAvaible = false;
                }else{
                    netWorkAvaible = true;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connReceiver, filter);
        // service一启动,就发送更新的消息
        handler.sendEmptyMessageDelayed(Constant.GET_TUGUA_UPDATE, Constant.UPDATE_INTERVAL);
        handler.sendEmptyMessage(Constant.GET_PAGE_SOURCE);
        //handler.sendEmptyMessageDelayed(Constant.CHECK_DATABASE, Constant.CHECK_DATABASE_INTERVAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(connReceiver != null){
            unregisterReceiver(connReceiver);
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constant.GET_PAGE_SOURCE:
                    String url = dataManager.getLink();
                    if(url != null && !url.isEmpty()){
                        // check network is availble
                        if(netWorkAvaible && NetworkMonitor.getNetWorkState(context) == NetworkMonitor.WIFI){
                            // 只在Wifi条件下加载源码
                            client.getPageContent(url, new SneezePageResponseHandler(context, url));
                        }
                    }
                    // 每5s检测一次队列,下载一个页面
                    sendEmptyMessageDelayed(Constant.GET_PAGE_SOURCE, Constant.SOURCE_INTERVAL);
                    break;
                case Constant.GET_TUGUA_UPDATE:
                    if(netWorkAvaible){
                        // 有网络就更新
                        client.getTugua(new SneezeJsonResponseHandler(context, Article.TUGUA));
                    }
                    // 10 min检测一次更新, 消息需要一直发送, 防止网络断开时消息中止
                    sendEmptyMessageDelayed(Constant.GET_TUGUA_UPDATE, Constant.UPDATE_INTERVAL);
                    break;
                default:break;
            }
        }
    };
}
