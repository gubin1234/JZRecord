package com.jx.jzrecord.utils;

import android.content.Context;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
   判断手机类型
 */
public class UtilsRom {
    private static final String TAG = "UtilsRom";
    //Runtime.getRuntime().exec方法解释：https://blog.csdn.net/wangbaochu/article/details/44941045
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    /**
     * 判断是否是  魅族 ROM
     */
    public static boolean checkIsMeizuRom() {
        //return Build.MANUFACTURER.contains("Meizu");
        String meizuFlymeOSFlag = getSystemProperty("ro.build.display.id");
        if (TextUtils.isEmpty(meizuFlymeOSFlag)) {
            return false;
        } else return meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme");
    }


    /**
     * 判断存储容量是否大于500MB
     */
    public static boolean isGoodSize(String path, Context context){
        try{
            StatFs statfs = new StatFs(path);
            // 得到目录中空闲的块
            long availableBlocksLong = statfs.getAvailableBlocksLong();
            // 得到目录中空闲块的大小
            long blockSizeLong = statfs.getBlockSizeLong();
            return (availableBlocksLong * blockSizeLong) / 1000000 > 500;
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }
}
