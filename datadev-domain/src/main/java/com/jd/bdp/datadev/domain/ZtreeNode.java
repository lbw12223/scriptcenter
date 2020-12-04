package com.jd.bdp.datadev.domain;

import java.util.List;

public class ZtreeNode {
    private String name;
    private Long id;
    private Integer parChl;//是目录还是文件  0目录 1文件
    private Integer runType;//如果是目录：0单文件运行 1以目录方式运行
    private Long pId;
    private Integer type;//脚本类型
    private List<ZtreeNode> children;
    private ZtreeNode parent;
    private String path;
    private String parentPath;
    private Long gitProjectId;
    private String version;
    private String md5;
    private Integer openStatus; //1 打开 其他都是关闭
    private Integer fullDir; // 1 文件夹带上所有子目录文件  0只有空文件夹
    private Integer selected;//0未选中 1选中
    private boolean chkDisabled; // true:禁用
    private boolean targetDir;//true:是打包目标目录

    private Integer tableLevel;//表明第几层，直接用ztree框架不需要这个属性，
    private String lastVersion;//最新版本
    private String lastModified;//最近修改时间
    private String modifier;
    private Integer deleted;
    private String gitStatus;

    public ZtreeNode() {

    }

    public ZtreeNode(DataDevScriptDir dird) {
        this.name = dird.getName();
        this.parChl = 0;
        this.gitProjectId = dird.getGitProjectId();
        this.path = dird.getGitProjectDirPath();
        this.parentPath = dird.getGitParentProjectDirPath();
    }

    public ZtreeNode(DataDevScriptFile file) {
        this.name = file.getName();
        this.parChl = 1;
        this.path = file.getGitProjectFilePath();
        this.version = file.getVersion();
        this.parentPath = file.getGitProjectDirPath();
        this.type = file.getType();
        this.gitProjectId = file.getGitProjectId();
        this.id = file.getId();
    }

    public ZtreeNode(DataDevFunDetail dataDevFunDetail) {
        this.name = dataDevFunDetail.getName();
        this.parChl = 1;
        this.id = dataDevFunDetail.getId();
        this.pId = dataDevFunDetail.getFunDirId();
    }

    public ZtreeNode(DataDevFunDir dataDevFunDir) {
        this.name = dataDevFunDir.getDirName();
        this.parChl = 0;
        this.id = dataDevFunDir.getId();
        this.pId = dataDevFunDir.getpId();
    }

    public String getGitStatus() {
        return gitStatus;
    }

    public void setGitStatus(String gitStatus) {
        this.gitStatus = gitStatus;
    }

    public ZtreeNode getParent() {
        return parent;
    }

    public void setParent(ZtreeNode parent) {
        this.parent = parent;
    }

    public Integer getTableLevel() {
        return tableLevel;
    }

    public void setTableLevel(Integer tableLevel) {
        this.tableLevel = tableLevel;
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }

    public String getLastModified() {
        return lastModified;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public boolean isTargetDir() {
        return targetDir;
    }

    public void setTargetDir(boolean targetDir) {
        this.targetDir = targetDir;
    }

    public boolean isChkDisabled() {
        return chkDisabled;
    }

    public void setChkDisabled(boolean chkDisabled) {
        this.chkDisabled = chkDisabled;
    }

    public Integer getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(Integer openStatus) {
        this.openStatus = openStatus;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getpId() {
        return pId;
    }

    public void setpId(Long pId) {
        this.pId = pId;
    }

    public List<ZtreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ZtreeNode> children) {
        this.children = children;
    }

    public Integer getParChl() {
        return parChl;
    }

    public void setParChl(Integer parChl) {
        this.parChl = parChl;
    }
    public boolean isDir(){
        return parChl==0;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Integer getRunType() {
        return runType;
    }

    public void setRunType(Integer runType) {
        this.runType = runType;
    }

    public Integer getFullDir() {
        return fullDir;
    }

    public void setFullDir(Integer fullDir) {
        this.fullDir = fullDir;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }
}
