package com.jx.productservice;

import java.util.HashMap;
import java.util.Map;

/**
 * @ 作者: LSY
 * @ 类名: ProServiceInfo
 * @ 包名: com.jx.productservice
 * @ 描述:产品服务信息接口,提供产品服务信息的数据以及功能
 * @ 日期: 2020/12/4 9:59
 **/
public class ProServiceInfo {

    private String m_szOlVersion;   //服务器版本,默认为产品编译时的版本号
    private String ProductId;   //产品ID
    private String ChannelId;  //渠道号
    private String Key;  //密钥

    //是否允许试用,默认:0
    private String  m_szOnTrial;

    //试用天数,默认:0
    private String m_szOnTrialDays;

    //QQ,默认:207281357
    private String m_szQQ;

    //是否需要登陆才能使用,默认:1  0:功能完全开放,不需要登陆,1:账户需要登陆才能进行功能转换(优先级高)
    private String m_szDologin;

    //产品官网:https://www.callmysoft.com/jiami
    private String  m_szUrl;

    //更新说明,默认:空
    private String m_szUpdateMemo;

    //手机,默认:17725738221
    private String m_szPhone;

    //ret字段,目前用于忘记密码的业务逻辑,1.等于null或者为空则弹出忘记密码的UI对话框 2.否则调用ShellExecute打开界面,默认为:空
    private String m_szRetValue;

    //广告值,默认为为空,1表示需要启动广告
    private String m_szAdvertise;

    //客服的url,默认:"https://jxxxkjhzyxgs.qiyukf.com/client?k=da145c49c514d71e9f8b7230cd95cf52&wp=1&robotShuntSwitch=1&robotId=3437939"
    private String m_szKFUrl;

    //动态字段,key是动态,value也是后台动态设置
    private HashMap<String ,String > m_keyData;

    //访问服务器回调的信息
    private String CallBackMsg;


    private static ProServiceInfo INSTANCE = null;

    private ProServiceInfo() {
        m_szOlVersion="131";  //版本号
        ProductId="535"; //产品ID
        ChannelId="100";   //渠道号
        Key="zw94PyHTak6U3oHuMOe"; //密钥
        CallBackMsg=null;

        m_szOnTrial="0";     //是否允许试用
        m_szOnTrialDays="0";  //试用天数
        m_szQQ="207281357";  //qq号
        m_szDologin="1";
        m_szUrl=null;      //产品官网
        m_szUpdateMemo=null;  //更新说明
        m_szPhone="17725738221";   //手机
        m_szRetValue=null;
        m_szAdvertise=null;   //广告值
        m_szKFUrl="https://jxxxkjhzyxgs.qiyukf.com/client?k=da145c49c514d71e9f8b7230cd95cf52&wp=1&robotShuntSwitch=1&robotId=3437939";  //客服的url
        m_keyData=null;
    }

    //获取类的单实例
    public static ProServiceInfo getInstance(){
        if(INSTANCE == null){
            synchronized (ProServiceInfo.class) {
                if(INSTANCE == null){
                    INSTANCE = new ProServiceInfo();
                }
            }
        }
        return INSTANCE;
    }

    public String getM_szOlVersion() {
        return m_szOlVersion;
    }

    public void setM_szOlVersion(String m_szOlVersion) {
        this.m_szOlVersion = m_szOlVersion;
    }

    public String getM_szOnTrial() {
        return m_szOnTrial;
    }

    public void setM_szOnTrial(String m_szOnTrial) {
        this.m_szOnTrial = m_szOnTrial;
    }

    public String getM_szOnTrialDays() {
        return m_szOnTrialDays;
    }

    public void setM_szOnTrialDays(String m_szOnTrialDays) {
        this.m_szOnTrialDays = m_szOnTrialDays;
    }

    public String getM_szQQ() {
        return m_szQQ;
    }

    public void setM_szQQ(String m_szQQ) {
        this.m_szQQ = m_szQQ;
    }

    public String getM_szDologin() {
        return m_szDologin;
    }

    public void setM_szDologin(String m_szDologin) {
        this.m_szDologin = m_szDologin;
    }

    public String getM_szUrl() {
        return m_szUrl;
    }

    public void setM_szUrl(String m_szUrl) {
        this.m_szUrl = m_szUrl;
    }

    public String getM_szUpdateMemo() {
        return m_szUpdateMemo;
    }

    public void setM_szUpdateMemo(String m_szUpdateMemo) {
        this.m_szUpdateMemo = m_szUpdateMemo;
    }

    public String getM_szPhone() {
        return m_szPhone;
    }

    public void setM_szPhone(String m_szPhone) {
        this.m_szPhone = m_szPhone;
    }

    public String getM_szRetValue() {
        return m_szRetValue;
    }

    public void setM_szRetValue(String m_szRetValue) {
        this.m_szRetValue = m_szRetValue;
    }

    public String getM_szAdvertise() {
        return m_szAdvertise;
    }

    public void setM_szAdvertise(String m_szAdvertise) {
        this.m_szAdvertise = m_szAdvertise;
    }

    public String getM_szKFUrl() {
        return m_szKFUrl;
    }

    public void setM_szKFUrl(String m_szKFUrl) {
        this.m_szKFUrl = m_szKFUrl;
    }

    public Map<String, String> getM_keyData() {
        return m_keyData;
    }

    public void setM_keyData(HashMap<String, String> m_keyData) {
        this.m_keyData = m_keyData;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getChannelId() {
        return ChannelId;
    }

    public void setChannelId(String channelId) {
        ChannelId = channelId;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getCallBackMsg() {
        return CallBackMsg;
    }

    public void setCallBackMsg(String callBackMsg) {
        CallBackMsg = callBackMsg;
    }

    public String getVersion_information() {
        return ProductId+m_szOlVersion+ChannelId;
    }
}
