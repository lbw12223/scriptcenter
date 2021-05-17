package com.jd.bdp.datadev.enums;

public enum DataDevProjectTypeEnum {



    ALL(-1, "All", -1L, -1L),
    GIT(1, "Git", 0L, 900000000L),
    CODING(2, "Coding", 900000000L, 1000000000L),
    LOCAL(3, "Local", 1000000000L, 1100000000L);


    public Integer code;
    public String desc;
    public Long idMin;
    public Long idMax;


    DataDevProjectTypeEnum(Integer code, String desc, Long idMin, Long idMax) {
        this.code = code;
        this.desc = desc;
        this.idMax = idMax;
        this.idMin = idMin;
    }

    public Integer tocode() {
        return this.code;
    }

    //ProjectType
    public static DataDevProjectTypeEnum enumValueOf(Integer code) {
        if (code != null) {
            DataDevProjectTypeEnum[] enums = DataDevProjectTypeEnum.values();
            for (DataDevProjectTypeEnum statusEnum : enums) {
                if (statusEnum.tocode().equals(code)) {
                    return statusEnum;
                }
            }
        }
        return null;
    }


}
