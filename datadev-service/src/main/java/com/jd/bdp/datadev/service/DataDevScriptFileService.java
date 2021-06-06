package com.jd.bdp.datadev.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevScriptGitStatusEnum;
import com.jd.bdp.datadev.jdgit.JDGitFiles;
import com.jd.bdp.datadev.model.Script;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface DataDevScriptFileService {


    /**
     * 只查找临时文件
     * 查找打包后的文件
     *
     * @param relationDependencyId
     * @return
     */
    DataDevScriptFile findTmpByRelationDependencyId(Long relationDependencyId);

    /**
     * 通过对外接口传递id方式都使用的的默认项目，不要改变这个值，有权限验证
     *
     * @return
     * @throws Exception
     */

    public Long getDefaultProjectId() throws Exception;

    /**
     * 通过对外接口传递id方式都使用的的默认项目，不要改变这个值，有权限验证
     *
     * @return
     * @throws Exception
     */

    public String getDefaultUserToken() throws Exception;


    DataDevScriptFile findById(Long id) throws Exception;


    /**
     * 检查上传脚本参数 并将参数设置dataDevScriptFile
     * 如果有id则检查 数据库应用目录id 与上传应用目录id是否相等
     *
     * @param script
     * @param
     * @throws Exception
     */
    void checkAndTransferScriptParam(Script script) throws Exception;


    /**
     * 根据应用id查找下面的脚本
     *
     * @param
     * @return
     * @throws Exception
     */
    List<DataDevScriptFile> findScriptsByFilter(DataDevScriptFile file) throws Exception;


    /**
     * 需要mender和applicationId验证权限
     * 以owner作为lock_erp
     * 保存脚本信息
     * 数据库更新记录
     * hbase存脚本
     *
     * @throws Exception
     * @Return 返回脚本的id version
     */
   /* DataDevScriptFile saveScriptFile(DataDevScriptFile file, DataDevScriptFile old) throws Exception;


    DataDevScriptFile saveScriptFile(DataDevScriptRunDetail runDetail) throws Exception;*/

    PageResultDTO list4page(DataDevScriptFileHis his, Pageable pageable) throws Exception;

    ZtreeNode copyScriptFile(DataDevScriptFile file) throws Exception;


    /**
     * 格式化sql语句
     *
     * @param sql
     * @return
     */
    String formatSqlToSrc(String sql) throws Exception;

    /**
     * 验证脚本名称
     *
     * @param name
     * @throws Exception
     */
    void validScriptName(String name) throws Exception;


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.

    /**
     * 单文件
     *
     * @param dataDevScriptFile
     */
    void insert(DataDevScriptFile dataDevScriptFile);

    /**
     * 多文件
     *
     * @param dataDevScriptFiles
     */
    void insertFiles(List<DataDevScriptFile> dataDevScriptFiles);

    /**
     * dos 改成unix编码
     *
     * @param content
     * @throws Exception
     */

    String dos2unix(String content) throws Exception;


    /**
     * 获取 Project 某个dirId下面的 脚本文件
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     * @throws Exception
     */
    List<DataDevScriptFile> getScriptsByGitProjectIdAndDirPath(Long gitProjectId, String gitProjectDirPath);

    /**
     * 获取 Project 某个dirId下面的 脚本文件
     *
     * @param gitProjectId
     * @param dirId
     * @return
     * @throws Exception
     */
    List<DataDevScriptFile> getScriptsByGitProjectIdAndDirId(Long gitProjectId, Long dirId);

    /**
     * 根据project与脚本全路径唯一确定一个脚本
     *
     * @param gitProjectId
     * @param filePath
     * @return
     */
    DataDevScriptFile getScriptByGitProjectIdAndFilePath(Long gitProjectId, String filePath);

    /**
     * 可以查出来被删除脚本
     *
     * @param gitProjectId
     * @param filePath
     * @return
     */
    DataDevScriptFile getScriptByGitProjectIdAndFilePathIgnoreDelete(Long gitProjectId, String filePath, String version);

    DataDevScriptFile getScriptByGitProjectIdAndFilePath(Long gitProjectId, String filePath, String version);

    /**
     * 修改脚本
     *
     * @param gitProjectId
     * @param filePath
     * @param params
     */
    void updateDataDevScriptFile(Long gitProjectId, String filePath, DataDevScriptFile params);

    /**
     * 获取可编辑文件的内容
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     */
    String getScriptContent(Long gitProjectId, String gitProjectFilePath, String version, String erp) throws Exception;

    String getScriptContentFromHbase(Long gitProjectId, String gitProjectFilePath, String version) throws Exception;

    String getScriptContentFromHbase(Long fileId, String version) throws Exception;


    /**
     * 获取脚本byte
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @return
     * @throws Exception
     */
    byte[] getScriptBytes(Long gitProjectId, String gitProjectFilePath, String version, String erp) throws Exception;

    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param version
     * @param erp
     * @param cache              是否使用缓存
     * @return
     * @throws Exception
     */
    byte[] getScriptBytes(Long gitProjectId, String gitProjectFilePath, String version, String erp, boolean cache) throws Exception;

    /**
     * 将某一次依赖打包成zip
     *
     * @param dependencyId
     * @param filePath     打包后的zip路径
     * @param erp
     * @return
     * @throws Exception
     */
    ZtreeNode packZip(Long dependencyId, String filePath, String erp, String comment) throws Exception;


    byte[] getScriptBytesFromHbase(Long gitProjectId, String gitProjectFilePath, String version) throws Exception;


    byte[] copyGitFileToHbase(DataDevScriptFile dataDevScriptFile, DataDevGitProject dataDevGitProject, String erp) throws Exception;

    ZtreeNode createNewFile(Long gitProjectId, String gitProjectFilePath, Integer scriptType, String erp, Integer isShow, byte[] bytes, String description, String startShellPath) throws Exception;

    ZtreeNode createNewFile(Long gitProjectId, String gitProjectFilePath, Integer scriptType, String erp, Integer isShow, byte[] bytes, String description, String startShellPath, String tmpDirPath) throws Exception;

    ZtreeNode createNewFile(Long gitProjectId, String gitProjectFilePath, Integer scriptType, String erp, Integer isShow, byte[] bytes, String description, String startShellPath, String tmpDirPath, String args, Long dependencyId, Long scriptUploadId) throws Exception;


    HoldDoubleValue<Boolean, JDGitFiles> tryUpdateFile(Long gitProjectId, String gitProjectFilePath, String erp, byte[] bytes, String version, String gitVersion, boolean isDiscover, String description, String startShellPath, Long relationDependencyId) throws Exception;

    HoldDoubleValue<Boolean, JDGitFiles> tryUpdateFile(Long gitProjectId, String gitProjectFilePath, String erp, byte[] bytes, String version, String gitVersion, boolean isDiscover) throws Exception;


    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @throws Exception
     */

    void deleteScriptFile(Long gitProjectId, String gitProjectFilePath, String erp) throws Exception;

    /**
     * rename
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param newFileName
     * @param erp
     * @return
     * @throws Exception
     */
    DataDevScriptFile renameScriptFile(Long gitProjectId, String gitProjectFilePath, String newFileName, String erp) throws Exception;

    /**
     * 保存文件
     *
     * @param dataDevScriptFile
     * @param gitProjectId
     * @param isDirectCover
     * @return
     * @throws Exception
     */
    /*   HoldDoubleValue<Boolean, JDGitFiles> updateScriptFileContent(DataDevScriptFile dataDevScriptFile, Long gitProjectId, Boolean isDirectCover) throws Exception*/;


    ZtreeNode moveScriptFile(Long gitProjectId, String gitProjectFilePath, String newDirPath, String newName, String description, String erp) throws Exception;


    /**
     * 获取GitContent
     *
     * @param gitProjectId
     * @param gitProjecFilePath
     * @param gitVersion
     * @return
     * @throws Exception
     */
    JDGitFiles getGitContent(Long gitProjectId, String gitProjecFilePath, String gitVersion) throws Exception;

    /**
     * pull文件内容
     *
     * @param gitProjectId
     * @param gitProjecFilePath
     * @param erp
     * @return
     * @throws Exception
     */
    DataDevScriptFile pullFile(Long gitProjectId, String gitProjecFilePath, String erp) throws Exception;

    /**
     * pull dir
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     * @throws Exception
     */
    HoldDoubleValue<List<DataDevScriptFile>, List<ZtreeNode>> pullDir(Long gitProjectId, String gitProjectDirPath, String erp) throws Exception;

    /**
     * pushDir
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @param erp
     * @return
     * @throws Exception
     */
    List<DataDevScriptFile> pushDir(Long gitProjectId, String gitProjectDirPath, String message, String erp) throws Exception;

    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @return
     * @throws Exception
     */
    DataDevScriptFile pushFile(Long gitProjectId, String gitProjectFilePath, String commitMessage, String erp) throws Exception;

    /**
     * 直接push
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param commitMessage
     * @param erp
     * @return
     * @throws Exception
     */
    DataDevScriptFile pushFileDirect(Long gitProjectId, String gitProjectFilePath, String commitMessage, String erp) throws Exception;



    /**
     * 在Git上创建文件
     *
     * @param dataDevGitProject
     * @param fileName
     * @param gitProjectFilePath
     * @param erp
     * @param scriptType
     * @param bytes
     * @throws Exception
     */
    void createGitFile(DataDevGitProject dataDevGitProject, String fileName, String gitProjectFilePath, String erp, Integer scriptType, byte[] bytes) throws Exception;

    /**
     * 是否可以修改脚本名称 看是否已经同步了调度
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    boolean canMoveScriptFile(Long gitProjectId, String gitProjectFilePath) throws Exception;


    /**
     * 定时删除临时文件
     *
     * @throws Exception
     */
    void deleteTmpFile() throws Exception;

    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    Integer countScriptFile(Long gitProjectId, String gitProjectFilePath) throws Exception;

    Long getMaxFileCount(Long gitProjectId, String gitProjectFilePath) throws Exception;

    /**
     * 获取文件不存在的版本号
     *
     * @return
     * @throws Exception
     */
    String getNoVersion() throws Exception;


    /**
     * 获取某次依赖的文件
     *
     * @param dependencyId
     * @return
     */
    List<DataDevScriptFile> getDependencyFiles(@Param("dependencyId") Long dependencyId) throws Exception;


    /**
     * 根据指定upload_id查找对应文件信息
     *
     * @param scriptUpLoadId
     * @return
     */
    Page<DataDevScriptFile> findByScriptUpLoadId(Long scriptUpLoadId, Pageable pageable);


    /**
     * 查询没有初始化的GitVersionMd5
     *
     * @param gitProjectId
     * @return
     * @throws Exception
     */
    List<DataDevScriptFile> getNoLastGitVersionMd5(Long gitProjectId) throws Exception;

    /**
     * 初始化文件类型
     *
     * @throws Exception
     */
    Integer initScriptType(Integer limit) throws Exception;

    /**
     * 初始化文件历史表脚本类型字段
     *
     * @throws Exception
     */
    void initScriptHisType() throws Exception;

    /**
     * 临时文件保存的时候更新历史，与操作记录
     *
     * @param scriptFileId
     * @param gitProjectFilePath
     * @param scriptFileName
     */
    void tmpFileUpdateHisAndDetail(Long scriptFileId, String gitProjectFilePath, String scriptFileName, Long gitProjectId, String oldGirProjectFilePath) throws Exception;

    /**
     * 获取脚本信息
     * @param scriptId
     * @param version
     * @return
     * @throws Exception
     */
    JSONObject getScriptDetail(Long scriptId, String version) throws Exception;
}
