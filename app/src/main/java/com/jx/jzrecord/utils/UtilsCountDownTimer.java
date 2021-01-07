package com.jx.jzrecord.utils;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;

import androidx.core.content.res.ResourcesCompat;

import com.jx.jzrecord.R;
import com.jx.jzrecord.login.ActivityLogin;
import com.jx.jzrecord.window.FloatWindowManager;


/**
 * @ 作者： LSY
 * @ 类名: UtilsCountDownTimer
 * @ 包名: com.jiangxia.screen
 * @ 描述: 验证码倒计时 重新验证
 * @ 日期: 2020.9.1
 **/
public class UtilsCountDownTimer extends CountDownTimer {
    private Button mButton; //验证码按钮
    private String btn_text;
    /**
     * @ 描述: mButton:验证码按钮  millisInFuture：倒计时多少毫秒数（毫秒）  countDownInterval：间隔时间（毫秒）
     * @ 参数: [mButton, millisInFuture, countDownInterval]
     */
    public UtilsCountDownTimer(Button mButton, long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.mButton=mButton;
    }

    /**
     * @ 作者: LSY
     * @ 方法名: onTick
     * @ 描述: 回调函数，每隔countDownInterval毫秒执行一次。 millisUntilFinished：当前倒计时毫秒数。
     * @ 参数: [millisUntilFinished]
     * @ 返回值: void
     */
    @Override
    public void onTick(long millisUntilFinished) {
            Log.w("TAG1111","1"+millisUntilFinished);
            mButton.setClickable(false); //设置不可点击
            btn_text=millisUntilFinished / 1000+ "秒后可重新发送";
            mButton.setBackgroundResource(R.drawable.codeview);//设置按钮为灰色，这时是不能点击的
            SpannableString spannableString=new SpannableString(btn_text);  //获取按钮上的文字
            ForegroundColorSpan span=new ForegroundColorSpan(Color.RED);  //设置文字颜色
            spannableString.setSpan(span,0,3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); //包含开始位置，不包含结束位置，将倒计时的时间设置为红色。
            mButton.setText(spannableString);
    }

    /**
     * @ 作者: LSY
     * @ 方法名: onFinish
     * @ 描述:倒计时结束执行该方法：改变按钮名字，设置按钮状态为可点击，设置按钮背景颜色，设置按钮状态为未发生状态
     */
    @Override
    public void onFinish() {
        Log.w("TAG","onFinish");
        mButton.setText("重新获取验证码");
        mButton.setClickable(true);
        mButton.setBackgroundResource(R.drawable.btnview);//设置按钮为橙色,可点击
        ActivityLogin.issent=false;
    }
}
