package com.jx.productservice;

import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @ 作者: LSY
 * @ 类名: ProServiceOkHttp
 * @ 包名: com.jx.productservice
 * @ 描述:使用OKhttp的post方法访问公司服务器产品接口（Get方法）
 * @ 日期: 2020/12/4 10:50
 **/


public class ProServiceOkHttp {

    private static final String SUCCESS="1000";
    private static final String TAG="ProServiceOkHttp";
    private static final String ERRNO="errno";   //返回结果
    private static final String ERRMSG="errmsg";  //错误信息
    //是否MD5加密
   public static String isMD5(String UrlPara,String key){
           String sign = UrlPara + "&key=" + key;
           Log.w(TAG, sign);
           Log.w(TAG, stringToMD5(sign));
           Log.w("ProServiceOkHttp", UrlPara);
           return UrlPara + "&sign=" + stringToMD5(sign);
   }

    /**
     * @ 描述:Get方法请求后台获取用户信息，UrlPara请求参数，UrlPart请求接口，UrlBase请求域名。
     * @ 参数: [UrlPara, UrlPart, UrlBase]
     */
    public static void GetProServiceData(final String UrlPara, final String UrlPart, final String UrlBase) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                     OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS)
                            .readTimeout(2, TimeUnit.SECONDS).build();
                    Request request = new Request.Builder().url(UrlBase + UrlPart + "?" + UrlPara).build();
                    Log.w(TAG, UrlBase + UrlPart + "?" + UrlPara);
                    final ProServiceInfo proServiceInfo=ProServiceInfo.getInstance();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            if (e instanceof SocketTimeoutException) {
                                //判断超时异常
                                proServiceInfo.setCallBackMsg("超时异常");
                                Log.w(TAG, "超时异常");
                            }

                            if (e instanceof ConnectException) {
                                //判断连接异常，
                                proServiceInfo.setCallBackMsg("连接异常");
                                Log.w(TAG, "连接异常");
                            }

                            if (e instanceof UnknownHostException) {
                                proServiceInfo.setCallBackMsg("没有网络");
                                Log.w(TAG, "没有网络");
                            }
                        }
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String data = response.body().string();
                            Log.w(TAG,data);
                            Parsingdata(data); //解析数据
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private static void Parsingdata(String str){
        try {
            JSONObject json = new JSONObject(str);
            ProServiceInfo proServiceInfo=ProServiceInfo.getInstance();
            String errno=json.getString(ERRNO);
            if(!errno.equals(SUCCESS)){
                String errmsg=json.getString(ERRMSG);
                proServiceInfo.setCallBackMsg(errmsg);
                Log.w(TAG,errmsg);
            }else {
                JSONObject res=json.getJSONObject("res");
//                String OlVersion=res.getString("OlVersion");
                proServiceInfo.setM_szUpdateMemo(res.getString("UpdateMemo"));
                proServiceInfo.setM_szOnTrial(res.getString("OnTrial"));   //是否试用
                proServiceInfo.setM_szOnTrialDays(res.getString("onTrialDays"));  //试用天数
                String LoadUrl=res.getString("LoadUrl");
                if(LoadUrl!=null){
                    //将string的url字符串转换为map存储并取出来设值
                    HashMap<String,String> map = new HashMap<>();
                    String [] KeyValuePair = LoadUrl.split(";");//将所有;符号截取出来变成一个数组
                    String KeyAndValue ;//获取每个;之内的内容
                    String key ;
                    String value ;
                    for (String result : KeyValuePair) {
                        KeyAndValue = result;//获取每个;分割的字符串
                        //获取每个：分割的字符串
                        for (int j = 0; j < KeyAndValue.length(); j++) {
                            if (KeyAndValue.charAt(j) == ':') {
                                key = KeyAndValue.substring(0, j);
                                if (j + 1 != KeyAndValue.length()) {
                                    value = KeyAndValue.substring(j + 1, KeyAndValue.length());
                                }else {
                                    value=null;
                                }
                                map.put(key, value);//将值放入map中
                                break;
                            }
                        }
                    }
                    proServiceInfo.setM_keyData(map);
                    Log.w(TAG, String.valueOf(res));
                    Log.w(TAG, map.toString());
                }
                proServiceInfo.setCallBackMsg("请求成功");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //将字符串转成MD5的值
    private static String stringToMD5(String value) {
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

}
