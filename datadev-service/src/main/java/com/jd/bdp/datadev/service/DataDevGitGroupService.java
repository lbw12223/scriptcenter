package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.jdgit.JDGitGroups;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
public interface DataDevGitGroupService {

    /**
     * @param dataDevGitGroupList
     */
    void batchInsert(List<DataDevGitGroup> dataDevGitGroupList);

    /**
     *
     */
    void deleteAll();

    /**
     * 得到需要初始化的GitGroup
     *
     * @return
     */
    DataDevGitGroup getNeedInitDataDevGitGroup();

    /**
     * 获取所有的groups
     * @return
     */
    List<DataDevGitGroup> listAllGroupsAndShares();

    /**
     * @param dataDevGitGroup
     */
    void update(DataDevGitGroup dataDevGitGroup);

    /**
     * @param erp
     * @return gitOrCodingCode 1:git  2:coding
     */
    List<DataDevGitGroup> listErpGroup(String erp,Integer gitOrCodingCode);

    /**
     * create group
     * @param jdGitGroups
     * @throws Exception
     */
    DataDevGitGroup createGroup(JDGitGroups jdGitGroups) throws Exception;

    DataDevGitGroup getGroupByGroupId(Long gitGroupId)throws Exception;

    void insertOneGroup(DataDevGitGroup  dataDevGitGroup)throws Exception;



}
