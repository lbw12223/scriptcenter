package com.jd.bdp.datadev.exception;

public class GitFileNotFoundException extends RuntimeException {
    public GitFileNotFoundException(){
        super();
    }
    public GitFileNotFoundException(String message){
        super(message);
    }
    public GitFileNotFoundException(String message, Exception e){
        super(message,e);
    }
}
