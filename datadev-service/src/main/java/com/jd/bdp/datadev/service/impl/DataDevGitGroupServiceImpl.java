package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.component.HttpClientUtil;
import com.jd.bdp.datadev.dao.DataDevGitGroupDao;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.jdgit.GitHttpUtil;
import com.jd.bdp.datadev.jdgit.JDGitGroups;
import com.jd.bdp.datadev.service.DataDevGitGroupService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
@Service
public class DataDevGitGroupServiceImpl implements DataDevGitGroupService {
    private static final Logger logger = Logger.getLogger(DataDevGitGroupServiceImpl.class);
    @Autowired
    private DataDevGitGroupDao dataDevGitGroupDao;


    @Override
    public void batchInsert(List<DataDevGitGroup> dataDevGitGroupList) {
        dataDevGitGroupDao.batchInsert(dataDevGitGroupList);
    }

    @Override
    public void deleteAll() {
        dataDevGitGroupDao.deleteAll();
    }


    @Override
    public DataDevGitGroup getNeedInitDataDevGitGroup() {
        return dataDevGitGroupDao.getNeedInitDataDevGitGroup();
    }

    @Override
    public void update(DataDevGitGroup dataDevGitGroup) {
        dataDevGitGroupDao.updateDataDevGitGroup(dataDevGitGroup);
    }


    @Override
    public List<DataDevGitGroup> listErpGroup(String erp,Integer gitOrCodingCode) {
        if(gitOrCodingCode == 1){
            return dataDevGitGroupDao.erpGroups(erp);
        }else if(gitOrCodingCode == 2){
            return dataDevGitGroupDao.erpCodingGroups(erp);
        }else{
            logger.error("根据erp查询用户组失败！参数错误 gitOrCodingCode =" + gitOrCodingCode);
            return null;
        }
    }

    /**
     * @param jdGitGroups
     * @throws Exception
     */
    @Override
    public DataDevGitGroup createGroup(JDGitGroups jdGitGroups) throws Exception {
        DataDevGitGroup insertGroup = jdGitGroups.createGroup();
        insertGroup.setFinishProjectMemberFlag(1);
        insertGroup.setGitGroupId(insertGroup.getGitGroupId());
        batchInsert(Arrays.asList(insertGroup));
        return insertGroup;
    }

    @Override
    public DataDevGitGroup getGroupByGroupId(Long gitGroupId) throws Exception {
        return dataDevGitGroupDao.getDataDevGitGroupByGroupId(gitGroupId);
    }

    @Override
    public void insertOneGroup(DataDevGitGroup dataDevGitGroup) throws Exception {
        dataDevGitGroupDao.insertOneDevGroup(dataDevGitGroup);
    }

    @Override
    public List<DataDevGitGroup> listAllGroupsAndShares() {
        return dataDevGitGroupDao.listAllGroupsAndShares();
    }

}
