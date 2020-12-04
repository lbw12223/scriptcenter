package com.jd.bdp.datadev.web.exception;

/**
 * 参数错误
 * Created by zhangrui25 on 2017/10/19.
 */
public class ParamsException extends  RuntimeException{
    private static final long serialVersionUID = 1L;

    public ParamsException(String message){
        super(message);
    }

    public ParamsException(String message , Throwable e){
        super(message, e);
    }
}
