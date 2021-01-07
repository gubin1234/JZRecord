package com.jx.jzrecord.Fragment;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.jx.jzrecord.BuildConfig;
import com.jx.jzrecord.R;
import com.jx.jzrecord.login.ActivityLogin;
import com.jx.jzrecord.login.BeanUserInfo;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.recording.ScreenRecorder;
import com.jx.jzrecord.recording.ScreenRecorderService;
import com.jx.jzrecord.recording.ScreenRecorderState;
import com.jx.jzrecord.recording.SensorManagerHelper;
import com.jx.jzrecord.setting.ActivitySetting;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoSettings;
import com.jx.jzrecord.utils.UtilScreen;
import com.jx.jzrecord.utils.UtilTimer1;
import com.jx.jzrecord.utils.UtilsPermission;
import com.jx.jzrecord.utils.UtilsRom;
import com.jx.jzrecord.utils.UtilsToast;
import com.jx.jzrecord.window.CountDownService;
import com.jx.jzrecord.window.FloatWindowManager;
import com.jx.jzrecord.window.FloatWindowService;
import com.jx.productservice.ProServiceInfo;

import java.io.IOException;
import java.io.InputStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.os.Build.VERSION_CODES.M;


/**
 * @ 作者: LSY
 * @ 类名: FragmentMain
 * @ 包名: com.jx.jzrecord.Fragment
 * @ 描述:录屏主界面的Fragment布局
 * @ 日期: 2020/10/12 14:05
 **/
public class FragmentMain extends Fragment {
    /**
     * ---录屏状态---
     **/
    public static final int SCREEN_CONTINUE = 1; //录屏继续状态
    public static final int SCREEN_PAUSE = 2; //  录屏暂停状态
    public static final int SCREEN_STOP = 3; //   录屏结束状态
    public static final int SCREEN_START = 4; //   录屏开始状态
    /**
     * ---Activity组件类型的变量---
     **/
    private ImageView iv_unlogin_HeadImg;       //未登录的头像显示
    private ImageView iv_login_HeadImg;  //登录后的头像显示
    private ImageButton ib_set;        //设置按钮
    private LinearLayout ll_main_kefu;        //客服按钮
    private LinearLayout ll_MainLogin;         //主页里面登录按钮布局
    private TextView tv_MainLogin;    //登录按钮
    private LoadImage loadImage;  //异步加载头像对象

    /**
     * --------录屏相关按钮变量--------
     **/
    private RelativeLayout main_screen_layout;     //录屏时背景
    private Button btn_recorder_start;          //录屏按钮
    private ImageButton ib_pause_icon;     //录屏暂停按钮
    private ImageButton ib_stop_icon;     //录屏停止按钮
    private ImageButton ib_continue_icon;    //录屏继续按钮
    private Button btn_VerticalScreen;     //选择横屏录制按钮
    private Button btn_HorizontalScreen;   //选择竖屏录制按钮


    private UtilTimer1 screen_timer;           //录屏计时工具类

     /* -------悬浮窗相关按钮变量--------*/
    private Switch start_float_window;  //悬浮窗开关
    /**
     * --------广播类型的变量-------
     **/
    public static final String FLOW_WINDOW_HIDE = BuildConfig.APPLICATION_ID+".FlowWindowHide"; //悬浮窗广播action宏定义
    public static final String COUNT_DOWN_ACTION = BuildConfig.APPLICATION_ID+".CountDown"; //倒计时广播action宏定义
    public static final String NOTIFICATION_RECORDER = BuildConfig.APPLICATION_ID+".NotificationRecorder"; //通知栏跟主页交互广播action宏定义
    public static final String FLOAT_WINDOW_RECORDER =BuildConfig.APPLICATION_ID+".FloatWindowRecorder"; //悬浮窗跟主页交互广播action宏定义
    public static final String MAIN_NOTIFICATION_RECORDER =BuildConfig.APPLICATION_ID+".MainNotificationRecorder"; //主页跟通知栏交互广播action宏定义
    public static final String MAIN_WINDOW_RECORDER = BuildConfig.APPLICATION_ID+".MainWindowRecorder"; //主页跟悬浮窗交互广播action宏定义
    public static final String FLOAT_WINDOW_HOME =BuildConfig.APPLICATION_ID+".FlowWindowHome";  //悬浮窗返回主页
    public static final String FLOAT_WINDOW_LOGIN = BuildConfig.APPLICATION_ID+".FlowWindowLogin";  //悬浮窗返回登录页面
    private MyBroadcastReceiver receiver;  //广播类
    private Intent service;  //录屏服务意图

    /**
     * --------录屏类型的变量-------
     **/
    private MediaProjectionManager mMediaProjectionManager; //屏幕服务管理类
    private static final int REQUEST_MEDIA_PROJECTION_INIT = 5; //刚打开界面的录屏权限申请吗
    private static final int REQUEST_MEDIA_PROJECTION = 6; //录屏权限申请吗
    private static final int REQUEST_PERMISSIONS = 7; //动态申请权限申请码
    private static final int REQUEST_PERMISSIONS_INIT = 8; //刚打开页面的权限申请码
    private ScreenRecorder mRecorder; //录屏类
    private View rootView;   //父布局
    private Activity activity;   //上下文对象
    private SensorManagerHelper sensorHelper; //传感器类对象
    private int mresultCode;   //捕捉屏幕允许的返回码
    private Intent mdata;     //捕捉屏幕允许的数据
    private String recording_direction;  //什么的录屏方向（主页，通知栏，悬浮窗）
    private String path;   //保存视频的目录，用于检查内存剩余容量
    public FragmentMain() {
        // Required empty public constructor
    }

    public FragmentMain(Activity activity) {
        mMediaProjectionManager = (MediaProjectionManager) activity.getSystemService(MEDIA_PROJECTION_SERVICE);
        this.activity = activity;
    }


    @RequiresApi(api = M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(activity!=null)
        if(!UtilsPermission.hasPermissions(activity,"ALL_PERMISSION")){
            requestPermissionsInit();
        }else {
            requestMediaProjection("REQUEST_MEDIA_PROJECTION_INIT");
        }

        path = Environment.getExternalStorageDirectory() + "/DCIM/";
            //注册广播
            Log.w("TAG1", "FragmentonCreate");
            receiver = new MyBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(FLOW_WINDOW_HIDE);
            intentFilter.addAction(NOTIFICATION_RECORDER);
            intentFilter.addAction(COUNT_DOWN_ACTION);
            intentFilter.addAction(FLOAT_WINDOW_RECORDER);
            intentFilter.addAction(FLOAT_WINDOW_HOME);
            intentFilter.addAction(FLOAT_WINDOW_LOGIN);
            activity.registerReceiver(receiver, intentFilter);
            /*
             * 设置摇晃监听事件
             */
            sensorHelper = new SensorManagerHelper(activity);
            sensorHelper.setOnShakeListener(new SensorManagerHelper.OnShakeListener() {
                @Override
                public void onShake() {
                    DaoSettings daoSettings = new DaoSettings();
                    BeanSettings beanSettings1 = daoSettings.get_Data(1);
                    if (beanSettings1.getEn_shake_stop() == 0) {              //有摇晃停止录屏权限
                        if (ScreenRecorderService.getRecorder() != null) {    //在录屏情况下
                            StopRecorder();
                        }
                    }
                }
            });
        }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("TAG1", "FragmentonDestroy");
        if (receiver != null) {
            activity.unregisterReceiver(receiver);
        }
        if(activity!=null){
            activity.finish();
        }
        sensorHelper.stop();  //撤销传感器对象
    }

    @RequiresApi(api = M)
    @Override
    public void onResume() {
            super.onResume();
            Log.w("TAG1", "FragmentMainResume");
            if(!UtilsPermission.commonROMPermissionCheck(activity)){
                start_float_window.setChecked(false);
            }
            SharedPreferences sharedPreferences = activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);  //获取数据库对象
            BeanUserInfo userInfo = BeanUserInfo.getInstance();
            String userId = sharedPreferences.getString("user_id", null);
            String openid = sharedPreferences.getString("openid", null);
            //注销登录(内存跟数据库中的用户ID已经被删除）
            if (userId == null && userInfo.getU_id() == null && openid == null) {
                Log.w("TAG", "注销登录");
                ll_MainLogin.setClickable(true);
                iv_login_HeadImg.setVisibility(View.GONE);
                iv_unlogin_HeadImg.setVisibility(View.VISIBLE);
                tv_MainLogin.setText("登录");
                loadImage = null;
            }

            //加载用户头像
            if (userInfo.getHead_portrait() != null && loadImage == null) {
                Log.w("TAG", "加载头像");
                if(userInfo.getHead_portrait().length()!=0){
                    loadImage = new LoadImage(userInfo.getHead_portrait());
                    loadImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //加载头像
                }
                //第一次登录的话数据库里面没用户ID为null,但是内存里面有用户ID不为null,写到数据库里面实现自动登录
                if (userId == null && userInfo.getU_id() != null) {
                    Log.w("TAG", "设置用户ID");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_id", userInfo.getU_id());  //用户ID
                    editor.apply();
                }
            }
        }

    public class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action!=null){
                switch (action){
                    case FLOW_WINDOW_HIDE:
                        //悬浮窗按钮为关闭状态
                          Log.w("TAGMainActivity", "WindowChange");
                          start_float_window.setChecked(false);
                        break;
                    case COUNT_DOWN_ACTION:
                        Log.w("TAG", "CountDownReceiver");
                            //倒计时完成后开始录屏
                      StartService(service);
                        break;
                    case NOTIFICATION_RECORDER:
                        NotificationBroadcast();
                        break;
                    case FLOAT_WINDOW_RECORDER:
                        WindowBroadcast();
                        break;
                    case FLOAT_WINDOW_HOME:
                       if(intent.getBooleanExtra("status",false)){
                           if(intent.getStringExtra("recording_direction").equals("Notification")){
                               recording_direction="Notification";
                           }else {
                               BackHome();
                               recording_direction="Window";
                           }
                         HasPermissionRecorder();
                       }else {
                           BackHome();
                       }
                        break;
                    case FLOAT_WINDOW_LOGIN:
                        BackHome();
                        startActivity(new Intent(activity,ActivityLogin.class));
                        break;
                    default:break;
                }
            }
        }
    }

    //通知栏传来的广播
    private void NotificationBroadcast(){
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
               /*------------------------未录屏情况下---------------------*/
        if(ScreenRecorderService.getRecorder()==null){
            recording_direction="Notification";   //通知栏录屏方向
            HasPermissionRecorder();
                 /*-------------------------录屏情况下------------------------*/
        }else {
              switch (screenRecorderState.getNotification_screen_state()){
                  case SCREEN_CONTINUE:
                      ContinueRecorder();
                      break;
                  case SCREEN_PAUSE:
                      PauseRecorder();
                      break;
                  case SCREEN_STOP:
                      StopRecorder();
                      break;
                  default:break;
              }
        }
    }

    //悬浮窗传来的广播
    private void WindowBroadcast(){
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
        /*------------------------未录屏情况下---------------------*/
        if(ScreenRecorderService.getRecorder()==null){
            recording_direction="Window";    //悬浮窗录屏方向
            HasPermissionRecorder();
            /*-------------------------录屏情况下------------------------*/
        }else {
            switch (screenRecorderState.getWindow_screen_state()){
                case SCREEN_CONTINUE:
                    ContinueRecorder();
                    break;
                case SCREEN_PAUSE:
                    PauseRecorder();
                    break;
                case SCREEN_STOP:
                    StopRecorder();
                    break;
                default:break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("TAG1", "FragmentMainPause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w("TAG1", "FragmentonCreateView");
        //------------------------- 加载主页的帧布局，并且实现各个按钮的逻辑代码---------------------------//
        rootView = inflater.inflate(R.layout.activity_main, container, false);
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();   //录屏状态类
        initActivityMain(); //初始化控件ID
        //------------------------录屏逻辑------------------------------//
        //录屏开始按钮点击事件
        btn_recorder_start.setOnClickListener(view -> {
            SharedPreferences sharedPreferences= activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);
            String userId=sharedPreferences.getString("user_id",null);
            String openid=sharedPreferences.getString("openid",null);  //微信登录
            if(userId!=null||openid!=null){
                //登录状态
                recording_direction="main";
                HasPermissionRecorder();
            }else {
                //跳转到登录界面
               startActivity(new Intent(activity,ActivityLogin.class));
            }
        });

        //录屏继续按钮点击事件
        ib_continue_icon.setOnClickListener(view -> {
            ContinueRecorder();
        });

        //录屏暂停按钮点击事件
        ib_pause_icon.setOnClickListener(view -> {
            PauseRecorder();
        });

        //录屏停止按钮点击事件
        ib_stop_icon.setOnClickListener(view -> {
            StopRecorder();
        });

        //竖屏屏按钮点击事件
        btn_VerticalScreen.setOnClickListener(view -> {
           screenRecorderState.setBl_main_Vertical(true);
            //按钮背景变化
            btn_HorizontalScreen.setBackgroundResource(R.drawable.screen_horizontal_grey);
            btn_VerticalScreen.setBackgroundResource(R.drawable.screen_vertical_orange);
        });

        //横屏按钮点击事件
        btn_HorizontalScreen.setOnClickListener(view -> {
            screenRecorderState.setBl_main_Vertical(false);
            btn_HorizontalScreen.setBackgroundResource(R.drawable.screen_horizontal_orange);
            btn_VerticalScreen.setBackgroundResource(R.drawable.screen_vertical_grey);
        });

        //悬浮窗开关按钮点击事件
        start_float_window.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ActivityCompat.requestPermissions(activity, new String[]{SYSTEM_ALERT_WINDOW},0);
                //先检查权限
                if (UtilsPermission.commonROMPermissionCheck(activity)) {
                      //打开悬浮窗
                    if(!screenRecorderState.isBl_hide_float_window()){
                        Intent intent = new Intent(activity, FloatWindowService.class);
                        activity.startService(intent);
                    }else {
                        start_float_window.setChecked(false);
                        new UtilsToast(activity,"开启了录屏时隐藏悬浮窗功能").show(Toast.LENGTH_SHORT);
                    }
                    //没有权限,弹窗申请权限
                } else {
                    showFloatWindowDialog();
                    start_float_window.setChecked(false);
                }
            } else {
                    //关闭悬浮窗
                    Intent intent = new Intent(activity, FloatWindowService.class);
                    activity.stopService(intent);
            }
        });
        //设置按钮点击事件
        ib_set.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ActivitySetting.class);
            startActivity(intent);
        });

                //客服按钮点击事件
        ll_main_kefu.setOnClickListener(view -> {
            Uri uri = Uri.parse(ProServiceInfo.getInstance().getM_szKFUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        //登录按钮点击事件
        ll_MainLogin.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ActivityLogin.class);//跳转到登录页面
            startActivity(intent);
        });
      return rootView;
    }

      //录屏停止
    private void StopRecorder(){
        //结束录屏服务
        Intent intent = new Intent(activity, ScreenRecorderService.class);
        activity.stopService(intent); //结束录屏服务
        screen_timer.Stop();  //计时停止
        btn_recorder_start.setVisibility(View.VISIBLE);  //只有开始录屏按钮的布局
        main_screen_layout.setVisibility(View.GONE);    //包括继续，暂停按钮的布局
        ib_continue_icon.setVisibility(View.GONE);   //继续按钮隐藏
        ib_pause_icon.setVisibility(View.VISIBLE);  //暂停按钮显示
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();  //录屏状态类
        //结束通知栏录制
        screenRecorderState.setNotification_screen_state(SCREEN_STOP);
        activity.sendBroadcast(new Intent().setAction(MAIN_NOTIFICATION_RECORDER));  //发送给通知栏广播
        if(ScreenRecorderState.getInstance().isBl_hide_float_window()){
            //是录屏时隐藏悬浮窗，所以录屏结束后打开悬浮窗
            screenRecorderState.setBl_hide_float_window(false);
            start_float_window.setChecked(true);   //悬浮窗按钮变为开启状态
        }else {
            if(FloatWindowManager.isWindowShowing()){
                //没开启录屏时隐藏悬浮窗，如果有打开悬浮窗，就发送广播跟主页同步
                screenRecorderState.setWindow_screen_state(SCREEN_STOP);
                activity.sendBroadcast(new Intent().setAction(MAIN_WINDOW_RECORDER));  //发送给悬浮窗广播
            }
        }
     }

     //录屏暂停
     private void PauseRecorder(){
         ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
         if(FloatWindowManager.isWindowShowing()){
             screenRecorderState.setWindow_screen_state(SCREEN_PAUSE);
             activity.sendBroadcast(new Intent().setAction(MAIN_WINDOW_RECORDER));  //发送给悬浮窗广播
         }
         screenRecorderState.setNotification_screen_state(SCREEN_PAUSE);
         activity.sendBroadcast(new Intent().setAction(MAIN_NOTIFICATION_RECORDER));  //发送给通知栏广播
         mRecorder = ScreenRecorderService.getRecorder();
         if (mRecorder != null) {
             mRecorder.pause();    //录屏暂停
             screen_timer.Pause();  //计时暂停
             ib_pause_icon.setVisibility(View.GONE);
             ib_continue_icon.setVisibility(View.VISIBLE);
         }
     }

     //录屏继续
     private void ContinueRecorder(){
         ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
         if(FloatWindowManager.isWindowShowing()){
             screenRecorderState.setWindow_screen_state(SCREEN_CONTINUE);
             activity.sendBroadcast(new Intent().setAction(MAIN_WINDOW_RECORDER));  //发送给悬浮窗广播
         }
         screenRecorderState.setNotification_screen_state(SCREEN_CONTINUE);
         activity.sendBroadcast(new Intent().setAction(MAIN_NOTIFICATION_RECORDER));  //发送给通知栏广播

         mRecorder = ScreenRecorderService.getRecorder();
         if (mRecorder != null) {
             mRecorder.screen_continue();  //录屏继续
             screen_timer.Continue(); //计时开始
             ib_pause_icon.setVisibility(View.VISIBLE);
             ib_continue_icon.setVisibility(View.GONE);
         }
     }



    //检查是否满足录屏条件（权限，存储量）
    private void HasPermissionRecorder(){
        if(UtilsRom.isGoodSize(path,activity)){  //有500MB内存以上
            if (UtilsPermission.hasPermissions(activity, "WRITE_PERMISSION") && UtilsPermission.hasPermissions(activity, "AUDIO_PERMISSION")) {
                if (mresultCode != -1) {
                    requestMediaProjection("REQUEST_MEDIA_PROJECTION");  //获取捕捉屏幕权限
                } else {
                    StartScreenRecorder();  //开始录屏
                }
            } else if (Build.VERSION.SDK_INT >= M) {
                //动态申请权限
                requestPermissions();
            }
        }else {
            new UtilsToast(activity,"内存低于500MB不许去录屏，请先清理内存").show(Toast.LENGTH_LONG);
        }
      }

    private void initActivityMain(){
        //竖屏按钮
        btn_VerticalScreen=rootView.findViewById(R.id.vertical_screen);
        //横屏按钮
        btn_HorizontalScreen=rootView.findViewById(R.id.horizontal_screen);
//        主页里的登录按钮布局
        ll_MainLogin=rootView.findViewById(R.id.ll_main_login);
        //主页里的登录按钮
        tv_MainLogin=rootView.findViewById(R.id.tv_main_login);
        //竖屏按钮
        btn_VerticalScreen=rootView.findViewById(R.id.vertical_screen);
        //横屏按钮
        btn_HorizontalScreen=rootView.findViewById(R.id.horizontal_screen);
        //悬浮窗开关按钮
        start_float_window=rootView.findViewById(R.id.start_float_window);
        //已登录图像显示
        iv_login_HeadImg=rootView.findViewById(R.id.login_HeadImg);
        //未登录图像显示
        iv_unlogin_HeadImg=rootView.findViewById(R.id.unlogin_HeadImg);
        //录屏开始按钮
        btn_recorder_start=rootView.findViewById(R.id.btn_recorder_start);
        //继续录屏按钮
        ib_continue_icon=rootView.findViewById(R.id.ib_continue_icon);
        //暂停录屏按钮
        ib_pause_icon=rootView.findViewById(R.id.ib_pause_icon);
        //停止录屏按钮
        ib_stop_icon= rootView.findViewById(R.id.ib_stop_icon);
        //录屏时背景按钮
        main_screen_layout=rootView.findViewById(R.id.main_screen_layout);
        //客户按钮
        ll_main_kefu=rootView.findViewById(R.id.ll_main_kefu);
        //设置按钮
        ib_set=rootView.findViewById(R.id.ib_set);
        //计时变量
        //录屏计时变量
        TextView tv_time = rootView.findViewById(R.id.tv_time);
        screen_timer=  UtilTimer1.getInstance(tv_time);
    }

    //拉起主页活动界面到前台展示
    private void  BackHome(){
        if(activity!=null){
            ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            manager.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
            Log.w("TAG", activity.getTaskId()+"活动的ID");
        }
    }

    //动态申请权限
    @RequiresApi(api = M)
    public  void requestPermissions() {
        String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};
        requestPermissions(permissions, REQUEST_PERMISSIONS);
    }

    /**
     * @ 作者: LSY
     * @ 方法名: onRequestPermissionsResult
     * @ 描述: requestCode:动态申请时设置的请求码；permissions：需动态申请的权限；grantResults：动态申请的结果
     * @ 参数: [requestCode, permissions, grantResults]
     * @ 返回值: void
     */
    @RequiresApi(api = M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            Log.w("TAG",grantResults[0]+".."+grantResults[1]);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(grantResults[1]!= PackageManager.PERMISSION_GRANTED){
                    AlertDialog alertDialog= new AlertDialog.Builder(activity)
                            .setMessage(getString(R.string.DialogAudioPermission))
                            .setCancelable(true)
                            .setPositiveButton("开启权限", (dialog, which) ->
                                    ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO},1)
                            )
                            .setNegativeButton("继续录制", (dialog, which) -> {
                                requestMediaProjection("REQUEST_MEDIA_PROJECTION");   //开始录屏（没带录音） //允许写权限即可录屏
                            })
                            .create();
                    alertDialog.show();
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#999999"));
                    Window dialogWindow=alertDialog.getWindow();
                    WindowManager.LayoutParams lp=dialogWindow.getAttributes();
                    dialogWindow.setGravity(Gravity.CENTER);
                    lp.width= UtilScreen.getScreenWidth(activity)*11/12;
                    dialogWindow.setAttributes(lp);
                }else {
                            //两个权限都允许了
                    requestMediaProjection("REQUEST_MEDIA_PROJECTION"); //开始录屏（带录音）
                }
            } else {
                new UtilsToast(activity,"请您去设置页面打开录屏必要的权限").show(Toast.LENGTH_SHORT);
            }
        }
        if(requestCode == REQUEST_PERMISSIONS_INIT){
            FragmentFile fileFragment=(FragmentFile) ActivityMain.mFragments.get(1);
            fileFragment.startScanTack();
            //初始化的时候，获取录屏权限数据
                requestMediaProjection("REQUEST_MEDIA_PROJECTION_INIT");
        }
    }

    //获取用户允不允许录屏
    private void requestMediaProjection(String str) {
        //这两行代码就是弹出要捕捉屏幕用于允不允许捕捉，在onActivityResult() 中响应用户动作，获得允许则开始屏幕录制。
        Log.w("TAG","requestMediaProjection");
        if(str.equals("REQUEST_MEDIA_PROJECTION")){
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
        }else if(str.equals("REQUEST_MEDIA_PROJECTION_INIT")){
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION_INIT);
        }
    }

    //接收requestMediaProjection返回的结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode != 0) {
            // NOTE: Should pass this result data into a Service to run ScreenRecorder.
            // The following codes are merely exemplary.
            mdata=data;
            mresultCode=resultCode;
            Log.w("ssss",mdata+"+"+mresultCode);
            StartScreenRecorder();
        }else {
            //加载视频文件
            FragmentFile fileFragment=(FragmentFile) ActivityMain.mFragments.get(1);
            fileFragment.startScanTack();
        }
        if(requestCode == REQUEST_MEDIA_PROJECTION_INIT&& resultCode != 0){
            mdata=data;
            mresultCode=resultCode;
        }

    }

    private void StartScreenRecorder(){
        boolean bl_Vertical; //默认竖屏录制
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
        if(recording_direction.equals("Window")){
            bl_Vertical=screenRecorderState.isBl_window_vertical();
        }else if(recording_direction.equals("Notification")){
            bl_Vertical=screenRecorderState.isBl_notification_vertical();
        }else {
            bl_Vertical=screenRecorderState.isBl_main_Vertical();
        }
        service = new Intent(activity, ScreenRecorderService.class);
            service.putExtra("code", mresultCode);
            service.putExtra("data", mdata);
            service.putExtra("bl_Vertical",bl_Vertical);
        BeanSettings beanSettings=new DaoSettings().get_Data(1);
        if(beanSettings.getCount_down().equals("无")){
            Log.w("TAG", "没有倒计时");
            activity.moveTaskToBack(true);  //拉下屏幕
            StartService(service);
        }else {
            //检查有没有权限
            if(UtilsPermission.commonROMPermissionCheck(activity)){
                    activity.moveTaskToBack(true);  //拉下屏幕
                    //开启倒计时服务
                    activity.startService(new Intent(activity, CountDownService.class));
            }else {
                AlertDialog alertDialog=new AlertDialog.Builder(activity)
                        .setMessage("检测到手机没有授予录屏倒计时权限，开启后才能使用倒计时功能")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, (dialog, which) ->
                                        UtilsPermission.commonROMPermissionApply(activity)
                        ).setNegativeButton(android.R.string.cancel, (dialog, which) ->
                        dialog.dismiss()
                )
                        .create();
                alertDialog.show();
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#999999"));
                Window dialogWindow=alertDialog.getWindow();
                WindowManager.LayoutParams lp=dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width= UtilScreen.getScreenWidth(activity)*11/12;
                dialogWindow.setAttributes(lp);
            }
        }
    }

    //开启录屏服务，开始录屏
    private void StartService(Intent intent){
        MainScreenStartBroadcast();
        activity.startService(intent);
        screen_timer.Start();  //开始计时
        btn_recorder_start.setVisibility(View.GONE);
        main_screen_layout.setVisibility(View.VISIBLE);
    }

    //主页开始录屏，发送广播给通知栏和悬浮窗使其做相应变化
 private void MainScreenStartBroadcast(){
        ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
         if(FloatWindowManager.isWindowShowing()){
             BeanSettings beanSettings=new DaoSettings().get_Data(1);  //获取数据库的值存入到设置Bean里面
             if(beanSettings.getEn_suspended_window()==0){
                 //录制时隐藏悬浮窗，结束后打开悬浮窗
                 screenRecorderState.setBl_hide_float_window(true);  //隐藏悬浮窗标志位为真
                 start_float_window.setChecked(false);   //悬浮窗按钮变为未开启状态（开关按钮变为false，执行false里面结束悬浮窗服务的方法）
             }else {
                 screenRecorderState.setWindow_screen_state(SCREEN_START);
                 activity.sendBroadcast(new Intent().setAction(MAIN_WINDOW_RECORDER));  //发送给悬浮窗广播
             }
         }
       screenRecorderState.setNotification_screen_state(SCREEN_START);
        activity.sendBroadcast(new Intent().setAction(MAIN_NOTIFICATION_RECORDER));  //发送给通知栏广播
    }

    //刚打开程序时动态申请权限
    @RequiresApi(api = M)
    public  void requestPermissionsInit() {
        String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO,READ_PHONE_STATE};
        requestPermissions(permissions, REQUEST_PERMISSIONS_INIT);
    }

    /**----------* 弹窗请求获取悬浮窗显示权限-------------*/
    private void showFloatWindowDialog(){
        AlertDialog alertDialog=new AlertDialog.Builder(activity)
                .setMessage("检测到手机没有授予悬浮窗权限，开启后才能使用悬浮窗功能。")
                .setCancelable(false)
                .setPositiveButton("马上开启", (dialog, which) ->
                        UtilsPermission.commonROMPermissionApply(activity)
                ).setNegativeButton("暂不开启", (dialog, which) ->
                        dialog.dismiss()
                )
                .create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#999999"));
        alertDialog.setCanceledOnTouchOutside(true);
        Window dialogWindow=alertDialog.getWindow();
        WindowManager.LayoutParams lp=dialogWindow.getAttributes(); //获取对话框当前的参数值
        dialogWindow.setGravity(Gravity.CENTER);          //居中显示
        lp.width= UtilScreen.getScreenWidth(activity)*11/12;   //宽度设置为屏幕的11/12
        dialogWindow.setAttributes(lp);
    }

    /**
     * 异步加载头像
     */
    class LoadImage extends AsyncTask<String,Void, Bitmap> {
        private Bitmap bitmap=null;
        private String imgUrl;
        public LoadImage(String imgUrl) {
            this.imgUrl = imgUrl;
            Log.w("TAGFragmentMain","LoadImage  imgUrl="+imgUrl);
        }
        //在子线程里面完成耗时工作
        @Override
        protected Bitmap doInBackground(String... strings) {
            OkHttpClient client=new OkHttpClient();
            try {
                Request request=new Request.Builder().url(imgUrl).build();
                ResponseBody requestBody=client.newCall(request).execute().body();
                InputStream inputStream=requestBody.byteStream();
                bitmap= BitmapFactory.decodeStream(inputStream);
                Log.w("TAG","doInBackground  bitmap="+bitmap);
            }catch (IOException e){
                Log.w("TAG","异常");
                SharedPreferences sharedPreferences= activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);
                final String userId=sharedPreferences.getString("user_id",null);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(userId!=null){
                    editor.remove("user_id");
                    BeanUserInfo.getInstance().setU_id(null);  //内存中的用户ID
                }else {
                    editor.remove("openid");
                    editor.remove("access_token");
                    editor.remove("refresh_token");
                }
                editor.apply();
                e.printStackTrace();
            }
            return bitmap;

        }
        //耗时完成后，调用此方法

        @RequiresApi(api = M)
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap==null) {
                return;
            }
            iv_unlogin_HeadImg.setVisibility(View.GONE); //隐藏游客头像
            //显示用户头像
            iv_login_HeadImg.setVisibility(View.VISIBLE);
            iv_login_HeadImg.setImageBitmap(bitmap);
            //设置按钮不可点击
            ll_MainLogin.setClickable(false);
            //用户名字符串长度大于4，后面用...表示
            String name=BeanUserInfo.getInstance().getU_name();
            if(name.length()>=4){
                name=name.substring(0,4)+"...";
            }
            tv_MainLogin.setText(name);
            new UtilsToast(activity,"登录成功").show(Toast.LENGTH_SHORT);
        }
    }
}

