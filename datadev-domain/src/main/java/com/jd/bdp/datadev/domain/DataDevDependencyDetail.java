package com.jd.bdp.datadev.domain;

public class DataDevDependencyDetail {
    private Long id;
    private Long dependencyId;
    private Long scriptId;
    private String gitProjectFilePath;
    private Long gitProjectId;
    private String dependencyGitProjectFilePath;
    private String dependencyVersion;
    public DataDevDependencyDetail(){

    }
    public DataDevDependencyDetail(DataDevDependencyDetail detail){
        this.id = detail.getId();
        this.dependencyId=detail.getDependencyId();
        this.scriptId=detail.getScriptId();
        this.gitProjectFilePath=detail.getGitProjectFilePath();
        this.gitProjectId = detail.getGitProjectId();
        this.dependencyGitProjectFilePath = detail.getDependencyGitProjectFilePath();
        this.dependencyVersion = detail.getDependencyVersion();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(Long dependencyId) {
        this.dependencyId = dependencyId;
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

    public String getDependencyGitProjectFilePath() {
        return dependencyGitProjectFilePath;
    }

    public void setDependencyGitProjectFilePath(String dependencyGitProjectFilePath) {
        this.dependencyGitProjectFilePath = dependencyGitProjectFilePath;
    }

    public String getDependencyVersion() {
        return dependencyVersion;
    }

    public void setDependencyVersion(String dependencyVersion) {
        this.dependencyVersion = dependencyVersion;
    }
}
