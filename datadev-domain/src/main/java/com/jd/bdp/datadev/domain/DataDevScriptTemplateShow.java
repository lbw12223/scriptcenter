package com.jd.bdp.datadev.domain;

public class DataDevScriptTemplateShow {


    private Long id;
    private Long templateId;
    private String erp;
    private Long showOrder;//置顶的显示顺序  值为0表示非置顶，值非0 表示置顶  ，值相同根据字母排序
    private Long deleted;

    public DataDevScriptTemplateShow() {

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

    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }

    public Long getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(Long showOrder) {
        this.showOrder = showOrder;
    }

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }
}