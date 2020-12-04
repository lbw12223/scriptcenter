package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevGitProjectSharedGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public interface DataDevGitProjectSharedGroupDao {

    /**
     * insert
     *
     * @param gitProjectSharedGroupList
     */
    void insert(@Param("gitProjectSharedGroupList")  List<DataDevGitProjectSharedGroup> gitProjectSharedGroupList);



    /**
     * 删除
     * @param projectId
     */
    void deleteByProjectId(@Param("gitProjectId") Long projectId);

    /**
     * 删除共享组
     * @param dataDevGitProjectSharedGroup
     */
    void deleteSharedGroup(DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup);

}
