package com.jd.bdp.datadev.enums;

/**
 *  参数传入的方式
 */
public enum ArgsImportTypeEnum {
    STANDARD(1,"standard","standard"),
    FILE(2,"file","file");
    private Integer code;
    private String name;
    private String desc;
    ArgsImportTypeEnum(Integer code, String name, String desc){
        this.code   =   code;
        this.name   =   name;
        this.desc   =   desc;
    }

    public String toName(){
        return name;
    }
    public Integer toCode(){
        return code ;
    }
}
