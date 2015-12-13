package com.example.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by liuchun on 2015/12/13.
 */
public class NetworkMonitor {
    public static final int NO_NETWORK = -1;
    public static final int WIFI = 1;
    public static final int CMWAP = 2;
    public static final int CMNET = 3;

    public static int getNetWorkState(Context context){
        int netType = NO_NETWORK;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo == null){
            return netType;
        }

        int nType = networkInfo.getType();

        if(nType == ConnectivityManager.TYPE_MOBILE){
            if(networkInfo.getExtraInfo().equalsIgnoreCase("cmnet")){
                netType = CMNET;
            }else{
                netType = CMWAP;
            }
        }else if(nType == ConnectivityManager.TYPE_WIFI){
            nType = WIFI;
        }

        return nType;
    }
}
