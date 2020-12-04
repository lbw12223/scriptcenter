package com.jd.bdp.datadev.enums;

public enum DataDevScriptFunTypeEnum {
    SystemFun(1,"系统自定义函数"),
    UserFun(2,"用户自定义函数");
    private Integer code;
    private String desc;
    DataDevScriptFunTypeEnum(Integer code,String desc){
        this.code=code;
        this.desc=desc;
    }
    public Integer tocode(){
        return this.code;
    }
    public String toDesc(){
        return this.desc;
    }
    public static DataDevScriptFunTypeEnum enumValueOf(Integer code){
        DataDevScriptFunTypeEnum[] typeEnums=DataDevScriptFunTypeEnum.values();
        for(DataDevScriptFunTypeEnum typeEnum:typeEnums){
            if(typeEnum.tocode().equals(code)){
                return typeEnum;
            }
        }
            return null;
    }
}
