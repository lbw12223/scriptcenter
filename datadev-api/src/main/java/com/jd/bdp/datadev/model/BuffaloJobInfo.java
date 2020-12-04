package com.jd.bdp.datadev.model;

import java.io.Serializable;

public class BuffaloJobInfo implements Serializable  {
    private String jobName;
    private Long marketId;
    private Long accountId;
    private Long queueId;
    private Long applicationId;
    private String owner;
    private Long scriptId;
    private String startShellPath;
    private Long buffaloScriptId;
    private String args;

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Long getBuffaloScriptId() {
        return buffaloScriptId;
    }

    public void setBuffaloScriptId(Long buffaloScriptId) {
        this.buffaloScriptId = buffaloScriptId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getScriptId() {
        return scriptId;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public String getStartShellPath() {
        return startShellPath;
    }

    public void setStartShellPath(String startShellPath) {
        this.startShellPath = startShellPath;
    }
}
