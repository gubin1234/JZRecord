package com.jx.jzrecord.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class UtilsPermission {
                           /**-------------------------------悬浮窗权限-------------------------------**/
    //6.0以上通用权限检查
    public static boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (UtilsRom.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                Log.e("TAG", Log.getStackTraceString(e));
            }
            //Settings.canDrawOverlays(context);//也可以直接用这一个
            return result;
        }
    }
    /**
     * 魅族悬浮窗检测权限
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean meizuPermissionCheck(Context context) {
//        AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        try {
//            Class clazz = AppOpsManager.class;
//            Method method = clazz.getDeclaredMethod("meizuPermissionCheck", int.class, int.class, String.class);
//            return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, 24, Binder.getCallingUid(), context.getPackageName());
//        } catch (Exception e) {
//            Log.e("TAG", Log.getStackTraceString(e));
//            return false;
//        }
        AppOpsManager appOps= (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
     int mode = 0;
     mode=appOps.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,android.os.Process.myUid(), context.getPackageName());
             return mode==AppOpsManager.MODE_ALLOWED;
    }

    /**
     * 去魅族悬浮窗权限申请页面
     */
    public  static  void meizuPermissionApply(Context context){
        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri=Uri.fromParts("package","com.jx.jzrecord",null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     try{
      context.startActivity(intent);
        }catch(Exception e){
         e.printStackTrace();
        }
      }

    /**
     * 6.0以后除了魅族其他类型通用悬浮窗申请权限页面
     * */
    public static void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (UtilsRom.checkIsMeizuRom()) {
            Log.w("TAG", "Log.getStackTraceString(e)1");
            meizuPermissionApply(context);
        } else {
            try {
                Class clazz = Settings.class;
                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
                Intent intent = new Intent(field.get(null).toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
               context.startActivity(intent);
            } catch (Exception e) {
                Log.e("TAG", Log.getStackTraceString(e));
            }
        }
    }

    /**-------------------------------检查要动态申请的权限有没有申请-------------------------------**/
    public  static boolean hasPermissions(Context context,String str) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        Log.w("TAGhasPermissions","*");
        //或运算两个同时为0，结果才为0
        int granted =  pm.checkPermission(RECORD_AUDIO, packageName) | pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName)|pm.checkPermission(READ_PHONE_STATE, packageName);
        int audioPermission=pm.checkPermission(RECORD_AUDIO, packageName);
        int writePermission=pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName);
        int phoneStatePermission=pm.checkPermission(READ_PHONE_STATE,packageName);
        if(granted == PackageManager.PERMISSION_GRANTED&&str.equals("ALL_PERMISSION")){
            return true;
        }
        if(audioPermission== PackageManager.PERMISSION_GRANTED&&str.equals("AUDIO_PERMISSION")){
            return true;
        }
        if(phoneStatePermission==PackageManager.PERMISSION_GRANTED&&str.equals("PHONE_PERMISSION")){
            return true;
        }
        return writePermission == PackageManager.PERMISSION_GRANTED && str.equals("WRITE_PERMISSION");
    }

}
