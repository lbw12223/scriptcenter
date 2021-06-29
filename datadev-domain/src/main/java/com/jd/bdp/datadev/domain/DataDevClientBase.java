package com.jd.bdp.datadev.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/9.
 */
public class DataDevClientBase {
    private Long id;
    private String ip;
    private Integer jobLimit;
    private Integer status;
    private String creator;
    private Date created;
    private String mender;
    private Date modified;
    private Integer deleted;
    private Date lastActiveTime;              //最后更新时间
    private Integer activeScriptRunDetail ;   // Client端运行数量
    private Integer cpu;
    private Integer disk;
    private String system ;

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getDisk() {
        return disk;
    }

    public void setDisk(Integer disk) {
        this.disk = disk;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getJobLimit() {
        return jobLimit;
    }

    public void setJobLimit(Integer jobLimit) {
        this.jobLimit = jobLimit;
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

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        /*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.parse(lastActiveTime);*/
        this.lastActiveTime = lastActiveTime;
    }

    public Integer getActiveScriptRunDetail() {
        return activeScriptRunDetail;
    }

    public void setActiveScriptRunDetail(Integer activeScriptRunDetail) {
        this.activeScriptRunDetail = activeScriptRunDetail;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "DataDevClientBase{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", jobLimit=" + jobLimit +
                ", status=" + status +
                ", creator='" + creator + '\'' +
                ", created=" + created +
                ", mender='" + mender + '\'' +
                ", modified=" + modified +
                ", deleted=" + deleted +
                ", lastActiveTime=" + lastActiveTime +
                ", activeScriptRunDetail=" + activeScriptRunDetail +
                ", cpu=" + cpu +
                ", disk=" + disk +
                '}';
    }
}
