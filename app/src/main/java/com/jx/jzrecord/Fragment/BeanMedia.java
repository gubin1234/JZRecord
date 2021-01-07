package com.jx.jzrecord.Fragment;

import android.graphics.Bitmap;

import org.litepal.crud.LitePalSupport;

public class BeanMedia {
    private int id;//文件id
    private String path ; // 文件路径
    private String mediaName ; // 文件名
    private Bitmap thumbImg ; // 缩略图
    private int duration;//视频时长
    private long date;//文件日期


    private String fileSize;//文件大小

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    //MyVideoThumbLoader myVideoThumbLoader=new MyVideoThumbLoader();
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getMediaName() {
        return mediaName;
    }
    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }
    public Bitmap getThumbImg(){
//      return path != null ? getVideoThumbNail(path) : null;
        return thumbImg;
    }
    public void setThumbImg(Bitmap thumbImg) {
        this.thumbImg = thumbImg;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
    @Override
    public String toString() {
        return "MediaBean [path=" + path + ", mediaName=" + mediaName + ", thumbImg=" + thumbImg + "]";
    }
}
