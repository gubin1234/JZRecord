package com.jx.jzrecord.window;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.jx.jzrecord.R;

import java.lang.reflect.Field;

                       //----------------------大悬浮窗布局类----------------------------//
public class FloatWindowSmallView extends LinearLayout {
    /**
     * 宏定义
     */
    private static final String SMALL_FLOAT_WINDOW="small";  //小悬浮窗
    private static final String TIME_SMALL_FLOAT_WINDOW="TimeSmall";  //带计时的小悬浮窗
    /**
     * 记录小悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int viewHeight;

  

    /**
     * 用于更新小悬浮窗的位置
     */
    private WindowManager windowManager;

    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;

    public FloatWindowSmallView(final Context context, String type) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (type.equals(SMALL_FLOAT_WINDOW)){
            //将布局R.layout.float_window_small添加到当前界面
            LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
            //得到布局的宽和高
            View view = findViewById(R.id.small_window_layout);
            if(viewWidth==0&&viewHeight==0){
                viewWidth = view.getLayoutParams().width;
                viewHeight = view.getLayoutParams().height;
            }
            Log.w("TAG1FloatSmallView1",viewWidth+"*"+viewHeight);
        }else if(type.equals(TIME_SMALL_FLOAT_WINDOW)){
            Log.w("TAG1FloatSmallView","TimeSmall");
            //将布局R.layout.float_window_small添加到当前界面
            LayoutInflater.from(context).inflate(R.layout.float_window_changesmall, this);
            //得到布局的宽和高
            View view = findViewById(R.id.ChangeSmall_window_layout);
                viewWidth = view.getLayoutParams().width;
                viewHeight = view.getLayoutParams().height;
            Log.w("TAG1FloatSmallView2",viewWidth+"*"+viewHeight);
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                Log.w("ServiceStartLogo","我有没有按下去");
                xInView = event.getX();  //记录手指按下时在小悬浮窗的View上的横坐标的值
                yInView = event.getY();
                if(isScreenOrientationPortrait(getContext())){
                    xDownInScreen = event.getRawX();  //记录手指按下时在屏幕上的横坐标的值
                    yDownInScreen = event.getRawY() - getStatusBarSize(getContext());
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY() - getStatusBarSize(getContext());
                }else {
                    xDownInScreen = event.getRawX()- getStatusBarSize(getContext());  //记录手指按下时在屏幕上的横坐标的值
                    yDownInScreen = event.getRawY() ;
                    xInScreen = event.getRawX()- getStatusBarSize(getContext());
                    yInScreen = event.getRawY() ;
                }
                Log.w("ServiceStartLogo","xInView"+xInView);
                Log.w("ServiceStartLogo","yInView"+yInView);
                Log.w("ServiceStartLogo","xDownInScreen"+xDownInScreen);
                Log.w("ServiceStartLogo","yDownInScreen"+yDownInScreen);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.w("ServiceStartLogo","我有没有移动");
                if(isScreenOrientationPortrait(getContext())){
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY() - getStatusBarSize(getContext());
                }else {
                    xInScreen = event.getRawX()- getStatusBarSize(getContext());
                    yInScreen = event.getRawY();
                }
                Log.w("ServiceStartLogo","xInScreen"+xInScreen);
                Log.w("ServiceStartLogo","yInScreen"+yInScreen);
                Log.w("ServiceStartLogo","getStatusBarHeight"+getStatusBarSize(getContext()));
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                Log.w("ServiceStartLogo","我有没有抬起来");
                if(Math.abs(xInScreen-xDownInScreen)<4&&Math.abs(yInScreen-yDownInScreen)<4){
                    FloatWindowManager.openBigWindow(getContext());
                }
                break;
            default:
                break;
        }
        return true;
    }

    //Android 判断当前屏幕是横屏还是竖屏
    private   boolean isScreenOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }


    //获取状态栏的大小
    private int getStatusBarSize(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
