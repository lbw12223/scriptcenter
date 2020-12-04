package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/1.
 * <p>
 * 文件信息表
 */
public class DataDevScriptFileHis {

    private Long id ;
    private Long gitProjectId;
    private String gitProjectFilePath;
    private Long fileId;
    private Long dirId;
    private String name ;
    private Integer scriptOperatorType;
    private Integer type;
    private Long size ;
    private String version;
    private String gitVersion;
    private String creator;
    private Date created;
    private String commitMessage;
    private String fileMd5;

    private Integer start;
    private Integer limit;
    private Date startTime;
    private Date endTime;
    private String scriptOperatorTypeStr ;
    private String sysCreatorName ;
    private Long relationDependencyId;

    public Long getRelationDependencyId() {
        return relationDependencyId;
    }

    public void setRelationDependencyId(Long relationDependencyId) {
        this.relationDependencyId = relationDependencyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getGitProjectFilePath() {
        return gitProjectFilePath;
    }

    public void setGitProjectFilePath(String gitProjectFilePath) {
        this.gitProjectFilePath = gitProjectFilePath;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getDirId() {
        return dirId;
    }

    public void setDirId(Long dirId) {
        this.dirId = dirId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScriptOperatorType() {
        return scriptOperatorType;
    }

    public void setScriptOperatorType(Integer scriptOperatorType) {
        this.scriptOperatorType = scriptOperatorType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGitVersion() {
        return gitVersion;
    }

    public void setGitVersion(String gitVersion) {
        this.gitVersion = gitVersion;
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

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }


    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getScriptOperatorTypeStr() {
        return scriptOperatorTypeStr;
    }

    public void setScriptOperatorTypeStr(String scriptOperatorTypeStr) {
        this.scriptOperatorTypeStr = scriptOperatorTypeStr;
    }

    public String getSysCreatorName() {
        return sysCreatorName;
    }

    public void setSysCreatorName(String sysCreatorName) {
        this.sysCreatorName = sysCreatorName;
    }

    @Override
    public String toString() {
        return "DataDevScriptFileHis{" +
                "id=" + id +
                ", gitProjectId=" + gitProjectId +
                ", gitProjectFilePath='" + gitProjectFilePath + '\'' +
                ", fileId=" + fileId +
                ", dirId=" + dirId +
                ", name='" + name + '\'' +
                ", scriptOperatorType=" + scriptOperatorType +
                ", type=" + type +
                ", size=" + size +
                ", version='" + version + '\'' +
                ", gitVersion='" + gitVersion + '\'' +
                ", creator='" + creator + '\'' +
                ", created=" + created +
                ", commitMessage='" + commitMessage + '\'' +
                ", file_md5='" + fileMd5 + '\'' +
                ", start=" + start +
                ", limit=" + limit +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
