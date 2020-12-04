package com.jd.bdp.datadev.enums;

public enum  DataDevScriptSaveTypeEnum {
    Save(0,"保存，内容改变"),
    SaveAs(1,"另存为"),
    SaveNoFile(2,"只保存属性，不改变内容");
    private Integer code;//保存类型
    private String desc;//描述

    DataDevScriptSaveTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer toCode() {
        return code;
    }

    public String toDesc() {
        return this.desc;
    }


}
