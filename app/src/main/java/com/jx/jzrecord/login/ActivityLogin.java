package com.jx.jzrecord.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.jx.jzrecord.ActivityHiddenPolicy;
import com.jx.jzrecord.ActivityUserAgree;
import com.jx.jzrecord.R;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.utils.UtilsCountDownTimer;
import com.jx.jzrecord.utils.UtilsToast;
import com.jx.jzrecord.wxapi.WxLogin;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jx.jzrecord.utils.UtilsNetWork.isConn;


public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    /**---控件变量----*/
    private TextView tv_customMultiHyperLink;   //用户协议文本
    private LinearLayout btn_back;         //返回主页按钮
    private Button btn_getcode;       //验证码按钮
    private Button btn_login;          //登录按钮
    private Button btn_WXLogin;        //微信登录按钮
    private EditText et_username;      //输入手机号或邮箱号文本框
    private EditText et_usercode;        //输入验证码文本框
    public static boolean issent=false;   //是否为验证码发送状态，验证码倒计时里面有用
    private CheckBox checkBox;      //是否勾选用户协议检查框
    private View loading;        //加载条变量
    private AsyncTaskLogin task;                       //异步加载数据类
     /**---获取手机登录api,邮箱登录api----*/
    ILogin ILoginPhone= FactoryLogin.createApi("Phone");
    ILogin  ILoginEmail= FactoryLogin.createApi("Email");
    UtilsCountDownTimer mCountDownTimerUtils;
   private String m_id;  //手机机器码
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
        Log.w("TAG","*"+m_id);
        //验证码点击监听器
        btn_getcode.setOnClickListener(this);
        //登录按钮点击监听器
        btn_login.setOnClickListener(this);
        //返回按钮点击监听器
        btn_back.setOnClickListener(this);
        //
        btn_WXLogin.setOnClickListener(this);

        //借助SpannableString类实现超链接文字
        tv_customMultiHyperLink.setText(getClickableSpan());
        //设置超链接可点击
        tv_customMultiHyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        /*
         * 实时监听手机或邮箱文本框，当有输入时验证码按钮呈可点击状态
         * @return
         */
       TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //改前
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //改中
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String data=et_username.getText().toString();
                if(!issent){
                    if(data.length()>0){
                        btn_getcode.setBackgroundResource(R.drawable.btnview);//设置按钮为橙色,可点击
                        btn_getcode.setEnabled(true);
                    }else{
                        btn_getcode.setEnabled(false);
                        btn_getcode.setBackgroundResource(R.drawable.codeview);//设置按钮为灰色，这时是不能点击的
                        }
                }
             }
        };
        et_username.addTextChangedListener(textWatcher);
    }

    /**--------------------Activity销毁时执行------------------**/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("TAG","LoginonDestroy");
        if(task!=null){
            task.cancel(true);
            task=null;
        }
        if(mCountDownTimerUtils!=null){
            btn_getcode.setText("重新获取验证码");
            btn_getcode.setClickable(true);
            btn_getcode.setBackgroundResource(R.drawable.btnview);//设置按钮为橙色,可点击
            issent=false;
            mCountDownTimerUtils.cancel();
            mCountDownTimerUtils=null;
        }
    }

    /**--------------------初始化按钮控件------------------**/
    private void initialize()
    {
        //获取Android机器码
        m_id= BeanUserInfo.getInstance().getM_id();
        //协议文本
        tv_customMultiHyperLink =  findViewById(R.id.TV);
        //验证码按钮
        btn_getcode=findViewById(R.id.bt_getcode_id);
        //手机或邮箱文本
        et_username= findViewById(R.id.et_register_username_id);
        //验证码文本框
        et_usercode=findViewById(R.id.et_register_code_id);
        //登录按钮
        btn_login=findViewById(R.id.login);
        //单选按钮
        checkBox=findViewById(R.id.checkbox);
        //返回按钮
        btn_back=findViewById(R.id.register_back_login);
        //加载条
        loading= findViewById(R.id.user_info_loading);
        //微信登录
        btn_WXLogin=findViewById(R.id.btn_WXLogin);
    }
                              /**--------------------按钮点击事件------------------**/
    @Override
    public void onClick(View view) {
        /*------返回主页按钮点击事件---*/
        if(view.getId()==R.id.register_back_login){
            this.finish();
        }

                     //检查网络是否连接
        if(isConn(getApplicationContext())) {
            //访问接口的参数URL
            HashMap<String, String> urlParam;
            /*------验证码按钮点击事件---*/
            if (view.getId() == R.id.bt_getcode_id) {
                String userdata = et_username.getText().toString(); //获取用户输入的手机号或邮箱号
                Log.w("TAG", "用户输入的数据:" + userdata);
                if (isNumeric(userdata)) {     //判断是否为整数
                    if (isMobile(userdata)) {  //判断是否为正确手机号
                        issent = true;         //验证码按钮状态变成发送状态
                        urlParam = new HashMap<>();
                        urlParam.put("phone", userdata);
                        ILoginPhone.SentCode(urlParam);          //发送手机验证码
                        //倒计时
                        mCountDownTimerUtils = new UtilsCountDownTimer(btn_getcode, 120000, 1000);
                        mCountDownTimerUtils.start();
                    } else {
                        new UtilsToast(ActivityLogin.this,"手机格式错误").show(Toast.LENGTH_SHORT);
                    }
                } else {
                    if (isEmail(userdata)) {    //判断邮箱号是否正确
                        issent = true;         //验证码按钮状态变成发送状态
                        /*-------发送邮箱验证码--------*/
                        urlParam = new HashMap<>();
                        urlParam.put("m_id", m_id);
                        urlParam.put("Email", userdata);
                        urlParam.put("version_information", BeanServerInfo.getInstance().getVersion_information());
                        ILoginEmail.SentCode(urlParam);
                        mCountDownTimerUtils = new UtilsCountDownTimer(btn_getcode, 120000, 1000);
                        mCountDownTimerUtils.start();
                    } else {
                        new UtilsToast(ActivityLogin.this,"邮箱格式错误").show(Toast.LENGTH_SHORT);
                    }
                }
            }

        /*------登录按钮点击事件---*/
        if(view.getId()==R.id.login){
            /*
             * 判断手机号或邮箱是否为空
             */
            if (TextUtils.isEmpty(et_username.getText().toString()))
            {
                new UtilsToast(ActivityLogin.this,"请输入手机或邮箱号").show(Toast.LENGTH_SHORT);
            }else if(TextUtils.isEmpty(et_usercode.getText().toString())){
                new UtilsToast(ActivityLogin.this,"请输入验证码").show(Toast.LENGTH_SHORT);
            }else {
                /*-------判断验证码正确与否，是否跳转到主页----------*/
                String userdata=et_username.getText().toString();  //获取用户输入的手机号或邮箱号
                String codedata=et_usercode.getText().toString();  //获取用户输入的验证码
                urlParam =new HashMap<>();
                urlParam.put("m_id",m_id);
                urlParam.put("version_information",BeanServerInfo.getInstance().getVersion_information());
                if(isNumeric(userdata)){
                    urlParam.put("v_code",codedata);
                    urlParam.put("phone",userdata);
                    ILoginPhone.ConfirmCode(urlParam);
                }else {
                    urlParam.put("EmailCode",codedata);
                    urlParam.put("Email",userdata);
                    ILoginEmail.ConfirmCode(urlParam);
                }
                    if(checkBox.isChecked()){
                        //异步加载数据，参数1：本类上下文，2:需要销毁的activity  3：跳转到的另一个activity,4：登录按钮，5：加载条
                            task = new AsyncTaskLogin(this,ActivityLogin.this, ActivityMain.class,btn_login, loading);
                            task.execute();
                    }else {
                        new UtilsToast(ActivityLogin.this,"请阅读并同意用户协议和隐私政策").show(Toast.LENGTH_SHORT);
                    }
              }
           }
            /*------微信登录点击事件-----*/
            if(view.getId()==R.id.btn_WXLogin){
                if(checkBox.isChecked()){
                    if(WxLogin.longWx()){
                        this.finish();
                    }
                }else {
                    new UtilsToast(ActivityLogin.this,"请阅读并同意用户协议和隐私政策").show(Toast.LENGTH_SHORT);
                }
               }
        }else {
              //没网络情况
            new UtilsToast(ActivityLogin.this,"当前网络不可用").show(Toast.LENGTH_SHORT);
        }

    }


    /**
     * 方法名：SpannableString
     * 自定义字符串
     * @return SpannableString(自定义字符串类型)
     */
    private SpannableString getClickableSpan() {
        SpannableString spannableString = new SpannableString("已阅读并同意 《用户协议》 和 《隐私政策》 ");
                                                          /*---------用户协议--------*/
        //设置下划线文字
        spannableString.setSpan(new UnderlineSpan(), 6, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      //设置点击动作
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent=new Intent(ActivityLogin.this, ActivityUserAgree.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan,6,14,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的前景色
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1D")), 6, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                        /*---------隐私政策----------*/
        //设置下划线文字
        spannableString.setSpan(new UnderlineSpan(), 15, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置点击动作
        ClickableSpan clickableSpan1=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent=new Intent(ActivityLogin.this, ActivityHiddenPolicy.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan1,15,23,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的前景色
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1D")), 15, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //设置字体大小（绝对值,单位：像素）,第二个参数boolean dip，如果为true，表示前面的字体大小单位为dip，否则为像素
        spannableString.setSpan(new AbsoluteSizeSpan(13, true), 0, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    /**
     * 方法名：isNumeric
     * 判断字符串是否全为整数
     * @return true(全为整数)
     */
    public boolean isNumeric(String number){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(number);
        return isNum.matches();
    }

    /**
     * 方法名：isMobile
     * 用正则表达式判断手机号正确性
     * @return true(手机号正确)
     */
    public static boolean isMobile(String number) {
        Pattern pattern = Pattern.compile("^((13[^4,\\D])" + "|(134[^9,\\D])" +
                "|(14[5,7])" +
                "|(15[^4,\\D])" +
                "|(17[3,6-8])" +
                "|(18[0-9]))\\d{8}$");
        Matcher isPone = pattern.matcher(number);
        return isPone.matches();
    }


    /**
     * 方法名：isEmail
     * 用正则表达式判断邮箱正确性
     * @return true(邮箱号正确)
     */
    public static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }

}

