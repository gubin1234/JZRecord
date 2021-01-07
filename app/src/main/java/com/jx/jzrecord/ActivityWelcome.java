package com.jx.jzrecord;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


import com.jx.jzrecord.login.AsyncTaskLogin;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.utils.UtilsToast;
import com.jx.jzrecord.wxapi.WXEntryActivity;
import com.jx.jzrecord.wxapi.WxData;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

import static com.jx.jzrecord.utils.UtilsNetWork.isConn;


public class ActivityWelcome extends ActivityBase {
    private AsyncTaskLogin task;   //异步加载数据类
    private Handler handler=new Handler();
    //-------------------微信登录有关变量---------------//
    private WXEntryActivity get_data=new WXEntryActivity();
    private String openid;  //微信登录的用户ID
    private String userId;  //手机或邮箱登录的用户ID
    private SharedPreferences sharedPreferences; //数据库对象

    //显示广告页面的时间，2 秒
    long showTime=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_layout);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//AS下全屏隐藏标题栏代码
        Log.w("TAGWelcome","onCreate");
        //数据库对象初始化
        sharedPreferences= getSharedPreferences("userdata", Context.MODE_PRIVATE);
        Login();  //登录
    }
    /**
     * @ 作者: LSY
     * @ 方法名: Login
     * @ 描述: 从启动页跳转到主页的功能，加载主页所需的数据数据
     * @ 参数: []
     * @ 返回值: void
     */
  private void Login(){
      Log.w("TAGWelcome","Login");
      userId=sharedPreferences.getString("user_id",null);
      openid=sharedPreferences.getString("openid",null);
        if(userId==null&&openid==null){
          //用户未登录过
          //延迟showTime秒跳转到主页面
          handler.postDelayed(jundToMainActivity, (showTime-1)*1000);
        }else {
            if(isConn(getApplicationContext())){
                //不是第一次登录
                if(userId!=null){
                    //是手机或邮箱登录
                    task = new AsyncTaskLogin(getApplicationContext(),userId,ActivityMain.class);
                    task.execute();
                }else {
                    //是微信登录
                    //刷新接口调用凭证
                    String refresh_token=sharedPreferences.getString("refresh_token",null);
                    Refresh_Token(refresh_token);
                }
            }else {
                //没网络情况
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(userId!=null){
                    editor.remove("user_id");
                }else {
                    editor.remove("openid");
                    editor.remove("access_token");
                    editor.remove("refresh_token");
                }
                editor.apply();
            }
            handler.postDelayed(jundToMainActivity, (showTime+1)*1000);
        }
  }
    //创建Runnable对象
    Runnable jundToMainActivity= () -> {
        Log.w("TAGWelcome","跳转页面");
        Intent intent=null;
        if(!isConn(getApplicationContext())){
            //没网络
            Log.w("TAGNoInternet","没网络");
            //获取数据库对象
            intent=new Intent(ActivityWelcome.this,ActivityNoInternet.class);
        }else {
            //有网络
            Log.w("TAGNoInternet","有网络");
            intent = new Intent(ActivityWelcome.this, ActivityMain.class);
        }
        startActivity(intent);
        finish();
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("TAGWelcome","onDestroy");
        if(task!=null){
            task.cancel(true);
            task=null;
        }
        handler.removeCallbacks(jundToMainActivity);
        get_data.finish();
    }

    /**
     * 用code请求吗获取openid（普通用户唯一标识）  accessToken（接口调用凭证）值用于后期操作
     * @param refresh_token 请求码
     */
    private void Refresh_Token(final String refresh_token) {
        if(refresh_token==null) return;
        final String path = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
                + WxData.WEIXIN_APP_ID
                + "&grant_type=refresh_token"
                + "&refresh_token="
                + refresh_token;
        OkHttpUtils.get().url(path).build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                new UtilsToast(getApplicationContext(),"网络不稳定，请检查网络").show(Toast.LENGTH_SHORT);
                Log.w("TAGRefresh_Token", "getAccess_token:onError");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.w("TAGRefresh_Token", response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    //最新的access_token接口调用凭证
                    String access_token = jsonObject.getString("access_token").trim();
                    String refresh_token = jsonObject.getString("refresh_token").trim();
                    get_data.getUserMesg(access_token,openid,1);
                    //数据库保存数据
                    SharedPreferences sharedPreferences= getSharedPreferences("userdata", Context.MODE_PRIVATE);  //获取数据库对象
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token",access_token); //刷新的接口调用凭证
                    editor.putString("refresh_token",refresh_token); //用户刷新 access_token
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}