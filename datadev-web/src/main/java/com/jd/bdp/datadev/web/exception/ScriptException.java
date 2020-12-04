package com.jd.bdp.datadev.web.exception;

public class ScriptException extends RuntimeException {
    public ScriptException(){
        super();
    }
    public ScriptException(Exception e){
        super(e);
    }
    public ScriptException(String message){
        super(message);
    }
    public ScriptException(String message,Exception e){
        super(message,e);
    }
}
