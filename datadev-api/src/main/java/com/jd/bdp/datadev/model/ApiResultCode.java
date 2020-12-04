package com.jd.bdp.datadev.model;

/**
 * Created by zhangrui25 on 2018/3/8.
 */
public enum ApiResultCode {

    SUCCESS(0,"SUCCESS","成功"),
    FAILED(1,"FAILED","失败"),
    AUTHORITY(3,"AUTHORITY","无权限");

    private Integer code;
    private String name;
    private String desc;

    ApiResultCode(Integer code, String name, String desc){
        this.code=code;
        this.name=name;
        this.desc=desc;
    }
    /**
     * 获取枚举类型数值编码
     */
    public Integer toCode() {
        return this.code == null ? this.ordinal() : this.code;
    }

    /**
     * 获取枚举类型英文编码名称
     */
    public String toName() {
        return this.name == null ? this.name() : this.name;
    }

}
