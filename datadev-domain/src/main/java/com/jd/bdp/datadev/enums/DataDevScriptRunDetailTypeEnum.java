package com.jd.bdp.datadev.enums;

public enum DataDevScriptRunDetailTypeEnum {

    SQL(1, "Sql", ".sql"),
    Shell(2, "Shell", ".sh"),
    Python2(3, "Python", ".py"),
    Zip(4, "Zip", ".zip"),
    Plumber(5, "Plumber", ".sql");
    private Integer code;
    private String name;
    private String suffix;

    DataDevScriptRunDetailTypeEnum(Integer code, String name, String suffix) {
        this.code = code;
        this.name = name;
        this.suffix = suffix;
    }
    public String toSuffix() {
        return this.suffix;
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


    public static DataDevScriptRunDetailTypeEnum enumValueOf(Integer code) {
        DataDevScriptRunDetailTypeEnum[] enums = DataDevScriptRunDetailTypeEnum.values();
        for (DataDevScriptRunDetailTypeEnum constantEnum : enums) {
            if (constantEnum.toCode().equals(code)) {
                return constantEnum;
            }
        }
        return null;
    }

}
