package com.jd.bdp.datadev.service;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevGitHisDetail;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataDevGitHisDetailService {

    /**
     * @param dataDevGitHisDetail
     */

    void insertDataDevGitHisDetail(DataDevGitHisDetail dataDevGitHisDetail);


    /**
     * 批量插入提交历史详情
     *
     * @param insertDataDevGitHisDetail
     */
    void batchInsertDataDevGitHisDetails(List<DataDevGitHisDetail> insertDataDevGitHisDetail);


    /**
     * 删除项目提交历史详情
     *
     * @param gitProjectId
     */
    void deleteDataDevGitHisDetail(Long gitProjectId);

    /**
     * @param dataDevGitHisDetail
     * @param pageable
     * @return
     */
    PageResultDTO queryDataDevGitHisDetail(DataDevGitHisDetail dataDevGitHisDetail, Pageable pageable);


    /**
     *
     * @param id
     * @return
     */
    DataDevGitHisDetail queryDataDevGitHisDetailById(Long id) ;


    /**
     *
     * @param gitProjectId
     * @param commitId
     * @return
     */
    List<DataDevGitHisDetail> queryDataDevGitHisDetailByCommitId(Long gitProjectId , String commitId) ;

    /**
     * commitId 的数量
     * @param commitId
     * @return
     */
    Long countGitHisDetailByCommitId(String commitId) ;
}
