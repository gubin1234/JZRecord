package com.jx.jzrecord.setting.dao;


import android.content.Context;
import android.widget.Toast;

import com.jx.jzrecord.setting.bean.BeanParams;

import org.litepal.LitePal;

import java.util.List;

/**
 * @ 作者: yjm
 * @ 类名: DAOParams
 * @ 包名: com.example.paramssetting.DAO
 * @ 描述:
 * @ 日期: 2020/9/16 16:19
 **/
public class DaoParams {
    private BeanParams videoParams=new BeanParams();

    public DaoParams(){
        LitePal.getDatabase();
    }
    //表中没有数据则增加，有数据则更新
    public void addData(BeanParams bean_params, Context context){
        videoParams.setResolution(bean_params.getResolution());
        videoParams.setBitrate(bean_params.getBitrate());
        videoParams.setFrame(bean_params.getFrame());
        boolean isSave = videoParams.saveOrUpdate("id=?", String.valueOf(1));//只保留一条数据，表中不存在数据则添加，存在则更新
        if (!isSave){
            Toast.makeText(context,"保存数据失败",Toast.LENGTH_SHORT).show();
        }
    }
    //获取数据库表的数据
    public BeanParams get_Data(int id){
        return LitePal.find(BeanParams.class,id);
    }

}
