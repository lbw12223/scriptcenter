package com.jd.bdp.datadev.domain;

public class DataDevApplication {
    /** 应用Id*/
    private Integer appgroupId;
    /** 应用代码*/
    private String appgroupCode;
    /** 应用名称*/
    private String appgroupName;
    /** 应用英文名*/
    private String appgroupDomain;
    /** 产品线Id*/
    private Integer prodlineId;
    /** 产品线名称*/
    private String prodlineName;
    /** 产品线编码*/
    private String prodlineCode;
    /** 子公司Id*/
    private Integer buId;
    /** 子公司名称*/
    private String buName;
    /** 子公司编码*/
    private String buCode;
    /** 应用秘钥*/
    private String accesskey;
    /**是否是默认应用 0 否 1 是*/
    private Integer defaultStatus;

    public Integer getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(Integer defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public String getProdlineName() {
        return prodlineName;
    }

    public void setProdlineName(String prodlineName) {
        this.prodlineName = prodlineName;
    }

    public String getProdlineCode() {
        return prodlineCode;
    }

    public void setProdlineCode(String prodlineCode) {
        this.prodlineCode = prodlineCode;
    }

    public String getBuCode() {
        return buCode;
    }

    public void setBuCode(String buCode) {
        this.buCode = buCode;
    }

    public String getBuName() {
        return buName;
    }

    public void setBuName(String buName) {
        this.buName = buName;
    }

    public Integer getAppgroupId() {
        return appgroupId;
    }

    public void setAppgroupId(Integer appgroupId) {
        this.appgroupId = appgroupId;
    }

    public String getAppgroupCode() {
        return appgroupCode;
    }

    public void setAppgroupCode(String appgroupCode) {
        this.appgroupCode = appgroupCode;
    }

    public String getAppgroupName() {
        return appgroupName;
    }

    public void setAppgroupName(String appgroupName) {
        this.appgroupName = appgroupName;
    }

    public String getAppgroupDomain() {
        return appgroupDomain;
    }

    public void setAppgroupDomain(String appgroupDomain) {
        this.appgroupDomain = appgroupDomain;
    }

    public Integer getProdlineId() {
        return prodlineId;
    }

    public void setProdlineId(Integer prodlineId) {
        this.prodlineId = prodlineId;
    }

    public Integer getBuId() {
        return buId;
    }

    public void setBuId(Integer buId) {
        this.buId = buId;
    }

    public String getAccesskey() {
        return accesskey;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }
}
