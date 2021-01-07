package com.jx.jzrecord.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.TextView;

import java.io.File;
import java.math.BigDecimal;

/**
 * @ 作者: yjm
 * @ 类名: GetFileSize
 * @ 包名: com.example.listthumbnailutils
 * @ 描述: 格式化文件大小
 * @ 日期: 2020/9/3 17:02
 **/
public class UtilFormatFileSize {

    private LruCache<String, String> lruCache_dur;//实现最近最少使用算法

    public UtilFormatFileSize(){
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取最大的运行内存
        int maxSize = maxMemory /4;
        lruCache_dur = new LruCache<String, String>(maxSize){
            @Override
            protected int sizeOf(String key, String value) {
                //这个方法会在每次存入缓存的时候调用
                return value.length();
            }
        };
    }

    /**加入缓存*/
    public void addFileSizeToCache(String path,String size){
        if(getFileSizeToCache(path) == null){
            //当前地址没有缓存时，就添加
            lruCache_dur.put(path, size);
        }
    }

    /**从缓存中获取*/
    public String getFileSizeToCache(String path){
        return lruCache_dur.get(path);
    }


   //获取视频的大小并格式化
    public String getFormatSize(String filepath){
        File file=new File(filepath);
        double size=file.length();
        double kiloByte=size/1024;
        if(kiloByte<1){
            return size+"B";
        }
        //KB显示
        double megaByte=kiloByte/1024;
        if(megaByte<1){
            //使用BigDecimal进行精确计算
            BigDecimal result1=new BigDecimal(Double.toString(kiloByte));
            /*setScale方法：格式化小数点
             * 参数：保留两位小数，四舍五入
             * toPlainString：直接显示不用科学计数法表示*/
            return result1.setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString()+"KB";
        }
        //MB显示
        double gigaByte=megaByte/1024;
        if(gigaByte<1){
            BigDecimal result2=new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString()+"MB";
        }
        //GB显示
        double teraBytes=gigaByte/1024;
        if(teraBytes<1){
            BigDecimal result3=new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString()+"GB";
        }
        //TB显示
        BigDecimal result4=new BigDecimal(teraBytes);
        return result4.setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString()+"TB";
    }

    /**显示视频时长*/
    public void showFileSizeByAsyncTask(String path, TextView tv){
        //如果缓存为空的话，就执行异步任务查找
        if(getFileSizeToCache(path)==null){
            new GetFileSizeAsyncTask(path,tv).execute(path);
        }
        else//缓存不为空，直接从缓存中读取
        {
            tv.setText(getFileSizeToCache(path));
        }

    }

    /**异步任务获取视频大小*/
    @SuppressLint("StaticFieldLeak")
    class GetFileSizeAsyncTask extends AsyncTask<String, Void, String> {
        private TextView tx;
        private String mPath;

        public GetFileSizeAsyncTask(String path,TextView textView) {
            this.tx=textView;
            this.mPath = path;
        }

        @Override
        protected String doInBackground(String... strings) {
            String size=getFormatSize(strings[0]);//格式化大小
            if(size!=null){
                if (getFileSizeToCache(strings[0])==null){
                    addFileSizeToCache(mPath,size);
                }
            }
            //返回任务执行结果
            return size;
        }
        //接收任务执行结果，将执行结果显示到UI组件
        @Override
        protected void onPostExecute(String size) {
            if (tx.getTag().equals(mPath)){
                tx.setText(size);
            }
        }
    }
}
