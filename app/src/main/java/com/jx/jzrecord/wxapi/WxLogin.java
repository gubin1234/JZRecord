package com.jx.jzrecord.wxapi;

import android.content.Context;
import android.widget.Toast;

import com.jx.jzrecord.utils.UtilsToast;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by 14178 on 2018/1/19.
 */

public class WxLogin {

    public static IWXAPI api;
    public  static Context mContext;

    /**
     * 微信的初始化
     */
    public static void initWx(Context context) {
        mContext = context;
        api = WXAPIFactory.createWXAPI(context, WxData.WEIXIN_APP_ID, true);
        api.registerApp(WxData.WEIXIN_APP_ID);
        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
        // 将该app注册到微信
        msgApi.registerApp(WxData.WEIXIN_APP_ID);

    }
    public static boolean longWx() {
        if (!api.isWXAppInstalled()) {
            new UtilsToast(mContext,"您还未安装微信客户端").show(Toast.LENGTH_SHORT);
            Toast.makeText(mContext, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
            return false;
        }
        //应用授权登录接入代码
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = WxData.SCOPE;
        req.state = WxData.STATE;
        api.sendReq(req);
        return true;
    }


}
