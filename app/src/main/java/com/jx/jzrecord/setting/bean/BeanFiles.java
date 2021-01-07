package com.jx.jzrecord.setting.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @ 作者: yjm
 * @ 类名: BeanFiles
 * @ 包名: com.example.paramssetting
 * @ 描述:
 * @ 日期: 2020/9/16 14:06
 **/
public class BeanFiles extends LitePalSupport{
    private int id;
    private String name;
    private String path;
    private Boolean isDelete;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }
}
