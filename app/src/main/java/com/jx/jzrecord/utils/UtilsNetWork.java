package com.jx.jzrecord.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @ 作者: LSY
 * @ 类名: UtilsNetWork
 * @ 包名: com.jiangxia.screen
 * @ 描述:判断是否有网络连接
 * @ 日期: 2020/9/14 14:11
 **/
public class UtilsNetWork {
    //判断是否有网络连接
    public static boolean isConn(Context context){
        boolean bisConnFlag=false;
        // ConnectivityManager可以获取Android系统的网络连接相关信息，它是系统服务中的一员：
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo mobNetInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//                NetworkInfo wifiNetInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //得到管理器对象后，调用getActiveNetworkInfo()即可获得当前活动的网络连接信息了，若无网络访问，返回NULL
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network!=null){
            bisConnFlag=network.isAvailable();
        }
        return bisConnFlag;
    }
}
