package com.jx.jzrecord.recording;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jx.jzrecord.Fragment.FragmentFile;
import com.jx.jzrecord.Fragment.FragmentMain;
import com.jx.jzrecord.R;
import com.jx.jzrecord.setting.bean.BeanParams;
import com.jx.jzrecord.setting.bean.BeanSettings;
import com.jx.jzrecord.setting.dao.DaoParams;
import com.jx.jzrecord.setting.dao.DaoSettings;
import com.jx.jzrecord.utils.UtilNotification;
import com.jx.jzrecord.utils.UtilsToast;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.internal.CustomAdapt;

import static android.os.Build.VERSION_CODES.M;

/**
 * @ 作者: LSY
 * @ 类名: ActivityMain
 * @ 包名: com.jiangxia.screen
 * @ 描述: 录屏主页Activity
 * @ 日期: 2020/9/16
 **/
public class ActivityMain extends AppCompatActivity  implements CustomAdapt  {

                                  /**---底部栏相关变量---**/
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    public static List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;


                                   /**---录屏所需变量---**/
    private UtilNotification mNotifications; //通知栏对象

    private long lastPressTime = 0L;  //用于判断两次按返回键的时间间隔

    @RequiresApi(api = M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);
        Log.w("TAG1","MainActivityonCreate");
        initActivity();  //初始化控件和数据
    }

    /**
     * 初始化Activity的控件变量
     */
    private void initActivity()
    {
        // find view
        mViewPager = findViewById(R.id.fragment_vp);
        mTabRadioGroup = findViewById(R.id.tabs_rg);
        // init fragment
        mFragments = new ArrayList<>(2);
        mFragments.add(new FragmentMain(ActivityMain.this));
        mFragments.add(new FragmentFile(ActivityMain.this));
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        //通知栏对象初始化
        mNotifications = UtilNotification.getInstance(getApplicationContext());
        //创建通知栏对象
        mNotifications.initAndNotify();
        //***------ 设置页面参数默认值---------- */
        DaoSettings daoSettings=new DaoSettings();
        BeanSettings beanSettings=new BeanSettings();
        if(daoSettings.get_Data(1)==null){
            beanSettings.setCount_down("无");
            beanSettings.setEn_shake_stop(1);//摇晃停止录屏   0表示开启,1表示关闭
            beanSettings.setEn_suspended_window(1); //0表示录制时隐藏悬浮窗
            beanSettings.setEn_audio(0);   //0表示允许音频，1表示拒绝
            daoSettings.add_data(beanSettings);
        }
        DaoParams daoParams=new DaoParams();
        BeanParams beanParams=new BeanParams();
        if(daoParams.get_Data(1)==null){
            beanParams.setResolution("720P");
            beanParams.setBitrate("自动");
            beanParams.setFrame("自动");
            daoParams.addData(beanParams,ActivityMain.this);
        }
    }


                                                   /**-------底部栏-----***/
    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.w("TAG","onPageSelected="+position);
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(position);
            radioButton.setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i).getId() == checkedId) {
                    mViewPager.setCurrentItem(i);
                    Log.w("TAG","setCurrentItem="+i);
                    return;
                }
            }
        }
    };

    /*---------------屏幕适配----------------*/
    @Override
    public boolean isBaseOnWidth() {
        return false;   //是否按照宽度进行等比例适配（true 是），false是高度进行等比例适配
    }

    @Override
    public float getSizeInDp() {
        return 640;         //以dp为单位
    }


    private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mList;

        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.mList = list;
        }


        @NotNull
        @Override
        public Fragment getItem(int position) {
            Log.w("TAG", "position=" + position);
            return this.mList == null ? null : this.mList.get(position);
        }

        @Override
        public int getCount() {
            return this.mList == null ? 0 : this.mList.size();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("TAG1","ActivityMainonDestroy");
        mNotifications.Destroy();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
        Intent intent = new Intent(ActivityMain.this, ScreenRecorderService.class);
        stopService(intent);
    }
    /**
     * 两次返回键退出App.实现的核心要素：
     *     原理：第一次按下返回键时，弹出提示Toast同时记录当时的时间。
     *          如果下一次返回键时间与本次间隔在2s内，则退出App，否则
     *          继续两次时间间隔检测
     *     注意：这里是完全复写onBackPressed方法，不要调用父类的onBackPressed
     *          否则按一下返回键就退出了
     */
    @Override
    public void onBackPressed() {
        int timeExpired = 2000; //毫秒
        if (System.currentTimeMillis() - lastPressTime > timeExpired) {
            // 两次间隔在expiredTime外，则弹出Toasr提示用户“再按一次退出程序”
            new UtilsToast(ActivityMain.this,"再按一次退出本程序").show(Toast.LENGTH_SHORT);
            lastPressTime = System.currentTimeMillis();
        } else {
            // 两次间隔在ExpiredTime内，直接退出程序
            Intent intent = new Intent();
              // 为Intent设置Action、Category属性
            intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
            intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
            startActivity(intent);
        }
        // 注意，这里不需要调用父类的onBackPressed方法了，否则每次运行到这调用父类的onBackPressed退出程序了
        //super.onBackPressed();不要调用
    }
}

