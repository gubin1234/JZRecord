package com.jx.jzrecord.setting;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jx.jzrecord.R;
import com.jx.jzrecord.utils.UtilsPermission;

public class ActivityPermissionSet extends AppCompatActivity {
    private TextView tv_window;
    private TextView tv_storage;
    private TextView tv_status;
    private TextView tv_audio;
    private LinearLayout btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_set);
        tv_window=(TextView)findViewById(R.id.en_window);
        tv_storage=(TextView)findViewById(R.id.en_storage);
        tv_status=(TextView)findViewById(R.id.status_and_identity);
        tv_audio=(TextView)findViewById(R.id.en_audio);
        btn_back=(LinearLayout) findViewById(R.id.btn_permission_back);
        //返回
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //是否开启悬浮窗
        if(UtilsPermission.commonROMPermissionCheck(this)){
            tv_window.setText("已允许");
            tv_window.setTextColor(Color.parseColor("#0AA444"));
        }
        else {
            tv_window.setText("已拒绝");
            tv_window.setTextColor(Color.parseColor("#FF5444"));
        }
        //是否允许录音存储
        if(UtilsPermission.hasPermissions(this,"AUDIO_PERMISSION"))
        {
            tv_audio.setText("已允许");
            tv_audio.setTextColor(Color.parseColor("#0AA444"));
        }
        else {
            tv_audio.setText("已拒绝");
            tv_audio.setTextColor(Color.parseColor("#FF5444"));
        }
        //是否允许读取内部存储
        if(UtilsPermission.hasPermissions(this,"WRITE_PERMISSION"))
        {
            tv_storage.setText("已允许");
            tv_storage.setTextColor(Color.parseColor("#0AA444"));
        }
        else {
            tv_storage.setText("已拒绝");
            tv_storage.setTextColor(Color.parseColor("#FF5444"));
        }
        //是否允许手机电话权限
        if(UtilsPermission.hasPermissions(this,"PHONE_PERMISSION"))
        {
            tv_status.setText("已允许");
            tv_status.setTextColor(Color.parseColor("#0AA444"));
        }
        else {
            tv_status.setText("已拒绝");
            tv_status.setTextColor(Color.parseColor("#FF5444"));
        }
    }
}