package com.jx.jzrecord.recording;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jx.jzrecord.Fragment.FragmentFile;
import com.jx.jzrecord.setting.bean.BeanParams;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoParams;
import com.jx.jzrecord.setting.dao.DaoSettings;
import com.jx.jzrecord.utils.UtilNotification;
import com.jx.jzrecord.utils.UtilsPermission;
import com.jx.jzrecord.utils.UtilsRom;
import com.jx.jzrecord.utils.UtilsToast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


import static com.jx.jzrecord.Fragment.FragmentMain.NOTIFICATION_RECORDER;
import static com.jx.jzrecord.Fragment.FragmentMain.SCREEN_STOP;
import static com.jx.jzrecord.recording.ScreenRecorder.AUDIO_AAC;
import static com.jx.jzrecord.recording.ScreenRecorder.VIDEO_AVC;


/**
 * @ 作者: LSY
 * @ 类名: ScreenRecorderService
 * @ 包名: com.jx.screentest
 * @ 描述:录屏服务类
 * @ 日期: 2020/9/27 17:48
 **/
public class ScreenRecorderService extends Service {
                /**-------------录屏参数------------------**/
    private static final String TAG = "TAGRecorderService";
    private static final String VIDEO_CODE = "OMX.google.h264.encoder"; //视频编解码器
    private static final String AUDIO_CODE ="OMX.google.aac.encoder"; //音频编解码器
    private int screen_width;  //宽
    private int screen_height;  //长
    private int screen_frame;   //帧率
    private int screen_bitrate;  //码率
    private int mResultCode;   //录屏结果码
    private Intent mResultData;  //录屏数据
    private boolean bl_Vertical; //录屏方向
    private  static ScreenRecorder mRecorder; //编码对象
    private MediaProjection mMediaProjection;  //获取屏幕数据对象
    private VirtualDisplay mVirtualDisplay;  //虚拟显示
    private MediaProjectionManager mMediaProjectionManager; //录屏管理类
    private Timer timers;   //计时器
    private UtilNotification mNotifications; //通知栏对象

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG,"onStartCommand");
        //通知栏对象初始化
        mNotifications = UtilNotification.getInstance(getApplicationContext());
        // 开始前台服务
        startForeground(0xa01, mNotifications.getNotification());
        //捕捉屏幕需要的数据
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        //录屏的方向
        bl_Vertical=intent.getBooleanExtra("bl_Vertical",true);
        //根据提供的捕捉屏幕数据创建录屏对象
        if(mMediaProjection==null){
            mMediaProjection=createMediaProjection();
        }
        //设置回调，接收错误信息
        mMediaProjection.registerCallback(mProjectionCallback, new Handler());
        //开始录屏
        startCapturing(mMediaProjection);
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG,"onCreate");
        //获取系统类型的录屏管理类
        mMediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        if(timers==null){
            //实例化计时器对象
            timers = new Timer();
        }
        //延迟delay毫秒后每隔period毫秒执行一次RefreshTask()方法，
        //period计划时间，最终的时间间隔是方法完成的时间间隔。
        timers.scheduleAtFixedRate(new RefreshTask(),  0, 30000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG,"onDestroy");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知

        if(mRecorder!=null){
            //释放录屏对象并且弹窗
            stopRecordingAndDialog(getApplicationContext());
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.setSurface(null);
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if(timers!=null){
            timers.cancel();
            timers = null;
        }
    }

    //服务运行中，监听app程序是否退出了
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.w("ScreenRecorderService","onTaskRemoved");
        //释放录屏对象
        if(mRecorder!=null){
            File file = new File(mRecorder.getSavedPath());
            new UtilsToast(getApplicationContext(),"录屏文件保存地址 " + file).show( Toast.LENGTH_LONG);
            stopRecorder();
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.setSurface(null);
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        //释放计时器对象
        if(timers!=null){
            timers.cancel();
            timers = null;
        }
    }

     //每隔30秒检测磁盘存储量
    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            if(!UtilsRom.isGoodSize(Environment.getExternalStorageDirectory()+"/DCIM/",getApplicationContext())){
                //容量低于500MB
                //结束录屏服务
                Intent intent = new Intent(getApplicationContext(), ScreenRecorderService.class);
                stopService(intent);
                new UtilsToast(getApplicationContext(),"磁盘存储量低于500M，自带停止录制").show(Toast.LENGTH_LONG);
            }
        }
    }


    //获取录屏对象
    public static ScreenRecorder getRecorder(){
        return mRecorder;
    }

    //开始录屏函数
    private void   startCapturing(MediaProjection mediaProjection) {
        Log.w(TAG,"startCapturing的createVideoConfig");
        VideoEncodeConfig video = createVideoConfig();
        AudioEncodeConfig audio = createAudioConfig(); // audio can be null
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "jinzhouluping"); //获取视频保存的目录
        if (!dir.exists() && !dir.mkdirs()) {
            cancelRecorder();
            return;
        }
        //创建格式
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        //给视频文件命名
        final File file = new File(dir,"record_"+format.format(new Date())+".mp4");
        Log.w(TAG, "Create recorder with :" + video + " \n " + audio + "\n " + file);
        //参数：录屏对象，视音频配置信息，保存文件地址。作用：实例化编码对象
        mRecorder = newRecorder(mediaProjection, video, audio, file);
        startRecorder();
    }
    /**开始录屏***/
    private void startRecorder() {
        if (mRecorder == null) return;
        mRecorder.start();
    }

    /**停止录屏***/
    private void stopRecorder() {
        if (mRecorder != null) {
            Log.w(TAG,"stopRecorder");
            mRecorder.quit();
        }
        mRecorder = null;
    }

    //取消录屏
    private void cancelRecorder() {
        if (mRecorder == null) return;
        new UtilsToast(this,"Permission denied! Screen recorder is cancel").show(Toast.LENGTH_SHORT);
        stopRecorder();
    }

    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        Log.w(TAG,"newRecorder");
        //通过mediaProjection, video参数信息创建VirtualDisplay对象
        final VirtualDisplay display = getOrCreateVirtualDisplay(mediaProjection, video);
        //output.getAbsolutePath()  返回该文件的绝对路径
        ScreenRecorder r = new ScreenRecorder(video, audio, display, output.getAbsolutePath());
        //结束编码时，执行下面的回调方法
        r.setCallback(new ScreenRecorder.Callback() {
            @Override
            public void onStop(Throwable error) {
                Log.w(TAG,"newRecorder里面的onStop");
                stopRecorder();
                if (error != null) {
                    error.printStackTrace();
                    Log.w(TAG,"onStop,删除视频文件");
                    output.delete();
                } else {
                    //保存录屏内容到指定路径
                    SaveVideoFile(output);
                }
            }
        });
        return r;
    }
    //创建虚拟显示对象
    private VirtualDisplay getOrCreateVirtualDisplay(MediaProjection mediaProjection, VideoEncodeConfig config) {
        if (mVirtualDisplay == null) {
            //名称	虚拟显示器的名称必须为非空且唯一
            //宽度	虚拟显示的宽度（以像素为单位）必须大于0。
            //高度	虚拟显示器的高度（以像素为单位）必须大于0。
            //密度Dpi	虚拟显示器的密度（以dpi为单位）必须大于0。
            //表面	虚拟显示内容应呈现到的表面必须为非空。
            //标志	虚拟显示标志的组合： VIRTUAL_DISPLAY_FLAG_PUBLIC，VIRTUAL_DISPLAY_FLAG_PRESENTATION 或VIRTUAL_DISPLAY_FLAG_SECURE。
            //surface为null表示不显示在surface上
            mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenRecorder-display0",
                    config.width, config.height, 1 /*dpi*/,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    null /*surface*/, null, null);
        } else {
            // resize if size not matched
            Point size = new Point();   //包含两个整数坐标的点
            mVirtualDisplay.getDisplay().getSize(size);
            if (size.x != config.width || size.y != config.height) {
                mVirtualDisplay.resize(config.width, config.height, 1);
            }
        }
        return mVirtualDisplay;
    }

    //处理录屏对象回调
    private MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            //录屏异常时，执行此方法
            if (mRecorder != null) {
                Log.w(TAG,"CallbackonStop");
                //结束录屏服务
                ScreenRecorderState screenRecorderState=ScreenRecorderState.getInstance();
                screenRecorderState.setNotification_screen_state(SCREEN_STOP);  //设置通知栏录屏状态
                sendBroadcast(new Intent().setAction(NOTIFICATION_RECORDER));  //发送录屏广播
                new UtilsToast(getApplicationContext(),"非法操作导致录屏异常，请重新录制").show(Toast.LENGTH_LONG);
            }
        }
    };

    //保存录屏内容到指定路径
    private void SaveVideoFile(File output){
        //如果视频完整就保存视频
           Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                   .addCategory(Intent.CATEGORY_DEFAULT)
                   .setData(Uri.fromFile(output));
           Log.w(TAG,"onStop,setdata="+Uri.fromFile(output));
           sendBroadcast(intent);
    }

    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

  //根据数据库设置的数据，创建视频信息（帧率，比特率，分辨率等）封装到VideoEncodeConfig类中
    private VideoEncodeConfig createVideoConfig() {
        Log.w(TAG,"createVideoConfigd的codec="+VIDEO_CODE);
        DaoParams daoParams=new DaoParams();
        BeanParams beanParams=daoParams.get_Data(1);
        SetFrame(beanParams.getFrame());
        SetBitrate(beanParams.getBitrate());
        SetResolution(beanParams.getResolution());
        Log.w(TAG,"createVideoConfigd的录屏宽高="+screen_width+"*"+screen_height);
        Log.w(TAG,"createVideoConfigd的framerate="+screen_frame);
        Log.w(TAG,"createVideoConfigd的bitrate="+screen_bitrate);
        return new VideoEncodeConfig(screen_width, screen_height, screen_bitrate,
                screen_frame, VIDEO_CODE, VIDEO_AVC);
    }

    private void SetResolution(String resolution){
        switch (resolution){
            case "240P":
                screen_width=bl_Vertical? 240:320;
                screen_height=bl_Vertical? 320:240;
                break;
            case "360P":
                screen_width=bl_Vertical? 360:480;
                screen_height=bl_Vertical? 480:360;
                break;
            case "480P":
                screen_width=bl_Vertical? 480:640;
                screen_height=bl_Vertical? 640:480;
                break;
            case "720P":
                screen_width=bl_Vertical? 720:1280;
                screen_height=bl_Vertical? 1280:720;
                break;
            case "1080P":
                screen_width=bl_Vertical? 1080:1920;
                screen_height=bl_Vertical? 1920:1080;
                break;
            default:
                break;
        }
    }

    private void SetFrame(String frame){
       switch (frame){
           case "自动":
           case "30FPS":
               screen_frame=30;
               break;
           case "15FPS":
               screen_frame=15;
               break;
           case "25FPS":
               screen_frame=25;
               break;
           case "40FPS":
               screen_frame=40;
               break;
           case "50FPS":
               screen_frame=50;
               break;
           case "60FPS":
               screen_frame=60;
               break;
           default:
               break;
       }
    }

    private void SetBitrate(String bitrate){
        switch (bitrate){
            case "自动":
            case "3Mbps":
                screen_bitrate=3000000;
                break;
            case "1Mbps":
                screen_bitrate=1000000;
                break;
            case "2Mbps":
                screen_bitrate=2000000;
                break;
            case "4Mbps":
                screen_bitrate=4000000;
                break;
            case "5Mbps":
                screen_bitrate=5000000;
                break;
            case "8Mbps":
                screen_bitrate=8000000;
                break;
            case "12Mbps":
                screen_bitrate=12000000;
                break;
            default:
                break;

        }
    }

    //根据数据库设置的数据，创建音频信息（采样率等）封装到AudioEncodeConfig类中
    private AudioEncodeConfig createAudioConfig() {
        //获取数据库允不允许录制音频（1拒绝，0同意）
        DaoSettings daoSettings=new DaoSettings();
        BeanSettings beanSettings=daoSettings.get_Data(1);
        if(!UtilsPermission.hasPermissions(getApplicationContext(),"AUDIO_PERMISSION")||beanSettings.getEn_audio()==1){
            return null;
        }
        Log.w(TAG,"Audiocode="+AUDIO_CODE);
        int bitrate = 320000;
        int samplerate = 44100;
        int channelCount = 2;
        int profile = 1;
        return new AudioEncodeConfig(AUDIO_CODE, AUDIO_AAC, bitrate, samplerate, channelCount, profile);

    }

    /**------------------------保存文件---------------------------**/
    public void stopRecordingAndDialog(Context context) {
        File file = new File(mRecorder.getSavedPath());
        new UtilsToast(context,"录屏结束").show(Toast.LENGTH_LONG);
        StrictMode.VmPolicy vmPolicy = StrictMode.getVmPolicy();
        //释放mRecorder对象
        stopRecorder();
        try {
            // disable detecting FileUriExposure on public file
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            // 通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        } finally {
                StrictMode.setVmPolicy(vmPolicy);
                FragmentFile fileFragment = (FragmentFile) ActivityMain.mFragments.get(1);
                fileFragment.startScanTack();  //将新增视频从数据库加载到文件列表
                fileFragment.showEndDialog(file); //录屏结束弹窗
        }
    }
}
