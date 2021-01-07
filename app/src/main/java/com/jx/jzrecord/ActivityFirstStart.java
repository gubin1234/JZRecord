package com.jx.jzrecord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jx.jzrecord.login.BeanUserInfo;
import com.jx.jzrecord.setting.ActivitySetting;
import com.jx.jzrecord.utils.UtilsToast;
import com.jx.productservice.ProSerUrlInfo;
import com.jx.productservice.ProServiceInfo;
import com.jx.productservice.ProServiceOkHttp;
import com.jx.productservice.UtilSetParam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class ActivityFirstStart extends AppCompatActivity {
    private  SharedPreferences sharedPreferences;  //数据库对象
    private SharedPreferences.Editor editor;    //数据库编辑对象
    private String m_id; //手机机器码
    private int install;  //2表示首次安装，1表示非首次安装
    private int tutorial; //1表示弹窗协议窗，0表示不用
    private int count;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        //防止启动后再次点击图片重新启动
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        init();
        //数据库对象初始化
         sharedPreferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
         install=sharedPreferences.getInt("install",2);  //2表示首次安装，1表示非首次安装

        //获取服务器返回的产品服务信息
        getProSerData();
         tutorial= sharedPreferences.getInt("tutorial",1);
        if(tutorial==1){
            showFirstDialog();
        }else {
            checkBackMsg();
        }
    }



    private void init(){
        count=0;
//        设置本地版本号和渠道号
        if(!UtilSetParam.SetLocalChannelName(this)||!UtilSetParam.SetLocalVersionName(this))
        {
            new UtilsToast(this,"设置本地版本号或渠道号错误").show(Toast.LENGTH_SHORT);
        }

        //获取Android手机机器码
        String ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        m_id=stringToMD5("LuckCPUID="+ANDROID_ID+"&1ife");
        Log.w("ActivityFirstStart机器码：",m_id);
        //设置手机机器码
       BeanUserInfo.getInstance().setM_id(m_id);
    }

    private void checkBackMsg(){
        if (timer == null) {
            timer = new Timer();
            //延迟delay毫秒后每隔period毫秒执行一次RefreshTask()方法
            //period计划时间，最终的时间间隔是方法完成的时间间隔。
        }
        timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
    }
    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            ProServiceInfo proServiceInfo=ProServiceInfo.getInstance();
            if(proServiceInfo.getCallBackMsg()!=null&&count==0){
                count++;
                if(proServiceInfo.getCallBackMsg().equals("请求成功")){
                    Log.w("111","SS");
                    //实例化SharedPreferences.Editor对象
                    editor = sharedPreferences.edit();
                    if(tutorial!=0){
                        editor.putInt("tutorial", 0);
                        editor.apply();                //同意之后以后都不弹出来了
                    }
                    if(install!=1){
                        editor.putInt("install", 1);
                        editor.apply();
                    }
                    Intent intent=new Intent(ActivityFirstStart.this,ActivityWelcome.class);
                    startActivity(intent);
                    finish();
                }else if(proServiceInfo.getCallBackMsg().equals("未设置的版本信息")){
                    Log.w("111","SSS");
                    HandlerToast(proServiceInfo.getCallBackMsg()); //弹窗提示错误信息
                    finish();
                }else {
                    Log.w("111","SSSS");
                    HandlerToast(proServiceInfo.getCallBackMsg()); //弹窗提示错误信息
                    Intent intent=new Intent(ActivityFirstStart.this,ActivityWelcome.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    private void HandlerToast(String msg){
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              Toast toast = Toast.makeText(ActivityFirstStart.this, msg, Toast.LENGTH_LONG);
                              toast.setGravity(Gravity.BOTTOM, 0, 0); //居中显示
                              LinearLayout linearLayout = (LinearLayout) toast.getView();
                              TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                              messageTextView.setTextSize(12);//设置toast字体大小
                              toast.show();
                              toast.show();
                          }
                      });
    }
    //将字符串转成MD5的值
    public static String stringToMD5(String value) {
        if (!TextUtils.isEmpty(value)) {
            byte[] hash;
            try {
                hash = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10)
                    hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        }
        return null;
    }

    //获取服务器返回的产品服务信息
    public void getProSerData(){
        ProServiceInfo proServiceInfo=ProServiceInfo.getInstance();
        String UrlParam="install="+install+"&m_id="+m_id+"&nonce_str="+getRandomString()
                +"&version_information="+proServiceInfo.getVersion_information();
        String  UrlParamMD5=ProServiceOkHttp.isMD5(UrlParam,proServiceInfo.getKey()); //UrlParam需要经过MD5加密才发送数据
        ProServiceOkHttp.GetProServiceData(UrlParamMD5,ProSerUrlInfo.getInstance().getUrlPart(), ProSerUrlInfo.getInstance().getUrlBase()); //请求服务器获取信息
    }

    //获取随机字符串
    private String getRandomString(){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuilder stringBuilder=new StringBuilder();  //可变字符串
        for(int i = 0; i< 32; i++){
            int index=random.nextInt(str.length());
            stringBuilder.append(str.charAt(index));  //添加随机下标所对应的字符串
        }
        return stringBuilder.toString();
    }

    public void showFirstDialog(){
        final AlertDialog aDialog=new AlertDialog.Builder(this).create();
        final View contentView=getLayoutInflater().inflate(R.layout.dialog_first_start_layout,null);//获取自定义资源布局
        aDialog.show();
        TextView tv=contentView.findViewById(R.id.content3);
        tv.setText(getClickableSpan());
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#222222"));
        aDialog.setCancelable(false);   //点击外部和返回键都不消失
//        aDialog.setCanceledOnTouchOutside(false);  //点击外部不消失，点击返回键消失
        //让超链接起作用
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        //获取屏幕的尺寸
        WindowManager manager=(WindowManager)this.getSystemService(ActivitySetting.WINDOW_SERVICE);
        Window dialogWindow=aDialog.getWindow();
        dialogWindow.setWindowAnimations(R.style.NoAnimationDialog); // 添加动画（取消动画效果）
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//消除背景白块
        dialogWindow.setContentView(contentView);
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        Display display=manager.getDefaultDisplay();
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width= (int) (display.getWidth()/1.3);
        Button btn_cancel=contentView.findViewById(R.id.btn_start_cancel);
        Button btn_certain=contentView.findViewById(R.id.btn_start_certain);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
                finish();
            }
        });
        btn_certain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
                checkBackMsg();
            }
        });
        dialogWindow.setAttributes(lp);
        aDialog.setCanceledOnTouchOutside(false);//dialog弹出后点击屏幕，dialog不消失；点击物理返回键dialog消失
    }

    /**
     * 方法名：SpannableString
     * 自定义字符串
     * @return SpannableString(自定义字符串类型)
     */
    private SpannableString getClickableSpan() {
        SpannableString spannableString = new SpannableString("您可阅读《隐私政策》和《用户协议》了解详细信息。如您同意，请点击“同意”开始接受我们的服务。");
        /*隐私政策*/
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1E")),4,10,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent_policy=new Intent(ActivityFirstStart.this, ActivityHiddenPolicy.class);
                startActivity(intent_policy);
            }
        },4,10,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        /*用户协议*/
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1E")),11,17,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent_user=new Intent(ActivityFirstStart.this, ActivityUserAgree.class);
                startActivity(intent_user);
            }
        },11,17,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    @Override
    protected void onDestroy() {
        Log.w("TAG","ActivityStartDestroy");
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    @Override
    public void onBackPressed() {
           this.finish();
        }
        // 注意，这里不需要调用父类的onBackPressed方法了，否则每次运行到这调用父类的onBackPressed退出程序了
        //super.onBackPressed();不要调用
}