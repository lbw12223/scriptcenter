package com.jd.bdp.datadev.enums;

/**
 * Created by zhanglei68 on 2017/10/31.
 */
public enum ScriptTypeEnum {
    SQL(1, "Sql", ".sql"),
    Shell(2, "Shell", ".sh"),
    Python2(3, "Python", ".py"),
    Zip(4, "Zip", ".zip");

    private Integer code;
    private String name;
    private String suffix;

    ScriptTypeEnum(Integer code, String name, String suffix) {
        this.code = code;
        this.name = name;
        this.suffix = suffix;

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

    /**
     * 返回后缀名
     *
     * @return
     */
    public String toSuffix() {
        return this.suffix;
    }

    public static ScriptTypeEnum enumValueOf(Integer code) {
        ScriptTypeEnum[] enums = ScriptTypeEnum.values();
        for (ScriptTypeEnum constantEnum : enums) {
            if (constantEnum.toCode().equals(code)) {
                return constantEnum;
            }
        }
        return null;
    }

    public static String suffixOf(Integer code) {
        ScriptTypeEnum[] scriptTypeEnums = ScriptTypeEnum.values();
        for (ScriptTypeEnum dataDevScriptTypeEnum : scriptTypeEnums) {
            if (dataDevScriptTypeEnum.toCode() == code) {
                return dataDevScriptTypeEnum.toSuffix();
            }
        }
        return null;
    }
}
