package com.jd.bdp.datadev.domain.diff;

import java.util.Date;

/**
 *   提交信息对比
 */

public class DiffInfoVo {

    /**
     * 待比较信息
     */
    private Object content;


    private String name;

    private String fileType;

    private String version;

    private String applyErp;

    private String applyName;

    private Date applyTime;

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApplyErp() {
        return applyErp;
    }

    public void setApplyErp(String applyErp) {
        this.applyErp = applyErp;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
