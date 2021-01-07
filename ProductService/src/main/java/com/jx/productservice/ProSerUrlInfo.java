package com.jx.productservice;

/**
 * @ 作者: LSY
 * @ 类名: ProSerUrlInfo
 * @ 包名: com.jx.ProductService
 * @ 描述:提供访问产品服务 服务器的URL
 * @ 日期: 2020/12/4 10:57
 **/
public class ProSerUrlInfo {

    private String UrlBase;  //服务器域名
    private String UrlPart;  //服务器接口

    private static ProSerUrlInfo INSTANCE = null;

    private ProSerUrlInfo() {
        UrlBase="http://app.jiangxiatech.com/";
        UrlPart="Index/UserLogin/DoLoadInfo";
    }

    //获取类的单实例
    public static ProSerUrlInfo getInstance(){
        if(INSTANCE == null){
            synchronized (ProSerUrlInfo.class) {
                if(INSTANCE == null){
                    INSTANCE = new ProSerUrlInfo();
                }
            }
        }
        return INSTANCE;
    }

    public String getUrlBase() {
        return UrlBase;
    }

    public void setUrlBase(String urlBase) {
        UrlBase = urlBase;
    }

    public String getUrlPart() {
        return UrlPart;
    }

    public void setUrlPart(String urlPart) {
        UrlPart = urlPart;
    }
}
