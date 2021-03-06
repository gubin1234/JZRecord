package com.jx.jzrecord.wxapi;

/**
 * XINHAO_HAN存储信息
 */

public class WxData {

    /**
     * 你的AppID
     *
     * 在此特别注意,本Demo只适用微信的登陆功能,并不能用到微信支付功能
     *      *
     * 在此注意,必须要和微信官网签名一致,否则调用不起来微信APP,
     *
     * 如果在你调用出错的情况下(微信APP死活不出来的情况下),请参阅作者简书网址 : https://www.jianshu.com/p/04ed0b65f3df
     *
     * 微信APP掉不出来:有以下原因:
     *
     * 1.签名不正确(APK所使用签名的MD5码) 签名MD5码不要有 : 如A0:5B:12:63.... ,要全部是小写(推荐)a05b1263...,这种形式的
     *
     * 2.APP_ID不正确
     *
     * 3.密匙不正确
     *
     * 4.包名不正确
     *
     * 不走回调WXEntryActivity
     *
     * 1.AndroidManifest.xml里没有配置
     *
     * <activity
     *       android:name=".wxapi.WXEntryActivity"
     *       android:exported="true"
     *       android:label="WXEntryActivity" />
     * 2.必须是   你的包名.wxapi
     *   如果你的所有信息填写正确,还是掉不起来微信,那就可能是微信服务器问题,请稍等3-5分钟,在尝试调用,否则就重复以上选项
     *   大兄弟,记得细心一点哟~~~
     *
     *
     *
     *
     */
    public static final String WEIXIN_APP_ID = "wx0408d4bbe9f17af0";  //这个APP_ID就是注册APP的时候生成的
    public static final String APP_SECRET = "f14aabd607e63bf3e25d32f57d4419c1";

    /**
     * 固定的
     *
     */

    public static final String SCOPE = "snsapi_userinfo";
    public static final String STATE = "wechat_sdk_demo_test_neng";


}
