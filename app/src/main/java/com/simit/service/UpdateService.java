package com.simit.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.simit.database.DbController;
import com.simit.network.HttpManager;
import com.simit.network.NetworkMonitor;
import com.simit.storage.FileUtils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by liuchun on 2015/12/17.
 */
public class UpdateService extends Service {
    private static final String TAG = "UpdateService";
    //
    private int curNetworkState = NetworkMonitor.NETWORK_INVALID;
    private boolean isNetworkAvailable = false;
    private boolean isWifiAvaiable = false;
    //阻塞队列和后台线程
    private HashSet<String> urlSet;   //防止重复添加到队列
    private BlockingQueue<String> remoteUrlQueue;
    private NetworkDispatcher dispatcher;
    //数据库
    private DbController dbHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        dbHelper = DbController.getInstance(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);

        registerReceiver(connReceiver, filter);

        //创建阻塞队列和后台线程
        urlSet = new HashSet<>();
        remoteUrlQueue = new ArrayBlockingQueue<String>(1024);
        dispatcher = new NetworkDispatcher();
        dispatcher.start();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            String url = intent.getStringExtra("link");
            if(!TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {

                if(!urlSet.contains(url)) {

                    try {
                        remoteUrlQueue.put(url);
                        urlSet.add(url);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "UpdateService--->onStartCommand: " + e.getMessage());
                    }
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(connReceiver != null){
            unregisterReceiver(connReceiver);
        }
        dispatcher.cancel();
    }

    /**
     * 监听网络变化和SD卡挂载的接收器
     */
    private BroadcastReceiver connReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                //网络发生了变化
                int state = NetworkMonitor.getNetworkType(context);
                if(state == curNetworkState){
                    Log.d(TAG, "No network change");
                    return;
                }
                //更新状态
                curNetworkState = state;
                if(curNetworkState == NetworkMonitor.NETWORK_INVALID){
                    isNetworkAvailable = false;
                    isWifiAvaiable = false;

                    showToast("网络连接已断开");
                }else {
                    isNetworkAvailable = true;
                    if(curNetworkState == NetworkMonitor.NETWORK_WIFI){
                        isWifiAvaiable = true;

                        showToast("Wifi网络已连接");
                    }else {
                        showToast("已切换到移动网络");
                    }
                }
            }else {
                // SD卡状态发生变化
                FileUtils.startScanTask(UpdateService.this);
            }
        }
    };


    /**
     * 显示Toast
     */
    private void showToast(CharSequence text){

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    /**
     * 网络获取页面源码调度线程
     */
    private class NetworkDispatcher extends Thread{
        boolean isRunning = false;

        /**
         * 取消线程的执行
         */
        public void cancel(){
            isRunning = false;
            interrupt();
        }

        @Override
        public synchronized void start() {
            isRunning = true;
            super.start();
        }

        @Override
        public void run() {

            FileUtils.scanSDCards(UpdateService.this);

            while (!isInterrupted()){

                if(isWifiAvaiable) {
                    try {
                        final String url = remoteUrlQueue.take();
                        urlSet.remove(url);

                        HttpManager.getInstance(UpdateService.this)
                                .getPageSource(url, new HttpManager.INetworkCallback<InputStream>() {
                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "UpdateService get page source failed");
                                    }

                                    @Override
                                    public void onSuccess(InputStream data) {
                                        /**TODO write to local file */
                                        String filepath = FileUtils.writeHTML(url, data);
                                        if(!TextUtils.isEmpty(filepath)){
                                            //更新数据库
                                            dbHelper.updateLocalLink(url, filepath);
                                        }
                                    }
                                });


                    } catch (InterruptedException e) {
                        Log.e(TAG, "NetworkDispatcher--->run() " + e.getMessage());
                    }
                }

                if(isRunning){
                    try{
                        sleep(5000);  // 5s
                    }catch (InterruptedException e){
                        Log.e(TAG, "NetworkDispatcher sleep " + e.getMessage());
                    }
                }else {
                    break;
                }
            }
        }
    }
}
