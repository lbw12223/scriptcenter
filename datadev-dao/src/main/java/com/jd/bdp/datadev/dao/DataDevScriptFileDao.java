package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFileHis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptFileDao {



    DataDevScriptFile findById(@Param("id") Long id);


    /**
     * 查找打包后的文件
     * @param relationDependencyId
     * @return
     */
    DataDevScriptFile findTmpByRelationDependencyId(@Param("relationDependencyId") Long relationDependencyId);


    /**
     * 插入脚本信息
     *
     * @param dataDevScriptFile
     * @return
     */
    Long insertScriptFile(DataDevScriptFile dataDevScriptFile);


    /**
     * 插入脚本信息
     *
     * @param files
     * @return
     */
    void insertScriptFiles(@Param("list") List<DataDevScriptFile> files);

    /**
     * 根据应用id查找脚本
     *
     * @param dataDevScriptFile
     * @return
     */
    List<DataDevScriptFile> findScriptsByFilter(DataDevScriptFile dataDevScriptFile);




    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.

    /**
     * 获取parentDirPath 下面的直接文件
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     */
    List<DataDevScriptFile> getScriptDirFile(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath);
    /**
     * 获取parentDirPath 下面的文件
     * 这个方法也会查询子目录的文件注意与 {@link com.jd.bdp.datadev.dao.DataDevScriptFileDao#getScriptDirFile(Long, String)} 区别
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     */
    List<DataDevScriptFile> getScriptDirFileRecur(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath);


    /**
     * 获取gitProjectId下面 gitProjectFilePath 文件
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     */
    DataDevScriptFile getSingleScriptFile(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);


    /**
     * 不考虑删除的情况
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     */
    DataDevScriptFile getSingleScriptFileIgnoreDeleted(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);



    /**
     * 删除具体的某个文件
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     */
    void realDeleteSingleScriptFile(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);

    /**
     * 删除gitProjectId 下面某个gitProjectDirPath
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     */
    void deleteScriptDirFile(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath);

    /**
     * 递归 删除gitProjectId 下面某个gitProjectDirPath
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     */
    void deleteScriptDirFileRecursion(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath);


    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param dataDevScriptFile
     */
    void updateGitScriptFile(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath, @Param("dataDevScriptFile") DataDevScriptFile dataDevScriptFile);


    /**
     * 查询某个项目下面的文件名个数
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     */
    Integer countScriptFile(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);


    /**
     * 根据path模糊查询
     *
     * @return
     */
    List<DataDevScriptFile> findScriptsByFuzzy(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);

    /**
     * 查询当前文件夹所有的数据
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     */
    List<DataDevScriptFile> queryDirAll(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath) ;

    List<DataDevScriptFile> getDependencyFiles(@Param("dependencyId") Long dependencyId);

    /**
     * 查询指定upload_id对应的文件个数
     * @param scriptUpLoadId
     * @return
     */
    int findCount(Long scriptUpLoadId);

    /**
     * 根据指定upload_id查找对应文件信息
     * @param scriptUpLoadId
     * @return
     */
    List<DataDevScriptFile> findByScriptUpLoadId(@Param("scriptUpLoadId") Long scriptUpLoadId, @Param("offSet") int offSet, @Param("pageSize") int pageSize);


    /**
     * 查询没有最新GitVersionMd5
     * @param gitProjectId
     * @return
     */
    List<DataDevScriptFile> getNoLastGitVersionMd5(@Param("gitProjectId") Long gitProjectId) ;

    List<DataDevScriptFile> getScriptsByType(@Param("scriptType")Integer scriptType,@Param("limit") Integer limit);

    void initScriptHisType();


    int countErpScriptFile(@Param("erp") String erp);


    int updateErpScriptFile(@Param("sourceErp") String sourceErp ,@Param("targetErp") String targetErp);


    List<DataDevScriptFile> selectAll(@Param("gitProjectId") Long gitProjectId) ;



    void fixBugUpdateVersion(@Param("fileId") Long fileId);





}
