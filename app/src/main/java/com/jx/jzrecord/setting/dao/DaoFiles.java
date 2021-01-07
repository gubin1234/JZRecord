package com.jx.jzrecord.setting.dao;

import android.content.ContentValues;
import com.jx.jzrecord.setting.bean.BeanFiles;
import org.litepal.LitePal;
import java.util.List;


/**
 * @ 作者: yjm
 * @ 类名: DAOFiles
 * @ 包名: com.example.paramssetting.DAO
 * @ 描述:
 * @ 日期: 2020/9/16 16:03
 **/
public class DaoFiles {
    private BeanFiles beanFiles =new BeanFiles();
    public DaoFiles(){
        LitePal.getDatabase();
    }
    //增
    public void add_data(BeanFiles bean_files){
        beanFiles.setId(bean_files.getId());
        beanFiles.setName(bean_files.getName());
        beanFiles.setPath(bean_files.getPath());
        beanFiles.setDelete(bean_files.getDelete());
        beanFiles.saveOrUpdate("path=?",beanFiles.getPath());
    }

    public void delete_data(String name){
        LitePal.deleteAll(BeanFiles.class,"name=?",name);
    }

    public void update_data_name(String newName,String oldName){
        ContentValues values=new ContentValues();
        values.put("name",newName);
        LitePal.updateAll(BeanFiles.class,values,"name=?",oldName);
    }

    public void update_data_path(String newPath,String oldPath){
        ContentValues values=new ContentValues();
        values.put("path",newPath);
        LitePal.updateAll(BeanFiles.class,values,"path=?",oldPath);
    }


    //查
    public BeanFiles singleFind(int id){
        return LitePal.find(BeanFiles.class,id);
    }

    //返回指定条件的一个文件
    public  List<BeanFiles> singleFind(String name){
        List<BeanFiles> filesList=LitePal.where("name=?",name).find(BeanFiles.class);
        return filesList;
    }

    //返回所有数据库记录的文件
    public List<BeanFiles> FindAll(){
        return LitePal.findAll(BeanFiles.class,true);
    }

}
