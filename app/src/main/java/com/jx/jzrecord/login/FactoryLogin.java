package com.jx.jzrecord.login;

/**
 * @ 作者: LSY
 * @ 类名: FactoryLogin
 * @ 包名: com.jiangxia.ilogin
 * @ 描述:工厂，根据参数类型生成相应对象
 * @ 日期: 2020/9/3 15:32
 **/
public class FactoryLogin {
    public static ILogin createApi(String type){
        ILogin api=null;
        if(type.equals("Phone")){
            api = new ImplPhone();
        }else if(type.equals("Email")){
            api = new ImplEmail();
        }
        return api;
    }
}

