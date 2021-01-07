package com.jx.jzrecord.setting.dao;

import android.content.ContentValues;
import android.util.Log;


import com.jx.jzrecord.setting.bean.BeanParams;
import com.jx.jzrecord.setting.bean.BeanSettings;

import org.litepal.LitePal;

import java.util.List;

/**
 * @ 作者: yjm
 * @ 类名: DAOSettings
 * @ 包名: com.example.paramssetting.DAO
 * @ 描述:
 * @ 日期: 2020/9/16 16:38
 **/
public class DaoSettings {
    private BeanSettings beanSettings =new BeanSettings();

    public DaoSettings(){
        LitePal.getDatabase();
    }

    public void add_data(BeanSettings bean_settings){
        beanSettings.setCount_down(bean_settings.getCount_down());
        beanSettings.setEn_audio(bean_settings.getEn_audio());
        beanSettings.setEn_shake_stop(bean_settings.getEn_shake_stop());
        beanSettings.setEn_suspended_window(bean_settings.getEn_suspended_window());
        beanSettings.saveOrUpdate("id=?",String.valueOf(1));
    }

    public void delete_data(BeanSettings bean_settings){
        LitePal.delete(BeanSettings.class,bean_settings.getId());
    }
    //更新数据
    public void update_data(int state,String filed){
        ContentValues values=new ContentValues();
        values.put(filed,state);
        LitePal.updateAll(BeanSettings.class,values);
    }
    //更新倒计时
    public void update_data_time(String state,String filed){
        ContentValues values=new ContentValues();
        values.put(filed,state);
        LitePal.updateAll(BeanSettings.class,values);
    }
    //返回设置表数据
    public BeanSettings get_Data(int id){
        return LitePal.find(BeanSettings.class,id);
    }
}

