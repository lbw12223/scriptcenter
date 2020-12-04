package com.jd.bdp.datadev.enums;

/**
 * DataDevScript jdq 消息前缀
 */
public enum ScriptJdqMessageEnum {
    DATA(0, "%s_%s_%s_%04d", "${type}_${scriptRunDetailId}_%{messageType}_%{index}", 1000),
    LOG(1, "%s_%s_%s_%06d", "${type}_${scriptRunDetailId}_%{messageType}_%{index}", 1000),
    STATUS(2, "%s_%s_%s_%04d", "${type}_${scriptRunDetailId}_%{messageType}_%{index}", 10),
    EXECODE(3, "%s_%s_%s_%04d", "${type}_${scriptRunDetailId}_%{messageType}_%{index}", 10);

    private Integer code;
    private String format;
    private String desc;
    private Integer limit;


    ScriptJdqMessageEnum(Integer code, String format, String desc, Integer limit) {
        this.code = code;
        this.format = format;
        this.desc = desc;
        this.limit = limit;
    }

    public Integer toLimit() {
        return this.limit;
    }

    public Integer toCode() {
        return this.code;
    }

    public String toFormat() {
        return this.format;
    }


    public static ScriptJdqMessageEnum enumValueOf(Integer code) {
        ScriptJdqMessageEnum[] enums = ScriptJdqMessageEnum.values();
        for (ScriptJdqMessageEnum constantEnum : enums) {
            if (constantEnum.toCode().equals(code)) {
                return constantEnum;
            }
        }
        return null;
    }

}
