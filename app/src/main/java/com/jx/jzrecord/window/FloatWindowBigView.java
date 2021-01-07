package com.jx.jzrecord.window;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.jx.jzrecord.R;
import com.jx.jzrecord.login.ActivityLogin;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.recording.ScreenRecorderState;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoSettings;
import com.jx.jzrecord.utils.UtilsPermission;
import com.jx.jzrecord.utils.UtilsToast;


import static com.jx.jzrecord.Fragment.FragmentMain.FLOAT_WINDOW_HOME;
import static com.jx.jzrecord.Fragment.FragmentMain.FLOAT_WINDOW_LOGIN;
import static com.jx.jzrecord.Fragment.FragmentMain.FLOAT_WINDOW_RECORDER;
import static com.jx.jzrecord.Fragment.FragmentMain.FLOW_WINDOW_HIDE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_CONTINUE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_PAUSE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_STOP;

                         //----------------------大悬浮窗布局类----------------------------//
public class FloatWindowBigView extends LinearLayout  {
     /**
     * 宏定义
     */
     private static final String SMALL_FLOAT_WINDOW="small";  //小悬浮窗
     private static final String TIME_SMALL_FLOAT_WINDOW="TimeSmall";  //带计时的小悬浮窗
     private static final String BEFORE_SCREEN_BIG_VIEW="big1";  //录屏前大悬浮窗
     private static final String RUN_SCREEN_BIG_VIEW="big2";  //录屏中大悬浮窗
     private static final String PAUSE_SCREEN_BIG_VIEW="big3";  //暂停中的大悬浮窗
    /**
     * 记录大悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录大悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 获取布局内容布局view
     */
    private View view;

    @RequiresApi(api = Build.VERSION_CODES.O)  //为了能使用API26的方法

    /*
     * @ 描述: 参数context：上下文对象，type：创建大悬浮窗的类型包括，录屏前，录屏中，暂停中，的大悬浮窗
     * @ 参数: [context, type]
     */
    public FloatWindowBigView(final Context context, String type ) {
        super(context);
        Log.w("TAGFloatWindowBigView","类型"+type);
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
        switch (type) {
            case BEFORE_SCREEN_BIG_VIEW:
                //将布局R.layout.float_window_small添加到当前界面
                LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
                //得到布局的宽和高
                view = findViewById(R.id.big_window_layout);
                viewWidth = view.getLayoutParams().width;
                viewHeight = view.getLayoutParams().height;
                Log.w("TAGFloatWindowBigView1", viewWidth + "*" + viewHeight);
                Button big_home = view.findViewById(R.id.big_home);
                Button big_hide = view.findViewById(R.id.big_hide);
                Button screen_horizontal = view.findViewById(R.id.screen_horizontal);
                Button screen_vertical = view.findViewById(R.id.screen_vertical);

                //横屏录制
                screen_horizontal.setOnClickListener(view -> {
                    Log.w("TAGFloatWindowBigView", "横屏录制");
                    if(UtilsPermission.hasPermissions(context, "WRITE_PERMISSION") && UtilsPermission.hasPermissions(context, "AUDIO_PERMISSION")&&IsLogin()) {
                            screenRecorderState.setBl_window_vertical(false);  //设置悬浮窗横屏录制
                            context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_RECORDER));  //发送录屏广播
                    }else {
                        if(!IsLogin()){
                            BackLoginActivity(context);
                        }else {
                            screenRecorderState.setBl_window_vertical(false);  //设置悬浮窗横屏录制
                            BackHomeActivity(context,true); //跳转到主页
                        }
                    }
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, SMALL_FLOAT_WINDOW);
                });

                //竖屏录制
                screen_vertical.setOnClickListener(view -> {
                    Log.w("TAGFloatWindowBigView", "竖屏录制");
                    if(UtilsPermission.hasPermissions(context, "WRITE_PERMISSION") && UtilsPermission.hasPermissions(context, "AUDIO_PERMISSION")&&IsLogin()) {
                        screenRecorderState.setBl_window_vertical(true);   //设置悬浮窗竖屏录制
                        context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_RECORDER));  //发送录屏广播
                    }else {
                        if(!IsLogin()){
                            BackLoginActivity(context);
                        }else {
                            screenRecorderState.setBl_window_vertical(true);   //设置悬浮窗竖屏录制
                            BackHomeActivity(context,true); //跳转到主页
                        }
                    }
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, SMALL_FLOAT_WINDOW);
                });

                //返回主页
                big_home.setOnClickListener(v -> {
                    BackHomeActivity(context,false); //跳转到主页
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, SMALL_FLOAT_WINDOW);
                });

                //隐藏悬浮窗
                big_hide.setOnClickListener(v -> {
                    // 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.removeSmallWindow(context);
                    //发送广播悬浮窗关闭广播
                    Log.w("FloatWindowTAG", "sendBroadcast");
                    context.sendBroadcast(new Intent().setAction(FLOW_WINDOW_HIDE));
                });
                break;
            case RUN_SCREEN_BIG_VIEW:
                //将布局R.layout.float_window_small添加到当前界面
                LayoutInflater.from(context).inflate(R.layout.float_window_changebig, this);
                //得到布局的宽和高
                view = findViewById(R.id.BigChange_window_layout);
                viewWidth = view.getLayoutParams().width;
                viewHeight = view.getLayoutParams().height;
                Log.w("TAGFloatWindowBigView2", viewWidth + "*" + viewHeight);
                Button change_pause = view.findViewById(R.id.change_pause);
                Button change_homepage = view.findViewById(R.id.change_home);
                Button change_stop = view.findViewById(R.id.change_stop);
                Button change_hide = view.findViewById(R.id.change_hide);

                //暂停
                change_pause.setOnClickListener(view -> {
                    FloatWindowManager.ispause = true;
                    screenRecorderState.setWindow_screen_state(SCREEN_PAUSE);  //设置悬浮窗录屏状态为暂停状态
                    context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_RECORDER));
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, TIME_SMALL_FLOAT_WINDOW);

                });

                //返回主页
                change_homepage.setOnClickListener(v -> {
                    BackHomeActivity(context,false); //跳转到主页
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, TIME_SMALL_FLOAT_WINDOW);
                });
                //停止
                change_stop.setOnClickListener(view -> {
                    FloatWindowManager.isrun = false;
                    screenRecorderState.setWindow_screen_state(SCREEN_STOP);  //设置悬浮窗录屏状态为停止状态
                    context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_RECORDER));
                    FloatWindowService.StopTime();
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, SMALL_FLOAT_WINDOW);
                });

                //隐藏
                change_hide.setOnClickListener(view -> {
                    // 点击关闭悬浮窗的时候，移除所有悬浮窗，
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.removeSmallWindow(context);
                    //发送广播悬浮窗关闭广播
                    Log.w("FloatWindowTAG", "sendBroadcast");
                    context.sendBroadcast(new Intent().setAction(FLOW_WINDOW_HIDE));

                });
                break;
            case PAUSE_SCREEN_BIG_VIEW:
                //将布局R.layout.float_window_small添加到当前界面
                LayoutInflater.from(context).inflate(R.layout.float_window_pausebig, this);
                //得到布局的宽和高
                view = findViewById(R.id.PauseChange_window_layout);
                viewWidth = view.getLayoutParams().width;
                viewHeight = view.getLayoutParams().height;
                Log.w("TAGFloatWindowBigView3", viewWidth + "*" + viewHeight);

                Button pause_continue = findViewById(R.id.pause_continue);
                Button pause_hide = findViewById(R.id.pause_hide);
                Button pause_home = findViewById(R.id.pause_home);
                Button pause_stop = findViewById(R.id.pause_stop);

                //继续
                pause_continue.setOnClickListener(view -> {
                    FloatWindowManager.ispause = false;
                    screenRecorderState.setWindow_screen_state(SCREEN_CONTINUE);  //设置悬浮窗录屏状态为继续状态
                    context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_RECORDER));
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, TIME_SMALL_FLOAT_WINDOW);
                });

                //跳转到主页
                pause_home.setOnClickListener(v -> {
                    BackHomeActivity(context,false); //跳转到主页
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, TIME_SMALL_FLOAT_WINDOW);
                });
                //隐藏
                pause_hide.setOnClickListener(v -> {
                    // 点击关闭悬浮窗的时候，移除所有悬浮窗，
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.removeSmallWindow(context);
                    //发送广播悬浮窗关闭广播
                    Log.w("FloatWindowTAG", "sendBroadcast");
                    context.sendBroadcast(new Intent().setAction(FLOW_WINDOW_HIDE));
                });

                //停止
                pause_stop.setOnClickListener(v -> {
                    FloatWindowManager.isrun = false;
                    screenRecorderState.setWindow_screen_state(SCREEN_STOP);  //设置悬浮窗录屏状态为停止状态
                    context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_RECORDER));
                    FloatWindowService.StopTime();
                    FloatWindowManager.removeBigWindow(context);
                    FloatWindowManager.createSmallWindow(context, SMALL_FLOAT_WINDOW);
                });
                break;
        }

    }

    //返回主页面
  private void BackHomeActivity(Context context,Boolean status){
      Intent intent = new Intent(context, ActivityMain.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      try {
          pendingIntent.send();
          Intent intent1=new Intent().setAction(FLOAT_WINDOW_HOME);
          intent1.putExtra("status", status);
          intent1.putExtra("recording_direction", "Window");
          context.sendBroadcast(intent1);  //发送返回主界面广播
      } catch (PendingIntent.CanceledException e) {
          e.printStackTrace();
      }
  }
    //返回登录页面
  private void BackLoginActivity(Context context){
      Intent intent = new Intent(context, ActivityLogin.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      try {
          pendingIntent.send();
          //有的手机执行上述代码回不到主页，执行下面广播才行
          context.sendBroadcast(new Intent().setAction(FLOAT_WINDOW_LOGIN));  //发送返回登录界面广播
      } catch (PendingIntent.CanceledException e) {
          e.printStackTrace();
      }
  }

    //检查数据库，判断是否为登录状态
    private boolean IsLogin(){
        SharedPreferences sharedPreferences= getContext().getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String userId=sharedPreferences.getString("user_id",null);
        String openid=sharedPreferences.getString("openid",null);  //微信登录
        return userId!=null||openid!=null;
    }

    /**
     * @ 作者: LSY
     * @ 方法名: onTouchEvent
     * @ 描述: 该方法功能：点击大悬浮窗外部关闭大悬浮窗创建小悬浮窗
     * @ 参数: [event]
     * @ 返回值: boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            int x = (int) event.getX();
            int y = (int) event.getY();
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            //判断点击的地方是不是在内容view里面
            if (!rect.contains(x, y)) {
                FloatWindowManager.removeBigWindow(getContext());
                if(FloatWindowManager.isrun){
                    FloatWindowManager.createSmallWindow(getContext(),TIME_SMALL_FLOAT_WINDOW);
                }else{
                    FloatWindowManager.createSmallWindow(getContext(),SMALL_FLOAT_WINDOW);
                }
            }
        }
        return true;
    }

}