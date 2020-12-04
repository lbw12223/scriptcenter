package com.jd.bdp.datadev.web.exception;

public class DownLoadScriptException extends RuntimeException {
    public DownLoadScriptException(){
        super();
    }
    public DownLoadScriptException(Exception e){
        super(e);
    }
    public DownLoadScriptException(String message){
        super(message);
    }
    public DownLoadScriptException(String message, Exception e){
        super(message,e);
    }
}
