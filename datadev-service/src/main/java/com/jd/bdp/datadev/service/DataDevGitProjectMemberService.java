package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/28.
 */
public interface DataDevGitProjectMemberService {

    /**
     * 删除一个项目下面的所有人员
     * @param gitProjectId
     */
    void deleteGitProjectMember(Long gitProjectId);

    /**
     * 删除一个项目下面选中的人员
     * @param gitProjectId
     * @param gitProjectId
     */
    void deleteGitProjectMemberById(Long gitProjectId, Long gitUserId);

    /**
     *
     * 添加人员信息
     * @param dataDevGitProjectMemberList
     */
    void insert(List<DataDevGitProjectMember> dataDevGitProjectMemberList);

    /**
     * 查询所有的projectMember 不包含Group里面的人员
     * @param gitProjectId
     * @return
     */
    List<DataDevGitProjectMember> findAll(Long gitProjectId) ;

    /**
     * 分页查询所有的projectMember 不包含Group里面的人员
     * @param gitProjectId
     * @return Pageable pageable
     */
    Page<DataDevGitProjectMember> findAllByPage(Long gitProjectId, Pageable pageable) ;

    /**
     * 根据erp查询DataDevGitProjectMember的信息
     * @param gitProjectId
     * @param erp
     */
    DataDevGitProjectMember findByErp(Long gitProjectId, String erp);

    /**
     * 根据id查询DataDevGitProjectMember的信息
     * @param id
     */
    DataDevGitProjectMember findById(Long id);

    /**
     * 删除一个项目下面选中的人员
     */
    void deleteById(Long id);


    DataDevGitProjectMember findLocalProjcetMaster(Long gitProject);
}
