package com.jd.bdp.datadev.domain;

import java.util.Date;
import java.util.List;

public class DataDevDependency {
    private Long id;
    private Long scriptId;
    private String gitProjectFilePath;
    private Long gitProjectId;
    private Date created;
    private String creator;
    private String md5;
    private List<DataDevDependencyDetail> dependencyDetails;

    public List<DataDevDependencyDetail> getDependencyDetails() {
        return dependencyDetails;
    }

    public void setDependencyDetails(List<DataDevDependencyDetail> dependencyDetails) {
        this.dependencyDetails = dependencyDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScriptId() {
        return scriptId;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public String getGitProjectFilePath() {
        return gitProjectFilePath;
    }

    public void setGitProjectFilePath(String gitProjectFilePath) {
        this.gitProjectFilePath = gitProjectFilePath;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
