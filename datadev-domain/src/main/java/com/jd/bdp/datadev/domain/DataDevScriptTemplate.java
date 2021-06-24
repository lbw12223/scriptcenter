package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * @Author wangxiaoyu12
 * @Date 2018/11/1 11:13
 * @Description 脚本模板表
 **/
public class DataDevScriptTemplate {
    private Long id;
    private String name;   //模板名称
    private String desc;//模板描述
    private String content;   //模板内容
    private Integer scriptType;  //脚本类型  1：sql  2：shell 3：python
    private Integer pythonType;// 0 无意义 1Python2 2python3  只有在scriptType为3时有意义
    private Integer templateType;  //模板类型  0：系统模板  1：自定义模板
    private String incharge;//负责人
    private Integer status;//0 可见  1临时模板不可见 保存后可见
    private String creator;
    private Date created;
    private String mender;
    private Date modified;
    private Integer deleted;
    private String args ;
    private Long showOrder; //置顶的显示顺序  值为0表示非置顶，值非0 表示置顶  ，值相同根据字母排序
    private Long wordOrder;
    private Long scriptFileId;
    private String shareErps;
//    private String shareGits;
    private boolean shareGits;
    private Long gitProjectId; //相关联的脚本项目id
    private String gitProjectFilePath;//相关联的脚本路径
    private Integer templateFrom;//0自己创建 1通过分享

    public Integer getPythonType() {
        return pythonType;
    }

    public void setPythonType(Integer pythonType) {
        this.pythonType = pythonType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getScriptType() {
        return scriptType;
    }

    public void setScriptType(Integer scriptType) {
        this.scriptType = scriptType;
    }

    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    public String getIncharge() {
        return incharge;
    }

    public void setIncharge(String incharge) {
        this.incharge = incharge;
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

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Long getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(Long showOrder) {
        this.showOrder = showOrder;
    }

    public Long getScriptFileId() {
        return scriptFileId;
    }

    public void setScriptFileId(Long scriptFileId) {
        this.scriptFileId = scriptFileId;
    }

    public String getShareErps() {
        return shareErps;
    }

    public void setShareErps(String shareErps) {
        this.shareErps = shareErps;
    }

    public boolean getShareGits() {
        return shareGits;
    }

    public void setShareGits(boolean shareGits) {
        this.shareGits = shareGits;
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

    public Integer getTemplateFrom() {
        return templateFrom;
    }

    public void setTemplateFrom(Integer templateFrom) {
        this.templateFrom = templateFrom;
    }

    public Long getWordOrder() {
        return wordOrder;
    }

    public void setWordOrder(Long wordOrder) {
        this.wordOrder = wordOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DataDevScriptTemplate && ((DataDevScriptTemplate) obj).getId().equals(this.id)){
            return true;
        }
        return false;
    }
}