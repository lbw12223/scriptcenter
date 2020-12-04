package com.jd.bdp.datadev.service;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.jdgit.JDGitFiles;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DataDevScriptDirService {


    void getRecursiveDir(Set<String> set, List<DataDevScriptDir> list, Long gitProjectId, String gitProjectDirPath) throws Exception;
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     * 插入 DataDevScriptDir
     *
     * @param dir
     * @return
     * @throws Exception
     */
    Long insertDir(DataDevScriptDir dir) throws Exception;

    /**
     * 通过GitProjectId 和 path 获取DataDevScriptDir
     *
     * @param gitProjectId
     * @param path
     * @return
     */
    DataDevScriptDir getDataDevScriptDir(Long gitProjectId, String path);

    /**
     * 获取GitProjectId下的所有目录
     * @param isTargetSelect 是否获取target目录
     * @param gitProjectId
     * @return
     */
    List<ZtreeNode> getAllDataDevScriptDir(Long gitProjectId,boolean isTargetSelect);

    /**
     * 查询目录下面的文件
     *
     * @param gitProjectId
     * @param dirPath
     * @return
     */
    List<ZtreeNode> getDirFiles(Long gitProjectId, String dirPath, String[] openDirs, String[] selectFilePath, String selectDirPath, Integer range, boolean isRootSelect,Integer targetRange) throws Exception;

    /**
     * 获取目录下下的所有目录文件
     * @param mode 0全部文件  1只获取目录
     * @param gitProjectId
     * @return
     */
    List<ZtreeNode> getAllDirsAndFiles(Long gitProjectId,String rootPath,Integer mode);
    List<ZtreeNode> getAllDirsAndFiles(Long gitProjectId,String rootPath);





    /**
     * @param dataDevGitProject
     * @param gitParentDirPath
     * @return
     */
    void loadGitFileTree(DataDevGitProject dataDevGitProject, String gitParentDirPath, Long parentDirId, String queryDirPath) throws Exception;

    /**
     * @param gitProjectId
     * @param dirPath
     * @param erp
     * @return
     * @throws Exception
     */
    ZtreeNode createScriptDir(Long gitProjectId, String dirPath, String erp) throws Exception;

    /**
     * 在数据库里面创建 addDir , addFile 过后 对应的目录，文件
     *
     * @param jdGitFiles
     */
    ZtreeNode createFileOrDirInDataBase(JDGitFiles jdGitFiles, boolean isCreateDir);

    /**
     * 将文件路径按照目录形式返回
     *
     * @param gitProjectId
     * @param filePath
     * @param version
     * @return
     */
    public ZtreeNode createFilePathZtreeNode(Long gitProjectId, String filePath, String version);

    /**
     * 删除文件夹
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @param erp
     * @throws Exception
     */

    void deleteDir(Long gitProjectId, String gitProjectDirPath, String erp) throws Exception;



    /**
     * 创建目标目录
     * @param gitProjectId
     * @throws Exception
     */
    void createTargetDir(Long gitProjectId)throws Exception;

    /**
     * 获取打包目录 全量部异步获取
     * @param gitProjectId
     * @return
     * @throws Exception
     */
    List<ZtreeNode> getTargetDirs(Long gitProjectId)throws Exception;

    /**
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     * @throws Exception
     */
    JSONObject getPushNum(Long gitProjectId, String gitProjectDirPath, String gitProjectFilePath)throws Exception;

    /**
     * 是否是target目录下
     * @param path
     * @return
     * @throws Exception
     */
    boolean isBelongTarget(String path)throws Exception;

}
