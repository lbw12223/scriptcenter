package com.jd.bdp.datadev.enums;

public enum DataDevScriptUploadStatusEnum {
    Running(1,"上传中"),
    Success(2,"上传成功"),
    Failure(3,"上传失败");



    private Integer code;
    private String name;
    DataDevScriptUploadStatusEnum(Integer code, String name){
        this.code=code;
        this.name=name;
    }

    public Integer toCode() {
        return code;
    }
}
