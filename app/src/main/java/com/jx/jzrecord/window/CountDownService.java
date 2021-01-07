package com.jx.jzrecord.window;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jx.jzrecord.Fragment.FragmentMain;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoSettings;

import java.util.Timer;
import java.util.TimerTask;

                           //----------------------倒计时服务----------------------------//
public class CountDownService extends Service {
    /**
     * 用于在线程中进行计时。
     */
    private Handler handler = new Handler();

    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
    private Timer timer;
    private  int CountDowntime; //倒计时的秒数

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔1秒刷新一次
        Log.w("TAGFloatService","onStartCommand");
        if (timer == null) {
            timer = new Timer();
            //延迟delay毫秒后每隔period毫秒执行一次RefreshTask()方法
            //period计划时间，最终的时间间隔是方法完成的时间间隔。
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 1000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("TAGFloatService","onCreate");
        BeanSettings beanSettings=new DaoSettings().get_Data(1);
           //设置倒计时时间
        SetCount_down(beanSettings.getCount_down());
        FloatWindowManager.createCountDownWindow(getApplicationContext());
        FloatWindowManager.UpdateCountDowntime(CountDowntime);
    }

    private void SetCount_down(String count_down) {
        switch (count_down) {
            case "3s":
                CountDowntime = 4;
                break;
            case "5s":
                CountDowntime = 6;
                break;
            case "10s":
                CountDowntime = 11;
                break;
            default:
                break;
        }
    }

    class RefreshTask extends TimerTask {
        @Override
        public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(CountDowntime==1){
                            //发送广播开始录屏
                            CountDowntime=0;
                            FloatWindowManager.UpdateCountDowntime(CountDowntime);
                            sendBroadcast(new Intent().setAction(FragmentMain.COUNT_DOWN_ACTION));
                            FloatWindowManager.removeCountDownView(getApplicationContext());
                            Intent intent = new Intent(getApplicationContext(), CountDownService.class);
                            getApplication().stopService(intent);
                        }else {
                            CountDowntime=CountDowntime-1;
                            FloatWindowManager.UpdateCountDowntime(CountDowntime);
                        }
                    }
                });
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }
}
