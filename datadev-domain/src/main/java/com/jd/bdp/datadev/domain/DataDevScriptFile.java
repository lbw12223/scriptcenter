package com.jd.bdp.datadev.domain;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/3/1.
 * <p>
 * 文件信息表
 */
public class DataDevScriptFile {

    private Long id;
    private Long dirId;    //目录ID
    private String name;   //目录地址
    private Integer type;  //脚本类型  1：sql  2：shell 3：python
    private Integer pythonType;//1 python2 2python3
    private Integer openType; //打开方式 0新建 1打开已有
    private Long size;     //脚本大小单位Byte
    private String version;    //脚本保存版本号
    private String gitVersion; //gitVersion
    private String publishVersion;  //最后发布版本
    private String hbasePreKey;    //脚本文件内容key前缀
    private String description;    //描述
    private String owner;           //文件负责人
    private Long applicationId;            //应用id
    private String startShellPath; //zip脚本运行路径
    private String fileMd5;
    private String creator;
    private Date created;
    private String mender;
    private Date modified;
    private Integer deleted;
    /*页面提交的Content*/
    private String content;
    /*Content File*/
    private File contentFile;
    private byte[] bytes;
    private Integer isShow; // 1 临时文件  其他正式文件
    private String applicationName;
    private String creatorName;
    private String createdStr;
    private Integer saveType;//0保存  1另存为   其他值或者默认值会校验md5 MD5一样不会保存
    private Integer exist;// 更新脚本的时候 1不检查重复  其他检测
    private String keyWord;
    private String gitProjectDirPath;
    private Long gitProjectId;
    private String gitProjectFilePath;
    private Integer whereIsNew; //1:git 2:hbase
    private Integer isBigFile;  //isBigFile

    private String newGitProjectFilePath;
    private String newGitVersion;  //新的Git版本    2018-12-20 同lastGitVersion
    private String typeStr;
    private String verDescription;
    private String args;

    private Integer limit;

    private List<Integer> typeList;
    private Long runDetailId;//用于存储到hbase
    private Long publishId;
    private Integer downType;//默认0正常下载，1下载打包文件
    private Integer isDirectRunVersion;    //是否指定版本运行 1:是 。 0: 否
    private Integer fileUpSamePathOpt = 0;     //上传文件遇到相同的filePath 处理 1：替换 2：保留 , 0 :正常操作
    private Long scriptUpLoadId;            //记录一下从上传来的ID

    private Long dependencyId; //依赖id
    private Long relationDependencyId; //依赖打包之后的文件对应的dependency_id

    private String sizeShow;

    private String lastGitVersionMd5;       //实时git版本md5
    private Date lastGitModified;           //实时git修改时间
    private String lastGitVersion;          //最新gitversion commit 版本
    private Integer gitDeleted ;            //git是否已经删除
    private Long templateId;                //模板id
    private boolean isTemplate;
    private String templateDesc;
    private String templateName;

    private String cgroupArgs ;             //cgroupArgs{cgroupCpuLimit,cgroupMemoryLimit,cgroupLimit}

    public DataDevScriptFile() {

    }

    public DataDevScriptFile(Long gitProjectId, String gitProjectFilePath) {
        this.gitProjectId = gitProjectId;
        this.gitProjectFilePath = gitProjectFilePath;
    }


    public Long getRelationDependencyId() {
        return relationDependencyId;
    }

    public void setRelationDependencyId(Long relationDependencyId) {
        this.relationDependencyId = relationDependencyId;
    }

    public Long getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(Long dependencyId) {
        this.dependencyId = dependencyId;
    }

    public Integer getDownType() {
        return downType;
    }

    public void setDownType(Integer downType) {
        this.downType = downType;
    }

    public Long getPublishId() {
        return publishId;
    }

    public void setPublishId(Long publishId) {
        this.publishId = publishId;
    }

    public Long getRunDetailId() {
        return runDetailId;
    }

    public void setRunDetailId(Long runDetailId) {
        this.runDetailId = runDetailId;
    }

    public String getVerDescription() {
        return verDescription;
    }

    public void setVerDescription(String verDescription) {
        this.verDescription = verDescription;
    }

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public Integer getExist() {
        return exist;
    }

    public void setExist(Integer exist) {
        this.exist = exist;
    }

    public Integer getSaveType() {
        return saveType;
    }

    public void setSaveType(Integer saveType) {
        this.saveType = saveType;
    }

    public String getCreatedStr() {
        return createdStr;
    }

    public void setCreatedStr(String createdStr) {
        this.createdStr = createdStr;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDirId() {
        return dirId;
    }

    public void setDirId(Long dirId) {
        this.dirId = dirId;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublishVersion() {
        return publishVersion;
    }

    public void setPublishVersion(String publishVersion) {
        this.publishVersion = publishVersion;
    }

    public String getHbasePreKey() {
        return hbasePreKey;
    }

    public void setHbasePreKey(String hbasePreKey) {
        this.hbasePreKey = hbasePreKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getStartShellPath() {
        return startShellPath;
    }

    public void setStartShellPath(String startShellPath) {
        this.startShellPath = startShellPath;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Integer getPythonType() {
        return pythonType;
    }

    public void setPythonType(Integer pythonType) {
        this.pythonType = pythonType;
    }

    public Integer getOpenType() {
        return openType;
    }

    public void setOpenType(Integer openType) {
        this.openType = openType;
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

    public Integer getWhereIsNew() {
        return whereIsNew;
    }

    public void setWhereIsNew(Integer whereIsNew) {
        this.whereIsNew = whereIsNew;
    }

    public Integer getIsBigFile() {
        return isBigFile;
    }

    public void setIsBigFile(Integer isBigFile) {
        this.isBigFile = isBigFile;
    }

    public String getGitProjectDirPath() {
        return gitProjectDirPath;
    }

    public void setGitProjectDirPath(String gitProjectDirPath) {
        this.gitProjectDirPath = gitProjectDirPath;
    }

    public String getNewGitProjectFilePath() {
        return newGitProjectFilePath;
    }

    public void setNewGitProjectFilePath(String newGitProjectFilePath) {
        this.newGitProjectFilePath = newGitProjectFilePath;
    }

    public void setGitVersion(String gitVersion) {
        this.gitVersion = gitVersion;
    }

    public String getGitVersion() {
        return gitVersion;
    }

    public String getNewGitVersion() {
        return newGitVersion;
    }

    public void setNewGitVersion(String newGitVersion) {
        this.newGitVersion = newGitVersion;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public List<Integer> getTypeList() {
        return typeList;
    }

    public void setTypeList(List<Integer> typeList) {
        this.typeList = typeList;
    }

    public Integer getIsDirectRunVersion() {
        return isDirectRunVersion;
    }

    public void setIsDirectRunVersion(Integer isDirectRunVersion) {
        this.isDirectRunVersion = isDirectRunVersion;
    }

    public Integer getFileUpSamePathOpt() {
        if (fileUpSamePathOpt == null) {
            return 0;
        }
        return fileUpSamePathOpt;
    }

    public void setFileUpSamePathOpt(Integer fileUpSamePathOpt) {
        this.fileUpSamePathOpt = fileUpSamePathOpt;
    }

    public Long getScriptUpLoadId() {
        return scriptUpLoadId;
    }

    public void setScriptUpLoadId(Long scriptUpLoadId) {
        this.scriptUpLoadId = scriptUpLoadId;
    }

    public void setSizeShow(String sizeShow) {
        this.sizeShow = sizeShow;
    }

    public String getSizeShow() {
        return sizeShow;
    }

    public String getLastGitVersionMd5() {
        return lastGitVersionMd5;
    }

    public void setLastGitVersionMd5(String lastGitVersionMd5) {
        this.lastGitVersionMd5 = lastGitVersionMd5;
    }

    public Date getLastGitModified() {
        return lastGitModified;
    }

    public void setLastGitModified(Date lastGitModified) {
        this.lastGitModified = lastGitModified;
    }

    public String getLastGitVersion() {
        return lastGitVersion;
    }

    public void setLastGitVersion(String lastGitVersion) {
        this.lastGitVersion = lastGitVersion;
    }

    public Integer getGitDeleted() {
        return gitDeleted;
    }

    public void setGitDeleted(Integer gitDeleted) {
        this.gitDeleted = gitDeleted;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCgroupArgs() {
        return cgroupArgs;
    }

    public void setCgroupArgs(String cgroupArgs) {
        this.cgroupArgs = cgroupArgs;
    }
}
