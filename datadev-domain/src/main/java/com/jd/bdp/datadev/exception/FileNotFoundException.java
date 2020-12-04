package com.jd.bdp.datadev.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(Long gitProjectId,String name){
        super("项目"+gitProjectId+"的文件("+name+")不存在!");
    }
    public FileNotFoundException(Long gitProjectId,String name,String version){
        super("项目"+gitProjectId+"的文件("+name+")("+version+")不存在!");
    }
    public FileNotFoundException(){
        super("文件不存在!");
    }
}
