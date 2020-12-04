package com.jd.bdp.datadev.domain;

import java.util.Date;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/3/1.
 * <p>
 * 脚本运行记录
 */
public class DataDevScriptRunDetail {

    private Long id;
    private Long scriptFileId;
    private Long scriptConfigId;
    private String version;
    private String operator;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String clusterCode;
    private String marketLinuxUser;
    private String queueCode;
    private String accountCode;
    private String args;
    private String creator;
    private Date created;
    private Integer type;
    private String startShellPath;

    private String ip;          //执行IP
    private String processId;  //执行进程

    private String scriptName;


    private Integer currentLogIndex;       //执行脚本时 当前日志index
    private Integer currentDataIndex;      //执行结果 dataIndex


    private Integer logCount;  //日志行数
    private Integer dataCount;  //数据行数
    private String dbName;


    private DataDevPlumberArgs dataDevPlumberArgs;
    private String content;//脚本信息
    private Long applicationId;//应用id
    /**
     * 脚本运行参数
     */
    private String envs;

    private String startTimeFromStr;
    private String startTimeToStr;
    private String endTimeFromStr;
    private String endTimetoStr;
    private Date startTimeFrom;
    private Date startTimeTo;
    private Date endTimeFrom;
    private Date endTimeTo;
    private String statusStr;
    private String timePeriod;
    private Integer start;
    private Integer limit;
    private List<Integer> statusList;
    private String responseCode;
    private Integer argsImportType;
    private Integer isShow; // 1 临时文件  其他正式文件
    private String stopErp;
    private Long gitProjectId;
    private String gitProjectFilePath;
    private String engineType;//hive或者presto，默认hive
    private String prestoQueryId;//presto执行时的ID
    private boolean isSysKill;//是否被系统强制停止
    private String userToken;
    private Object ext1; //预留字段
    private Object ext2; //预留字段
    private Integer runType;  //0：执行单个脚本 ， 1 ：依赖方式执行
    private String gitProjectDirPath;
    private String runTypeStr;
    private Long dependencyId;
    private Integer debugFlag;  //1：Debugyunx ， 0 ：普通运行

    private String runDetailFilePath; //客户端执行的时候，实际文件的路径
    private List<String> devDependencyDetails;
    private Integer pythonVersion; //0python2.x  1python3.x
    private Boolean runByContent;
    private Integer runTmp;
    private String runClusterCode;//实际运行的集群code
    private String runMarketLinuxUser;//实际运行的marketLinuxUser
    private Integer runByDataPreview;


    private Integer cgroupLimit = 1;
    private Integer cgroupMemoryLimit = 10;
    private Integer cgroupCpuLimit = 5;
    private String  doSqlToolCheck = "true";


    public Integer getRunByDataPreview() {
        return runByDataPreview;
    }

    public void setRunByDataPreview(Integer runByDataPreview) {
        this.runByDataPreview = runByDataPreview;
    }

    public Boolean isRunByContent() {
        return runByContent;
    }

    public void setRunByContent(Boolean runByContent) {
        this.runByContent = runByContent;
    }

    public Integer getPythonVersion() {
        return pythonVersion;
    }

    public void setPythonVersion(Integer pythonVersion) {
        this.pythonVersion = pythonVersion;
    }

    public Long getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(Long dependencyId) {
        this.dependencyId = dependencyId;
    }

    public String getRunTypeStr() {
        return runTypeStr;
    }

    public void setRunTypeStr(String runTypeStr) {
        this.runTypeStr = runTypeStr;
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

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getStopErp() {
        return stopErp;
    }

    public void setStopErp(String stopErp) {
        this.stopErp = stopErp;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public List<Integer> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Integer> statusList) {
        this.statusList = statusList;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getStartTimeFromStr() {
        return startTimeFromStr;
    }

    public void setStartTimeFromStr(String startTimeFromStr) {
        this.startTimeFromStr = startTimeFromStr;
    }

    public String getStartTimeToStr() {
        return startTimeToStr;
    }

    public void setStartTimeToStr(String startTimeToStr) {
        this.startTimeToStr = startTimeToStr;
    }

    public String getEndTimeFromStr() {
        return endTimeFromStr;
    }

    public void setEndTimeFromStr(String endTimeFromStr) {
        this.endTimeFromStr = endTimeFromStr;
    }

    public String getEndTimetoStr() {
        return endTimetoStr;
    }

    public void setEndTimetoStr(String endTimetoStr) {
        this.endTimetoStr = endTimetoStr;
    }

    public Date getStartTimeFrom() {
        return startTimeFrom;
    }

    public void setStartTimeFrom(Date startTimeFrom) {
        this.startTimeFrom = startTimeFrom;
    }

    public Date getStartTimeTo() {
        return startTimeTo;
    }

    public void setStartTimeTo(Date startTimeTo) {
        this.startTimeTo = startTimeTo;
    }

    public Date getEndTimeFrom() {
        return endTimeFrom;
    }

    public void setEndTimeFrom(Date endTimeFrom) {
        this.endTimeFrom = endTimeFrom;
    }

    public Date getEndTimeTo() {
        return endTimeTo;
    }

    public void setEndTimeTo(Date endTimeTo) {
        this.endTimeTo = endTimeTo;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
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

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getStartShellPath() {
        return startShellPath;
    }

    public void setStartShellPath(String startShellPath) {
        this.startShellPath = startShellPath;
    }

    public synchronized Integer getCurrentLogIndex() {
        return currentLogIndex;
    }

    public synchronized void setCurrentLogIndex(Integer currentLogIndex) {
        this.currentLogIndex = currentLogIndex;
    }

    public synchronized Integer getCurrentDataIndex() {
        return currentDataIndex;
    }

    public synchronized void setCurrentDataIndex(Integer currentDataIndex) {
        this.currentDataIndex = currentDataIndex;
    }

    public Integer getLogCount() {
        return logCount;
    }

    public void setLogCount(Integer logCount) {
        this.logCount = logCount;
    }

    public Integer getDataCount() {
        return dataCount;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }


    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public DataDevPlumberArgs getDataDevPlumberArgs() {
        return dataDevPlumberArgs;
    }

    public void setDataDevPlumberArgs(DataDevPlumberArgs dataDevPlumberArgs) {
        this.dataDevPlumberArgs = dataDevPlumberArgs;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getScriptConfigId() {
        return scriptConfigId;
    }

    public void setScriptConfigId(Long scriptConfigId) {
        this.scriptConfigId = scriptConfigId;
    }

    public String getEnvs() {
        return envs;
    }

    public void setEnvs(String envs) {
        this.envs = envs;
    }

    public Integer getArgsImportType() {
        return argsImportType;
    }

    public void setArgsImportType(Integer argsImportType) {
        this.argsImportType = argsImportType;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getPrestoQueryId() {
        return prestoQueryId;
    }

    public void setPrestoQueryId(String prestoQueryId) {
        this.prestoQueryId = prestoQueryId;
    }

    public boolean isSysKill() {
        return isSysKill;
    }

    public void setSysKill(boolean sysKill) {
        isSysKill = sysKill;
    }

    public Object getExt1() {
        return ext1;
    }

    public void setExt1(Object ext1) {
        this.ext1 = ext1;
    }

    public Object getExt2() {
        return ext2;
    }

    public void setExt2(Object ext2) {
        this.ext2 = ext2;
    }

    public Integer getRunType() {
        return runType;
    }

    public void setRunType(Integer runType) {
        this.runType = runType;
    }

    public String getGitProjectDirPath() {
        return gitProjectDirPath;
    }

    public void setGitProjectDirPath(String gitProjectDirPath) {
        this.gitProjectDirPath = gitProjectDirPath;
    }

    public Integer getDebugFlag() {
        return debugFlag;
    }

    public void setDebugFlag(Integer debugFlag) {
        this.debugFlag = debugFlag;
    }

    public String getRunDetailFilePath() {
        return runDetailFilePath;
    }

    public void setRunDetailFilePath(String runDetailFilePath) {
        this.runDetailFilePath = runDetailFilePath;
    }

    public List<String> getDevDependencyDetails() {
        return devDependencyDetails;
    }

    public void setDevDependencyDetails(List<String> devDependencyDetails) {
        this.devDependencyDetails = devDependencyDetails;
    }

    public Integer getRunTmp() {
        return runTmp;
    }

    public void setRunTmp(Integer runTmp) {
        this.runTmp = runTmp;
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

    public Integer getCgroupLimit() {
        return cgroupLimit;
    }

    public void setCgroupLimit(Integer cgroupLimit) {
        this.cgroupLimit = cgroupLimit;
    }

    public Integer getCgroupMemoryLimit() {
        return cgroupMemoryLimit;
    }

    public void setCgroupMemoryLimit(Integer cgroupMemoryLimit) {
        this.cgroupMemoryLimit = cgroupMemoryLimit;
    }

    public Integer getCgroupCpuLimit() {
        return cgroupCpuLimit;
    }

    public void setCgroupCpuLimit(Integer cgroupCpuLimit) {
        this.cgroupCpuLimit = cgroupCpuLimit;
    }

    public String getDoSqlToolCheck() {
        return doSqlToolCheck;
    }

    public void setDoSqlToolCheck(String doSqlToolCheck) {
        this.doSqlToolCheck = doSqlToolCheck;
    }
}
