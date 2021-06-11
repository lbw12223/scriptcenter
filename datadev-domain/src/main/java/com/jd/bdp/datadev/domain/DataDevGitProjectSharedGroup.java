package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public class DataDevGitProjectSharedGroup {

    private Long id ;
    private Long gitProjectId;
    private Long gitGroupId;
    private String groupName ;
    private Integer groupAccessLevel;

    private Integer isCanSysProjectScript ;


    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public Long getGitGroupId() {
        return gitGroupId;
    }

    public void setGitGroupId(Long gitGroupId) {
        this.gitGroupId = gitGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getGroupAccessLevel() {
        return groupAccessLevel;
    }

    public void setGroupAccessLevel(Integer groupAccessLevel) {
        this.groupAccessLevel = groupAccessLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIsCanSysProjectScript() {
        return isCanSysProjectScript;
    }

    public void setIsCanSysProjectScript(Integer isCanSysProjectScript) {
        this.isCanSysProjectScript = isCanSysProjectScript;
    }

    @Override
    public String toString() {
        return "DataDevGitProjectSharedGroup{" +
                "gitProjectId=" + gitProjectId +
                ", gitGroupId=" + gitGroupId +
                ", groupName='" + groupName + '\'' +
                ", groupAccessLevel=" + groupAccessLevel +
                '}';
    }

}
