package com.jx.jzrecord.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.Util;


/**
 * @ 作者: yjm
 * @ 类名: UtilThumb
 * @ 包名: com.jx.jzrecord.utils
 * @ 描述:
 * @ 日期: 2020/10/27 14:47
 **/
public class UtilThumb {
    public static void addImageView(Context context, Uri string, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); //设置ImageView的填充方式（等比缩放）
            Glide.with(context).asBitmap().load(string).frame(1000*1000).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Bitmap bitmap=null;
                    if(resource.getWidth()<resource.getHeight())
                    {
                        bitmap =scaleBitmap(resource,16,9);
                    }else{
                        bitmap =resource;
                    }
                    if(bitmap!=null&&imageView.getTag().equals(string)){  //防止错位
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }); //方法中设置asBitmap可以设置回调类型
        //Glide.with(context).asBitmap().load(string).frame(1000*1000).into(imageView);
    }

    //裁剪
    public static Bitmap scaleBitmap(Bitmap bitmap,float w,float h){
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float x = 0,y = 0,scaleWidth = width,scaleHeight = height;
        Bitmap newbmp=null;
        if(w > h){//比例宽度大于高度的情况
            float scale = w/h;
            float tempH = width/scale;
            if(height > tempH){
                x = 0;
                y=(height-tempH)/2;
                scaleWidth = width;
                scaleHeight = tempH;
            }else{
                scaleWidth = height*scale;
                x = (width - scaleWidth)/2;
                y= 0;
            }
        }else if(w < h){//比例宽度小于高度的情况
            float scale = h/w;
            float tempW = height/scale;
            if(width > tempW){
                y = 0;
                x = (width -tempW)/2;
                scaleWidth = tempW;
                scaleHeight = height;
            }else{
                scaleHeight = width*scale;
                y = (height - scaleHeight)/2;
                x = 0;
                scaleWidth = width;
            }

        }else{//比例宽高相等的情况
            if(width > height){
                x= (width-height)/2;
                y = 0;
                scaleHeight = height;
                scaleWidth = height;
            }else {
                y=(height - width)/2;
                x = 0;
                scaleHeight = width;
                scaleWidth = width;
            }
        }
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) scaleWidth, (int) scaleHeight, null, false);// createBitmap()方法中定义的参数x+width要小于或等于bitmap.getWidth()，y+height要小于或等于bitmap.getHeight()
            //bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newbmp;
    }
}
