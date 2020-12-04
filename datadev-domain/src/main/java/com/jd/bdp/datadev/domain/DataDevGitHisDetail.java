package com.jd.bdp.datadev.domain;

import java.util.Date;


/**
 * 每次提交记录 详细数据
 */
public class DataDevGitHisDetail {
    private Long id;
    private Long gitProjectId;
    private String gitProjectFilePath;
    private Date created;
    private String creator;
    private String comment;
    private String commitId;
    private Long gitHisId;             //提交ID
    private Integer commitType;        //提交类型 DataDevHisTypeEnum
    private String beforeCommitIds ;   //前一次提交的ID
    private String email ;              //email

    private Integer start;
    private Integer limit ;
    private boolean isDir;
    private String keyWord ;

    private boolean isCommitByBDPIDE ;  //是否是BDP IDE提交的（从提交信息提取出来了erp）


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


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }



    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }


    public String getGitProjectFilePath() {
        return gitProjectFilePath;
    }

    public void setGitProjectFilePath(String gitProjectFilePath) {
        this.gitProjectFilePath = gitProjectFilePath;
    }

    public Long getGitHisId() {
        return gitHisId;
    }

    public void setGitHisId(Long gitHisId) {
        this.gitHisId = gitHisId;
    }

    public Integer getCommitType() {
        return commitType;
    }

    public void setCommitType(Integer commitType) {
        this.commitType = commitType;
    }

    public String getBeforeCommitIds() {
        return beforeCommitIds;
    }

    public void setBeforeCommitIds(String beforeCommitIds) {
        this.beforeCommitIds = beforeCommitIds;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public boolean isCommitByBDPIDE() {
        return isCommitByBDPIDE;
    }

    public void setCommitByBDPIDE(boolean commitByBDPIDE) {
        isCommitByBDPIDE = commitByBDPIDE;
    }
}
