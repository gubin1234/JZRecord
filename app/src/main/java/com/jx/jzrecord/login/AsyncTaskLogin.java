package com.jx.jzrecord.login;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jx.jzrecord.utils.UtilsToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * @ 作者: LSY
 * @ 类名: AsyncTaskLogin
 * @ 包名: com.jiangxia.ilogin
 * @ 描述:异步加载数据
 * @ 日期: 2020/9/9 15:10
 * 注意：黄色警告说明容易内存泄漏，因此必须在传来Activity引用的那个Activity销毁时AsyncTask对象的cancel方法,并且必须横竖屏切换时不消毁activity
 *  对象。在配置文件哪里设置
 **/
public class AsyncTaskLogin extends AsyncTask<Integer,Integer,Integer> {
    private Context context;  //上下文对象
    private Activity loginActivity;
    private Button login_btn;  //登录按钮控件
    private View loading;  //加载控件
    private Class activity;
    private String u_id; //机器码
    private int flag;  //标志位，当等于0时表示手动点击登录，当等于1时表示开启程序时自动登录
    private final static int OVERTIME=0;  //超时
    private final static int FAIL=1;     //失败
    private final static int SUCCESS=2;  //成功

    public AsyncTaskLogin(Context context,Activity loginActivity, Class activity, Button login_btn, View loading) {
        this.context = context;
        this.loginActivity = loginActivity;
        this.login_btn = login_btn;
        this.loading=loading;
        this.activity=activity;
        this.flag=0;
    }

    public AsyncTaskLogin(Context context,String u_id, Class activity) {
        this.context = context;
        this.activity=activity;
        this.flag=1;
        this.u_id=u_id;
    }

    //异步类执行时，最先执行的方法
    @Override
    protected void onPreExecute() {
        if(flag==0){
            loading.setVisibility(View.VISIBLE);  //显示加载条
            login_btn.setClickable(false); //设置登录按钮不可点击
        }
    }

    @Override
    protected Integer doInBackground(Integer... strings) {
        BeanUserInfo userData= BeanUserInfo.getInstance();
        for(int i=0;i<20;i++){
            try {
                Log.w("TAG","doInBackground+i:"+i);
                Thread.sleep(500);
                //第一次登录执行这些逻辑
                if(userData.getUserResponseData()!=null){
                    Log.w("TAG","执行第一次登录的代码");
                    JSONObject jsonObject=new JSONObject(userData.getUserResponseData());
                    int result_code=jsonObject.getInt("result_code");
                    userData.setResult_code(result_code);
                    if(result_code==-1){
                        return FAIL;
                    }else {
                        String u_id=jsonObject.getString("u_id");  //用户ID
                        userData.setU_id(u_id);
                        return GetUserData(userData,u_id);
                    }
                }
                //自动登录执行这些逻辑
                if(flag==1){
                    Log.w("TAG","自动登录");
                     return GetUserData(userData,u_id);
                }
            } catch (InterruptedException | JSONException e) {
                e.printStackTrace();
                new UtilsToast(context,"网络请求超时，请检查网络").show(Toast.LENGTH_SHORT);
                Log.w("TAG","doInBackground:捕捉到异常，退出");
                break;
            }

        }
        return OVERTIME;
    }

    //耗时完成后，调用此方法
    @Override
    protected void onPostExecute(Integer is) {
        if(flag==0){
            loading.setVisibility(View.INVISIBLE);
            login_btn.setClickable(true);
        }
        switch (is){
            case OVERTIME:
                new UtilsToast(context,"网络请求超时，请检查网络").show(Toast.LENGTH_SHORT);
                //当网络不好加载不了头像的时候
                if(flag==1){
                    SharedPreferences sharedPreferences= context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String userId=sharedPreferences.getString("user_id",null);
                    if(userId!=null){
                        editor.remove("user_id");
                    }else {
                        editor.remove("openid");
                        editor.remove("access_token");
                        editor.remove("refresh_token");
                    }
                    editor.apply();
                }
                break;
            case FAIL:
                new UtilsToast(context,"验证码错误或超时").show(Toast.LENGTH_SHORT);
                break;
            case SUCCESS:
                if(flag==0){
                    Intent intent=new Intent(context,activity);
                    context.startActivity(intent);
                    loginActivity.finish();
                    break;
                }
            default:break;
        }
    }

    private int GetUserData(BeanUserInfo beanUserInfo, String u_id) throws InterruptedException {
        //获取用户数据，所需参数机器码，随机字符串，用户ID，版本信息，需要MD5加密（true)。
//        Random r=new Random();  //生成随机数 r.nextInt(20)+1（随机数范围1到20）
        String version_information= BeanServerInfo.getInstance().getVersion_information();
        String UrlParam= "m_id="+ beanUserInfo.getM_id()+"&nonce_str="+getRandomString()+
                "&u_id="+u_id+"&version_information="+version_information;
        Log.w("TAG","get_userdata"+UrlParam);
        //true表示需要MD5加密
        MyOkhttp.SentDataUser(UrlParam,true, MyOkhttp.GET_USERDATA,"https://app.jiangxiatech.com/");
        for(int j=0;j<20;j++){
            Log.i("TAG","doInBackground+j:"+j);
            Thread.sleep(500);
            //用户头像和VIP权限都获取到了
            if(beanUserInfo.getHead_portrait()!=null&& beanUserInfo.getPermissions()!=0){
                Log.w("TAG", beanUserInfo.getHead_portrait());
                return SUCCESS;
            }
        }
        return OVERTIME;
    }
    //获取随机字符串(32位）
    private String getRandomString(){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuilder stringBuilder=new StringBuilder();  //可变字符串
        for(int i = 0; i< 32; i++){
            int index=random.nextInt(str.length());
            stringBuilder.append(str.charAt(index));
        }
        return stringBuilder.toString();
    }
}
