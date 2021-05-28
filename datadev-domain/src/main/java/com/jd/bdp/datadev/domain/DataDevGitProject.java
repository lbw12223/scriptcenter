package com.jd.bdp.datadev.domain;

import java.util.Date;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public class DataDevGitProject {

    private Long id ;
    private Long gitProjectId;
    private String gitProjectPath;
    private String gitProjectName;
    private String lastCommitSha;
    private String branch;
    private Integer deleted;
    private Integer finishProjectTreeFlag;
    private Integer finishProjectMemberFlag;
    private Date refreshTime;


    private String memberState ;
    private Integer memberAccessLevel;

    private List<DataDevGitProjectSharedGroup> sharedGitProjectGroup;

    private Long groupId ;

    private String description ;

    private Integer projectType ;

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getGitProjectPath() {
        return gitProjectPath;
    }

    public void setGitProjectPath(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
    }

    public String getGitProjectName() {
        return gitProjectName;
    }

    public void setGitProjectName(String gitProjectName) {
        this.gitProjectName = gitProjectName;
    }

    public String getLastCommitSha() {
        return lastCommitSha;
    }

    public void setLastCommitSha(String lastCommitSha) {
        this.lastCommitSha = lastCommitSha;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getFinishProjectTreeFlag() {
        return finishProjectTreeFlag;
    }

    public void setFinishProjectTreeFlag(Integer finishProjectTreeFlag) {
        this.finishProjectTreeFlag = finishProjectTreeFlag;
    }

    public Integer getFinishProjectMemberFlag() {
        return finishProjectMemberFlag;
    }

    public void setFinishProjectMemberFlag(Integer finishProjectMemberFlag) {
        this.finishProjectMemberFlag = finishProjectMemberFlag;
    }

    public Date getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Date refreshTime) {
        this.refreshTime = refreshTime;
    }

    public List<DataDevGitProjectSharedGroup> getSharedGitProjectGroup() {
        return sharedGitProjectGroup;
    }

    public void setSharedGitProjectGroup(List<DataDevGitProjectSharedGroup> sharedGitProjectGroup) {
        this.sharedGitProjectGroup = sharedGitProjectGroup;
    }

    public String getMemberState() {
        return memberState;
    }

    public void setMemberState(String memberState) {
        this.memberState = memberState;
    }

    public Integer getMemberAccessLevel() {
        return memberAccessLevel;
    }

    public void setMemberAccessLevel(Integer memberAccessLevel) {
        this.memberAccessLevel = memberAccessLevel;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "DataDevGitProject{" +
                "gitProjectId=" + gitProjectId +
                ", gitProjectPath='" + gitProjectPath + '\'' +
                ", gitProjectName='" + gitProjectName + '\'' +
                ", lastCommitSha='" + lastCommitSha + '\'' +
                ", branch='" + branch + '\'' +
                ", deleted=" + deleted +
                ", finishProjectTreeFlag=" + finishProjectTreeFlag +
                ", finishProjectMemberFlag=" + finishProjectMemberFlag +
                ", refreshTime=" + refreshTime +
                ", sharedGitProjectGroup=" + sharedGitProjectGroup +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProjectType() {
        return projectType;
    }

    public void setProjectType(Integer projectType) {
        this.projectType = projectType;
    }
}
