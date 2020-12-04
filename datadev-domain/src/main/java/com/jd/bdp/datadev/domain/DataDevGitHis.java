package com.jd.bdp.datadev.domain;

import java.util.Date;


/**
 * 每次提交记录数据 （目录PUSh，单次提交）
 */
public class DataDevGitHis {

    private Long id;
    private Long gitProjectId;
    private String branch;
    private String commitPath;
    private Date created;
    private String creator;
    private String email;
    private Date submitTime;
    private String submitErp;
    private String comment;
    private String commitId;
    private String beforeCommitId;     //上一次提交历史


    private Integer start;
    private Integer limit ;

    private boolean isCommitByBDPIDE = false;  //是否是BDP IDE提交的（从提交信息提取出来了erp）

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

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public String getSubmitErp() {
        return submitErp;
    }

    public void setSubmitErp(String submitErp) {
        this.submitErp = submitErp;
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

    public String getCommitPath() {
        return commitPath;
    }

    public void setCommitPath(String commitPath) {
        this.commitPath = commitPath;
    }

    public String getBeforeCommitId() {
        return beforeCommitId;
    }

    public void setBeforeCommitId(String beforeCommitId) {
        this.beforeCommitId = beforeCommitId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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

    public boolean isCommitByBDPIDE() {
        return isCommitByBDPIDE;
    }

    public void setCommitByBDPIDE(boolean commitByBDPIDE) {
        isCommitByBDPIDE = commitByBDPIDE;
    }
}
