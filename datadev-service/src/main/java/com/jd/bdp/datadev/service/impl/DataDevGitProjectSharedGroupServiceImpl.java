package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.dao.DataDevGitGroupDao;
import com.jd.bdp.datadev.dao.DataDevGitGroupMemberDao;
import com.jd.bdp.datadev.dao.DataDevGitProjectSharedGroupDao;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitGroupMember;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevGitProjectSharedGroup;
import com.jd.bdp.datadev.jdgit.JDGitGroups;
import com.jd.bdp.datadev.service.DataDevGitProjectSharedGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
@Service
public class DataDevGitProjectSharedGroupServiceImpl implements DataDevGitProjectSharedGroupService {

    @Autowired
    private DataDevGitProjectSharedGroupDao dataDevGitProjectSharedGroupDao;

    @Autowired
    private DataDevGitGroupDao dataDevGitGroupDao;

    @Autowired
    private DataDevGitGroupMemberDao dataDevGitGroupMemberDao;

    @Override
    public void handGitProjectSharedGroup(DataDevGitProject dataDevGitProject) {
        dataDevGitProjectSharedGroupDao.deleteByProjectId(dataDevGitProject.getGitProjectId());
        List<DataDevGitProjectSharedGroup> sharedGitProjectGroup = dataDevGitProject.getSharedGitProjectGroup();
        if (sharedGitProjectGroup != null && sharedGitProjectGroup.size() > 0) {
            dataDevGitProjectSharedGroupDao.insert(sharedGitProjectGroup);
        }
    }

    @Override
    public void insertGitSharedGroup(DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup) {
        try {
            dataDevGitProjectSharedGroupDao.insert(Arrays.asList(dataDevGitProjectSharedGroup));
            Long gitGroupId = dataDevGitProjectSharedGroup.getGitGroupId();
            int exists = dataDevGitGroupDao.findExists(gitGroupId);
            if (exists == 0) {//如果git_group中不存在这个共享组,则要插入该组
                DataDevGitGroup dataDevGitGroup = new DataDevGitGroup();
                dataDevGitGroup.setGitGroupId(gitGroupId);
                String groupName = dataDevGitProjectSharedGroup.getGroupName();
                dataDevGitGroup.setName(groupName);
                dataDevGitGroup.setFullName(groupName);
                dataDevGitGroup.setFullPath(groupName);
                dataDevGitGroup.setFinishProjectMemberFlag(1);
                dataDevGitGroupDao.insertOneDevGroup(dataDevGitGroup);//向git_group插入组

                JDGitGroups jdGitGroups = new JDGitGroups();
                jdGitGroups.setJdGroupId(gitGroupId);
                List<DataDevGitGroupMember> list = jdGitGroups.listGroupMembers();
                dataDevGitGroupMemberDao.batchInsert(list);//向git_group_member插入组成员
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSharedGroupFromProject(DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup) {
        dataDevGitProjectSharedGroupDao.deleteSharedGroup(dataDevGitProjectSharedGroup);
    }

}
