package com.jx.jzrecord.Fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jx.jzrecord.R;

import com.jx.jzrecord.setting.ActivitySetting;
import com.jx.jzrecord.setting.bean.BeanFiles;
import com.jx.jzrecord.setting.dao.DaoFiles;
import com.jx.jzrecord.utils.UtilFile;
import com.jx.jzrecord.utils.UtilScreen;
import com.jx.jzrecord.utils.UtilThumb;
import com.jx.jzrecord.utils.UtilsToast;


import java.io.File;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jx.jzrecord.utils.UtilsNetWork.isConn;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentFile extends Fragment {
                                    /**-----------文件页面的变量----------**/
    private View rootView;
    private Activity activity;
    private ListView mMediaListView ; // 媒体文件的网格列表
    private LinearLayout mLinearLayout;  //视频缺省页
    private List<BeanMedia> mChildList; // 存放异步查询文件结果的数据集合
    private AdapterVideoCursor myAdapter ; // 列表适配器
    private AsyncTaskScanner mAnsyTask; // 扫描文件的异步任务
    private Boolean isCheck=false;//是否选择了删除本地文件
    private BeanMedia beanMedia;//获取一条item的数据信息
    private EditText editText;//编辑框
    private String filepath= Environment.getExternalStorageDirectory()+"/DCIM/jinzhouluping/";//视频文件路径
    private String newname=null;//修改的文件名
    private String newPath=null;//文件新路径
    private AlertDialog Dialog_end;//录制结束弹窗
    private Boolean bl_isRepeat=false;//是否重复命名

    public FragmentFile() {
        // Required empty public constructor
    }
    public FragmentFile(Activity activity) {
         this.activity=activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("TAG1","FragmentonDestroy");
        if(activity!=null){
            activity.finish();
        }
        //清除 Handler 消息队列里的所有消息
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w("TAG1","FragmentonCreateView");
            //------------------------- 加载文件页面的帧布局，并且实现该页面的逻辑代码---------------------------//
            rootView = inflater.inflate(R.layout.activity_file, container, false);
            initActivityFile();
            startScanTack();
            return rootView;
    }

    //--------------------------------------------文件页面-------------------------------------//
    private void initActivityFile(){
        beanMedia=new BeanMedia();
        mMediaListView = (ListView) rootView.findViewById(R.id.mediaListView);
        mLinearLayout=(LinearLayout)rootView.findViewById(R.id.no_video);
        //设置视频缺省页不可见,进度条转圈时不显示缺省图片
        mLinearLayout.setVisibility(View.GONE);
    }
    /**
     * @ 作者: yjm
     * @ 方法名: startScanTack
     * @ 描述: 执行后台的扫描任务,先查询本地文件，在跟数据库中的文件比较（没有添加进来，有（但标记了删除则不添加进来），
     * 有（未标记删除加载进来）
     * @ 参数: []
     * @ 返回值: void
     */
    public void startScanTack() {
        /**新开一个线程，用来完成后台的异步任务*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mAnsyTask = new AsyncTaskScanner(mLinearLayout); //初始化扫描任务
                    mAnsyTask.execute();//以单线程队列方式运行
                    mChildList = mAnsyTask.get(); //获取文件查询返回的文件集合
                    //去除后缀名
                    for(int i=0;i<mChildList.size();i++){
                        String filename=mChildList.get(i).getMediaName();
                        String name=filename.substring(0,filename.lastIndexOf(".")); //左闭右开
                        mChildList.get(i).setMediaName(name);
                    }
                    // 在子线程中填充列表
                    if(mChildList.size() > 0){
                        // 这是新开的一个子线程，最好不要直接在这里更新UI的操作，使用推荐的Handler来更新界面
                        mHandler.sendEmptyMessage(0x101);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //在主线程中执行UI操作
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            if(msg.what == 0x101 ){
                mMediaListView.setVisibility(View.VISIBLE); //设置列表控件可见
                mMediaListView.setEmptyView(mLinearLayout);//listView无数据时显示缺省页
                myAdapter = new AdapterVideoCursor(activity.getBaseContext(),mChildList); // 填充适配器
                mMediaListView.setAdapter(myAdapter); // 设置适配器
                myAdapter.setOnInnerItemOnClickListener(new AdapterVideoCursor.InnerItemOnclickListener() {
                    @Override
                    public void itemClick(int position, View v) {
                        switch (v.getId()){
                            case R.id.img_play:
                                //播放
                                playVideo(position, activity);
                                break;
                            case R.id.tv_delete:
                                //删除
                                showDeleteDialog(position);
                                break;
                            case R.id.tv_edit:
                                //重命名
                                showEditDialog(position);
                                break;
                            case R.id.tv_share:
                                //分享
                                if(isConn(activity)){
                                    allShare(position);
                                }else {
                                    Toast.makeText(activity,"当前网络不可用",Toast.LENGTH_SHORT).show();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    };

    //录屏结束弹窗
    public void showEndDialog(File file) {
        if(Dialog_end!=null){
            Dialog_end.dismiss();
        }
        Dialog_end=new AlertDialog.Builder(activity).create();
        final View contentView=getLayoutInflater().inflate(R.layout.dialog_record_end,null);//获取自定义资源布局
        Dialog_end.setView(contentView);
        RelativeLayout rl_file_message=contentView.findViewById(R.id.layout_file_message);
        RelativeLayout rl_wait=contentView.findViewById(R.id.layout_wait);
        rl_file_message.setVisibility(View.GONE);
        Handler handler=new Handler();
        //创建定时器
        Timer mTimer=new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        startScanTack();
                        //主线程更新
                        ImageView img_thumb=(ImageView) contentView.findViewById(R.id.img_thumb);
                        img_thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);//按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽)
                        Glide.with(activity).asBitmap().load(Uri.fromFile(file)).frame(1000*1000).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Bitmap bitmap=null;
                                if(resource.getWidth()<resource.getHeight())
                                {
                                    bitmap =UtilThumb.scaleBitmap(resource,16,9);
                                }else{
                                    bitmap =resource;
                                }
                                if(bitmap!=null){
                                    img_thumb.setImageBitmap(bitmap);
                                }
                            }
                        });
                        rl_file_message.setVisibility(View.VISIBLE);
                        rl_wait.setVisibility(View.GONE);

                    }
                });
            }
        },2*1000);

        //视频名字
        TextView tv_name=(TextView)contentView.findViewById(R.id.video_name);
        //去除后缀名
        String name=file.getName().substring(0,file.getName().lastIndexOf("."));
        tv_name.setText(name);
//       TextViewCompat.setAutoSizeTextTypeWithDefaults(tv_name,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//       TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(tv_name,10,18,1, TypedValue.COMPLEX_UNIT_SP);
        //播放视频
        ImageView img_play=(ImageView)contentView.findViewById(R.id.img_play);
        img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo(0,activity);
            }
        });
        //分享视频
        ImageView img_share=(ImageView)contentView.findViewById(R.id.img_share);
        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allShare(0);
            }
        });
        //删除视频
        ImageView img_delete=(ImageView)contentView.findViewById(R.id.img_delete);
        img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteSourceDialog(0);
                }
            });
            Dialog_end.show();
        //获取屏幕的尺寸
        Window dialogWindow=Dialog_end.getWindow();
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width= Math.min(UtilScreen.getScreenWidth(activity), UtilScreen.getScreenHeight(activity))*11/12;
        dialogWindow.setAttributes(lp);
        Dialog_end.setCanceledOnTouchOutside(true);//点击dialog外部区域dialog关闭
        //弹窗消失监听，当弹窗消失时
        Dialog_end.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mTimer.cancel();
            }
        });
    }

    public void showDeleteSourceDialog(int position){
        final androidx.appcompat.app.AlertDialog aDialog=new androidx.appcompat.app.AlertDialog.Builder(activity).create();
        final View contentView=getLayoutInflater().inflate(R.layout.dialog_delete_file_end,null);//获取自定义资源布局
        aDialog.show();
        //获取屏幕的尺寸
        WindowManager manager=(WindowManager)activity.getSystemService(ActivitySetting.WINDOW_SERVICE);
        Window dialogWindow=aDialog.getWindow();
        dialogWindow.setWindowAnimations(R.style.NoAnimationDialog); // 添加动画（取消动画效果）
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//消除背景白块
        dialogWindow.setContentView(contentView);
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        Display display=manager.getDefaultDisplay();
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width= (int) (display.getWidth()/1.5);
        //取消删除按钮
        Button btn_cancel = contentView.findViewById(R.id.btn_end_cancel);
        //确认删除按钮
        Button btn_certain = contentView.findViewById(R.id.btn_end_certain);
        btn_certain.setOnClickListener(v -> {
            //第一步：删除源文件
            beanMedia=myAdapter.getItem(position);//获取当前点击item的信息
            File file=new File(beanMedia.getPath());
            UtilFile utilFile =new UtilFile();
            utilFile.deleteLocal(file);//删除源文件
            DaoFiles daoFiles=new DaoFiles();
            //第二步：删除数据库信息
            daoFiles.delete_data(beanMedia.getMediaName());//删除文件表对应信息
            //第三步：刷新ListView
            mChildList.remove(position);//删除当前的item
            myAdapter.notifyDataSetChanged();//刷新listView
            new UtilsToast(activity,"删除成功").show(Toast.LENGTH_SHORT);
            aDialog.dismiss();
            Dialog_end.dismiss();
            // 通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            activity.sendBroadcast(intent);
        });
        btn_cancel.setOnClickListener(view -> aDialog.dismiss());
        dialogWindow.setAttributes(lp);
        aDialog.setCanceledOnTouchOutside(false);//dialog弹出后点击屏幕，dialog不消失；点击物理返回键dialog消失
    }

    //删除弹窗
    public void showDeleteDialog(int position){
        final androidx.appcompat.app.AlertDialog aDialog=new androidx.appcompat.app.AlertDialog.Builder(activity).create();
        final View contentView=getLayoutInflater().inflate(R.layout.dialog_delete_file,null);//获取自定义资源布局
        aDialog.setView(contentView);
        aDialog.show();
        //获取屏幕的尺寸
        WindowManager manager=(WindowManager)activity.getSystemService(ActivitySetting.WINDOW_SERVICE);
        Window dialogWindow=aDialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//消除背景白块
        //dialogWindow.setContentView(contentView);
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        Display display=manager.getDefaultDisplay();
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width= (int) (display.getWidth()/1.5);
        //是否删除本地文件复选框
        CheckBox isDelete = contentView.findViewById(R.id.cb_delete);
        //取消删除按钮
        Button btn_cancel = contentView.findViewById(R.id.btn_cancel_delete_file);
        //确认删除按钮
        Button btn_certain = contentView.findViewById(R.id.btn_certain_delete_file);
        //设置复选框的监听事件
        isDelete.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                isCheck=true;
                Log.d("CHECKDE","你选中删除");
            }
            else{
                isCheck=false;
                Log.d("CHECKDE","你没有选中删除");
            }
        });
        btn_certain.setOnClickListener(view -> {
            beanMedia=myAdapter.getItem(position);//获取当前点击item的信息
            DaoFiles daoFiles=new DaoFiles();
            // DaoDeleteItem daoDeleteItem=new DaoDeleteItem();
            //复选框被选中
            if(isCheck){
                File file=new File(beanMedia.getPath());
                UtilFile utilFile =new UtilFile();
                utilFile.deleteLocal(file);//删除源文件
                daoFiles.delete_data(beanMedia.getMediaName());//删除文件表对应信息
                Log.d("DELETE","你删除了源文件");
                isCheck=false;
                // 通知图库更新
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                activity.sendBroadcast(intent);
            }
            //复选框没被选中
            else
            {
                BeanFiles beanFiles=new BeanFiles();
                beanFiles.setName(beanMedia.getMediaName());
                beanFiles.setPath(beanMedia.getPath());
                beanFiles.setDelete(true);
                daoFiles.add_data(beanFiles);  //有就更新，没有就添加到数据库
                Log.d("DELETE","你没有删除源文件");
            }
            mChildList.remove(position);//删除当前的item
            myAdapter.notifyDataSetChanged();//刷新listView
            aDialog.dismiss();//关闭弹窗
//            if(Dialog_end!=null){
//                Dialog_end.dismiss();//关闭录制结束弹窗
//            }
        });
        btn_cancel.setOnClickListener(view -> aDialog.dismiss());
        dialogWindow.setAttributes(lp);  //弹窗背景
        aDialog.setCanceledOnTouchOutside(false);//dialog弹出后点击屏幕，dialog不消失；点击物理返回键dialog消失
    }

    //重命名弹窗
    public void showEditDialog(int position){
        final androidx.appcompat.app.AlertDialog aDialog=new androidx.appcompat.app.AlertDialog.Builder(activity).create();
        final View contentView=getLayoutInflater().inflate(R.layout.dialog_change_title,null);//获取自定义资源布局
        // aDialog.setView(contentView);
        aDialog.show();
        //获取屏幕的尺寸
        WindowManager manager=(WindowManager)activity.getSystemService(ActivitySetting.WINDOW_SERVICE);
        Window dialogWindow=aDialog.getWindow();
        Objects.requireNonNull(aDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);//弹出软键盘
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//消除白块
        dialogWindow.setContentView(contentView);
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);//弹窗居中显示
        Display display=manager.getDefaultDisplay();
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width= (int) (display.getWidth()/1.5);//设置弹窗的宽度
        editText=contentView.findViewById(R.id.et_text);
        //取消修改文件名按钮
        Button btn_cancel_change_title = contentView.findViewById(R.id.btn_cancel_change_title);//取消按钮
        //确认修改文件名按钮
        Button btn_certain_change_title = contentView.findViewById(R.id.btn_certain_change_title);//确定按钮
        //文件过滤
        InputFilter typeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                //匹配英文字母、数字、下划线和中文字符
                Pattern p = Pattern.compile("[^a-zA-Z0-9\u4E00-\u9FA5_]");
                Matcher m = p.matcher(source.toString());
                return m.replaceAll("").trim();//去掉首尾空格
            }
        };
        editText.setFilters(new InputFilter[]{typeFilter});
        beanMedia=myAdapter.getItem(position);//获取当前点击item的信息
        editText.setText(beanMedia.getMediaName());
        //确定按钮的点击事件
        btn_certain_change_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UtilFile utilFile =new UtilFile();
                newname=editText.getText().toString();//获取editText的数据
                DaoFiles daoFiles=new DaoFiles();//打开数据库
                List<BeanFiles> list=daoFiles.FindAll();
                for(int i=0;i<list.size();i++){
                   if (list.get(i).getName().equalsIgnoreCase(newname+".mp4")){
                       //有名字重复的文件，设为true
                        bl_isRepeat=true;
                    }
                }
                if(!bl_isRepeat&& !newname.equals("")){
                    Log.d("NAME","名字不一样");
                    newPath=filepath+newname+".mp4";//设置文件新路径
                    utilFile.renameFile(beanMedia.getPath(),newPath);//修改文件名
                    daoFiles.update_data_name(newname+".mp4",beanMedia.getMediaName()+".mp4");//数据库更新，更新文件信息表的文件名字
                    daoFiles.update_data_path(newPath,beanMedia.getPath());//数据库更新，更新文件信息表的文件路径
                    mChildList.get(position).setMediaName(newname);//设置新文件名
                    //mChildList.get(position).setFileSize(beanMedia.getFileSize());
                    Log.d("SIZE",mChildList.get(position).getFileSize());
                    myAdapter.notifyDataSetChanged();//mChildList数据改变了，刷新才有效
                    aDialog.dismiss();//关闭弹窗
                    startScanTack();
                }else{
                    if(newname.equals(""))
                    {
                        new UtilsToast(activity,"文件名不能为空").show(Toast.LENGTH_SHORT);
                    }
                    else{
                        new UtilsToast(activity,"名字已存在，请重新命名").show(Toast.LENGTH_SHORT);
                    }
                }
                bl_isRepeat=false;
            }
        });

        //关闭按钮的点击事件
        btn_cancel_change_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aDialog.dismiss();
            }
        });
        dialogWindow.setAttributes(lp);//设置弹窗大小
        aDialog.setCanceledOnTouchOutside(false);//dialog弹出后点击屏幕，dialog不消失；点击物理返回键dialog消失
    }

    //播放视频代码
    public void playVideo(int position,Context context){
        beanMedia=myAdapter.getItem(position);//获取当前点击item的信息
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);//根据用户的数据类型打开相应的Activity
        DaoFiles daoFiles=new DaoFiles();
        List<BeanFiles> list=daoFiles.singleFind(beanMedia.getMediaName()+".mp4");
        File file=new File(list.get(0).getPath());
        Uri uri;
        //判断是否是AndroidN以及更高的版本，AndroidN=API Level24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //这个uri打印出来是:content://路径
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//Content Provider的临时权限访问
            Uri contentUri = FileProvider.getUriForFile(context, activity.getApplicationContext().getPackageName() + ".FileProvider", file);//将一个file://转换成 content://的一个Uri对象
            intent.setDataAndType(contentUri, "video/*");//对跳转的传输的数据和类型进行设置
        } else {
            //这个uri打印出来是:file://路径
            uri = Uri.fromFile(file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "video/*");
        }
        startActivity(intent);
    }

    //分享代码
    public void allShare(int position){
        beanMedia=myAdapter.getItem(position);
        DaoFiles daoFiles=new DaoFiles();
        List<BeanFiles> list=daoFiles.singleFind(beanMedia.getMediaName()+".mp4");
        File file=new File(list.get(0).getPath());
        Uri videoUri=Uri.fromFile(file);
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        //判断是否是AndroidN以及更高的版本，AndroidN=API Level24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //这个uri打印出来是:content://路径
            share_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//Content Provider的临时权限访问
            Uri contentUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".FileProvider", file);//将一个file://转换成 content://的一个Uri对象
            share_intent.setDataAndType(contentUri, "video/*");//对跳转的传输的数据和类型进行设置
        } else {
            //这个uri打印出来是:file://路径
            //Uri uri = Uri.fromFile(file);
            share_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share_intent.setDataAndType(videoUri, "video/*");
        }
        share_intent.putExtra(Intent.EXTRA_STREAM,videoUri);//添加分享内容
        share_intent = Intent.createChooser(share_intent, "分享到："); //创建分享的Dialog
        startActivity(share_intent);
    }

}
