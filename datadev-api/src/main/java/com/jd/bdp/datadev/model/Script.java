package com.jd.bdp.datadev.model;

import com.jd.bdp.datadev.enums.ScriptTypeEnum;

import java.io.Serializable;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class Script implements Serializable {
    private Long id;
    private String name;
    /** 脚本类型{@link ScriptTypeEnum#toCode()}*/
    private Integer type;
    /** 当上传脚本类型为zip时，必须指定脚本启动路径*/
    private String startShellPath;
    /** 脚本大小，单位是byte*/
    private Long size;
    /** 脚本版本号*/
    private String version;
    /** 线上发布的脚本号*/
    private String publishVersion;
    /** 脚本md5值*/
    private String fileMd5;
    /** 脚本目录id*/
    private Long dirId;
    /**脚本前缀标识*/
    private String hbasePreKey;
    private String owner;
    private String description;
    /**脚本目录路径*/
    private String gitProjectDirPath;
    /**脚本git应用id*/
    private Long gitProjectId;
    /**脚本git路径*/
    private String gitProjectFilePath;
    private String gitProjectPath ;
    private Long applicationId;
    private Long runDetailId;//用于存储到hbase

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getGitProjectDirPath() {
        return gitProjectDirPath;
    }

    public void setGitProjectDirPath(String gitProjectDirPath) {
        this.gitProjectDirPath = gitProjectDirPath;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getStartShellPath() {
        return startShellPath;
    }

    public void setStartShellPath(String startShellPath) {
        this.startShellPath = startShellPath;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getPublishVersion() {
        return publishVersion;
    }

    public void setPublishVersion(String publishVersion) {
        this.publishVersion = publishVersion;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Long getDirId() {
        return dirId;
    }

    public void setDirId(Long dirId) {
        this.dirId = dirId;
    }

    public String getHbasePreKey() {
        return hbasePreKey;
    }

    public void setHbasePreKey(String hbasePreKey) {
        this.hbasePreKey = hbasePreKey;
    }

    public String getGitProjectPath() {
        return gitProjectPath;
    }

    public void setGitProjectPath(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
    }

    public Long getRunDetailId() {
        return runDetailId;
    }

    public void setRunDetailId(Long runDetailId) {
        this.runDetailId = runDetailId;
    }
}
