package com.jx.jzrecord.setting.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @ 作者: yjm
 * @ 类名: BeanSettings
 * @ 包名: com.example.paramssetting
 * @ 描述:
 * @ 日期: 2020/9/16 13:58
 **/
public class BeanSettings extends LitePalSupport {
    private int id;
    private int en_audio;
    private int en_suspended_window;
    private int en_shake_stop;
    private String count_down;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEn_audio() {
        return en_audio;
    }

    public void setEn_audio(int en_audio) {
        this.en_audio = en_audio;
    }

    public int getEn_suspended_window() {
        return en_suspended_window;
    }

    public void setEn_suspended_window(int en_suspended_window) {
        this.en_suspended_window = en_suspended_window;
    }

    public int getEn_shake_stop() {
        return en_shake_stop;
    }

    public void setEn_shake_stop(int en_shake_stop) {
        this.en_shake_stop = en_shake_stop;
    }

    public String getCount_down() {
        return count_down;
    }

    public void setCount_down(String count_down) {
        this.count_down = count_down;
    }

}
