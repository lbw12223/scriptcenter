package com.jd.bdp.datadev.domain;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/1.
 *
 * 脚本配置表
 */
public class DataDevScriptConfig {

    private Long id ;
    private String name ;   //配置项属性名
    private Integer type ;  // 1: dataDev , 2: scriptCenter
    private String clusterCode ; //集群code
    private String marketLinuxUser; //集市linux用户
    private String queueCode ;  //队列code
    private String accountCode ; //生产账号code
    private Integer used ;  //是否使用此配置
    private String owner;//负责人
    private String creator ;
    private Date created ;
    private String mender;
    private Date modified ;
    private Integer deleted ;
    private Long showOrder;
    private Integer status;//1添加 2删除 3改变order 其他值:默认不变
    private Long oriId;//前端传过来的id值
    private Long marketId;
    private Long accountId;
    private Long queueId;
    private String engineType;//引擎
    private String runClusterCode;//实际运行的集群code
    private String runMarketLinuxUser;//实际运行的marketLinuxUser
    private Long projectSpaceId;//项目空间ID


    private String clusterName ;  //页面用于显示名称
    private String marketName ;   //页面用于显示名称
    private String accountName ;  //页面用于显示名称
    private String queueName ;    //页面用于显示名称
    private boolean hasRight;      //默认配置是否有权限
    private Integer configType = 1;    //1: 个人 ， 2 ： 项目空间默认




    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Long getOriId() {
        return oriId;
    }

    public void setOriId(Long oriId) {
        this.oriId = oriId;
    }

    public Long getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(Long showOrder) {
        this.showOrder = showOrder;
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

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public String getMarketLinuxUser() {
        return marketLinuxUser;
    }

    public void setMarketLinuxUser(String marketLinuxUser) {
        this.marketLinuxUser = marketLinuxUser;
    }

    public String getQueueCode() {
        return queueCode;
    }

    public void setQueueCode(String queueCode) {
        this.queueCode = queueCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getRunClusterCode() {
        return runClusterCode;
    }

    public void setRunClusterCode(String runClusterCode) {
        this.runClusterCode = runClusterCode;
    }

    public String getRunMarketLinuxUser() {
        return runMarketLinuxUser;
    }

    public void setRunMarketLinuxUser(String runMarketLinuxUser) {
        this.runMarketLinuxUser = runMarketLinuxUser;
    }

    public Long getProjectSpaceId() {
        return projectSpaceId;
    }

    public void setProjectSpaceId(Long projectSpaceId) {
        this.projectSpaceId = projectSpaceId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean isHasRight() {
        return hasRight;
    }

    public void setHasRight(boolean hasRight) {
        this.hasRight = hasRight;
    }

    public Integer getConfigType() {
        return configType;
    }

    public void setConfigType(Integer configType) {
        this.configType = configType;
    }
}
