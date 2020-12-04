package com.jd.bdp.datadev.web.worker;


import com.jd.bdp.datadev.component.LockUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitGroupMember;
import com.jd.bdp.datadev.jdgit.JDGitGroups;
import com.jd.bdp.datadev.service.DataDevGitGroupMemberService;
import com.jd.bdp.datadev.service.DataDevGitGroupService;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.gd.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;

/**
 * Created by zhangrui25 on 2018/5/28.
 */
public class InitSingleGitGroup {


    private static final Log logger = LogFactory.getLog(InitSingleGitGroup.class);


    @Autowired
    private DataDevGitGroupService dataDevGitGroupService;

    @Autowired
    private DataDevGitGroupMemberService dataDevGitGroupMemberService;

    private static final String PROJECT_ALL_GROUP_KEY = "bdp_data_dev_%s_refresh_all_group";
    private static final String PROJECT_SINGLE_GROUP_KEY = "bdp_data_dev_%s_refresh_single_group_%s";

    @Autowired
    private LockUtil lockUtil;

    /**
     * 1.init group member
     */
    public void initGitGroups() {
        String refreshSingleGroupKey = String.format(PROJECT_ALL_GROUP_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"));
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleGroupKey, requestId, 5*60);
            if (result) {
                List<DataDevGitGroup> allGroups = dataDevGitGroupService.listAllGroupsAndShares();
                if (allGroups != null && allGroups.size() > 0) {
                    doInitGroupsMember(allGroups);
                }
            }
        } catch (Exception e) {
            logger.error("initGitGroups",e);
        }finally {
            if (result == true){
                lockUtil.unLock(refreshSingleGroupKey,requestId);
            }
        }
    }

    public void initSingleGitGroup(Long groupId) {
        String refreshSingleGroupKey = String.format(PROJECT_SINGLE_GROUP_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"),groupId);
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleGroupKey, requestId, 2*60);
            if (result) {
                logger.error("initSingleGitGroup===========" + groupId);
                Thread.sleep(100);
                doInitSingleGroupMember(groupId);
            }
        } catch (Exception e) {
            logger.error(e);
        }finally {
            if (result == true){
                lockUtil.unLock(refreshSingleGroupKey,requestId);
            }
        }
    }

    private void doInitGroupsMember(List<DataDevGitGroup> allGroups) throws Exception {
        for (DataDevGitGroup gitGroup : allGroups) {
            try {
                if(gitGroup!=null){
                    initSingleGitGroup(gitGroup.getGitGroupId());
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }


    private void doInitSingleGroupMember(Long groupId) throws Exception {
        try {
            if (groupId != null) {
                JDGitGroups jdGitGroups = new JDGitGroups();
                jdGitGroups.setJdGroupId(groupId);
                List<DataDevGitGroupMember> gitGroupMembers = jdGitGroups.listGroupMembers();
                if(gitGroupMembers!=null&&gitGroupMembers.size()>0) {
                    dataDevGitGroupMemberService.deleteAll(groupId);
                    dataDevGitGroupMemberService.batchInsert(gitGroupMembers);
                }
            }
        } catch (Exception e) {
            logger.error("doInitSingleGroupMember",e);
        }

    }


}
