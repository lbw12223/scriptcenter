package com.jd.bdp.datadev.domain;

public class DataDevScriptBuffaloJob {

    private Integer taskId;
    private String buffaloVersion;
    private String datadevVersion;
    private String taskVersion;//任务版本　1 4.0任务　２3.x任务
    private String taskVersionStr;//任务版本　1 4.0任务　２3.x任务
    private Long appGroupId;
    private String appGroupName;
    /**
     * 3.5审批状态 1测试任务 2线上任务 3待上线任务 4线下任务 5过期任务
     * 4.0审批状态 1-测试任务 2-上线审批中 3-线上任务 4-下线审批中 5-过期任务
     */
    private Long approveStatus;
    private String approveStatusStr;
    private String taskName;
    private String managers;
    private String managersName;
    private String description;
    private String buffaloKeyWord;

    public String getBuffaloKeyWord() {
        return buffaloKeyWord;
    }

    public void setBuffaloKeyWord(String buffaloKeyWord) {
        this.buffaloKeyWord = buffaloKeyWord;
    }

    public String getApproveStatusStr() {
        return approveStatusStr;
    }

    public void setApproveStatusStr(String approveStatusStr) {
        this.approveStatusStr = approveStatusStr;
    }

    public String getBuffaloVersion() {
        return buffaloVersion;
    }

    public void setBuffaloVersion(String buffaloVersion) {
        this.buffaloVersion = buffaloVersion;
    }

    public String getDatadevVersion() {
        return datadevVersion;
    }

    public void setDatadevVersion(String datadevVersion) {
        this.datadevVersion = datadevVersion;
    }

    public String getTaskVersion() {
        return taskVersion;
    }

    public void setTaskVersion(String taskVersion) {
        this.taskVersion = taskVersion;
    }

    public Long getAppGroupId() {
        return appGroupId;
    }

    public void setAppGroupId(Long appGroupId) {
        this.appGroupId = appGroupId;
    }

    public String getAppGroupName() {
        return appGroupName;
    }

    public void setAppGroupName(String appGroupName) {
        this.appGroupName = appGroupName;
    }

    public Long getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(Long approveStatus) {
        this.approveStatus = approveStatus;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getManagers() {
        return managers;
    }

    public void setManagers(String managers) {
        this.managers = managers;
    }

    public String getManagersName() {
        return managersName;
    }

    public void setManagersName(String managersName) {
        this.managersName = managersName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTaskVersionStr() {
        return taskVersionStr;
    }

    public void setTaskVersionStr(String taskVersionStr) {
        this.taskVersionStr = taskVersionStr;
    }
}
