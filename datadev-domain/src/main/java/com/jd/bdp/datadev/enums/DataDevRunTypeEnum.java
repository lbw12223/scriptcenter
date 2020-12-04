package com.jd.bdp.datadev.enums;

public enum DataDevRunTypeEnum {

    SingeRun(0, "单文件","单文件"),
    DependencyRun(1, "目录","目录");

    private Integer code;
    private String runDesc;
    private String uplineDesc;

    DataDevRunTypeEnum(Integer code, String runDesc ,String uplineDesc) {
        this.code = code;
        this.runDesc = runDesc;
        this.uplineDesc = uplineDesc;
    }

    public Integer tocode() {
        return this.code;
    }
    public String toRunDesc(){return this.runDesc;}
    public String toUplineDesc(){return this.uplineDesc;}


    //默认返回单文件运行
    public static DataDevRunTypeEnum enumValueOf(Integer code) {
        if(code!=null){
            DataDevRunTypeEnum[] enums = DataDevRunTypeEnum.values();
            for (DataDevRunTypeEnum statusEnum : enums) {
                if (statusEnum.tocode().equals(code)) {
                    return statusEnum;
                }
            }
        }
        return SingeRun;
    }


}
