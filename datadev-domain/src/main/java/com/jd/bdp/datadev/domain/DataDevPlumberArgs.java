package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/21.
 */
public class DataDevPlumberArgs {
    private Long id;
    private Long scriptRunDetailId;
    private Long scriptFileId;
    private String sourceSubprotocol;
    private String sourceDatabase;
    private String sourceExtend;
    private String targetType;
    private String targetHost;
    private String targetPort;
    private String targetDatabase;
    private String targetUser;
    private String targetPassword;
    private String targetTableName;
    private String targetCharset;
    private String targetExtend;
    private String targetExtend1;
    private String dataDate;//yyyy-MM-dd
    private String deleteSql;

    private String creator;
    private Date created;
    private String mender;
    private Date modified;
    private String sourceType;
    private String templateExtend;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScriptRunDetailId() {
        return scriptRunDetailId;
    }

    public void setScriptRunDetailId(Long scriptRunDetailId) {
        this.scriptRunDetailId = scriptRunDetailId;
    }

    public Long getScriptFileId() {
        return scriptFileId;
    }

    public void setScriptFileId(Long scriptFileId) {
        this.scriptFileId = scriptFileId;
    }

    public String getSourceSubprotocol() {
        return sourceSubprotocol;
    }

    public void setSourceSubprotocol(String sourceSubprotocol) {
        this.sourceSubprotocol = sourceSubprotocol;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceExtend() {
        return sourceExtend;
    }

    public void setSourceExtend(String sourceExtend) {
        this.sourceExtend = sourceExtend;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getTargetPassword() {
        return targetPassword;
    }

    public void setTargetPassword(String targetPassword) {
        this.targetPassword = targetPassword;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getTargetCharset() {
        return targetCharset;
    }

    public void setTargetCharset(String targetCharset) {
        this.targetCharset = targetCharset;
    }

    public String getTargetExtend() {
        return targetExtend;
    }

    public void setTargetExtend(String targetExtend) {
        this.targetExtend = targetExtend;
    }

    public String getTargetExtend1() {
        return targetExtend1;
    }

    public void setTargetExtend1(String targetExtend1) {
        this.targetExtend1 = targetExtend1;
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getTemplateExtend() {
        return templateExtend;
    }

    public void setTemplateExtend(String templateExtend) {
        this.templateExtend = templateExtend;
    }
}
