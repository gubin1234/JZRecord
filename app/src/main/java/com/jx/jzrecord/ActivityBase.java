package com.jx.jzrecord;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jx.jzrecord.login.BeanServerInfo;
import com.jx.jzrecord.wxapi.WxLogin;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.BuglyStrategy;
import com.tencent.bugly.beta.Beta;

import java.net.InetAddress;


/**
 * @ 作者: LSY
 * @ 类名: ActivityBase
 * @ 包名: com.jx.jzrecord
 * @ 描述:初始化信息：注册微信登录；注册Android异常上传
 * @ 日期: 2020/10/27 16:05
 **/
public class ActivityBase extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("TAGActivityBase","onCreate");
        WxLogin.initWx(getApplicationContext());        //注册微信登录
        initBugly();   //注册异常上传和版本更新
    }

    /**
     * 初始化腾讯bug管理平台
     */
    private void initBugly() {

        /* Beta高级设置*****/
        /*
         * true表示app启动自动初始化升级模块；
         * false不自动初始化
         * 开发者如果担心sdk初始化影响app启动速度，可以设置为false
         * 在后面某个时刻手动调用
         */
        Beta.autoInit = true;//设为false不会回调upgradeStateListener
            /*
             * true表示初始化时自动检查升级
             * false表示不会自动检查升级，需要手动调用Beta.checkUpgrade()方法
             */
        Beta.autoCheckUpgrade = true;

        /*
         * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度
         */
        Beta.initDelay = 3*1000;

        /*
         * 设置sd卡的Download为更新资源保存目录;
         * 后续更新资源会保存在此目录，需要在manifest中添加WRITE_EXTERNAL_STORAGE权限;
         */
        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /*
        *设置自定义弹窗布局
        */
        Beta.upgradeDialogLayoutId = R.layout.dialog_tips_upgrade;
        /**
         * 点击过确认的弹窗在APP下次启动自动检查更新时会再次显示;
         */
        BuglyStrategy strategy = new BuglyStrategy();
        strategy.setAppChannel(getChannelName(getApplicationContext()));
        strategy.setAppVersion(BeanServerInfo.getInstance().getVersionId());
        Bugly.init(getApplicationContext(), "dbc3f63ca4", false,strategy);
    }

    /**
     * 获取渠道名
     * @param context 此处习惯性的设置为activity，实际上context就可以
     * @return 如果没有获取成功，那么返回值为空
     */
    public static String getChannelName(Context context) {
        if (context == null) {
            return null;
        }
        String channelName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.
                        getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = String.valueOf(applicationInfo.metaData.get("UMENG_CHANNEL"));
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return channelName;
    }


}
