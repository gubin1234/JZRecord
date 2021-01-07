package com.jx.jzrecord.setting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.jx.jzrecord.R;

public class ActivityTutorial extends AppCompatActivity {
    private LinearLayout btn_back_tutorial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        btn_back_tutorial=findViewById(R.id.btn_tutorial_back);
        btn_back_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}