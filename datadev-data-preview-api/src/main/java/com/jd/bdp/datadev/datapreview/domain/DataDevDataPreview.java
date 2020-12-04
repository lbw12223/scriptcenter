package com.jd.bdp.datadev.datapreview.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhanglei68.
 * @date 2019-03-01.
 * @time 17:00.
 */
public class DataDevDataPreview implements Serializable {
    private Long id;
    private Long runDetailId;
    private String clusterCode;
    private String linuxUser;
    private String dbName;
    private String tbName;
    private Date modified;
    private String creator;
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRunDetailId() {
        return runDetailId;
    }

    public void setRunDetailId(Long runDetailId) {
        this.runDetailId = runDetailId;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public String getLinuxUser() {
        return linuxUser;
    }

    public void setLinuxUser(String linuxUser) {
        this.linuxUser = linuxUser;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTbName() {
        return tbName;
    }

    public void setTbName(String tbName) {
        this.tbName = tbName;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
