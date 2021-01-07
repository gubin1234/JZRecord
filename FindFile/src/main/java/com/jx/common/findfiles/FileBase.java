package com.jx.common.findfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @ 作者: yjm
 * @ 类名: FileBase
 * @ 包名: com.example.listthumbnailutils
 * @ 描述: 查找文件/文件夹的父类
 * @ 日期: 2020/9/2 17:14
 **/
public class FileBase {
    public static java.util.List<File> List=new ArrayList<File>();//文件或文件夹集合
    public ICallBackTarget objCallTarget; //接口
    public void setCallBackTarget(ICallBackTarget obj){
        this.objCallTarget=obj;
    }
    /**
     * @ 作者: yjm
     * @ 方法名: TraverseFile
     * @ 描述: 查找目标文件/文件夹
     * @ 参数: [file]
     * @ 返回值: java.util.List<java.io.File>
     */
    public  List<File> TraverseFile(File file){
        return List;
    }
}
