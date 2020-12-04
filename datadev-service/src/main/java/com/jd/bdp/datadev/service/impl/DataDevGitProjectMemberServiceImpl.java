package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.dao.DataDevGitProjectDao;
import com.jd.bdp.datadev.dao.DataDevGitProjectMemberDao;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import com.jd.bdp.datadev.service.DataDevGitProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
@Service
public class DataDevGitProjectMemberServiceImpl implements DataDevGitProjectMemberService {

    @Autowired
    private DataDevGitProjectMemberDao dataDevGitProjectMemberDao;

    @Override
    public void deleteGitProjectMember(Long gitProjectId) {
        dataDevGitProjectMemberDao.deleteByProjectId(gitProjectId);
    }

    @Override
    public void deleteGitProjectMemberById(Long gitProjectId, Long gitUserId) {
        dataDevGitProjectMemberDao.deleteByProjectIdAndUserId(gitProjectId, gitUserId);
    }

    @Override
    public void insert(List<DataDevGitProjectMember> dataDevGitProjectMemberList) {
        dataDevGitProjectMemberDao.batchInsert(dataDevGitProjectMemberList);
    }

    @Override
    public List<DataDevGitProjectMember> findAll(Long gitProjectId) {
        List<DataDevGitProjectMember> datas = dataDevGitProjectMemberDao.findAll(gitProjectId);
        return datas;
    }

    @Override
    public Page<DataDevGitProjectMember> findAllByPage(Long gitProjectId, Pageable pageable) {
        long total = dataDevGitProjectMemberDao.findAllCount(gitProjectId);
        List<DataDevGitProjectMember> datas = dataDevGitProjectMemberDao.findAllByPage(gitProjectId, pageable.getOffset(), pageable.getPageSize());
        return new PageImpl<>(datas, pageable, total);
    }


    public DataDevGitProjectMember findByErp(Long gitProjectId, String erp) {
        DataDevGitProjectMember dataDevGitProjectMember = dataDevGitProjectMemberDao.findByErp(gitProjectId, erp);
        return dataDevGitProjectMember;
    }
}
