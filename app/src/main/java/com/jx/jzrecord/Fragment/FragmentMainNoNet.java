package com.jx.jzrecord.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.jx.jzrecord.ActivityNoInternet;
import com.jx.jzrecord.R;
import com.jx.jzrecord.login.ActivityLogin;
import com.jx.jzrecord.recording.ActivityMain;
import com.jx.jzrecord.setting.ActivitySetting;
import com.jx.jzrecord.utils.UtilsToast;

import static com.jx.jzrecord.utils.UtilsNetWork.isConn;

/**
 * @ 作者: LSY
 * @ 类名: FragmentMainNoNet
 * @ 包名: com.jx.jzrecord.Fragment
 * @ 描述:
 * @ 日期: 2020/10/26 10:07
 **/
public class FragmentMainNoNet extends Fragment {
    private LinearLayout NoNetLoginBtn; //登录按钮
    private ImageButton NoNetKeFuBtn; //客服按钮
    private ImageButton NoNetSetBtn;  //设置按钮
    private Button NoNetRepeatBtn;  //重新加载网络按钮
    private Activity activity;

    public FragmentMainNoNet() {
        // Required empty public constructor
    }

    public FragmentMainNoNet(Activity activity) {
        this.activity = activity;
    }



    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main_nonet, container, false);
        NoNetRepeatBtn=rootView.findViewById(R.id.NoNetRepeatBtn);
        NoNetLoginBtn=rootView.findViewById(R.id.ll_noNet_login);
        NoNetKeFuBtn=rootView.findViewById(R.id.NoNetKeFuBtn);
        NoNetSetBtn=rootView.findViewById(R.id.NoNetSetBtn);
        //重新加载按钮点击事件
        NoNetRepeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConn(activity.getApplicationContext())) {
                    //没网络
                    Log.w("TAGNoInternet", "没网络");
                    new UtilsToast(activity,"请打开网络连接").show(Toast.LENGTH_SHORT);
                } else {
                    //有网络
                    Log.w("TAGNoInternet", "有网络");
                    Intent intent = new Intent(activity, ActivityMain.class);
                    startActivity(intent);
                    activity.finish();
                }
            }
        });
        //登录按钮点击事件
        NoNetLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UtilsToast(activity,"请先点击重新加载，加载网络").show(Toast.LENGTH_SHORT);
            }
        });

        //设置按钮点击事件
        NoNetSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, ActivitySetting.class));
                activity.finish();
            }
        });

        //客服按钮点击事件
        NoNetKeFuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://jxxxkjhzyxgs.qiyukf.com/client?k=da145c49c514d71e9f8b7230cd95cf52&wp=1&robotShuntSwitch=1&robotId=3437939");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return rootView;
    }
}
