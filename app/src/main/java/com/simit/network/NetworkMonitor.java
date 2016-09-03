package com.simit.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by liuchun on 2015/12/13.
 */
public class NetworkMonitor {
    /** 没有网络 */
    public static final int NETWORK_INVALID = 0;
    /** 移动网络 */
    public static final int NETWORK_MOBILE = 0x4;
    /** 移动网络掩码 */
    public static final int NETWORK_MOBILE_MASK = 0x04;
    /** 2G网络 */
    public static final int NETWORK_MOBILE_2G = 0x5;
    /** 3G网络 */
    public static final int NETWORK_MOBILE_3G = 0x6;
    /** 4G网络 */
    public static final int NETWORK_MOBILE_4G = 0x7;
    /** WIFI网络 */
    public static final int NETWORK_WIFI = 0x8;


    /**
     * 获取当前的网络类型
     * @param context
     * @return
     */
    public static int getNetworkType(Context context){
        int networkType = NETWORK_INVALID;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()){

            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                // wifi网络
                networkType = NETWORK_WIFI;
            }else if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                // mobile网络
                networkType = getMobileNetworkType(context);
            }

        }


        return networkType;
    }


    /**
     * 获取移动网络类型
     * @param context
     * @return
     */
    public static int getMobileNetworkType(Context context){
        int mobileType = NETWORK_INVALID;

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = tm.getNetworkType();
        switch (type){
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                mobileType = NETWORK_MOBILE_2G;
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                mobileType = NETWORK_MOBILE_3G;
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                mobileType = NETWORK_MOBILE_4G;
                break;
            default:
                mobileType = NETWORK_MOBILE;
                break;
        }

        return mobileType;
    }


    /**
     * 判定当前是否有网络连接
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context){
        boolean isConnected = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()){
            isConnected = true;
        }

        return isConnected;
    }


    /**
     * 判定当前网络是否是Wifi连接
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context){
        boolean isWifi = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected() &&
                networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            isWifi = true;
        }

        return isWifi;
    }



    /**
     * 判断当前网络是否是移动网络
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context){
        boolean isMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected() &&
                networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            isMobile = true;
        }

        return isMobile;
    }


    /**
     * 获取网络类型的名称
     * @param networkType
     * @return
     */
    public static String getNetworkTypeName(int networkType){

        switch (networkType){
            case NETWORK_WIFI:
                return "WIFI";
            case NETWORK_MOBILE:
                return "MOBILE";
            case NETWORK_MOBILE_2G:
                return "MOBILE 2G";
            case NETWORK_MOBILE_3G:
                return "MOBILE 3G";
            case NETWORK_MOBILE_4G:
                return "MOBILE 4G";
            default:
                return "NO NETWORK";
        }
    }
}
