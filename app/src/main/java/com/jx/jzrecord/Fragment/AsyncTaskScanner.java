package com.jx.jzrecord.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.jx.common.findfiles.FindFiles;
import com.jx.common.findfiles.ICallBackTarget;
import com.jx.jzrecord.setting.bean.BeanFiles;
import com.jx.jzrecord.setting.dao.DaoFiles;
import com.jx.jzrecord.utils.UtilFormatFileSize;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AsyncTaskScanner extends AsyncTask<String, Integer, List<BeanMedia>> {
    private List<BeanMedia> mMediaInfoList = new ArrayList<BeanMedia>(); // 媒体列表类
    @SuppressLint("StaticFieldLeak")
    private LinearLayout mLinearLayout;
    @SuppressLint("StaticFieldLeak")

    public AsyncTaskScanner(LinearLayout linearLayout) {
        super();
        this.mLinearLayout = linearLayout;
    }
    /**执行任务中的耗时操作，不能更改UI信息*/
    @Override
    protected List<BeanMedia> doInBackground(String... params) {
        //设置文件/文件夹查询路径
        String path= Environment.getExternalStorageDirectory()+"/DCIM/jinzhouluping";
        File file=new File(path);
        //文件查询,查找不以"."开头的文件
        FindFiles fFiles=new FindFiles();
        //设置要查怎样的文件
        fFiles.setCallBackTarget(new ICallBackTarget() {
                    @Override
                    public boolean isTarget(File file) {
                        //选出文件不以“.”开头的且文件名以mp4结尾的
                        String firstCharacter=file.getName().substring(0,1);
                        if(!firstCharacter.equals(".")&&file.getName().endsWith("mp4")){
                            return true;
                        }
                     return false;
            }
        });
        //获取文件查询结果集合
        List<File> list=fFiles.FindTarget(file);  //开始查找
        boolean have=false;
        //将查询的结果保存在数据库并将文件信息添加到集合中
        for (File value:list) {
            boolean isDelete=true; //标志某个item是否被删除过
            int count=0;
            //保存要存入数据库的信息
            BeanFiles beanFiles=new BeanFiles();
            DaoFiles daoFiles=new DaoFiles();
            beanFiles.setName(value.getName());
            beanFiles.setPath(value.getPath());
            //保存要显示在listView上的信息
            BeanMedia beanMedia = new BeanMedia();
            beanMedia.setMediaName(value.getName());//文件名称
            beanMedia.setPath(value.getAbsolutePath());//文件路径
            beanMedia.setDate(value.lastModified());//文件最后一次被修改的时间
            UtilFormatFileSize utilFormatFileSize=new UtilFormatFileSize();
            String fileSize=utilFormatFileSize.getFormatSize(value.getPath());
            beanMedia.setFileSize(fileSize);//文件大小
            List<BeanFiles> mList=daoFiles.FindAll();//返回数据库中文件表集合
            Log.d("TEST","第一：mList "+mList.size());
            /**文件表不为空*/
            if(mList.size()!=0)
            {
                //本地找到的文件与数据库中的文件一一对比，看是否存在
                for(int i=0;i<mList.size();i++){
                    if (beanFiles.getPath().equals(mList.get(i).getPath())){
                        isDelete=false;
                        count=i;
                        break;
                    }
                }
                Log.d("TEST","isDelete:"+isDelete);
                //有一样的文件
                if(!isDelete){
                    Log.d("TEST","第二:有一样的文件");
                    if(!mList.get(count).getDelete()){
                        //判断在文件列表点击删除，但未勾选删除本地文件的情况
                        mMediaInfoList.add(beanMedia);
                        Log.d("TEST","第二：这条视频没有被删除过");
                    }
                }
                //没有一样的文件
                else{
                    Log.d("TEST","第三：没有一样的文件");
                    daoFiles.add_data(beanFiles);
                    mMediaInfoList.add(beanMedia);
                }
            }
            /**文件列表为空*/
            else
            {   Log.d("TEST","文件表为空，添加！");
               daoFiles.add_data(beanFiles);
                mMediaInfoList.add(beanMedia);
            }
        }
        //对在本地进行文件删除的情况做处理，删除数据库对应数据
        DaoFiles daofiles=new DaoFiles();
        List<BeanFiles> list_data_file=daofiles.FindAll();
        Log.d("TEST","list_data_file:"+list_data_file.size());
        for(int i=0;i<list_data_file.size();i++){
            for(int j=0;j<list.size();j++){
                if (list_data_file.get(i).getPath().equals(list.get(j).getPath()))
                {
                    have=true;
                }
            }
            //如果这条数据库数据在本地文件中不存在，那么删掉这条数据
            if(!have){
                //删掉数据库数据
                daofiles.delete_data(list_data_file.get(i).getName());
            }
            have=false;
        }
        return mMediaInfoList;
    }

    /**在主线程显示线程任务的执行进度*/
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    /**接收线程任务的执行结果*/
    @Override
    protected void onPostExecute(List<BeanMedia> videoInfos) {
        super.onPostExecute(videoInfos);
        //如果没有找到文件，则显示暂无文件
        //也可以将下面的mMediaInfoList换成videoInfos
        if(mMediaInfoList.size()==0){
          mLinearLayout.setVisibility(View.VISIBLE);
        }
    }
}