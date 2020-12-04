package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;

/**
 * Created by zhangrui25 on 2018/5/17.
 */
public class JDGitMembers extends GitConvertToDataDevDomain<DataDevGitProjectMember, JDGitMembers> implements JSONObjectCovertToGitDomain<JDGitMembers> {

    private Long gitProjectId;
    private String projectPath;
    private String projectName;
    private Long gitGroupId;

    private Long gitUserId;
    private String userName;
    private String state;          //active,
    private Integer accessLevel;   //10 => Guest access 20 => Reporter access 30 => Developer access 40 => Master access  50 => Owner access # Only valid for groups
    private String createdAt;
    private String name;



    @Override
    DataDevGitProjectMember convertDataDevDomain(JDGitMembers jdGitMembers) {
        DataDevGitProjectMember dataDevGitProjectMember = new DataDevGitProjectMember();
        dataDevGitProjectMember.setGitProjectId(jdGitMembers.getGitProjectId());
        dataDevGitProjectMember.setAccessLevel(jdGitMembers.getAccessLevel());
        dataDevGitProjectMember.setGitMemberId(jdGitMembers.getGitUserId());
        dataDevGitProjectMember.setGitMemberName(jdGitMembers.getName());
        dataDevGitProjectMember.setGitMemberUserName(jdGitMembers.getUserName());
        dataDevGitProjectMember.setState(jdGitMembers.getState());
        return dataDevGitProjectMember;
    }

    @Override
    public JDGitMembers covertGitDomain(JSONObject jsonObject) {
        JDGitMembers jdGitMember = new JDGitMembers();
        jdGitMember.setGitUserId(jsonObject.getLong("id"));
        jdGitMember.setName(jsonObject.getString("name"));
        jdGitMember.setUserName(jsonObject.getString("username"));
        jdGitMember.setState(jsonObject.getString("state"));
        jdGitMember.setAccessLevel(jsonObject.getInteger("access_level"));
        jdGitMember.setCreatedAt(jsonObject.getString("created_at"));
        return jdGitMember;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JDGitMembers that = (JDGitMembers) o;
        return userName.equals(that.userName);
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getGitUserId() {
        return gitUserId;
    }

    public void setGitUserId(Long gitUserId) {
        this.gitUserId = gitUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGitGroupId() {
        return gitGroupId;
    }

    public void setGitGroupId(Long gitGroupId) {
        this.gitGroupId = gitGroupId;
    }


    @Override
    public String toString() {
        return "JDGitMembers{" +
                "gitProjectId=" + gitProjectId +
                ", projectPath='" + projectPath + '\'' +
                ", projectName='" + projectName + '\'' +
                ", gitGroupId=" + gitGroupId +
                ", gitUserId=" + gitUserId +
                ", userName='" + userName + '\'' +
                ", state='" + state + '\'' +
                ", accessLevel=" + accessLevel +
                ", createdAt='" + createdAt + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
