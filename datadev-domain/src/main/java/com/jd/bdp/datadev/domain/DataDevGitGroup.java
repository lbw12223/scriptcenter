package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
public class DataDevGitGroup {

    private Long gitGroupId ;
    private String name ;
    private String path;
    private String fullName ;
    private String fullPath;
    private Integer finishProjectMemberFlag;

    public Long getGitGroupId() {
        return gitGroupId;
    }

    public void setGitGroupId(Long gitGroupId) {
        this.gitGroupId = gitGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Integer getFinishProjectMemberFlag() {
        return finishProjectMemberFlag;
    }

    public void setFinishProjectMemberFlag(Integer finishProjectMemberFlag) {
        this.finishProjectMemberFlag = finishProjectMemberFlag;
    }
}
