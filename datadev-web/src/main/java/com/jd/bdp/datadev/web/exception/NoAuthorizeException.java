package com.jd.bdp.datadev.web.exception;

/**
 * 无权限
 * Created by zhangrui25 on 2017/10/19.
 */
public class NoAuthorizeException extends  RuntimeException{
    private static final long serialVersionUID = 1L;

    public NoAuthorizeException(String message){
        super(message);
    }

    public NoAuthorizeException(String message , Throwable e){
        super(message, e);
    }
}
