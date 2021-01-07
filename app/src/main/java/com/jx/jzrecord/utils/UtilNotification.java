package com.jx.jzrecord.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.jx.jzrecord.BuildConfig;
import com.jx.jzrecord.R;
import com.jx.jzrecord.login.ActivityLogin;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.recording.ScreenRecorderService;
import com.jx.jzrecord.recording.ScreenRecorderState;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoSettings;

import java.lang.reflect.Method;

import static com.jx.jzrecord.Fragment.FragmentMain.FLOAT_WINDOW_HOME;
import static com.jx.jzrecord.Fragment.FragmentMain.MAIN_NOTIFICATION_RECORDER;
import static com.jx.jzrecord.Fragment.FragmentMain.NOTIFICATION_RECORDER;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_CONTINUE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_PAUSE;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_START;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_STOP;



/**
 * @ 作者: yjm
 * @ 类名: MyNotification
 * @ 包名: com.jx.noti
 * @ 描述:
 * @ 日期: 2020/10/12 17:12
 **/
public class UtilNotification {
    /**
     * 通知栏按钮点击事件对应的ACTION（标识广播）
     */
    public final static String ACTION_PORTRAIT = BuildConfig.APPLICATION_ID+".action.PortraitClick";
    public final static String ACTION_LANDSCAPE=BuildConfig.APPLICATION_ID+".action.LandscapeClick";
    public final static String ACTION_HOME=BuildConfig.APPLICATION_ID+".action.Home";

    /**
     * 标识按钮状态：是否是暂停
     */
    public boolean isPause =false;
    /**
     * 广播方面的类
     */
    private ButtonBroadcastReceiver notification_receiver;  //通知栏按钮广播类

    /**
     * 播放/暂停 按钮点击 ID
     */
    private final int NOTIFICATION_ID = 0xa01;

    private Context context;

    private NotificationManager notificationManager;
    private RemoteViews contentView;
    private   Notification notification;
    private static UtilNotification INSTANCE = null;

    private UtilNotification() {
    }


    private UtilNotification(Context context) {
        this.context = context;
    }

    //获取类的单实例
    public static UtilNotification getInstance(Context context){
        if(INSTANCE == null){
            synchronized (UtilNotification.class) {
                if(INSTANCE == null){
                    INSTANCE = new UtilNotification(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化通知栏信息，并显示
     *
     */
    public void initAndNotify() {
        //      当用户下来通知栏时候看到的就是RemoteViews中自定义的Notification布局
        contentView = new RemoteViews(context.getPackageName(), R.layout.view_notify);
//      设置按钮
        contentView.setImageViewResource(R.id.img_portrait, R.drawable.window_vscreen_icon);
        contentView.setImageViewResource(R.id.img_landscape,R.drawable.window_hscreen_icon);
        contentView.setImageViewResource(R.id.img_home,R.drawable.window_homepage_icon);
        String channelId="ID";
        notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        //获取Notification实例
        notification=new NotificationCompat.Builder(context,channelId)
                .setWhen(System.currentTimeMillis())
                .setCustomContentView(contentView)// 设置自定义的RemoteView，需要API最低为24
                .setSmallIcon(R.drawable.logo_notification)
                .build();
        //添加渠道
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId, "JZRecord", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        // 设置常驻 Flag
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        //设置通知栏信息
        //注册通知栏广播
        notification_receiver = new ButtonBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_PORTRAIT);
            intentFilter.addAction(ACTION_LANDSCAPE);
            intentFilter.addAction(ACTION_HOME);
            intentFilter.addAction(MAIN_NOTIFICATION_RECORDER);
            //LocalBroadcastManager.getInstance(context).registerReceiver(receiver,intentFilter);
            context.registerReceiver(notification_receiver, intentFilter);

            /*-------------------------------------设置点击的事件-----------------------------*/
            //设置竖屏按钮点击事件
            Intent portraitIntent = new Intent(ACTION_PORTRAIT);
            //获得一个PendingIntent,该待定意图发生时，相当于Context.sendBroadcast,发送广播
            PendingIntent intent_portrait = PendingIntent.getBroadcast(context, 0, portraitIntent, 0);
            //为view添加单击事件
            contentView.setOnClickPendingIntent(R.id.img_portrait, intent_portrait);

            //设置横屏按钮点击事件
            Intent landscapeIntent=new Intent(ACTION_LANDSCAPE);
            PendingIntent intent_landscape= PendingIntent.getBroadcast(context,0,landscapeIntent,0);
            contentView.setOnClickPendingIntent(R.id.img_landscape,intent_landscape);

            //设置主页按钮点击事件
            Intent homeIntent=new Intent(ACTION_HOME);
            PendingIntent intent_home= PendingIntent.getBroadcast(context,0,homeIntent,0);
            contentView.setOnClickPendingIntent(R.id.img_home,intent_home);
        // 发送到手机的通知栏
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    public  Notification getNotification(){
        return notification;
    }

    /**
     * （通知栏中的点击事件是通过广播来通知的，所以在需要处理点击事件的地方注册广播即可），广播接收者
     * 广播监听按钮点击事件
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action!=null)
            {
                switch (action)
                {
                    case ACTION_PORTRAIT:
                        Log.d("TAG", "点击竖屏录制");
                        onFirstBtnClick();
                        collapseStatusBar(context);   //收起通知栏
                        break;
                    case ACTION_LANDSCAPE:
                        onSecondBtnClick();
                        collapseStatusBar(context);   //收起通知栏
                        Log.d("TAG", "点击横屏录制");
                        break;
                    case ACTION_HOME:
                                    //返回主页
                         intent = new Intent(context, ActivityMain.class);
                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         context.startActivity(intent);
                        collapseStatusBar(context);   //收起通知栏
                        break;
                    case MAIN_NOTIFICATION_RECORDER:
                          //主页录屏时发来的广播
                        HomePageBroadcast();
                        break;
                }
            }
        }
    }
    //检查数据库，判断是否为登录状态
    private boolean IsLogin(){
        SharedPreferences sharedPreferences= context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String userId=sharedPreferences.getString("user_id",null);
        String openid=sharedPreferences.getString("openid",null);  //微信登录
        return userId!=null||openid!=null;
    }
    //第一个按钮的点击事件
    private void onFirstBtnClick() {
        //通知栏第一个按钮录屏状态变化，发送广播给主页，使主页与通知栏录屏按钮同步
            ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
            if(UtilsPermission.hasPermissions(context, "WRITE_PERMISSION") && UtilsPermission.hasPermissions(context, "AUDIO_PERMISSION")&&IsLogin()){
                //未录制状态，则开始竖屏录制
                if(ScreenRecorderService.getRecorder()==null){
                    BeanSettings beanSettings=new DaoSettings().get_Data(1);
                    if(beanSettings.getCount_down().equals("无")||UtilsPermission.commonROMPermissionCheck(context)){
                        screenRecorderState.setBl_notification_vertical(true);
                        context.sendBroadcast(new Intent().setAction(NOTIFICATION_RECORDER));  //发送录屏广播
                    }else {
                        screenRecorderState.setBl_notification_vertical(true);
                        applyPermission();
                    }
                }
                else {
                    //进行录制状态
                    if(isPause){
                        //是暂停录制状态，设置变为继续状态
                        screenRecorderState.setNotification_screen_state(SCREEN_CONTINUE);
                        isPause=false;
                    }else {
                        //录屏状态，设置变为暂停状态
                        screenRecorderState.setNotification_screen_state(SCREEN_PAUSE);
                        isPause=true;
                    }
                    context.sendBroadcast(new Intent().setAction(NOTIFICATION_RECORDER));  //发送录屏广播
                }
            }else {
                if(!IsLogin()){
                    Intent intent = new Intent(context, ActivityLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else {
                    screenRecorderState.setBl_notification_vertical(true);
                    applyPermission();
                }
            }
        }


    //第二个按钮点击事件,通知栏第二个按钮录屏状态变化，发送广播给主页，使主页与通知栏录屏按钮同步
    private void onSecondBtnClick(){
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
        if(UtilsPermission.hasPermissions(context, "WRITE_PERMISSION") && UtilsPermission.hasPermissions(context, "AUDIO_PERMISSION")&&IsLogin()) {
            //未录制状态，则开始横屏录制
            if (ScreenRecorderService.getRecorder()==null) {
                BeanSettings beanSettings=new DaoSettings().get_Data(1);
                if(beanSettings.getCount_down().equals("无")||UtilsPermission.commonROMPermissionCheck(context)) {
                    screenRecorderState.setBl_notification_vertical(false);
                    context.sendBroadcast(new Intent().setAction(NOTIFICATION_RECORDER));  //发送录屏广播
                }else {
                    screenRecorderState.setBl_notification_vertical(false);
                    applyPermission();
                }
            } else {
                //当前是录屏状态，则变为停止录制状态，将图片设为未录制前状态
                screenRecorderState.setNotification_screen_state(SCREEN_STOP);  //设置通知栏录屏状态
                isPause = false;
                context.sendBroadcast(new Intent().setAction(NOTIFICATION_RECORDER));  //发送录屏广播
            }
        }else {
            if(!IsLogin()){
                Intent intent = new Intent(context, ActivityLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }else {
                screenRecorderState.setBl_notification_vertical(false);
                applyPermission();
            }
        }
    }


    private void applyPermission(){
        Intent intent = new Intent(context, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Intent intent1=new Intent().setAction(FLOAT_WINDOW_HOME);
        intent1.putExtra("status", true);
        intent1.putExtra("recording_direction", "Notification");
        context.sendBroadcast(intent1);  //发送返回主界面广播
    }

    //主页录屏时发来的广播,接收广播进行操作，使主页跟通知栏录屏按钮同步
    private void HomePageBroadcast() {
        ScreenRecorderState screenRecorderState = ScreenRecorderState.getInstance();
        switch (screenRecorderState.getNotification_screen_state()) {
            case SCREEN_START:
                //开始录屏状态
                contentView.setImageViewResource(R.id.img_portrait, R.drawable.window_pause_icon);
                contentView.setImageViewResource(R.id.img_landscape, R.drawable.window_stop_icon);
                notificationManager.notify(NOTIFICATION_ID, notification);
                break;
            case SCREEN_CONTINUE:
                //继续录屏状态，即录屏中
                contentView.setImageViewResource(R.id.img_portrait, R.drawable.window_pause_icon);
                notificationManager.notify(NOTIFICATION_ID, notification);
                break;
            case SCREEN_PAUSE:
                //暂停状态
                contentView.setImageViewResource(R.id.img_portrait, R.drawable.window_continue_icon);
                notificationManager.notify(NOTIFICATION_ID, notification);
                break;
            case SCREEN_STOP:
                //结束录屏
                contentView.setImageViewResource(R.id.img_portrait, R.drawable.window_vscreen_icon);
                contentView.setImageViewResource(R.id.img_landscape, R.drawable.window_hscreen_icon);
                notificationManager.notify(NOTIFICATION_ID, notification);
                break;
            default:
                break;
        }
    }

    /**
     * 关闭通知
     */
    public void Destroy(){
        if (notification_receiver != null) {
            context.unregisterReceiver(notification_receiver);
        }
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        INSTANCE=null;
    }

    /**
     *
     * 收起通知栏
     */
    public  void collapseStatusBar(Context context) {
        try {
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            collapse = statusBarManager.getClass().getMethod("collapsePanels");
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}