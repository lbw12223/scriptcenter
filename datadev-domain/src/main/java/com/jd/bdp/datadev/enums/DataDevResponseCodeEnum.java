package com.jd.bdp.datadev.enums;

public enum DataDevResponseCodeEnum {
    Success(200, "成功"),
    DependencyNotFound(101,"依赖不存在");


    DataDevResponseCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    private Integer code;
    private String desc;

    public Integer toCode() {
        return this.code;
    }

    public String toDesc() {
        return this.desc;
    }
}
