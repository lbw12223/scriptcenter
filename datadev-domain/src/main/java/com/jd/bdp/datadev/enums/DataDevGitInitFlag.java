package com.jd.bdp.datadev.enums;

public enum DataDevGitInitFlag {
    NEED_INIT(1, "需要初始化"),
    INIT_ING(2, "初始化中"),
    INIT_FINISH(3, "初始化完成");
    private Integer code;
    private String desc;

    DataDevGitInitFlag(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer tocode() {
        return this.code;
    }
}
