package com.jx.jzrecord.login;

import java.util.HashMap;

/**
 * @ 作者: LSY
 * @ 类名: ILogin
 * @ 包名: com.jiangxia.ilogin
 * @ 描述:手机登录，邮箱登录，查找用户数据的接口
 * @ 日期: 2020/9/3 15:28
 **/
public interface ILogin {
    void  GetUserData(final String Param, final boolean blHasSign); //得到用户数据
    void SentCode(final HashMap<String, String> UrlPara);    //发送验证码
    void ConfirmCode(final HashMap<String, String> UrlPara);  //验证验证码
}
