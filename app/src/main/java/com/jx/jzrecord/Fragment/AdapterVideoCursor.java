package com.jx.jzrecord.Fragment;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.jx.jzrecord.R;
import com.jx.jzrecord.utils.UtilFormatFileSize;
import com.jx.jzrecord.utils.UtilGetVideoDuration;
import com.jx.jzrecord.utils.UtilThumb;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/**
 * @ 作者: yjm
 * @ 类名: BeanFiles
 * @ 包名: com.example.paramssetting
 * @ 描述: ListView适配器
 * @ 日期: 2020/9/16 14:06
 **/
public class AdapterVideoCursor extends BaseAdapter implements View.OnClickListener {
    private Context mContext; //上下文
    private List<BeanMedia> mChildList; //数据集合
    private UtilGetVideoDuration videoDuration; //视频时长加载
    private UtilFormatFileSize fileSize; //文件大小格式化
    private InnerItemOnclickListener mListener;

    public AdapterVideoCursor(Context mContext, List<BeanMedia> mChildList) {
        super();
        this.mContext = mContext;
        this.mChildList = mChildList;  //两个指向同个地址
        sortLetter(mChildList);
        videoDuration=new UtilGetVideoDuration();
        fileSize=new UtilFormatFileSize();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mChildList.size();
    }

    @Override
    public BeanMedia getItem(int position) {
        // TODO Auto-generated method stub
        return mChildList.get(position);
    }

    @Override
    public long getItemId(int id) {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View item=null;
        viewHolder holder=null;
        if(view==null){
            item = LayoutInflater.from(mContext).inflate(R.layout.display_mediainfo_item, null);
            holder= new viewHolder();
            holder.tv_dur=(TextView)item.findViewById(R.id.duration);//视频时长
            holder.img_thumb=(ImageView)item.findViewById(R.id.iv);//视频缩略图
            holder.tv_name=(TextView)item.findViewById(R.id.file_name);//文件名
            holder.tv_size=(TextView)item.findViewById(R.id.size);//文件大小
            holder.img_play=(ImageView)item.findViewById(R.id.img_play);//播放
            holder.tv_share=(ImageView)item.findViewById(R.id.tv_share);
            holder.tv_edit=(ImageView)item.findViewById(R.id.tv_edit);
            holder.tv_delete=(ImageView)item.findViewById(R.id.tv_delete);
            item.setTag(holder);
        }else
        {
            item=view;
            holder= (viewHolder) item.getTag();
        }
        holder.img_play.setOnClickListener(this);
        holder.tv_share.setOnClickListener(this);
        holder.tv_edit.setOnClickListener(this);
        holder.tv_delete.setOnClickListener(this);

        holder.tv_name.setText(getItem(position).getMediaName());
        String path=mChildList.get(position).getPath();
        Uri uri=Uri.fromFile(new File(path));
        holder.img_thumb.setTag(uri);
        holder.tv_dur.setTag(path);
        holder.tv_size.setTag(path);

        //四个点击按钮
        holder.img_play.setTag(position);
        holder.tv_share.setTag(position);
        holder.tv_edit.setTag(position);
        holder.tv_delete.setTag(position);

        UtilThumb.addImageView(mContext, Uri.fromFile(new File(path)),holder.img_thumb);//设置缩略图
        videoDuration.showDurationByAsyncTask(path,holder.tv_dur);//设置视频时长
        fileSize.showFileSizeByAsyncTask(path,holder.tv_size);//设置视频大小
        return item;
    }

    @Override
    public void onClick(View view) {
        mListener.itemClick((Integer) view.getTag(),view);
    }

    public static class viewHolder{
        public TextView tv_name;//文件名
        public TextView tv_size;//文件大小
        public ImageView img_thumb;//视频缩略图
        private TextView tv_dur;//视频时长
        private ImageView img_play;//视频播放
        private ImageView tv_share;//分享
        private ImageView tv_edit;//编辑
        private ImageView tv_delete;//删除
    }

    interface InnerItemOnclickListener{
        void itemClick(int position, View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }


    /**
     * @ 作者: yjm
     * @ 方法名: sortLetter
     * @ 描述: 将视频文件按时间排序，最近的文件显示在顶部
     * @ 参数: [datas]
     * @ 返回值: java.util.List<com.jx.jzrecord.Fragment.BeanMedia>
     */
    private List<BeanMedia> sortLetter(List<BeanMedia> datas){
        Collections.sort(datas, new Comparator<BeanMedia>() {
            @Override
            public int compare(BeanMedia lhs, BeanMedia rhs) {
                if( lhs.getDate() < rhs.getDate()){
                    // 返回值为int类型，大于0表示正序，小于0表示逆序
                    return 1;
                }else{
                    return -1;
                }
            }
        });
        return datas;
    }
}
