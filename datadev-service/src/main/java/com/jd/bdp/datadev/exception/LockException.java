package com.jd.bdp.datadev.exception;

public class LockException extends Exception{
    public LockException(){
        super();
    }
    public LockException(Exception e){
        super(e);
    }
    public LockException(String message){
        super(message);
    }
    public LockException(String message,Exception e){
        super(message,e);
    }
}
