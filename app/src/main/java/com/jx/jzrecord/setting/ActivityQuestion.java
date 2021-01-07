package com.jx.jzrecord.setting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.jx.jzrecord.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityQuestion extends AppCompatActivity {
    private LinearLayout btn_back;//返回按钮
    private String[] question={"为什么无法录制内部声音？","在录制过程中遇到卡顿问题，如何处理？","如何开启悬浮窗？","录制时怎么隐藏悬浮窗？","关闭软件后，通知栏仍显示软件？","授权给金舟录屏大师安全吗？"};
    private String[] answer={"安卓不允许应用录制系统声音（内部声音），请打开扬声器",
                             "卡顿问题受设备配置、当前录制的应用及录制设置影响。内存" +
                             "及CPU使用率过高时,就可能发生卡顿。你可以在设置" +
                             "页面调整视频分辨率、码率及帧率选项,以改善卡顿问题。我" +
                             "们建议你在遇到卡顿问题时降低分辨率,码率及帧率均使用"+
                             "“自动”选项。另外,请在录制时尽可能关闭后台运行的应用。",
                             "可在主页中打开“悬浮窗”选项",
                             "可在设置中开启“录制时隐藏悬浮窗”功能",
                             "打开手机的“设置”，找到“通知管理”，进入后找到“金舟录屏大师”重新开启通知栏即可",
                             "金舟录屏大师已通过各大安全厂商安全认证检测，请放心使用"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        btn_back=(LinearLayout)findViewById(R.id.btn_question_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        requestData();
    }
    private void requestData(){
        ArrayList<HashMap<String, String>> datas = new ArrayList<HashMap<String,String>>();
        for(int i = 0; i <= 5; i++){
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("question",question[i]);
            item.put("answer",answer[i]);
            datas.add(item);
        }
        ListView lvProduct = (ListView) findViewById(R.id.lv_question);
        OneExpandAdapter adapter = new OneExpandAdapter(this, datas);
        lvProduct.setAdapter(adapter);
    }
}