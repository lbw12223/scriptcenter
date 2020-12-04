package com.jd.bdp.datadev.model;


import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class ScriptRunDetail implements Serializable {
    /**
     * 脚本运行记录id
     */
    private Long id;
    /**
     * 脚本文件id
     */
    private Long scriptFileId;
    /**
     * 脚本文件版本号
     */
    private String version;
    /**
     * 脚本运行操作人
     */
    private String operator;
    /**
     * 脚本运行开始时间
     */
    private Date startTime;
    /**
     * 脚本运行结束时间
     */
    private Date endTime;
    /**
     * {@link com.jd.bdp.datadev.enums.ScriptRunStatusEnum#toCode()}
     */
    private Integer status;
    /**
     * 集群code
     */
    private String clusterCode;
    /**
     * 集市linux用户
     */
    private String marketLinuxUser;
    /**
     * 队列code
     */
    private String queueCode;
    /**
     * 生产账号code
     */
    private String accountCode;

    /**
     * 脚本运行参数
     */
    private Map<String,String> args;

    private String creator;

    private Date created;
    /**
     * 脚本类型{@link com.jd.bdp.datadev.enums.ScriptTypeEnum#toCode()}
     */
    private Integer type;
    private String startShellPath;

    /**
     * jdq 使用的字段
     */
    private Integer currentDataIndex;
    private Integer currentLogIndex;
    /**
     * 数据库名
     */
    private String dbName;

    /*每页条数*/
    private Integer rows;
    /*请求当前第几页*/
    private Integer page;
    /**
     * 脚本运行参数
     */
    private Map<String,String> envs;

    private Integer argsImportType ; //ArgsImportTypeEnum
    private Long gitProjectId;
    private String gitProjectFilePath ;
    private Integer runType = 0;   //执行方式

    private String sqlContent ;
    private String engineType ;
    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScriptFileId() {
        return scriptFileId;
    }

    public void setScriptFileId(Long scriptFileId) {
        this.scriptFileId = scriptFileId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
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

    public Integer getCurrentDataIndex() {
        return currentDataIndex;
    }

    public void setCurrentDataIndex(Integer currentDataIndex) {
        this.currentDataIndex = currentDataIndex;
    }

    public Integer getCurrentLogIndex() {
        return currentLogIndex;
    }

    public void setCurrentLogIndex(Integer currentLogIndex) {
        this.currentLogIndex = currentLogIndex;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getArgsImportType() {
        return argsImportType;
    }

    public void setArgsImportType(Integer argsImportType) {
        this.argsImportType = argsImportType;
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

    public Integer getRunType() {
        return runType;
    }

    public void setRunType(Integer runType) {
        this.runType = runType;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }
}
