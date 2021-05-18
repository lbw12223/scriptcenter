package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptDir;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptDirDao {


    /**
     * 添加目录
     *
     * @param dir
     */
    Long insertDir(DataDevScriptDir dir);

    /**
     * 根据id查找目录
     *
     * @param id
     * @return
     */
    DataDevScriptDir findById(Long id);

    /**
     * 根据id查找目录
     *
     * @param gitProjectId
     * @return
     */
    List<DataDevScriptDir> getDirsByProjectId(@Param("gitProjectId") Long gitProjectId);

    /**
     * deleted 置1
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 修改目录名称
     *
     * @param name
     * @param id
     * @return
     */
    Long updateName(@Param("name") String name, @Param("id") Long id);

    /**
     * @param gitProjectId
     * @param path
     * @return
     */
    DataDevScriptDir getDataDevScriptDirBy(@Param("gitProjectId") Long gitProjectId, @Param("path") String path);

    /**
     * @param gitProjectId
     * @param pId
     * @return
     */
    List<DataDevScriptDir> getDirsByGitProjectId(@Param("gitProjectId") Long gitProjectId, @Param("pId") Long pId);

    /**
     * @param gitParentProjectDirPath
     * @param gitProjectId
     * @return
     */
    List<DataDevScriptDir> findSubDirsByGitProjectId(@Param("gitParentProjectDirPath") String gitParentProjectDirPath, @Param("gitProjectId") Long gitProjectId);

    /**
     * 删除gitParentProjectDirPath
     *
     * @param gitProjectId
     * @param gitParentProjectDirPath
     */
    void deleteSubScriptDir(@Param("gitProjectId") Long gitProjectId, @Param("gitParentProjectDirPath") String gitParentProjectDirPath);

    /**
     * 删除文件夹（子文件夹）
     *
     * @param gitProjectId
     * @param gitProjectDirPath
     */
    void deleteScriptDirRecursion(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath, @Param("gitProjectDirPathWithSep") String gitProjectDirPathWithSep);


    /**
     * 查询gitProjectId , gitProjectDirPath
     *
     * @param gitProjectDirPath
     * @return
     */
    DataDevScriptDir selectOneSubDirRunAsDir(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath);



    /**
     * 获取父目录下的所有目录及子目录
     * @param gitProjectId
     * @param gitProjectDirPath
     * @return
     */
    List<DataDevScriptDir> getAllDataDevScriptDirsByRoot(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectDirPath") String gitProjectDirPath);




}
