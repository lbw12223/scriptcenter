package com.jd.bdp.datadev.dao;


import com.jd.bdp.datadev.domain.DataDevGitHis;
import com.jd.bdp.datadev.domain.DataDevGitHisDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevGitHisDetailDao {

    /**
     * @param dataDevGitHisDetailList
     * @throws Exception
     */
    void batchInsertGitHisDetail(@Param("list") List<DataDevGitHisDetail> dataDevGitHisDetailList);

    /**
     * 删除项目提交历史
     *
     * @param gitProjectId
     * @throws Exception
     */
    void deleteGitHisDetail(@Param("gitProjectId") Long gitProjectId);


    /**
     * 计算总数
     *
     * @param dataDevGitHisDetail
     * @return
     */
    Long countFileCommit(@Param("dataDevGitHisDetail") DataDevGitHisDetail dataDevGitHisDetail);

    /**
     * 查询列表
     *
     * @param dataDevGitHisDetail
     * @return
     */
    List<DataDevGitHisDetail> listFileCommit(@Param("dataDevGitHisDetail") DataDevGitHisDetail dataDevGitHisDetail);


    /**
     * 计算总数
     *
     * @param dataDevGitHisDetail
     * @return
     */
    Long countDirCommit(@Param("dataDevGitHisDetail") DataDevGitHisDetail dataDevGitHisDetail);

    /**
     * 查询列表
     *
     * @param dataDevGitHisDetail
     * @return
     */
    List<DataDevGitHisDetail> listDirCommit(@Param("dataDevGitHisDetail") DataDevGitHisDetail dataDevGitHisDetail);


    /**
     *
     * @param id
     * @return
     */
    DataDevGitHisDetail selectGitHisDetailById(@Param("id") Long id);

    /**
     *
     * @param commitId
     * @return
     */
    List<DataDevGitHisDetail> selectGitHisDetailByCommitId(@Param("gitProjectId") Long gitProjectId , @Param("commitId") String commitId);

    /**
     *
     * @param commitId
     * @return
     */
    Long countGitHisDetailByCommitId(@Param("commitId") String commitId) ;

}
