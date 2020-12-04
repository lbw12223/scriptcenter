package com.jd.bdp.datadev.exception;

public class DependencyDetailNotFoundException extends RuntimeException {
    public DependencyDetailNotFoundException(Long gitProjectId, String name){
        super("项目"+gitProjectId+"的文件("+name+")不存在!");
    }
    public DependencyDetailNotFoundException(){
        super("文件不存在!");
    }
}
