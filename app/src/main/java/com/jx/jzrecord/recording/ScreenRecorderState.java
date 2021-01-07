package com.jx.jzrecord.recording;




/**
 * @ 作者: LSY
 * @ 类名: ScreenRecorderState
 * @ 包名: com.jx.jzrecord.recording
 * @ 描述:
 * @ 日期: 2020/10/19 20:31
 **/
public class ScreenRecorderState {
    private   boolean bl_notification_vertical;
    private  int notification_screen_state;    //1代表变为继续录屏，2代表变为暂停，3代表变为停止,4代表开始录屏

    private   boolean bl_window_vertical;
    private  int window_screen_state;   //1代表变为继续录屏，2代表变为暂停，3代表变为停止，4代表开始录屏

    private   boolean bl_main_Vertical;  //主页里面录屏方向
    private boolean bl_hide_float_window;  //是否录屏是隐藏 悬浮窗


    private static ScreenRecorderState INSTANCE = null;

    private ScreenRecorderState() {
        bl_notification_vertical=true;        //通知栏录屏方向(默认竖屏）
        bl_window_vertical=true;              //悬浮窗录屏方向（默认竖屏）
        bl_main_Vertical=true;                 //主页录屏方向
        bl_hide_float_window=false;            //默认录屏时不隐藏悬浮窗
    }

    //获取类的单实例
    public static ScreenRecorderState getInstance(){
        if(INSTANCE == null){
            synchronized (ScreenRecorderState.class) {
                if(INSTANCE == null){
                    INSTANCE = new ScreenRecorderState();
                }
            }
        }
        return INSTANCE;
    }

    public boolean isBl_notification_vertical() {
        return bl_notification_vertical;
    }

    public void setBl_notification_vertical(boolean bl_notification_vertical) {
        this.bl_notification_vertical = bl_notification_vertical;
    }

    public int getNotification_screen_state() {
        return notification_screen_state;
    }

    public void setNotification_screen_state(int notification_screen_state) {
        this.notification_screen_state = notification_screen_state;
    }

    public boolean isBl_window_vertical() {
        return bl_window_vertical;
    }

    public void setBl_window_vertical(boolean bl_window_vertical) {
        this.bl_window_vertical = bl_window_vertical;
    }

    public int getWindow_screen_state() {
        return window_screen_state;
    }

    public void setWindow_screen_state(int window_screen_state) {
        this.window_screen_state = window_screen_state;
    }

    public boolean isBl_main_Vertical() {
        return bl_main_Vertical;
    }

    public void setBl_main_Vertical(boolean bl_Vertical) {
        this.bl_main_Vertical = bl_Vertical;
    }

    public boolean isBl_hide_float_window() {
        return bl_hide_float_window;
    }

    public void setBl_hide_float_window(boolean bl_hide_float_window) {
        this.bl_hide_float_window = bl_hide_float_window;
    }
}
