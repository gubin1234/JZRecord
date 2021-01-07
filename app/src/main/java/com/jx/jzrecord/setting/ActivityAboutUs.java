package com.jx.jzrecord.setting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jx.jzrecord.ActivityHiddenPolicy;
import com.jx.jzrecord.ActivityUserAgree;
import com.jx.jzrecord.R;

public class ActivityAboutUs extends AppCompatActivity {
    private TextView tv_qq_url;
    private TextView tv_user_agreement;
    private LinearLayout btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        tv_qq_url=(TextView)findViewById(R.id.tv_qq_url);
        tv_qq_url.setText(getSpan());
        tv_qq_url.setTextColor(Color.parseColor("#999999"));
        tv_user_agreement=(TextView)findViewById(R.id.tv_user_agreement);
        tv_user_agreement.setText(getClickableSpan());
        //让超链接起作用
        tv_user_agreement.setMovementMethod(LinkMovementMethod.getInstance());
        btn_back=(LinearLayout) findViewById(R.id.btn_about_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 方法名：SpannableString
     * 自定义字符串
     * @return SpannableString(自定义字符串类型)
     */
    private SpannableString getSpan() {
        SpannableString spannableString = new SpannableString("用户交流群：891706875  官方网址：www.callmysoft.com");
        //设置文字的前景色
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1D")), 22, 40, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置字体大小（绝对值,单位：像素）,第二个参数boolean dip，如果为true，表示前面的字体大小单位为dip，否则为像素
        spannableString.setSpan(new AbsoluteSizeSpan(12, true), 0, 40, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * 方法名：SpannableString
     * 自定义字符串
     * @return SpannableString(自定义字符串类型)
     */
    private SpannableString getClickableSpan() {
        SpannableString spannableString = new SpannableString("隐私政策和用户协议");
        /*隐私政策*/
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1E")),0,4,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent_policy=new Intent(ActivityAboutUs.this, ActivityHiddenPolicy.class);

                startActivity(intent_policy);
            }
        },0,4,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        /*用户协议*/
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F75F1E")),5,9,Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent_user=new Intent(ActivityAboutUs.this, ActivityUserAgree.class);

                startActivity(intent_user);
            }
        },5,9,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableString;
    }
}