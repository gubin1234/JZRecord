package com.jx.productservice;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * @ 作者: LSY
 * @ 类名: UtilSetParam
 * @ 包名: com.jx.productservice
 * @ 描述:动态获取项目的版本号和渠道号
 * @ 日期: 2020/12/4 15:18
 **/
public class UtilSetParam {
    /**
     * 获取渠道名
     * @param context 此处习惯性的设置为activity，实际上context就可以
     * @return 如果没有获取成功，那么返回值为空
     */
    public static boolean SetLocalChannelName(Context context) {
        if (context == null) {
            return false;
        }
        String channelName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.
                        getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = String.valueOf(applicationInfo.metaData.get("UMENG_CHANNEL"));
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        ProServiceInfo.getInstance().setChannelId(channelName);
        Log.w("TAG本地渠道号", channelName);
        return true;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static boolean SetLocalVersionName(Context context) {
        if (context == null) {
            return false;
        }
        String localVersion = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
       ProServiceInfo.getInstance().setM_szOlVersion(localVersion);
        Log.w("TAG本地版本号", localVersion);
        return true;
    }


}
