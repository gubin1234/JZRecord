package com.jx.jzrecord.setting;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.jx.jzrecord.ActivityNoInternet;
import com.jx.jzrecord.R;
import com.jx.jzrecord.login.BeanUserInfo;
import com.jx.jzrecord.setting.bean.BeanParams;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoParams;
import com.jx.jzrecord.setting.dao.DaoSettings;
import com.jx.jzrecord.utils.UtilScreen;
import com.jx.jzrecord.utils.UtilsToast;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;
import static com.jx.jzrecord.utils.UtilsNetWork.isConn;


public class ActivitySetting extends AppCompatActivity {

    private LinearLayout btn_setting_back;//标题栏返回图标
    private Button btn_exit;   //退出登录
    private String change_rl;//保存设置的分辨率参数
    private String change_bit;//保存设置的码率参数
    private String change_frame;//保存设置的帧数参数
    private String change_time="3s";//改变的倒计时
    private TextView tv_rl;//显式当前选择的分辨率
    private TextView tv_bit;//显示当前选择的码率
    private TextView tv_frame;//显示当前选择的帧数
    private TextView tv_cutdown;//显示当前选择的倒计时数
    private TextView tv_space;//可用空间
    private Switch sw_audio;//音频开关
    private Switch sw_suspended_window;//悬浮窗开关
    private Switch sw_shake_stop;//摇动手机停止录制开关
    private Button btn_ok;//确认按钮
    private LinearLayout layout_rl;//分辨率的布局
    private LinearLayout layout_bit;//码率的布局
    private LinearLayout layout_frame;//帧数的布局
    private LinearLayout layout_countdown; //倒计时布局
    private String[] array_rl ={"240","360","480","720","1080"};//分辨率滑动条刻度
    private String[] array_bit ={"自动","1","2","3 ","4","5","8","12"};//码率滑动条刻度
    private String[] array_frame ={"自动","15","25","30","40","50","60"};//帧数滑动条刻度
    private int progress_rl=75;//分辨率进度值
    private int progress_bit=67;//码率进度值
    private int progress_frame=40;//帧数刻度值
    private RadioGroup rg_countdown;//倒计时单选

    private LinearLayout layout_show_click;//显示点击操作
    private LinearLayout layout_frequent_question;//显示常见问题
    private LinearLayout layout_use_tutorial;//显示使用教程
    private LinearLayout layout_permission_set;//显示权限设置
    private LinearLayout layout_us_info;//显示关于我们
    private LinearLayout layout_check_update;//检查更新

    private CustomNodeSeekBar customNodeSeekBar_resolution;//分辨率滑动块
    private CustomNodeSeekBar customNodeSeekBar_bitrate;//码率滑动块
    private CustomNodeSeekBar customNodeSeekBar_frame_rate;//帧数滑动块
    private SharedPreferences sharedPreferences;//数据库对象
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Log.w("TAG", "ActivitySettingonCreate");
        //变量初始化
        init();
        //参数初始值显示
        ParamsDisplay();
        //各种监听
        ClickListener();
    }

    //变量初始化
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init() {
        //初始化数据库对象
        sharedPreferences= getSharedPreferences("userdata", Context.MODE_PRIVATE);
        //返回主页按钮
        btn_setting_back = findViewById(R.id.btn_setting_back);
        tv_rl = findViewById(R.id.resolution_num);
        tv_bit = findViewById(R.id.bitrate_num);
        tv_frame = findViewById(R.id.frame_num);
        tv_space= findViewById(R.id.tv_available_space);
        layout_rl = findViewById(R.id.resolution_layout);
        layout_bit =  findViewById(R.id.bitrate_layout);
        layout_frame =  findViewById(R.id.frame_layout);
        layout_countdown= findViewById(R.id.countdown_layout);
        tv_cutdown =  findViewById(R.id.countdown_seconds);
        sw_audio =  findViewById(R.id.audio_switch);
        sw_suspended_window=findViewById(R.id.Suspended_window_switch);
        sw_shake_stop=findViewById(R.id.shake_stop_switch);
        btn_exit=findViewById(R.id.exit); //退出登录
        layout_show_click=findViewById(R.id.show_Click);
        layout_frequent_question=findViewById(R.id.frequent_question);
        layout_permission_set=findViewById(R.id.permission_set);
        layout_use_tutorial=findViewById(R.id.use_tutorial);
        layout_us_info=findViewById(R.id.us_info);
        layout_check_update=findViewById(R.id.check_update);
        Beta.autoInit = true;
        Beta.autoCheckUpgrade = false;
        Beta.upgradeStateListener=new UpgradeStateListener() {
            @Override
            public void onUpgradeFailed(boolean b) {
            }

            @Override
            public void onUpgradeSuccess(boolean b) {
            }
            //没有新版本
            @Override
            public void onUpgradeNoVersion(boolean b){
                new UtilsToast(getApplicationContext(),"你已经是最新版本了").show(Toast.LENGTH_SHORT);
                Log.d("ABC","没有最新版本");
            }

            @Override
            public void onUpgrading(boolean b) {
                new UtilsToast(getApplicationContext(),"正在检测最新版本...").show(Toast.LENGTH_SHORT);
                Log.d("ABC","正在检测");
            }
            @Override
            public void onDownloadCompleted(boolean b) {
            }
        };
        Beta.init(getApplicationContext(),false);//再次初始化
    }

    //各种点击事件监听
    public void ClickListener(){
        //退出登录按钮处理
        final String userId=sharedPreferences.getString("user_id",null);
        final String openid=sharedPreferences.getString("openid",null);
        if(userId==null&&openid==null){
            btn_exit.setVisibility(View.GONE);
        }else {
            btn_exit.setVisibility(View.VISIBLE);
        }
        //三个布局的共同监听
        layout_rl.setOnClickListener(new OnClickListener());
        layout_bit.setOnClickListener(new OnClickListener());
        layout_frame.setOnClickListener(new OnClickListener());
        //返回按钮监听
        btn_setting_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConn(getApplicationContext())&&userId==null&&openid==null){
                    startActivity(new Intent(ActivitySetting.this, ActivityNoInternet.class));
                }
                finish();
            }
        });
        //倒计时布局监听
        layout_countdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCutdownTime();
            }
        });
        //退出登录按钮监听
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                BeanUserInfo.getInstance().setHead_portrait(null);  //内存中的头像
                new UtilsToast(ActivitySetting.this,"注销登录成功").show(Toast.LENGTH_SHORT);
                btn_exit.setVisibility(View.GONE);
            }
        });
        //权限设置布局监听
        layout_permission_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_show=new Intent(ActivitySetting.this, ActivityPermissionSet.class);
                startActivity(intent_show);
            }
        });
        //关于我们布局监听
        layout_us_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_us=new Intent(ActivitySetting.this, ActivityAboutUs.class);
                startActivity(intent_us);
            }
        });
        //常见问题布局监听
        layout_frequent_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_question=new Intent(ActivitySetting.this,ActivityQuestion.class);
                startActivity(intent_question);
            }
        });
        //显示点击操作布局监听
        layout_show_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_click=new Intent(ActivitySetting.this, ActivityClickOperate.class);
                startActivity(intent_click);
            }
        });
        //使用教程
        layout_use_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_click=new Intent(ActivitySetting.this, ActivityTutorial.class);
                startActivity(intent_click);
            }
        });
        //检查更新监听
        layout_check_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /******检查更新********/
                //参数1:isManual 用户手动点击检查,非用户点击操作请传false 参数2:isSilence 是否显示弹窗等交互,[true:没有弹窗和toast]
                Beta.checkUpgrade(true,false);
            }
        });

        DaoSettings daoSettings=new DaoSettings();
        BeanSettings beanSettings=new BeanSettings();
        /*******************设置开关状态变化监听事件************************/
        //音频开关监听
        sw_audio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    beanSettings.setEn_audio(0);//0表示开启
                    daoSettings.update_data(0,"en_audio");//数据库更新
                }else
                {
                    beanSettings.setEn_audio(1);//1表示关闭
                    daoSettings.update_data(1,"en_audio");//数据库更新
                }
            }
        });
        //悬浮窗开关监听
        sw_suspended_window.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    beanSettings.setEn_suspended_window(0);
                    daoSettings.update_data(0,"en_suspended_window");
                }else
                {
                    beanSettings.setEn_suspended_window(1);
                    daoSettings.update_data(1,"en_suspended_window");
                }

            }
        });
        //摇晃停止开关监听
        sw_shake_stop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    beanSettings.setEn_shake_stop(0);
                    daoSettings.update_data(0,"en_shake_stop");
                }else{
                    beanSettings.setEn_shake_stop(1);
                    daoSettings.update_data(1,"en_shake_stop");
                }

            }
        });
    }

    //参数显示
    public void ParamsDisplay(){
        /*---------获取数据库参数表数据--------*/
        DaoParams daoParams=new DaoParams();
        BeanParams beanParams=daoParams.get_Data(1);
        tv_rl.setText(beanParams.getResolution());//设置分辨率
        tv_bit.setText(beanParams.getBitrate());//设置码率
        tv_frame.setText(beanParams.getFrame());//设置帧率
        change_rl=beanParams.getResolution();//设置分辨率进度值
        change_bit=beanParams.getBitrate();//设置码率进度值
        change_frame=beanParams.getFrame();//设置帧数进度值
        /*--------获取指定路径的剩余空间--------*/
        String path= Environment.getExternalStorageDirectory()+"/DCIM/jinzhouluping/";
        String getRoom=getRom(path);//获取手机剩余空间
        if(getRoom!=null){
            String availableRoom=getRoom.replace(" ","");
            tv_space.setText(availableRoom+"可用");//设置可用空间大小
        }
        /*---------获取数据库设置表数据----------*/
        DaoSettings daoSettings=new DaoSettings();
        BeanSettings getBeanSettings=daoSettings.get_Data(1);
        //设置音频开关，0表示开启，1表示关闭
        if(getBeanSettings.getEn_audio()==0){
            sw_audio.setChecked(true);
        }
        else{
            sw_audio.setChecked(false);
        }
        //设置摇晃手机停止录制开关
        if(getBeanSettings.getEn_shake_stop()==0){
            sw_shake_stop.setChecked(true);
        }
        else{
            sw_shake_stop.setChecked(false);
        }
        //设置悬浮窗开关
        if (getBeanSettings.getEn_suspended_window()==0){
            sw_suspended_window.setChecked(true);
        }
        else{
            sw_suspended_window.setChecked(false);
        }
        //设置倒计时
        tv_cutdown.setText(getBeanSettings.getCount_down());
    }

    /**
     * 三个参数布局的共同监听事件
     */
    private class OnClickListener implements View.OnClickListener {
        public void onClick(View view) {
            final AlertDialog addDialog=new AlertDialog.Builder(ActivitySetting.this).create();//创建AlertDialog实例
            final View contentView=getLayoutInflater().inflate(R.layout.dialog_node_seek_bar,null);//获取自定义资源布局
            addDialog.setView(contentView);
            addDialog.show();
            Window dialogWindow=addDialog.getWindow();
            WindowManager.LayoutParams lp=dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.BOTTOM|Gravity.CENTER);//调整弹窗位置在底部中间
            lp.x = 0; //以屏幕左上角为原点，设置x,y的初值
            lp.y = 0;
            lp.width= UtilScreen.getScreenWidth(ActivitySetting.this)*11/12;//设置弹窗宽度
            dialogWindow.setAttributes(lp);
            addDialog.setCancelable(true);//dialog弹出后会点击屏幕或物理返回键，dialog不消失
            btn_ok=contentView.findViewById(R.id.ok_button);
            //实例化滑动块对象
             customNodeSeekBar_resolution=contentView.findViewById(R.id.nodeseekbar);
             customNodeSeekBar_bitrate =contentView.findViewById(R.id.bitrate_bar);
             customNodeSeekBar_frame_rate =contentView.findViewById(R.id.frame_rate_bar);
            //设置刻度值大小以及刻度值与滑动条的距离
            customNodeSeekBar_resolution.setSize(TextSize(),offsetY());
            customNodeSeekBar_bitrate.setSize(TextSize(),offsetY());
            customNodeSeekBar_frame_rate.setSize(TextSize(),offsetY());
            //打开数据库，获取参数表数据
            DaoParams daoParams=new DaoParams();
            BeanParams beanParams=daoParams.get_Data(1);
            //设置分辨率进度
            switch (beanParams.getResolution()) {
                case "240P":
                    progress_rl = 0;
                    break;
                case "360P":
                    progress_rl = 25;
                    break;
                case "480P":
                    progress_rl = 50;
                    break;
                case "720P":
                    progress_rl = 75;
                    break;
                case "1080P":
                    progress_rl = 100;
                    break;
            }
            customNodeSeekBar_resolution.setProgress(progress_rl);
            //设置码率进度
            switch(beanParams.getBitrate()){
                case "自动":
                    progress_bit=0;
                    break;
                case "1Mbps":
                    progress_bit=15;
                    break;
                case "2Mbps":
                    progress_bit=29;
                    break;
                case "3Mbps":
                    progress_bit=44;
                    break;
                case "4Mbps":
                    progress_bit=58;
                    break;
                case "5Mbps":
                    progress_bit=72;
                    break;
                case "8Mbps":
                    progress_bit=86;
                    break;
                case "12Mbps":
                    progress_bit=100;
                    break;
            }
            customNodeSeekBar_bitrate.setProgress(progress_bit);
            //设置帧数进度
            switch (beanParams.getFrame()){
                case "自动":
                    progress_frame=0;
                    break;
                case "15FPS":
                    progress_frame=17;
                    break;
                case "25FPS":
                    progress_frame=33;
                    break;
                case "30FPS":
                    progress_frame=50;
                    break;
                case "40FPS":
                    progress_frame=67;
                    break;
                case "50FPS":
                    progress_frame=83;
                    break;
                case "60FPS":
                    progress_frame=100;
                    break;
            }
            customNodeSeekBar_frame_rate.setProgress(progress_frame);
            //视频分辨率的seekBar设置
            customNodeSeekBar_resolution.setNodeCount(5);
            customNodeSeekBar_resolution.setArray(array_rl);
            customNodeSeekBar_resolution.setName("分辨率(P)");
            customNodeSeekBar_resolution.setOnChoiceListener(new CustomNodeSeekBar.OnSeekChoiceListener() {
                @Override
                public void onSeekChoosen(int index) {
                    switch (index) {
                        case 0:
                            change_rl="240P";
                            break;
                        case 1:
                            change_rl="360P";
                            break;
                        case 2:
                            change_rl="480P";
                            break;
                        case 3:
                            change_rl="720P";
                            break;
                        case 4:
                            change_rl="1080P";
                            break;
                        default:
                            break;
                    }
                }
            });
            //视频码率的seekBar设置
            customNodeSeekBar_bitrate.setNodeCount(8);
            customNodeSeekBar_bitrate.setArray(array_bit);
            customNodeSeekBar_bitrate.setName("码率(Mbps)");
            customNodeSeekBar_bitrate.setOnChoiceListener(new CustomNodeSeekBar.OnSeekChoiceListener() {
                @Override
                public void onSeekChoosen(int index) {
                    switch (index){
                        case 0:
                            change_bit="自动";
                            break;
                        case 1:
                            change_bit="1Mbps";
                            break;
                        case 2:
                            change_bit="2Mbps";
                            break;
                        case 3:
                            change_bit="3Mbps";
                            break;
                        case 4:
                            change_bit="4Mbps";
                            break;
                        case 5:
                            change_bit="5Mbps";
                            break;
                        case 6:
                            change_bit="8Mbps";
                            break;
                        case 7:
                            change_bit="12Mbps";
                            break;
                        default:
                            break;
                    }
                }
            });

            //视频帧数的seekBar设置
            customNodeSeekBar_frame_rate.setNodeCount(7);
            customNodeSeekBar_frame_rate.setArray(array_frame);
            customNodeSeekBar_frame_rate.setName("帧率(fps)");
            customNodeSeekBar_frame_rate.setOnChoiceListener(new CustomNodeSeekBar.OnSeekChoiceListener() {
                @Override
                public void onSeekChoosen(int index) {
                    switch (index) {
                        case 0:
                            change_frame="自动";
                            break;
                        case 1:
                            change_frame="15FPS";
                            break;
                        case 2:
                            change_frame="25FPS";
                            break;
                        case 3:
                            change_frame="30FPS";
                            break;
                        case 4:
                            change_frame="40FPS";
                            break;
                        case 5:
                            change_frame="50FPS";
                            break;
                        case 6:
                            change_frame="60FPS";
                            break;
                        default:
                            break;
                    }
                }
            });
            backgroundAlpha(0.618f);//设置背景透明度
            //确认按钮的点击事件
            btn_ok.setOnClickListener(new View.OnClickListener() {
                DaoParams daoParams=new DaoParams();
                BeanParams beanParams=new BeanParams();
                @Override
                public void onClick(View view) {
                    //设置改变后参数并更新数据库
                    beanParams.setFrame(change_frame);
                    beanParams.setBitrate(change_bit);
                    beanParams.setResolution(change_rl);
                    daoParams.addData(beanParams,ActivitySetting.this);
                    tv_rl.setText(beanParams.getResolution());
                    tv_bit.setText(beanParams.getBitrate());
                    tv_frame.setText(beanParams.getFrame());
                    addDialog.dismiss();//弹窗消失
                }
            });
            //弹窗消失监听，当弹窗消失时设置背景透明度
            addDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //为三个change设置值，不然下次点击确定会出错
                    DaoParams daoParams=new DaoParams();
                    BeanParams beanParams_dismiss=daoParams.get_Data(1);
                    change_frame=beanParams_dismiss.getFrame();
                    change_bit=beanParams_dismiss.getBitrate();
                    change_rl=beanParams_dismiss.getResolution();
                    backgroundAlpha(1f);//恢复背景透明度（透明）
                }
            });
        }
    }
    /**
     * @ 作者: yjm
     * @ 方法名: showWindow
     * @ 描述: 倒计时弹窗
     * @ 参数: []
     * @ 返回值: void
     */
    public void showCutdownTime(){
        final AlertDialog aDialog=new AlertDialog.Builder(ActivitySetting.this).create();
        final View contentView=getLayoutInflater().inflate(R.layout.dialog_cutdown_time,null);//获取自定义资源布局
        aDialog.setView(contentView);
        aDialog.show();
        rg_countdown =(RadioGroup)contentView.findViewById(R.id.rg_cut_down);
        //获取屏幕的尺寸
        Window dialogWindow=aDialog.getWindow();
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width= UtilScreen.getScreenWidth(ActivitySetting.this)*3/5;//设置弹窗宽度为屏幕的3/5
        //从数据库中获取数据并设置单选按钮的选中状况
        DaoSettings daoSettings=new DaoSettings();
        BeanSettings beanSettings=daoSettings.get_Data(1);
        switch(beanSettings.getCount_down()){
            case "无":
                rg_countdown.check(R.id.btn_non);
                break;
            case "3s":
                rg_countdown.check(R.id.btn_3s);
                break;
            case "5s":
                rg_countdown.check(R.id.btn_5s);
                break;
            case "10s":
                rg_countdown.check(R.id.btn_10s);
                break;
        }
        //单选监听
        rg_countdown.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.btn_non:
                        change_time="无";
                        break;
                    case R.id.btn_3s:
                        change_time="3s";
                        break;
                    case R.id.btn_5s:
                        change_time="5s";
                        break;
                    case R.id.btn_10s:
                        change_time="10s";
                        break;
                }
                DaoSettings daoSettings=new DaoSettings();
                daoSettings.update_data_time(change_time,"count_down");
                tv_cutdown.setText(change_time);
            }
        });
        dialogWindow.setAttributes(lp);
        aDialog.setCancelable(true);//点击dialog外部区域dialog不会关闭
    }

    /**
     * 方法名：backgroundAlpha
     * 功能:设置添加屏幕的背景透明度
     * @param bgAlpha 0.0-1.0，0.0是不透明，1.0是最透明
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=bgAlpha;
        getWindow().setAttributes(lp);
    }

    /*参数设置弹窗刻度值的大小*/
    private int TextSize(){
        // 1.获取当前设备的屏幕大小
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float)screenWidth /720;
        float ratioHeight = (float)screenHeight /1280;
        // 2.计算与你开发时设定的屏幕大小的纵横比(这里假设你开发时定的屏幕大小是720*1280)
        float RATIO= Math.min(ratioWidth, ratioHeight);
        //3.根据上一步计算出来的最小纵横比来确定字体的大小(假定在480*800屏幕下字体大小设定为35)
        int TEXT_SIZE = Math.round(25 * RATIO);  //四舍五入3
        return TEXT_SIZE;
    }

    /*参数设置弹窗刻度值的偏移量(根据屏幕的高）*/
    private float offsetY(){
        return (float) (UtilScreen.getScreenHeight(this)*0.0183);
    }

    /*获取剩余空间*/
    private String getRom(String path) {
        try{
            StatFs statfs = new StatFs(path);
            // 得到目录中空闲的块
            long availableBlocksLong = statfs.getAvailableBlocksLong();
            // 得到目录中空闲块的大小
            long blockSizeLong = statfs.getBlockSizeLong();
            // 转换进制/
            return Formatter.formatFileSize(this, availableBlocksLong * blockSizeLong);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}