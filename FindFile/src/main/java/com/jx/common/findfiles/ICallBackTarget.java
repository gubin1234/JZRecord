package com.jx.common.findfiles;

import java.io.File;

/**
 * @ 作者: yjm
 * @ 接口名: ICallBackFiles
 * @ 包名: com.example.listthumbnailutils
 * @ 描述: 接口定义
 * @ 日期: 2020/9/1 17:41
 **/
public interface ICallBackTarget {
    boolean isTarget(File file);//是否为目标文件/文件夹
}
