package com.jx.jzrecord;



import android.os.Bundle;

import android.util.Log;


import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jx.jzrecord.Fragment.FragmentFile;
import com.jx.jzrecord.Fragment.FragmentMainNoNet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.internal.CustomAdapt;


public class ActivityNoInternet extends AppCompatActivity  implements CustomAdapt {

    /**---底部栏相关变量---**/
    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    public List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);
      initActivityNoInternet();
    }

    private void  initActivityNoInternet(){
        // find view
        mViewPager = findViewById(R.id.fragment_vp);
        mTabRadioGroup = findViewById(R.id.tabs_rg);
        // init fragment
        mFragments = new ArrayList<>(2);
        mFragments.add(new FragmentMainNoNet(ActivityNoInternet.this));
        mFragments.add(new FragmentFile(ActivityNoInternet.this));
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
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

    /*---------------屏幕适配----------------*/
    @Override
    public boolean isBaseOnWidth() {
        return false;   //是否按照宽度进行等比例适配（true 是），false是高度进行等比例适配
    }

    @Override
    public float getSizeInDp() {
        return 640;         //以dp为单位
    }


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
        Log.w("TAGActivityNoInternet", "onDestroy");
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }
}