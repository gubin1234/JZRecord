package com.jx.jzrecord.window;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.jx.jzrecord.R;

//倒计时布局对象
public class CountDownView extends LinearLayout  {

    /**
     * 记录倒计时窗口的宽度
     */
    public static int viewWidth;

    /**
     * 记录倒计时窗口的高度
     */
    public static int viewHeight;

    @RequiresApi(api = Build.VERSION_CODES.O)  //为了能使用API26的方法

    public CountDownView(final Context context ) {
        super(context);
                //将布局R.layout.float_window_small添加到当前界面
                LayoutInflater.from(context).inflate(R.layout.dialog_count_down, this);
                //得到布局的宽和高
        View view = findViewById(R.id.big_window_layout);
                viewWidth = view.getLayoutParams().width;
                viewHeight = view.getLayoutParams().height;
                Log.w("TAGCountDownView", viewWidth + "*" + viewHeight);
        }

}