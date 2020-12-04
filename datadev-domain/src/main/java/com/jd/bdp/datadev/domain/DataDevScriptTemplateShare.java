package com.jd.bdp.datadev.domain;

import java.util.Date;

public class DataDevScriptTemplateShare{


    private Long id;
    private Long templateId;
    private Long shareType;//1;erp 2:分享给git或者gitgroup
    private String shareTarget;//share_type =1: erp.
    private String creator;
    private Date created;
    public DataDevScriptTemplateShare(){

    }

    public DataDevScriptTemplateShare( Long templateId, Long shareType, String shareTarget, String creator) {
        this.templateId = templateId;
        this.shareType = shareType;
        this.shareTarget = shareTarget;
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getShareType() {
        return shareType;
    }

    public void setShareType(Long shareType) {
        this.shareType = shareType;
    }

    public String getShareTarget() {
        return shareTarget;
    }

    public void setShareTarget(String shareTarget) {
        this.shareTarget = shareTarget;
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
}