package com.jx.jzrecord.login;

import android.util.Log;

import java.util.HashMap;

/**
 * @ 作者: LSY
 * @ 类名: ImplEmail
 * @ 描述: 实现接口方法
 * @ 日期: 2020/9/1 16:20
 **/
public  class ImplEmail implements ILogin {

    @Override
    public void GetUserData(String Param, boolean blHasSign) {
        Log.w("TAG","Email_GetUserData");
        BeanUrlInfo url= BeanUrlInfo.getInstance();
        if(!(url.getUrlPart().equals("default"))) {
            MyOkhttp.SentDataUser(Param,blHasSign,url.getUrlPart(),url.getUrlBase());
        }else {
            MyOkhttp.SentDataUser(Param,blHasSign, MyOkhttp.GET_USERDATA,url.getUrlBase());
        }
    }

    @Override
    public void SentCode( HashMap<String,String> UrlPara ) {
        Log.w("TAG","Email_SentCode");
        BeanUrlInfo url= BeanUrlInfo.getInstance();
        if(!(url.getUrlPart().equals("default"))) {
            MyOkhttp.SentHttpData(UrlPara,url.getUrlPart(),url.getUrlBase());
        }else {
            MyOkhttp.SentHttpData(UrlPara, MyOkhttp.EMAIL_CODE,url.getUrlBase());
        }
    }

    @Override
    public void ConfirmCode( HashMap<String,String> UrlPara ) {
        Log.w("TAG","Email_ConfirmCode");
        BeanUrlInfo url= BeanUrlInfo.getInstance();
        if(!(url.getUrlPart().equals("default"))) {
            MyOkhttp.SentHttpData(UrlPara,url.getUrlPart(),url.getUrlBase());
        }else {
            MyOkhttp.SentHttpData(UrlPara, MyOkhttp.EMAIL_CONFIRM,url.getUrlBase());
        }
    }
}

