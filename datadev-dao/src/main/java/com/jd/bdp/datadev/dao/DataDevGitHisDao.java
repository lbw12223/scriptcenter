package com.jd.bdp.datadev.dao;


import com.jd.bdp.datadev.domain.DataDevGitHis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevGitHisDao {

    /**
     * 插入一条提交历史
     * @param dataDevGitHis
     * @throws Exception
     */
    void insertGitHis(DataDevGitHis dataDevGitHis)throws Exception;

    /**
     * 删除项目提交历史
     * @param gitProjectId
     * @throws Exception
     */
    void deleteGiHis(@Param("gitProjectId") Long gitProjectId ) throws Exception ;




}
