package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevGitDto;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.jdgit.JDGitProjects;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/28.
 */
public interface DataDevGitProjectService {

    List<DataDevGitProject> listAll();

    //插入多条
    void insert(List<DataDevGitProject> inserts);

    //删除多条
    void delete(List<DataDevGitProject> deletes);

    //更新member
    void updateAllProjectInitMember();

    /**
     * 获取一条需要初始化人员信息的git project
     *
     * @return
     */
    DataDevGitProject getNeedInitMemeberDataDevGitProject();

    /**
     * 更新
     *
     * @param dataDevGitProject
     */
    void upateDataDevGitProject(DataDevGitProject dataDevGitProject);

    /**
     * 获取某个人可以看见的Project
     *
     * @param erp
     * @return
     */
    List<DataDevGitProject> getErpProject(String erp);


    /**
     * 验证用户是否有项目权限 没有的话直接抛无权限异常
     *
     * @param erp
     * @param gitProjectId
     * @throws Exception
     */
    void verifyUserAuthority(String erp, Long gitProjectId) throws Exception;

    /**
     * 获取某个人可以看见的Project
     * 支持搜索
     *
     * @param erp
     * @return
     */
    List<DataDevGitProject> getErpProjectBySearch(String erp, String keyword, Integer projectType);

    /**
     * gitProjectId
     *
     * @param gitProjectId
     * @return
     */
    DataDevGitProject getGitProjectBy(Long gitProjectId);

    /**
     * @param jdGitProjects
     * @throws Exception
     */
    DataDevGitProject createProject(JDGitProjects jdGitProjects) throws Exception;

    /**
     * gitProjectId
     *
     * @param gitProjectPath
     * @return
     */
    DataDevGitProject getGitProjectBy(String gitProjectPath);

    void insertOneProject(DataDevGitProject dataDevGitProject) throws Exception;


    /**
     * 根据关键词获取相关的项目或者项目组
     *
     * @param keyWord
     * @return
     * @throws Exception
     */
    List<DataDevGitDto> getGitAndGroupByKeyWord(String erp, String keyWord) throws Exception;


    List<String> getAllUserHaveSameProject(List<DataDevGitProject> projects) throws Exception;

    Long getCurrentLocalGitProject() throws Exception ;

}
