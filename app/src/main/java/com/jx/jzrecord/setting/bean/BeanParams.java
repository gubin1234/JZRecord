package com.jx.jzrecord.setting.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @ 作者: yjm
 * @ 类名: BeanParams
 * @ 包名: com.example.paramssetting
 * @ 描述:
 * @ 日期: 2020/9/16 13:51
 **/
public class BeanParams extends LitePalSupport {
    private int id;
    private String resolution;
    private String bitrate;
    private String frame;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }
}
