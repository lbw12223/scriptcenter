package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevGitGroupMember;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public interface DataDevGitGroupMemberDao {

    void batchInsert(@Param("dataDevGitGroupMemberList") List<DataDevGitGroupMember> dataDevGitGroupMemberList);

    void deleteByGroupId(Long gitGroupId);

    List<DataDevGitGroupMember> queryByGroupId(Long groupId);

    List<DataDevGitGroupMember> findGroupId(String erp);

    DataDevGitGroupMember getMerberIdByErp(@Param("erp") String erp);

}
