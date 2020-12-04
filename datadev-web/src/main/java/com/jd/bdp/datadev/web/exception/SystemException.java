package com.jd.bdp.datadev.web.exception;

/**
 * 系统错误
 * Created by zhangrui25 on 2017/10/19.
 */
public class SystemException extends  RuntimeException{
    private static final long serialVersionUID = 1L;

    public SystemException(String message){
        super(message);
    }

    public SystemException(String message , Throwable e){
        super(message, e);
    }
}
