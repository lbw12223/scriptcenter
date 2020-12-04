package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public class DataDevGitGroupMember {

    private Long id ;
    private Long gitGroupId ;
    private Long gitMemberId;
    private String gitMemberName;
    private String gitMemberUserName;
    private String state;
    private Integer accessLevel;
    private String accessLevelRight ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitGroupId() {
        return gitGroupId;
    }

    public void setGitGroupId(Long gitGroupId) {
        this.gitGroupId = gitGroupId;
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

    @Override
    public String toString() {
        return "DataDevGitGroupMember{" +
                "id=" + id +
                ", gitGroupId=" + gitGroupId +
                ", gitMemberId=" + gitMemberId +
                ", gitMemberName='" + gitMemberName + '\'' +
                ", gitMemberUserName='" + gitMemberUserName + '\'' +
                ", state='" + state + '\'' +
                ", accessLevel=" + accessLevel +
                ", accessLevelRight='" + accessLevelRight + '\'' +
                '}';
    }
}
