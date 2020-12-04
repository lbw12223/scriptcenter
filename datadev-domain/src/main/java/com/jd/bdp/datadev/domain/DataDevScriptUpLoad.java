package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/2.
 *
 *
 * 目录表
 */
public class DataDevScriptUpLoad {
    private Long id ;
    private String creator;
    private Date created;
    private String description ;
    private Long gitProjectId ;                 //保存的gitProjectId
    private String gitProjectDirPath ;          //选择的保存的dir路径

    private int fileCount; //一次上传文件的个数

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getGitProjectDirPath() {
        return gitProjectDirPath;
    }

    public void setGitProjectDirPath(String gitProjectDirPath) {
        this.gitProjectDirPath = gitProjectDirPath;
    }

    public void setFileCount(int fileCount) {this.fileCount = fileCount;}

    public int getFileCount() {return fileCount;}
}
