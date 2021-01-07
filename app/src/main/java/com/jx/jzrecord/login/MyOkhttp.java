package com.jx.jzrecord.login;


import android.text.TextUtils;
import android.util.Log;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @ 作者: LSY
 * @ 类名: MyOkhttp
 * @ 描述: 使用OKhttp的post方法访问公司服务器接口
 * @ 日期: 2020/9/1 16:30
 **/
public class MyOkhttp {
    //常量定义
    protected static final String PHONE_CODE = "Index/UserLogin/PhoneCode";
    protected static final String PHONE_CONFIRM = "Index/UserLogin/PhoneConfirm";
    protected static final String EMAIL_CODE = "Index/UserLogin/EmailCode";
    protected static final String EMAIL_CONFIRM = "Index/UserLogin/EmailConfirm";
    protected static final String GET_USERDATA = "Index/UserLogin/GetUserData";

    public static void SentDataUser(String Param, boolean blHasSign, final String UrlPart, final String UrlBase) {
        if (blHasSign) {
            String sign = Param + "&key=" + BeanServerInfo.getInstance().getKey();
            Log.w("TAG", sign);
            Log.w("TAG", stringToMD5(sign));
            String Param1 = Param + "&sign=" + stringToMD5(sign);
            MyOkhttp.GetUserData(Param1, UrlPart, UrlBase);
        } else {
            MyOkhttp.GetUserData(Param, UrlPart, UrlBase);
        }
    }

    /**
     * @ 作者: LSY
     * @ 方法名: SentHttpData，发送邮箱，手机验证
     * @ 描述: post方法向服务器端发送请求，UrlPara请求参数，UrlPart请求接口，UrlBase请求域名。
     * @ 参数: [UrlPara, UrlPart, UrlBase]
     * @ 返回值: void
     */
    public static void SentHttpData(final HashMap<String, String> UrlPara, final String UrlPart, final String UrlBase) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS).build();
                    //建立请求表单，添加上传服务器的参数
                    FormBody.Builder fbuilder = new FormBody.Builder();
                    //获取用户输入参数的键跟值
                    Set<String> keys = UrlPara.keySet();  //map.keySet()返回key的集合
                    for (String key : keys) {
                        fbuilder.add(key, Objects.requireNonNull(UrlPara.get(key)));
                        Log.w("TAGSentHttpData", key + ":" + UrlPara.get(key));   //map.get(key)返回key所对应的value值
                    }
                    RequestBody requestBody = fbuilder.build();
                    //发起请求
                    Request request = new Request.Builder().url(UrlBase + UrlPart).post(requestBody).build();
                    client.newCall(request).enqueue(new Callback(){
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String data = response.body().string();
                            if (!(UrlPart.equals(PHONE_CODE) || UrlPart.equals(EMAIL_CODE))) {
                                BeanUserInfo userData = BeanUserInfo.getInstance();
                                userData.setUserResponseData(data);
                            }
                            Log.w("TAG", "返回结果" + data);
                        }
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            if (e instanceof SocketTimeoutException) {
                                //判断超时异常
                                client.newCall(call.request()).enqueue(this);
                                Log.w("TAG", "超时异常");
                            }
                            if (e instanceof ConnectException) {
                                //判断连接异常，
                                client.newCall(call.request()).enqueue(this);
                                Log.w("TAG", "连接异常");
                            }
                        }
                    });
                } catch (Exception e) {
                    if (e instanceof JSONException) {
                        e.printStackTrace();
                    }
                    Log.w("TAG", "什么情况");
                    e.printStackTrace();
                }
            }
        }).start();

    }

         /* @ 描述:Get方法向服务器端发送请求，UrlPara请求参数，UrlPart请求接口，UrlBase请求域名。*/
//    public static void SentHttpData ( final String UrlPara,final String UrlPart,final String UrlBase){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
//                            .readTimeout(20, TimeUnit.SECONDS).build();
//                    Request request = new Request.Builder().url(UrlBase+UrlPart+"?"+UrlPara).build();
//                    Log.w("TAG",UrlBase+UrlPart+"?"+UrlPara);
//                    Response response = client.newCall(request).execute();
//                    if (response.isSuccessful()) {
//                        String HttpResponseData=response.body().string();
//                        BeanUserInfo userData=BeanUserInfo.getInstance();
//                        userData.setUserResponseData(HttpResponseData);
//                        JSONObject jsonObject=new JSONObject(HttpResponseData);
//                        int result_code=jsonObject.getInt("result_code");
//                        String u_id=jsonObject.getString("u_id");
//                        userData.setResult_code(result_code);
//                        userData.setU_id(u_id);
//                        Log.w("TAG","返回结果"+response.body().string());
//                    } else {
//                        throw new IOException("Unexpected code:" + response);
//                    }
//                }catch (Exception e){
//                    if (e instanceof UnknownHostException) {
//                        Log.w("TAG","没有网络");
//                    }
//                    if (e instanceof JSONException) {
//                        e.printStackTrace();
//                    }
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

    /**
     * @ 描述:Get方法请求后台获取用户信息，UrlPara请求参数，UrlPart请求接口，UrlBase请求域名。
     * @ 参数: [UrlPara, UrlPart, UrlBase]
     */
    public static void GetUserData(final String UrlPara, final String UrlPart, final String UrlBase) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS).build();
                    Request request = new Request.Builder().url(UrlBase + UrlPart + "?" + UrlPara).build();
                    Log.w("TAG", UrlBase + UrlPart + "?" + UrlPara);
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String userdata = Objects.requireNonNull(response.body()).string();
                        JSONObject jsonObject = new JSONObject(userdata);
                        int result_code = jsonObject.getInt("result_code");
                        //返回数据成功，保存数据
                        if (result_code == 1) {
                            ResultData(userdata);
                        }
                        Log.w("TAG", "返回结果" + userdata);
                    } else {
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (Exception e) {
                    if (e instanceof UnknownHostException) {
                        Log.w("TAG", "没有网络");
                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //将字符串转成MD5的值
    public static String stringToMD5(String value) {
        if (!TextUtils.isEmpty(value)) {
            byte[] hash;
            try {
                hash = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10)
                    hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            //toUpperCase()把字符串转换成大写
            return hex.toString().toUpperCase();
        }
        return null;
    }

    private static void ResultData(String value) {
        try {
            JSONObject json = new JSONObject(value);
            BeanUserInfo data = BeanUserInfo.getInstance();
            data.setResult_code(json.getInt("result_code"));
            data.setPermissions_describe(json.getString("permissions_describe"));
            data.setPermissions(json.getInt("permissions"));
            data.setLogin_method(json.getInt("login_method"));
            data.setStart_date(json.getString("start_date"));
            data.setPackage_validity(json.getString("package_validity"));
            data.setU_name(json.getString("u_name"));
            data.setHead_portrait(json.getString("head_portrait"));
            data.setP_type(json.getInt("p_type"));
            data.setDays_left(json.getInt("days_left"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
