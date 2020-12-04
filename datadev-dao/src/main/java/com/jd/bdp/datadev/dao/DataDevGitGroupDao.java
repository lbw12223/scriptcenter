package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public interface DataDevGitGroupDao {

    /**
     * 批量插入
     *
     * @param dataDevGroupList
     */
    void batchInsert(@Param("dataDevGroupList") List<DataDevGitGroup> dataDevGroupList);

    /**
     * 删除所有
     */
    void deleteAll();

    /**
     * 得到一个需要初始化的GitGroup
     *
     * @return
     */
    DataDevGitGroup getNeedInitDataDevGitGroup();

    /**
     * 更新
     *
     * @param dataDevGitGroup
     */
    void updateDataDevGitGroup(DataDevGitGroup dataDevGitGroup);

    /**
     * 获取erp的Group
     * @param erp
     * @return
     */
    List<DataDevGitGroup> erpGroups(@Param("erp") String erp);

    /**
     * 获取erp的CodingGroup
     * @param erp
     * @return
     */
    List<DataDevGitGroup> erpCodingGroups(@Param("erp") String erp);

    /**
     *
     * @return
     */
    List<DataDevGitGroup> listAllGroupsAndShares();

    DataDevGitGroup getDataDevGitGroupByGroupId(@Param("gitGroupId") Long gitGroupId);

    void insertOneDevGroup(DataDevGitGroup dataDevGitGroup);

    /**
     * 根据gitProjectId判断是否存在
     * @param gitProjectId
     * @return
     */
    int findExists(Long gitProjectId);


    /**
     * 根据关键词获取项目组
     * @param keyWord
     * @param limit
     * @return
     */
    List<DataDevGitGroup> getAllGitGroupByKeyWord(@Param("keyWord") String keyWord,@Param("erp")String erp, @Param("limit")Integer limit);


}
