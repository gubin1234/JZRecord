package com.jx.jzrecord.login;

import com.jx.productservice.ProServiceInfo;

/**
 * @ 作者: LSY
 * @ 类名: BeanServerInfo
 * @ 包名: com.jiangxia.ilogin
 * @ 描述:记录产品信息的类
 * @ 日期: 2020/9/3 15:27
 **/
public class BeanServerInfo {
    private String ProductId;   //产品ID
    private String VersionId;  //版本号
    private String ChannelId;  //渠道号
    private String Key;

    private static BeanServerInfo INSTANCE = null;



    private BeanServerInfo() {
        ProServiceInfo proServiceInfo=ProServiceInfo.getInstance();
        ProductId = proServiceInfo.getProductId();
        VersionId = proServiceInfo.getM_szOlVersion();
        ChannelId = proServiceInfo.getChannelId();
        Key=proServiceInfo.getKey();
    }

    //获取类的单实例
    public static BeanServerInfo getInstance(){
        if(INSTANCE == null){
            synchronized (BeanServerInfo.class) {
                if(INSTANCE == null){
                    INSTANCE = new BeanServerInfo();
                }
            }
        }
        return INSTANCE;
    }

    /**Get方法****/
    public String getProductId() {
        return ProductId;
    }

    public String getVersionId() {
        return VersionId;
    }

    public String getChannelId() {
        return ChannelId;
    }

    public String getKey() {
        return Key;
    }

    /**Set方法****/
    public void setProductId(String productId) {
        ProductId = productId;
    }

    public void setVersionId(String versionId) {
        VersionId = versionId;
    }

    public void setChannelId(String channelId) {
        ChannelId = channelId;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getVersion_information() {
        return ProductId+VersionId+ChannelId;
    }


}
