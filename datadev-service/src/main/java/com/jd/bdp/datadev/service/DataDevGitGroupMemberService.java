package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitGroupMember;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
public interface DataDevGitGroupMemberService {

    /**
     * @param dataDevGitGroupMemberList
     */
    void batchInsert(List<DataDevGitGroupMember> dataDevGitGroupMemberList);


    void deleteAll(Long groupId);

    List<DataDevGitGroupMember> queryFromGroupId(Long groupId) throws Exception;

    List<DataDevGitGroupMember> findGroupId(String erp) throws Exception;

    Long getMemberIdByErp(String erp)throws Exception;

    DataDevGitGroupMember getDataDevGitGroupMebByErp(String erp,Long groupId,Integer gitOrCodingCode) throws Exception;
}
