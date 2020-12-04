package com.jd.bdp.datadev.enums;

public enum DataDevScriptLockStatusEnum {
    UnLock(0,"解锁状态"),
    Lock(1,"锁定状态");
    private Integer code;
    private String desc;
    DataDevScriptLockStatusEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public Integer tocode(){
        return this.code;
    }
}
