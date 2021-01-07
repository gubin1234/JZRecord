package com.jx.jzrecord.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jx.jzrecord.R;

import java.util.ArrayList;

public class CustomNodeSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private int mNodeCount = 4;
    private String[] num =null;
    private int TEXT_SIZE=0;
    private float offsetY=0;
    private String Name=null;

    /*设置刻度值大小以及刻度值与滑动条的距离*/
    public void setSize(int mTextSize,float mOffsetY){
        TEXT_SIZE=mTextSize;
        offsetY=mOffsetY;
    }
    public CustomNodeSeekBar(@NonNull Context context) {
        super(context);
    }

    public CustomNodeSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context,attrs);
        initView();
    }

    public CustomNodeSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context,attrs);
        initView();
    }

    private void initAttr(Context context,AttributeSet attrs){
        //该方法是Context类为我们提供的获取style中特定属性值的方法
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.nodeseekbar_attrs);
        mNodeCount = typedArray.getInteger(R.styleable.nodeseekbar_attrs_node_number,4);
    }
    //节点选择接口
    public interface OnSeekChoiceListener{
        void onSeekChoosen(int index);
    }
    private OnSeekChoiceListener mSeekChoiceListener;
    //在ActivitySetting中调用该方法实现接口的onSeekChoosen函数
    public void setOnChoiceListener(OnSeekChoiceListener onChoiceListener){
        mSeekChoiceListener = onChoiceListener;
    }
    //List是接口，ArrayList是用一个数组实现的List类
    private ArrayList<Integer> mGreps = new ArrayList<Integer>();

    private void initView() {
        initGreps();
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public int mIndex;
            /**
             * 当进度条发生变化时调用该方法
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("jz", "progress:" + progress);
                mIndex = getClosedNodeIndex(progress);
                Log.e("jz", "index:" + mIndex);
            }

            /**
             * 当用户开始滑动滑块时调用该方法
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             * 当用户结束对滑块滑动时,调用该方法
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setProgress(mIndex * getMax() / (mNodeCount - 1));
                if (mSeekChoiceListener != null)
                    mSeekChoiceListener.onSeekChoosen(mIndex);
            }
        });
    }


    private int getClosedNodeIndex(int progress) {
        for(int i = 0 ;i < mGreps.size()-1 ;i++){
            int min = mGreps.get(i);
            int max = mGreps.get(i+1);
            if(min <= progress  && progress < max){
                Log.d("NODE","i:"+i);
                return i;
            }else if(progress == getMax()){
                Log.d("NODE","mGreps.size() - 2:"+(mGreps.size() - 2));
                return mGreps.size() - 2;
            }
        }
        return 0;
    }

    public void setNodeCount(int count){
        if(count < 2)
            count = 2;
        mNodeCount = count;
        initGreps();
    }

    private void initGreps() {
        //清除当前列表中的所有元素
        mGreps.clear();
        mGreps.add(0);
        int step = getMax()/(mNodeCount -1)/2;
        Log.d("NODE","getMax()"+getMax());
        Log.d("NODE","mNodeCount:"+mNodeCount);
        Log.d("NODE","step:"+step);
        for(int i = 0; i < mNodeCount - 1;i++){
            Log.d("NODE","param:"+(getMax()/(mNodeCount -1) * i + step));
            mGreps.add(getMax()/(mNodeCount -1) * i + step );
        }
        mGreps.add(getMax());
    }
    //设置刻度值
    public void setArray(String... a){
        this.num=a;
    }
    //设置名字（分辨率、码率、帧率）
    public void setName(String name){
        this.Name=name;
    }
    /**
     * 绘制图形
     * @param canvas
     * Canvas类就是一块画布
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation")
        Paint paintText=new Paint();//刻度画笔
        paintText.setTextSize(TEXT_SIZE);//设置字体大小
        paintText.setAntiAlias(true);//抗锯齿
        paintText.setDither(true);//防抖动
        paintText.setColor(Color.parseColor("#222222"));//设置画笔颜色
        Paint.FontMetrics fontMetrics=paintText.getFontMetrics();//创建FontMetrics对象
        Log.d("VIEW","------------------------------");
        //getMeasuredWidth()获取的是view原始的大小，也就是这个view在XML文件中配置或者是代码中设置的大小
        //getWidth（）获取的是这个view最终显示的大小，这个大小有可能等于原始的大小也有可能不等于原始大小。
        if(num!=null&& getMeasuredWidth() != 0 && getMeasuredHeight() != 0){
            int y = getHeight()/2;
            for(int i=0; i<mNodeCount;i++){
                int x=0;
                if(i == 0){
                    paintText.setTextAlign(Paint.Align.LEFT);
                    int saveCount = canvas.save();//锁画布，保存之前的画布状态
                    canvas.translate( getPaddingLeft(), getPaddingTop());//把当前画布的原点移到(dx,dy),后面的操作都以(dx,dy)作为参照点，默认原点为(0,0)
                    canvas.drawText(num[i],0,y+offsetY-fontMetrics.ascent,paintText);//绘制刻度，设置位置使它离基准线的距离为offsetY (y-fontMetrics.ascent)基准线下移字符上方和中线相切
                    canvas.drawText(Name,0,y-offsetY-fontMetrics.descent,paintText);//绘制名字，设置位置使它离基准线的距离为offsetY
                    canvas.restoreToCount(saveCount);
                    Log.d("VIEW","第一个");
                }else if( i == mNodeCount-1){
                    paintText.setTextAlign(Paint.Align.RIGHT);
                    int saveCount = canvas.save();
                    canvas.translate( - getPaddingRight() , getPaddingTop());  //改变画布起始点的位置
                    x = getMeasuredWidth() ;
                    canvas.drawText(num[i],x,y+offsetY-fontMetrics.ascent,paintText);//绘制刻度
                    canvas.restoreToCount(saveCount);
                    Log.d("VIEW","最后一个");
                }else {
                    paintText.setTextAlign(Paint.Align.CENTER);
                    int step = (getMeasuredWidth()- getPaddingLeft() - getPaddingRight() )/(mNodeCount-1);
                    x = step * i + getPaddingLeft();
                    canvas.drawText(num[i],x,y+offsetY-fontMetrics.ascent,paintText);//绘制刻度  x是竖,y代表横
                    Log.d("VIEW","中间的");
                }
            }
        }
        super.onDraw(canvas);
    }
    //测量控件的大小
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
