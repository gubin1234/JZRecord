package com.jx.jzrecord.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.jx.jzrecord.R;
import com.jx.jzrecord.login.BeanUserInfo;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.utils.UtilsToast;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;


/**
 * @ 作者: LSY
 * @ 类名: WXEntryActivity
 * @ 包名: com.jiangxia.screen.wxapi
 * @ 描述:微信数据回调接收类
 * @ 日期: 2020/9/10 10:30
 **/
//微信会把数据包装在Intent中传入这个"WXEntryActivity"中
//
//        我们不需要自己处理Intent,
//
//        只需要在这个activity中调用api.handleIntent(Intent intent, IWXAPIEventHandler i),
//        这个方法会自动调用 IWXAPIEventHandler 对象的 onResp(BaseResp resp) 方法
//        我们只需要把处理回调的逻辑写进onResp(BaseResp resp) 方法中即可!
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI mWeixinAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxentry);
        //接收到分享以及登录的intent传递handleIntent方法，处理结果
        mWeixinAPI = WXAPIFactory.createWXAPI(this, WxData.WEIXIN_APP_ID, true);
        mWeixinAPI.handleIntent(this.getIntent(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("TAG","WXEntryActivityDestroy");
    }

    //    为了防止这个Activity处于栈顶的时候微信回调我们
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mWeixinAPI.handleIntent(intent, this);//必须调用此句话
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.e("-----", "onReq: " + baseReq);
        finish();
    }

    //在这个方法中处理微信传回的数据
    @Override
    public void onResp(BaseResp baseResp) {
        Log.w("TAGonResp", "errStr: " + baseResp.errStr);
        Log.w("TAGonResp", "openId: " + baseResp.openId);
        Log.w("TAGonResp", "transaction: " + baseResp.transaction);
        Log.w("TAGonResp", "errCode: " + baseResp.errCode);
        Log.w("TAGonResp", "getType: " + baseResp.getType());
        Log.w("TAGonResp", "checkArgs: " + baseResp.checkArgs());
        switch (baseResp.errCode) {
            //ERR_OK = 0(用户同意) ERR_AUTH_DENIED = -4（用户拒绝授权） ERR_USER_CANCEL = -2（用户取消）
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                new UtilsToast(getApplicationContext(),"登录失败").show(Toast.LENGTH_SHORT);
                finish();
                break;
            case BaseResp.ErrCode.ERR_OK:
                        //拿到了微信返回的code,立马再去请求access_token
                        String code = ((SendAuth.Resp) baseResp).code;
                Log.w("TAGonResp", "code: " + code);
                        getAccess_token(code);
                        //就在这个地方，用网络库什么的或者自己封的网络api，发请求去咯，注意是get请求
                        break;
            default:
                finish();
                break;
        }
    }

    /**
     * 用code请求吗获取openid（普通用户唯一标识）  accessToken（接口调用凭证）值用于后期操作
     * @param code 请求码
     */
    private void getAccess_token(final String code) {
        if(code==null) return;
        final String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                + WxData.WEIXIN_APP_ID
                + "&secret="
                + WxData.APP_SECRET
                + "&code="
                + code
                + "&grant_type=authorization_code";
        OkHttpUtils.get().url(path).build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                new UtilsToast(getApplicationContext(),"网络不稳定，请检查网络").show(Toast.LENGTH_SHORT);
                finish();
                Log.w("TAGgetAccess_token", "getAccess_token:onError");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.w("TAGgetAccess_token", "onResponse: " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String openid = jsonObject.getString("openid").trim();
                    String access_token = jsonObject.getString("access_token").trim();
                    String refresh_token = jsonObject.getString("refresh_token").trim();
                    //数据库保存数据
                    SharedPreferences sharedPreferences= getSharedPreferences("userdata", Context.MODE_PRIVATE);  //获取数据库对象
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("openid",openid);            //用户ID
                    editor.putString("access_token",access_token); //接口调用凭证
                    editor.putString("refresh_token",refresh_token); //用户刷新 access_token
                    editor.apply();
                    getUserMesg(access_token, openid,0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 获取微信的个人信息
     * @param access_token
     * @param openid
     * param count  为0跳转到主页，为1不跳转到主页
     */
    public void getUserMesg(final String access_token, final String openid,final int count) {
        final String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
                + access_token
                + "&openid="
                + openid;
        OkHttpUtils.get().url(path).build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                new UtilsToast(getApplicationContext(),"网络不稳定，请检查网络").show(Toast.LENGTH_SHORT);
                Log.e("TAGgetUserMesg", "getUserMesg:onError");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.w("TAGonResponse", "全部数据: " + response);
                if(response==null) return;
                JSONObject jsonObject = null;
                try {
                    BeanUserInfo WxData=BeanUserInfo.getInstance();
                    jsonObject = new JSONObject(response);
                    //普通用户昵称
                    String nickname = jsonObject.getString("nickname");
                    //普通用户性别，1 为男性，2 为女性
                    int sex = Integer.parseInt(jsonObject.get("sex").toString());
                    //用户头像，最后一个数值代表正方形头像大小（有 0、46、64、96、132 数值可选，0 代表 640*640 正方形头像），用户没有头像时该项为空
                    String headimgurl = jsonObject.getString("headimgurl");
                    //普通用户的标识，对当前开发者帐号唯一
                    String openid1 = jsonObject.getString("openid");
                    WxData.setHead_portrait(headimgurl);
                    WxData.setU_name(nickname);
                    if(count==0){
                        startActivity(new Intent(WXEntryActivity.this, ActivityMain.class));
                    }
                    finish();
                    Log.w("TAGonResponse", "用户基本信息:");
                    Log.w("TAGonResponse", "openid:" + openid1);
                    Log.w("TAGonResponse", "nickname:" + nickname);
                    Log.w("TAGonResponse", "sex:       " + sex);
                    Log.w("TAGonResponse", "headimgurl:" + headimgurl);
                } catch (JSONException e) {
                    e.printStackTrace();
                    new UtilsToast(getApplicationContext(),"登陆错误,请重新再试").show(Toast.LENGTH_SHORT);
                }
            }
        });
    }
}
