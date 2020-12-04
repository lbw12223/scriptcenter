package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public interface DataDevGitProjectMemberDao {

    void deleteByProjectId(Long gitProjectId);

    void deleteByProjectIdAndUserId(@Param("gitProjectId") Long gitProjectId, @Param("gitUserId") Long gitUserId);

    void batchInsert(@Param("dataDevGitProjectMemberList") List<DataDevGitProjectMember> dataDevGitProjectMemberList);

    List<DataDevGitProjectMember> findAll(Long gitProjectId);

    List<DataDevGitProjectMember> findAllByPage(@Param("gitProjectId") Long gitProjectId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    Long findAllCount(Long gitProjectId);

    DataDevGitProjectMember findByErp(@Param("gitProjectId") Long gitProjectId, @Param("erp") String erp);
}
