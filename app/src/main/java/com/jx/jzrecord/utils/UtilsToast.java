package com.jx.jzrecord.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jx.jzrecord.R;

/**
 * @ 作者: LSY
 * @ 类名: UtilsToast
 * @ 包名: com.jx.jzrecord.utils
 * @ 描述:自定义Toast弹窗
 * @ 日期: 2020/11/12 16:40
 **/
public class UtilsToast {
    private Context context;
    private String message;

    public UtilsToast(Context context,String message) {
        this.context = context;
        this.message=message;
    }

    public void show(int time){
        Log.w("TAG","ServiceStartLogo");
        Toast toast = new Toast(context);
        //加载布局
        View view1 = LayoutInflater.from(context).inflate(R.layout.activity_toast,null);
        TextView textView = view1.findViewById(R.id.tv_notify_content);
        textView.setText(message);
        toast.setDuration(time);
        toast.setGravity(Gravity.BOTTOM,0,100);
        toast.setView(view1);
        toast.show();
    }
}
