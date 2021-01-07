package com.jx.common.findfiles;

import java.io.File;
import java.io.FileFilter;

/**
 * @ 作者: yjm
 * @ 类名: FindFolder
 * @ 包名: com.example.listthumbnailutils
 * @ 描述: 获取指定路径下的指定文件夹/所有文件夹
 * @ 日期: 2020/9/2 23:48
 **/
public class FindFolder extends FileBase {

    /**
     * @ 作者: yjm
     * @ 方法名: FindTarget
     * @ 描述: 对前一次调用的List进行清空再返回此次调用的文件夹集合
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
     * @ 描述: 返回查找的目标文件夹的List集合
     * @ 参数: [file]
     * @ 返回值: java.util.List<java.io.File>
     */
    @Override
    public java.util.List<File> TraverseFile(File file){

        file.listFiles(new FileFilter() {
            //重写文件过滤方法
            @Override
            public boolean accept(File file) {
                //判断是不是目录
                if(file.isDirectory())
                {
                    //判断有没有接口或者是不是目标文件夹
                    if(objCallTarget ==null || objCallTarget.isTarget(file))
                    {
                        //没有接口则添加所有的文件夹，有接口则添加目标文件夹
                        List.add(file);
                    }
                    //继续遍历此文件夹
                    TraverseFile(file);
                }
                return false;
            }
        });
        return List;
    }
}
