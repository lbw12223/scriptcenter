package com.jd.bdp.datadev.exception;

public class HbaseUploadException extends RuntimeException {
    public HbaseUploadException(){
        super();
    }
    public HbaseUploadException(String message){
        super(message);
    }
    public HbaseUploadException(String message,Exception e){
        super(message,e);
    }
}
