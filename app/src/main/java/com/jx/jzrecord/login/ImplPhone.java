package com.jx.jzrecord.login;

import android.util.Log;

import java.util.HashMap;

/**
 * @ 作者: LSY
 * @ 类名: ImplPhone
 * @ 描述: 实现接口方法
 * @ 日期: 2020/9/1 16:20
 **/
public class ImplPhone implements ILogin {
    @Override
    public void GetUserData(String Param, boolean blHasSign) {
        Log.w("TAG","Phone_GetUserData");
        BeanUrlInfo url= BeanUrlInfo.getInstance();
        if(!(url.getUrlPart().equals("default"))) {
            MyOkhttp.SentDataUser(Param,blHasSign,url.getUrlPart(),url.getUrlBase());
        }else {
            MyOkhttp.SentDataUser(Param,blHasSign, MyOkhttp.GET_USERDATA,url.getUrlBase());
        }
    }

    @Override
    public void SentCode( HashMap<String,String> UrlPara ) {
        Log.w("TAG","Phone_SentCode");
        BeanUrlInfo url= BeanUrlInfo.getInstance();
        Log.w("TAG",url.getUrlPart());
        Log.w("TAG",url.getUrlBase());
        if(!(url.getUrlPart().equals("default"))) {
            MyOkhttp.SentHttpData(UrlPara,url.getUrlPart(),url.getUrlBase());
        }else {
            MyOkhttp.SentHttpData(UrlPara, MyOkhttp.PHONE_CODE,url.getUrlBase());
        }
    }

    @Override
    public void ConfirmCode( HashMap<String,String> UrlPara ) {
        Log.w("TAG","Phone_ConfirmCode");
        BeanUrlInfo url= BeanUrlInfo.getInstance();
        if(!(url.getUrlPart().equals("default"))) {
            MyOkhttp.SentHttpData(UrlPara,url.getUrlPart(),url.getUrlBase());
        }else {
            MyOkhttp.SentHttpData(UrlPara, MyOkhttp.PHONE_CONFIRM,url.getUrlBase());
        }
    }
}
