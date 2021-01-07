package com.jx.common.findfiles;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;

/**
 * @ 作者: yjm
 * @ 类名: FindFiles
 * @ 包名: com.example.listthumbnailutils
 * @ 描述: 获取指定路径下的指定文件/所有文件
 * @ 日期: 2020/9/1 15:57
 **/
public  class FindFiles extends FileBase{
    /**
     * @ 作者: yjm
     * @ 方法名: FindTarget
     * @ 描述: 对前一次调用的List进行清空再返回此次调用的文件集合
     * @ 参数: [file]
     * @ 返回值: java.util.List<java.io.File>
     */
    public java.util.List<File> FindTarget(File file)
    {
        List.clear();//清空前一次调用中List里面的数据
        return TraverseFile(file);
    }

    /**
     * @ 作者: yjm
     * @ 方法名: TraverseFile
     * @ 描述: 返回查找的目标目录中的文件的List集合
     * @ 参数: [file]：目标目录，这方法是目标目录下的文件夹或者文件，执行到没文件夹或文件自带结束
     * 例如：sdcard为目标目录,就是Android->DCIM->Download这样遍历  即Android目录下的文件或文件夹。
     *      DCIM为目标目录，就是DCIM下的文件或文件夹遍历
     * @ 返回值: java.util.List<java.io.File>
     */
    @Override
    public java.util.List<File> TraverseFile(File file){
        Log.d("FILE","1");
        file.listFiles(new FileFilter() {
            //重写文件过滤方法
            @Override
            public boolean accept(File file) {
                Log.d("FILE","2");
                //判断是不是目录
                if (!file.isDirectory())
                {
                    //判断有没有接口或者是不是目标文件
                    if (objCallTarget == null || objCallTarget.isTarget(file))
                    {
                        Log.d("FILE","有");
                        //没有接口则添加所有文件，有接口则添加目标文件
                        List.add(file);
                        return true;   //不管返回true或者false都会继续循环找下一个文件，只是true代表加入File数组里面
                    }
                }
                else
                {
                    TraverseFile(file);//是目录，继续遍历
                }
                return false;
            }
        });
        return List;
    }
}

