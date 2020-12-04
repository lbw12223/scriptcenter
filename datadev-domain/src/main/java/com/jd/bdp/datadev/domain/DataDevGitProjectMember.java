package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public class DataDevGitProjectMember {

    private Long id;
    private Long gitProjectId;
    private Long gitMemberId;
    private String gitMemberName; //别名
    private String gitMemberUserName; //erp
    private String state;
    private Integer accessLevel;
    private String accessLevelRight ;

    private String systemUserName ; //erp(中文名);

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

    public Long getGitMemberId() {
        return gitMemberId;
    }

    public void setGitMemberId(Long gitMemberId) {
        this.gitMemberId = gitMemberId;
    }

    public String getGitMemberName() {
        return gitMemberName;
    }

    public void setGitMemberName(String gitMemberName) {
        this.gitMemberName = gitMemberName;
    }

    public String getGitMemberUserName() {
        return gitMemberUserName;
    }

    public void setGitMemberUserName(String gitMemberUserName) {
        this.gitMemberUserName = gitMemberUserName;
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
        setAccessLevelRight();
    }

    public String getAccessLevelRight() {
        return accessLevelRight;
    }

    public void setAccessLevelRight() {
        if(this.accessLevel != null && this.accessLevel > 0){
            if( this.accessLevel <= 10){
                this.accessLevelRight = "Guest";
                return;
            }
            if( this.accessLevel <= 20){
                this.accessLevelRight = "Reporter";
                return;
            }
            if( this.accessLevel <= 30){
                this.accessLevelRight = "Developer";
                return;
            }
            if( this.accessLevel <= 40){
                this.accessLevelRight = "Master";
                return;
            }
            if( this.accessLevel <= 50){
                this.accessLevelRight = "Owner ";
                return;
            }

        }else{
            this.accessLevelRight = "Guest";
        }

    }

    public String getSystemUserName() {
        return systemUserName;
    }

    public void setSystemUserName(String systemUserName) {
        this.systemUserName = systemUserName;
    }

    @Override
    public String toString() {
        return "DataDevGitProjectMember{" +
                "id=" + id +
                ", gitProjectId=" + gitProjectId +
                ", gitMemberId=" + gitMemberId +
                ", gitMemberName='" + gitMemberName + '\'' +
                ", gitMemberUserName='" + gitMemberUserName + '\'' +
                ", state='" + state + '\'' +
                ", accessLevel=" + accessLevel +
                '}';
    }
}
