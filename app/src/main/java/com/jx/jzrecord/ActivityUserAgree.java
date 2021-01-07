package com.jx.jzrecord;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;


public class ActivityUserAgree extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agree);
        LinearLayout user_agree_back=(LinearLayout) findViewById(R.id.user_agree_back);
        user_agree_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();   //退出当前界面
            }
        });
    }

}