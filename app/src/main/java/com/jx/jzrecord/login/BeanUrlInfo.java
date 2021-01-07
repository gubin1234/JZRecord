package com.jx.jzrecord.login;

/**
 * @ 作者: LSY
 * @ 类名: BeanUrlInfo
 * @ 包名: com.jiangxia.ilogin
 * @ 描述:记录访问服务器URL的类
 * @ 日期: 2020/9/3 15:23
 **/
public class BeanUrlInfo {
    private String UrlBase; //服务器域名
    private String UrlPart;  //服务器接口

    private static BeanUrlInfo INSTANCE = null;



    //私有化构造子,阻止外部直接实例化对象
    private BeanUrlInfo() {
        UrlBase = "https://app.jiangxiatech.com/";
        UrlPart="default";
    }

    //获取类的单实例
    public static BeanUrlInfo getInstance(){
        if(INSTANCE == null){
            synchronized (BeanUrlInfo.class) {
                if(INSTANCE == null){
                    INSTANCE = new BeanUrlInfo();
                }
            }
        }
        return INSTANCE;
    }

    public String getUrlBase() {
        return UrlBase;
    }

    public String getUrlPart() {
        return UrlPart;
    }


    public void setUrlBase(String urlBase) {
        UrlBase = urlBase;
    }

    public void setUrlPart(String urlPart) {
        UrlPart = urlPart;
    }
}
