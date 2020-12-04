package com.jd.bdp.datadev.enums;

public enum DataDevHisTypeEnum {

    ADD(1, "新增"),
    MOIDFY(2, "修改"),
    DELETE(3, "删除");

    private Integer code;
    private String desc;

    DataDevHisTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer tocode() {
        return this.code;
    }

    public String toDesc() {
        return this.desc;
    }

    public static DataDevHisTypeEnum enumValueOf(Integer code) {
        DataDevHisTypeEnum[] enums = DataDevHisTypeEnum.values();
        for (DataDevHisTypeEnum statusEnum : enums) {
            if (statusEnum.tocode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }


}
