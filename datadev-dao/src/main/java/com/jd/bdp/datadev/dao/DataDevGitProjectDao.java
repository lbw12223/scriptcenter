package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevGitProject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public interface DataDevGitProjectDao {

    /**
     * insert
     *
     * @param dataDevGitProject
     */
    void insertDataDevGitProject(DataDevGitProject dataDevGitProject);

    /**
     * update
     *
     * @param dataDevScriptProject
     */
    void updateDataDevGitProject(DataDevGitProject dataDevScriptProject);

    /**
     * @param gitProjectId
     * @return
     */
    DataDevGitProject getDataDevScriptProjectById(@Param("gitProjectId") Long gitProjectId);

    /**
     * 查询所有的
     *
     * @return
     */
    List<DataDevGitProject> listAll();

    /**
     * 批量删除
     *
     * @param gitProjectIds
     */
    void batchDelete(@Param("gitProjectIds") Long gitProjectIds);

    /**
     * update init member
     */
    void updateAllProjectInitMember();

    /**
     * 获取一条需要初始化的DataDevGitProject
     *
     * @return
     */
    DataDevGitProject getNeedInitMember();

    /**
     * 获取人员可以看见的GitProject
     * @param erp
     * @return
     */
    List<DataDevGitProject> getErpDataDevGitProject(@Param("erp") String erp ,@Param("keyword") String  keyword , @Param("idMin") Long idMin, @Param("idMax") Long idMax );


    /**
     * 通过分享组来查询
     * @param erp
     * @return
     */
    List<DataDevGitProject> getErpDataDevGitProjectBySharedGroup(@Param("erp") String erp ,@Param("keyword")String  keyword ,@Param("idMin") Long idMin, @Param("idMax") Long idMax);


    /**
     * @param gitProjectPath
     * @return
     */
    DataDevGitProject getGitProjectByPath(@Param("gitProjectPath") String gitProjectPath) ;

    /**
     * 根据关键词搜索所有项目
     * @param keyWord
     * @return
     */
    List<DataDevGitProject> getAllGitProjectByKeyWord(@Param("keyWord") String keyWord,
                                                      @Param("erp") String erp,
                                                      @Param("limit")Integer limit);

    List<String> getAllUserHaveSameProject(@Param("projects") List<DataDevGitProject> projects);


    DataDevGitProject getLocalProjectByPath(@Param("gitProjectPath") String gitProjectPath) ;
    void updateDataDevGitProjectById(DataDevGitProject dataDevScriptProject);

}
