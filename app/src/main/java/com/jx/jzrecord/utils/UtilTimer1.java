package com.jx.jzrecord.utils;


import android.os.Handler;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class UtilTimer1 {
    /**
     * 定时器，进行计时
     */
    private Handler handler ;
    private  static Timer timer;   //录屏计时器
    private  long mytime;  //录屏时间
    private boolean mRunning;
    private static UtilTimer1 INSTANCE = null;
   private TextView textView;

    private UtilTimer1(TextView textView) {
        this.textView=textView;
        this.handler = new Handler();
        this.mytime=0;
        this.mRunning=false;
    }
    //获取类的单实例
    public  static UtilTimer1 getInstance(TextView textView){
        if(INSTANCE == null){
            synchronized (UtilTimer1.class) {
                if(INSTANCE == null){
                    INSTANCE = new UtilTimer1(textView);
                }
            }
        }
        return INSTANCE;
    }

    public static UtilTimer1 getINSTANCE(){
        return INSTANCE;
    }


    public void Start()
    {
        if(timer==null){
            timer = new Timer();
        }
        //延迟delay毫秒后每隔period毫秒执行一次RefreshTask()方法
        //period计划时间，最终的时间间隔是方法完成的时间间隔。
        mRunning=true;
        timer.scheduleAtFixedRate(new RefreshTask(),  1000, 1000);
    }

    public void Pause()
    {
      mRunning=false;
    }

    public void Continue(){
        mRunning=true;
    }

    public void Stop()
    {
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        mytime=0;
        mRunning=false;
        textView.setText("00:00:00");
    }



    class RefreshTask extends TimerTask {
        @Override
        public void run() {
           handler.post(new Runnable() {
               @Override
               public void run() {
                   if(mRunning){
                       UpdateTime(true);
                   }else {
                       UpdateTime(false);
                   }
               }
           });
        }
    }

   public void UpdateTime(boolean run)
   {
       if(run){
           mytime++;
       }
       int hour=(int)(mytime/3600);
       int minute = (int) ((mytime -hour*3600)/ 60);
       int second = (int) ((mytime -hour*3600) - minute * 60);
       String hh = hour < 10 ? "0" + hour : hour + "";
       String mm = minute < 10 ? "0" + minute : minute + "";
       String ss = second < 10 ? "0" + second : second + "";
       String timeFormat = hh+ ":"+ mm + ":" + ss;
       textView.setText(timeFormat);
   }
   public long GetTime(){
        return mytime;
   }
}
