package com.jd.bdp.datadev.domain;

/**
 * @author zhanglei68.
 * @date 2019-03-01.
 * @time 11:36.
 */
public class UgdapTableInfo {

    private String businessLine;
    private String physicalClusterCode;
    private String owner;

    public String getBusinessLine() {
        return businessLine;
    }

    public void setBusinessLine(String businessLine) {
        this.businessLine = businessLine;
    }

    public String getPhysicalClusterCode() {
        return physicalClusterCode;
    }

    public void setPhysicalClusterCode(String physicalClusterCode) {
        this.physicalClusterCode = physicalClusterCode;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
