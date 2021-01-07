package com.jx.jzrecord.window;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jx.jzrecord.recording.ScreenRecorder;
import com.jx.jzrecord.recording.ScreenRecorderService;
import com.jx.jzrecord.recording.ScreenRecorderState;
import com.jx.jzrecord.utils.UtilTimer1;

import java.util.Timer;
import java.util.TimerTask;

import static com.jx.jzrecord.Fragment.FragmentMain.MAIN_WINDOW_RECORDER;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_CONTINUE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_PAUSE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_START;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_STOP;
import static com.jx.jzrecord.window.FloatWindowManager.SMALL_FLOAT_WINDOW;
import static com.jx.jzrecord.window.FloatWindowManager.TIME_SMALL_FLOAT_WINDOW;

/**
 * @ 作者: LSY
 * @ 类名: FloatWindowService
 * @ 包名: com.jx.jzrecord.window
 * @ 描述:悬浮窗服务
 * @ 日期: 2020/9/20 21:59
 **/
public class FloatWindowService extends Service {
    /**
     * 用于在线程中进行计时。
     */
    private Handler handler = new Handler();
    /**
     * 定时器，进行计时
     */
    private WindowBroadcastReceiver receiver;   //悬浮窗计时广播
    private  static Timer timer;   //录屏计时器


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("TAGFloatService","onCreate");
        //开启悬浮窗
        ScreenRecorder record=ScreenRecorderService.getRecorder();
       if(record!=null){
           Log.w("TAGFloatService","record！=null");
           //录制状态，设置大悬浮窗类型
            if(record.getmIsPause()) {
                FloatWindowManager.isrun = true;
                FloatWindowManager.ispause = true;
            }else {
                FloatWindowManager.isrun = true;
                FloatWindowManager.ispause = false;
            }
           CreateTimer();
           FloatWindowManager.createSmallWindow(getApplicationContext(),TIME_SMALL_FLOAT_WINDOW);

       }else {
           //前面两个设置决定大悬浮窗的类型
           FloatWindowManager.isrun=false;
           FloatWindowManager.ispause=false;
           FloatWindowManager.createSmallWindow(getApplicationContext(),SMALL_FLOAT_WINDOW);
       }
         receiver=new WindowBroadcastReceiver();
        IntentFilter IntentFilter = new IntentFilter();
        IntentFilter.addAction(MAIN_WINDOW_RECORDER);  //主页与悬浮窗录屏交互动作
        registerReceiver(receiver, IntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
        Log.w("TAG","FloatServiceDestroy");
        //释放计时器对象
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        //注销广播对象
        if(receiver!=null){
            unregisterReceiver(receiver);
        }
        FloatWindowManager.removeSmallWindow(getApplicationContext()); //移除大悬浮窗
        FloatWindowManager.removeBigWindow(getApplicationContext());  //移除小悬浮窗
        FloatWindowManager.isrun=false;
        FloatWindowManager.ispause=false;
    }


    //服务运行中，监听app程序是否退出了
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.w("TAGFloatWindowService","onTaskRemoved");
        Intent intent = new Intent(getApplicationContext(), FloatWindowService.class);
        getApplication().stopService(intent);
    }

    public class WindowBroadcastReceiver extends BroadcastReceiver{

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action!=null){
                if (MAIN_WINDOW_RECORDER.equals(action)) {
                    ScreenRecorderState screenRecorderState = ScreenRecorderState.getInstance();
                    switch (screenRecorderState.getWindow_screen_state()) {
                        case SCREEN_CONTINUE:
                            FloatWindowManager.isrun = true;
                            FloatWindowManager.ispause = false;
                            break;
                        case SCREEN_PAUSE:
                            FloatWindowManager.isrun = true;
                            FloatWindowManager.ispause = true;
                            break;
                        case SCREEN_STOP:
                            FloatWindowManager.isrun = false;
                            FloatWindowManager.ispause = false;
                            FloatWindowManager.removeSmallWindow(context);
                            FloatWindowManager.createSmallWindow(context, SMALL_FLOAT_WINDOW);
                            StopTime();  //停止计时
                            break;
                        case SCREEN_START:
                            FloatWindowManager.isrun = true;
                            FloatWindowManager.ispause = false;
                            FloatWindowManager.removeSmallWindow(context);
                            FloatWindowManager.createSmallWindow(context, TIME_SMALL_FLOAT_WINDOW);
                            CreateTimer();  //初始化计时器，开始计时
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }


     private void CreateTimer(){
        if(timer==null){
            timer = new Timer();
        }
        //延迟delay毫秒后每隔period毫秒执行一次RefreshTask()方法
        //period计划时间，最终的时间间隔是方法完成的时间间隔。
        timer.scheduleAtFixedRate(new RefreshTask(),  0, 1000);
     }

    public static void StopTime(){
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
    }

    //格式化时间
    public  static void UpdateTime(){
        if(UtilTimer1.getINSTANCE()!=null){
            long mytime=UtilTimer1.getINSTANCE().GetTime();
            int minute = (int) (mytime / 60);
            int second = (int) (mytime - minute * 60);
            String mm = minute < 10 ? "0" + minute : minute + "";
            String ss = second < 10 ? "0" + second : second + "";
            String timeFormat = mm + ":" + ss;
            FloatWindowManager.updatetime(timeFormat);
        }
    }

    //每隔1秒在主线程更新UI
     class RefreshTask extends TimerTask {
        @Override
        public void run() {
            if(FloatWindowManager.isrun){
                handler.post(FloatWindowService::UpdateTime);
            }
        }
    }
}
