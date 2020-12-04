package com.jd.bdp.datadev.enums;

import org.apache.commons.lang3.StringUtils;

public enum DataDevGitOrCodingEnum {

    GIT(1, "Git"),
    CODING(2, "Coding");

    private Integer code;
    private String desc;

    DataDevGitOrCodingEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer tocode() {
        return this.code;
    }


    public static DataDevGitOrCodingEnum enumValueOf(Integer code) {
        DataDevGitOrCodingEnum[] enums = DataDevGitOrCodingEnum.values();
        for (DataDevGitOrCodingEnum statusEnum : enums) {
            if (statusEnum.tocode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }


    public static DataDevGitOrCodingEnum checkUrl(String webUrl){
        if(StringUtils.isNotBlank(webUrl)){
            if(webUrl.contains("coding.jd.com")){
                return DataDevGitOrCodingEnum.CODING;
            }else if(webUrl.contains("git.jd.com")){
                return DataDevGitOrCodingEnum.GIT;
            }
        }
        return null;
    }

}
