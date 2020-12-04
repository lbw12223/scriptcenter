package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/1.
 *
 * 目录表
 */
public class DataDevScriptDir {

    private Long id ;
    private String name ;       //目录地址
    private Long pId ;          //上级目录ID


    private String creator ;
    private Date created ;
    private String mender;
    private Date modified ;
    private Integer deleted ;

    private Long gitProjectId ;
    private String gitProjectDirPath ;
    private String gitParentProjectDirPath ;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getpId() {
        return pId;
    }

    public void setpId(Long pId) {
        this.pId = pId;
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

    public String getMender() {
        return mender;
    }

    public void setMender(String mender) {
        this.mender = mender;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getGitProjectDirPath() {
        return gitProjectDirPath;
    }

    public void setGitProjectDirPath(String gitProjectDirPath) {
        this.gitProjectDirPath = gitProjectDirPath;
    }

    public String getGitParentProjectDirPath() {
        return gitParentProjectDirPath;
    }

    public void setGitParentProjectDirPath(String gitParentProjectDirPath) {
        this.gitParentProjectDirPath = gitParentProjectDirPath;
    }
}
