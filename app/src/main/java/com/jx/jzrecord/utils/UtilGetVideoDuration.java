package com.jx.jzrecord.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.TextView;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * @ 作者: yjm
 * @ 类名: getVideoDuration
 * @ 包名: com.example.listthumbnailutils
 * @ 描述:
 * @ 日期: 2020/9/3 16:20
 **/
public class UtilGetVideoDuration {

    private LruCache<String, String> lruCache_dur;
    public UtilGetVideoDuration(){
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

    public void addVideoThumbToCache(String path,String s){
        if(getVideoDurationToCache(path) == null){
            //当前地址没有缓存时，就添加
            lruCache_dur.put(path, s);
        }
    }

    public String getVideoDurationToCache(String path){
        return lruCache_dur.get(path);
    }
    public String getDuration(String filePath){
        String duration = null;
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        try{
            mmr.setDataSource(filePath);
            int time = Integer.parseInt(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            Log.d("sfa",time+"s");//秒数
            if (time < 60) {
                duration = String.format("00:00:%02d", time % 60);
            } else if (time < 3600) {
                duration = String.format("00:%02d:%02d", time / 60, time % 60);
            } else {
                duration = String.format("%02d:%02d:%02d", time / 3600, time % 3600 / 60, time % 60);
            }
            Log.d("sf",duration);
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                mmr.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return duration;
    }

    public void showDurationByAsyncTask(String path, TextView tv){
        if(getVideoDurationToCache(path)==null){
            new GetDurationAsyncTask(path,tv).execute(path);
        }else{
            tv.setText(getVideoDurationToCache(path));
        }

    }

    class GetDurationAsyncTask extends AsyncTask<String, Void, String>{
        private TextView tx;
        private String mPath;

        public GetDurationAsyncTask(String path,TextView textView) {
            this.tx=textView;
            this.mPath = path;
        }

        @Override
        protected String doInBackground(String... strings) {
           String s=getDuration(strings[0]);
           if(s!=null){
               if (getVideoDurationToCache(strings[0])==null){
                   addVideoThumbToCache(mPath,s);
               }
           }
           return s;
        }

        @Override
        protected void onPostExecute(String s) {
            if (tx.getTag().equals(mPath)){
                tx.setText(s);
            }
        }
    }
}
