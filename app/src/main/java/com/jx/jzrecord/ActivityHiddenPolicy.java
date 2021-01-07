package com.jx.jzrecord;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;



public class ActivityHiddenPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_policy);
        LinearLayout hidden_policy_back=(LinearLayout)findViewById(R.id.hidden_policy_back);
        hidden_policy_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();   //结束当前页面
            }
        });
    }

}