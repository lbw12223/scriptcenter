package com.jd.bdp.datadev.enums;

public enum DataDevGitAccessLevelEnum {
    Guest(10,"guest"),
    Reporter (20,"Reporter"),
    Developer(30,"Developer"),
    Master(40,"Master"),
    Owner(50,"Owner");
    private Integer code;
    private String desc;
    DataDevGitAccessLevelEnum(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }
    public Integer toCode(){
        return this.code;
    }
}
