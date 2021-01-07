package com.jx.jzrecord.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.jx.jzrecord.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @ 作者: yjm
 * @ 类名: OneExpandAdapter
 * @ 包名: com.jx.jzrecord.setting
 * @ 描述:
 * @ 日期: 2020/10/22 13:50
 **/
public class OneExpandAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String,String>> list;
    private int currentItem=-1;//用于记录点击的Item的position,是控制item展开的核心

    public OneExpandAdapter(Context context,ArrayList<HashMap<String,String>> list){
        super();
        this.context=context;
        this.list=list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
     ViewHolder holder=null;
     if(view==null){
         view= LayoutInflater.from(context).inflate(R.layout.question_item_layout,viewGroup,false);
         holder=new ViewHolder();
         holder.showArea=(LinearLayout)view.findViewById(R.id.layout_showArea);
         holder.tvQuestion=(TextView)view.findViewById(R.id.tv_question);
         holder.tvAnswer=(TextView)view.findViewById(R.id.tv_answer);
         holder.hideArea=(RelativeLayout)view.findViewById(R.id.layout_hideArea);
         holder.imgControl=(ImageView)view.findViewById(R.id.img_control);
         view.setTag(holder);
     }else
     {
         holder=(ViewHolder)view.getTag();
     }
     HashMap<String,String> item=list.get(i);
        // 注意：我们在此给响应点击事件的区域添加Tag，为了记录点击的 position，我们正好用 position 设置 Tag
        holder.showArea.setTag(i);
        holder.tvQuestion.setText(item.get("question"));
        holder.tvAnswer.setText(item.get("answer"));
        //根据 currentItem 记录的点击位置来设置"对应Item"的可见性（在list依次加载列表数据时，每加载一个时都看一下是不是需改变可见性的那一条）
        if (currentItem == i) {
            holder.hideArea.setVisibility(View.VISIBLE);
            holder.imgControl.setImageDrawable(context.getResources().getDrawable(R.drawable.question_close_item));
        } else {
            holder.hideArea.setVisibility(View.GONE);
            holder.imgControl.setImageDrawable(context.getResources().getDrawable(R.drawable.question_open_item));
        }
        holder.showArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //用 currentItem 记录点击位置
                int tag = (Integer) view.getTag();
                Log.d("TAG_",tag+"");
                if (tag == currentItem) { //再次点击
                    currentItem = -1; //给 currentItem 一个无效值
                } else {
                    currentItem = tag;
                }
                //通知adapter数据改变需要重新加载
                notifyDataSetChanged(); //必须有的一步
            }
        });
        return view;
    }

    private static class ViewHolder{
        private LinearLayout showArea;
        private TextView tvQuestion;
        private TextView tvAnswer;
        private ImageView imgControl;
        private RelativeLayout hideArea;
    }
}
