package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevGitProjectSharedGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/22.
 */
public class JDGitProjectShareGroups extends GitConvertToDataDevDomain<DataDevGitProjectSharedGroup, JDGitProjectShareGroups> {

    private Long gitProjectId;
    private Long jdGroupId;
    private String groupName;
    private Integer groupAccessLevel;
    private String groupAccessLevelRight;

    //JDGitGroups{gitProjectId=23057, jdGroupId=14397, groupName='frank_test1', groupAccessLevel=40}
    @Override
    DataDevGitProjectSharedGroup convertDataDevDomain(JDGitProjectShareGroups jdGitProjectShareGroups) {
        DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup = new DataDevGitProjectSharedGroup();
        dataDevGitProjectSharedGroup.setGitProjectId(jdGitProjectShareGroups.getGitProjectId());
        dataDevGitProjectSharedGroup.setGitGroupId(jdGitProjectShareGroups.getJdGroupId());
        dataDevGitProjectSharedGroup.setGroupAccessLevel(jdGitProjectShareGroups.getGroupAccessLevel());
        dataDevGitProjectSharedGroup.setGroupName(jdGitProjectShareGroups.getGroupName());
        return dataDevGitProjectSharedGroup;
    }

    /**
     * 转换gitGroup
     *
     * @param gitGroups
     * @return
     */
    public static List<JDGitProjectShareGroups> covertGitGroups(Long gitProjectId, JSONArray gitGroups) {
        List<JDGitProjectShareGroups> result = new ArrayList<JDGitProjectShareGroups>();
        if (gitGroups != null && gitGroups.size() > 0) {
            for (int index = 0; index < gitGroups.size(); index++) {
                JDGitProjectShareGroups temp = new JDGitProjectShareGroups();
                JSONObject tempGroupJson = gitGroups.getJSONObject(index);
                temp.setGitProjectId(gitProjectId);
                temp.setJdGroupId(tempGroupJson.getLong("group_id"));
                temp.setGroupName(tempGroupJson.getString("group_name"));
                temp.setGroupAccessLevel(tempGroupJson.getInteger("group_access_level"));
                result.add(temp);
            }
        }
        return result;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public Long getJdGroupId() {
        return jdGroupId;
    }

    public void setJdGroupId(Long jdGroupId) {
        this.jdGroupId = jdGroupId;
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
        setGroupAccessLevelRight();
    }

    public String getGroupAccessLevelRight() {
        return groupAccessLevelRight;
    }

    public void setGroupAccessLevelRight() {
        if(this.groupAccessLevel != null && this.groupAccessLevel > 0){
            if( this.groupAccessLevel <= 10){
                this.groupAccessLevelRight = "Guest";
                return;
            }
            if( this.groupAccessLevel <= 20){
                this.groupAccessLevelRight = "Reporter";
                return;
            }
            if( this.groupAccessLevel <= 30){
                this.groupAccessLevelRight = "Developer";
                return;
            }
            if( this.groupAccessLevel <= 40){
                this.groupAccessLevelRight = "Master";
                return;
            }
            if( this.groupAccessLevel <= 50){
                this.groupAccessLevelRight = "Owner ";
                return;
            }
        }else{
            this.groupAccessLevelRight = "Guest";
        }
    }

    @Override
    public String toString() {
        return "JDGitGroups{" +
                "gitProjectId=" + gitProjectId +
                ", jdGroupId=" + jdGroupId +
                ", groupName='" + groupName + '\'' +
                ", groupAccessLevel=" + groupAccessLevel +
                '}';
    }
}
