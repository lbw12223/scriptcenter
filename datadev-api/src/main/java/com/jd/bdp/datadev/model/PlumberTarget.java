package com.jd.bdp.datadev.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/21.
 */
public class PlumberTarget implements Serializable {
    private String sourceSubprotocol;
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
    private Date dataDate;
    private String deleteSql;
    private String templateExtend;


    public String getSourceSubprotocol() {
        return sourceSubprotocol;
    }

    public void setSourceSubprotocol(String sourceSubprotocol) {
        this.sourceSubprotocol = sourceSubprotocol;
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

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    public String getTemplateExtend() {
        return templateExtend;
    }

    public void setTemplateExtend(String templateExtend) {
        this.templateExtend = templateExtend;
    }
}
