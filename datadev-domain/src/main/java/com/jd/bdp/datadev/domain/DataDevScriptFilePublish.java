package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/1.
 * <p>
 * 脚本发布记录表
 */
public class DataDevScriptFilePublish {

    private Long id;
    private String version;    //脚本保存版本号
    private String comment ;    //发布备注
    private String publisher ;  //发布人员
    private String publishSys ;     //发布系统调度 ja
    private Integer status ;    //发布状态 发布中 发不成功 发布失败
    private String creator;
    private Date created;
    private String mender;
    private Date modified;
    private Integer deleted;
    private String checkMd5 ;   //校验文件完整性
    private Long requestId;
    private String fileName;//文件名加后缀
    private Long gitProjectId;
    private String gitProjectFilePath;
    private Long applicationId;
    private Long buffaloScriptId;
    private String buffaloScriptVersion;
    private String applicationName;
    private Integer runType;
    private String runTypeStr;
    public String getRunTypeStr() {
        return runTypeStr;
    }

    public void setRunTypeStr(String runTypeStr) {
        this.runTypeStr = runTypeStr;
    }

    public Integer getRunType() {
        return runType;
    }

    public void setRunType(Integer runType) {
        this.runType = runType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getGitProjectFilePath() {
        return gitProjectFilePath;
    }

    public void setGitProjectFilePath(String gitProjectFilePath) {
        this.gitProjectFilePath = gitProjectFilePath;
    }
    private Integer start;
    private Integer limit;
    private String statusStr;
    private String modifiedStr;

    public String getModifiedStr() {
        return modifiedStr;
    }

    public void setModifiedStr(String modifiedStr) {
        this.modifiedStr = modifiedStr;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishSys() {
        return publishSys;
    }

    public void setPublishSys(String publishSys) {
        this.publishSys = publishSys;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getMender() {
        return mender;
    }

    public void setMender(String mender) {
        this.mender = mender;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getCheckMd5() {
        return checkMd5;
    }

    public void setCheckMd5(String checkMd5) {
        this.checkMd5 = checkMd5;
    }

    public Long getBuffaloScriptId() {
        return buffaloScriptId;
    }

    public void setBuffaloScriptId(Long buffaloScriptId) {
        this.buffaloScriptId = buffaloScriptId;
    }

    public String getBuffaloScriptVersion() {
        return buffaloScriptVersion;
    }

    public void setBuffaloScriptVersion(String buffaloScriptVersion) {
        this.buffaloScriptVersion = buffaloScriptVersion;
    }
}
