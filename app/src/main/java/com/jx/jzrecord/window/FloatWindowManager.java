package com.jx.jzrecord.window;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.jx.jzrecord.R;
import com.jx.jzrecord.utils.UtilsToast;


/**
 * @ 作者: LSY
 * @ 类名: FloatWindowManager
 * @ 包名: com.jx.jzrecord.window
 * @ 描述:悬浮窗管理类（负责创建悬浮窗，关闭悬浮窗，更新悬浮窗计时UI）
 * @ 日期: 2020/9/20 21:59
 **/
public class FloatWindowManager {
    /**
     * 宏定义
     */
    public static final String SMALL_FLOAT_WINDOW="small";  //小悬浮窗
    public static final String TIME_SMALL_FLOAT_WINDOW="TimeSmall";  //带计时的小悬浮窗
    private static final String BEFORE_SCREEN_BIG_VIEW="big1";  //录屏前大悬浮窗
    private static final String RUN_SCREEN_BIG_VIEW="big2";  //录屏中大悬浮窗
    private static final String PAUSE_SCREEN_BIG_VIEW="big3";  //暂停中的大悬浮窗
    /**
     * 小悬浮窗View的实例
     */
    private static FloatWindowSmallView smallWindow;

    /**
     * 大悬浮窗View的实例
     */
    private static FloatWindowBigView bigWindow;

    /**
     * 倒计时View的实例
     */
    private static CountDownView countDownView;
    /**
     * 倒计时View的参数
     */
    private static WindowManager.LayoutParams countDownParams;

    /**
     * 小悬浮窗View的参数
     */
    private static WindowManager.LayoutParams smallWindowParams;

    /**
     * 大悬浮窗View的参数
     */
    private static WindowManager.LayoutParams bigWindowParams;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;

    public static boolean  isrun=false;      //是否为录屏状态
    public static boolean ispause=false;     //是否为暂停录屏状态
    private  static int downtime; //弹窗倒计时时间

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createSmallWindow(Context context, String type) {
        if(type.equals(SMALL_FLOAT_WINDOW))
        {
                //创建小悬浮窗
                smallWindow = new FloatWindowSmallView(context,type);
                setsmallWindowParams(context);
        }else if(type.equals(TIME_SMALL_FLOAT_WINDOW)){
            Log.w("TAGFloatWindowBigView","changesmall");
            //创建小悬浮窗
            smallWindow = new FloatWindowSmallView(context,type);
            setsmallWindowParams(context);
            //使时间显示的刹那肉眼看到不是从零开始
            FloatWindowService.UpdateTime();

        }
    }



    private static void setsmallWindowParams(Context context){
        //获取窗口管理对象
        WindowManager windowManager = getWindowManager(context);
        //获取屏幕宽度和高度
        int screenWidth=context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight=context.getResources().getDisplayMetrics().heightPixels;
        Log.w("TAGMyWindowManager",screenWidth+"*"+screenHeight);
        if(smallWindowParams!=null) {
            smallWindowParams=null;
        }
            smallWindowParams = new WindowManager.LayoutParams();
            //判断Android是否大于8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //在Android 8.0之后，如果要实现悬浮窗这种需要位于应用程序上层的功能，必须设置type为TYPE_APPLICATION_OVERLAY
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else{
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            //标志位觉得小窗口聚焦否，能不能移动否
            smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            smallWindowParams.format = PixelFormat.RGBA_8888;
            smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            smallWindowParams.width =FloatWindowSmallView.viewWidth;
            smallWindowParams.height =FloatWindowSmallView.viewHeight;
            Log.w("TAGSmallWindowParams",smallWindowParams.width+"*"+ smallWindowParams.height);
            //刚开始显示在屏幕上的位置
            smallWindowParams.x = screenWidth;
            smallWindowParams.y = screenHeight / 2;
            smallWindow.setParams(smallWindowParams);
        windowManager.addView(smallWindow, smallWindowParams);
    }
    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /**
     * 根据不同条件打开相应的大悬浮窗，同时关闭小悬浮窗。
     * @param context
     * 必须为应用程序的Context.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void openBigWindow(Context context) {
        Log.w("TAGopenBigWindow","+"+downtime);
        if(downtime==0){
            if(isrun) {
                if(!ispause) {
                    Log.w("TAGFloatWindowSmallView", "openBigWindowbig2");
                    createBigWindow(context, RUN_SCREEN_BIG_VIEW);
                }else{
                    createBigWindow(context, PAUSE_SCREEN_BIG_VIEW);
                }
            }else{
                Log.w("TAGFloatWindowSmallView","openBigWindowbig1");
                createBigWindow(context,BEFORE_SCREEN_BIG_VIEW);
            }
            //防止刚创建时时间为00开始
            FloatWindowService.UpdateTime();
            removeSmallWindow(context);
        }else {
            new UtilsToast(context,"录屏倒计时中不能打开大悬浮窗").show(Toast.LENGTH_SHORT);
        }
    }

    /**
     * 根据type指定的类型创建一个大悬浮窗。位置为屏幕正中间。
     * @param context
     * 必须为应用程序的Context.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createBigWindow(Context context, String type) {
        switch (type) {
            case BEFORE_SCREEN_BIG_VIEW:       //录屏前
            case RUN_SCREEN_BIG_VIEW:         //录屏中
            case PAUSE_SCREEN_BIG_VIEW:       //暂停中
                //创建自定义的大窗口布局对象
                bigWindow = new FloatWindowBigView(context, type);
                setbigWindowParams(context);
                break;
        }
    }

    //设置大悬浮窗参数
    private static void setbigWindowParams(Context context){
        //获取窗口管理对象
        WindowManager windowManager = getWindowManager(context);
        //获取屏幕宽度和高度
        int screenWidth=context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight=context.getResources().getDisplayMetrics().heightPixels;
        if(bigWindowParams!=null){
            bigWindowParams=null;
        }
            bigWindowParams = new WindowManager.LayoutParams();
            //刚开始显示在屏幕上的位置,为正中间。
            bigWindowParams.x = screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
            bigWindowParams.y = screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
            //判断Android是否大于8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //在Android 8.0之后，如果要实现悬浮窗这种需要位于应用程序上层的功能，必须设置type为TYPE_APPLICATION_OVERLAY
                bigWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else{
                bigWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            bigWindowParams.format = PixelFormat.RGBA_8888;
           bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            bigWindowParams.width = FloatWindowBigView.viewWidth;
            bigWindowParams.height =FloatWindowBigView.viewHeight ;
        windowManager.addView(bigWindow, bigWindowParams);
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     */
    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }

    /**
     * 创建一个倒计时弹窗位置为屏幕正中间。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createCountDownWindow(Context context) {
        countDownView = new CountDownView(context);
        setCountDownWindowParams(context);

    }
    private static void setCountDownWindowParams(Context context){
        //获取窗口管理对象
        WindowManager windowManager = getWindowManager(context);
        //获取屏幕宽度和高度
        int screenWidth=context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight=context.getResources().getDisplayMetrics().heightPixels;
        Log.w("TAGMyWindowManager",screenWidth+"*"+screenHeight);
        if(countDownParams!=null){
            countDownParams=null;
        }
            countDownParams = new WindowManager.LayoutParams();
            //刚开始显示在屏幕上的位置,为正中间。
            countDownParams.x = 0;
            countDownParams.y = 0;
        Log.w("TAGMyWindowManager",CountDownView.viewWidth +"*"+CountDownView.viewHeight);
            //判断Android是否大于8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //在Android 8.0之后，如果要实现悬浮窗这种需要位于应用程序上层的功能，必须设置type为TYPE_APPLICATION_OVERLAY
                countDownParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                countDownParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            //标志位觉得小窗口聚焦否，能不能移动否
            countDownParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            countDownParams.format = PixelFormat.RGBA_8888;
            countDownParams.gravity = Gravity.CENTER;
            countDownParams.width = CountDownView.viewWidth;
            countDownParams.height = CountDownView.viewHeight;
        windowManager.addView(countDownView, countDownParams);
    }


             /**
                * 移除倒计时弹窗
              */
    public static void removeCountDownView(Context context) {
        if (countDownView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(countDownView);
            countDownView = null;
        }
    }
    /*-------------------------------弹窗倒计时----------------------*/
    public static void UpdateCountDowntime(int time) {
        if (countDownView != null) {
            Log.w("TAGupdatetime", "我是更新时间的" + time);
            if(time==0){
                downtime=time;
            }else {
                downtime=time;
                String mytime=" "+time+" ";
                TextView textView = (TextView) countDownView.findViewById(R.id.text_time);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,47);
                textView.setTextColor(Color.WHITE);
                textView.setText(mytime);
            }
        }
    }

               //更新悬浮窗的录屏时间
    public static void updatetime(String time){
        if(bigWindow!=null&&FloatWindowManager.isrun) {
            if(FloatWindowManager.ispause){
                Log.w("TAGupdatetime", "我是更新时间的" + time);
                TextView textView = (TextView) bigWindow.findViewById(R.id.pasuetime);
                textView.setText(time);
            }else{
                Log.w("TAGupdatetime", "我是更新时间的" + time);
                TextView textView = (TextView) bigWindow.findViewById(R.id.time);
                textView.setText(time);
            }
        }
        if(smallWindow!=null&&FloatWindowManager.isrun){
            Log.w("TAGsamlltime", "我是更新时间的" + time);
            TextView textView = (TextView) smallWindow.findViewById(R.id.smalltime);
            textView.setText(time);
        }
    }


    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return smallWindow != null || bigWindow != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context
     *            必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }
}