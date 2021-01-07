package com.jx.jzrecord.login;

import com.jx.productservice.ProServiceInfo;

import java.util.HashMap;

/**
 * @ 作者: LSY
 * @ 类名: BeanUserInfo
 * @ 包名: com.jiangxia.ilogin
 * @ 描述:赖汉式单例模式  UserDataBean用来存储用户数据
 * @ 日期: 2020/9/3 15:22
 **/
public class BeanUserInfo {
    private String u_id;    //用户ID
    private int result_code;    //状态码，1登录成功，-1登录失败
    // 权限状态描述,ok(正常使用)，no_now_machine_code(无可使用的机器码用于授权此机器)，package_expired(套餐已到期)",
    // no_package(没有购买过套餐),no_trial(试用已到期)
    private String permissions_describe;
    private int permissions; //vip权限1有，-1没有
    private int login_method;//登录方式 1.微信登录 2.QQ登录  3.手机短信登录
    private String start_date; //购买日期
    private String package_validity; //到期日期
    private String u_name;   //用户昵称
    private String head_portrait;   //用户头像地址
    private int p_type;  //套餐类型，0无可用套餐，1试用，2非永久套餐，3永久套餐 ，4普通套餐包，5全家桶
    private int days_left; //用户套餐的剩余天数，0为已过期或者无套餐，1-35999为剩余天数，36000为永久有效
    private String UserResponseData;  //Http返回的用户数据 ，一般为JSON字符串
    private String m_id;     //手机机器码
    private HashMap<String,String> UrlParam;
    private static BeanUserInfo INSTANCE = null;

    //私有化构造子,阻止外部直接实例化对象
    private BeanUserInfo() {
    }
    //获取类的单实例
    public static BeanUserInfo getInstance(){
        if(INSTANCE == null){
            synchronized (BeanUserInfo.class) {
                if(INSTANCE == null){
                    INSTANCE = new BeanUserInfo();
                }
            }
        }
        return INSTANCE;
    }



    /**---各变量get方法----*/
    public String getU_id() {
        return u_id;
    }

    public int getResult_code() {
        return result_code;
    }

    public String getPermissions_describe() {
        return permissions_describe;
    }

    public int getPermissions() {
        return permissions;
    }

    public int getLogin_method() {
        return login_method;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getPackage_validity() {
        return package_validity;
    }

    public String getU_name() {
        return u_name;
    }

    public String getHead_portrait() {
        return head_portrait;
    }

    public int getP_type() {
        return p_type;
    }

    public int getDays_left() {
        return days_left;
    }

    public String getM_id() {
        return m_id;
    }

    public HashMap<String, String> getUrlParam() {
        return UrlParam;
    }
    public String getUserResponseData() {
        return UserResponseData;
    }

    /**---各变量set方法----*/
    public void setU_id(String u_id) {
        this.u_id = u_id;
    }

    public void setResult_code(int result_code) {
        this.result_code = result_code;
    }

    public void setPermissions_describe(String permissions_describe) {
        this.permissions_describe = permissions_describe;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public void setLogin_method(int login_method) {
        this.login_method = login_method;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public void setPackage_validity(String package_validity) {
        this.package_validity = package_validity;
    }

    public void setU_name(String u_name) {
        this.u_name = u_name;
    }

    public void setHead_portrait(String head_portrait) {
        this.head_portrait = head_portrait;
    }

    public void setP_type(int p_type) {
        this.p_type = p_type;
    }

    public void setDays_left(int days_left) {
        this.days_left = days_left;
    }


    public void setUserResponseData(String userResponseData) {
        UserResponseData = userResponseData;
    }

    public void setM_id(String m_id) {
        this.m_id = m_id;
    }

    public void setUrlParam(HashMap<String, String> urlParam) {
        UrlParam = urlParam;
    }
}
