package com.jd.bdp.datadev.enums;

public enum DataDevScriptFileWhereIsNewEnum {

    GIT(1, "Git"),
    HBASE(2, "hbase"),
    BOTH(3, "Both"),
    NOCONTENT(4, "NOCONTENT");

    private Integer code;
    private String desc;

    DataDevScriptFileWhereIsNewEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer tocode() {
        return this.code;
    }

    public String toDesc() {
        return this.desc;
    }
}
