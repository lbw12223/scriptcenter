package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/1.
 *
 * 函数表
 */
public class DataDevFunDetail {
    private Long id ;
    private Long funDirId ;     //函数目录ID
    private String name ;       //函数名称
    private Integer type ;      //函数类型
    private String owner ;      //方法负责人
    private String format ;
    private String comment ;
    private String creator ;
    private Date created ;
    private String mender;
    private Date modified ;
    private String typeStr;
    private String modifiedStr;

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getModifiedStr() {
        return modifiedStr;
    }

    public void setModifiedStr(String modifiedStr) {
        this.modifiedStr = modifiedStr;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunDirId() {
        return funDirId;
    }

    public void setFunDirId(Long funDirId) {
        this.funDirId = funDirId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
}
