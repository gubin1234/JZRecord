package com.jx.jzrecord.utils;



import java.io.File;

/**
 * @ 作者: yjm
 * @ 类名: UtilFile
 * @ 包名: com.jx.jzrecord.utils
 * @ 描述: 文件工具类
 * @ 日期: 2020/9/22 23:55
 **/
public class UtilFile {
    /**
     *删除本地文件
     */
    public boolean deleteLocal(File file) {
        if (file.exists()) {
            if (file.isFile()) {
             file.delete();//如果为文件，直接删除
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File file1 : files) {
                    deleteLocal(file1);//如果为文件夹，递归调用
                }
            }
            return true;
        }
        return false;
    }
    /**
     * @ 作者: yjm
     * @ 方法名: renameFile
     * @ 描述: 修改文件名
     * @ 参数: [oldPath, newPath]
     * @ 返回值: boolean
     */
    public boolean renameFile(String oldPath,String newPath){
        File oldFile=new File(oldPath);
        File newFile=new File(newPath);
        return oldFile.renameTo(newFile);
    }
}
