package com.jd.bdp.datadev.web.worker;

import com.jd.bdp.datadev.component.EnvInit;
import com.jd.bdp.datadev.component.LockUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import com.jd.bdp.datadev.enums.DataDevGitInitFlag;
import com.jd.bdp.datadev.jdgit.JDGitBranchs;
import com.jd.bdp.datadev.jdgit.JDGitProjects;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptDirService;
import com.jd.bdp.datadev.service.DataDevGitProjectMemberService;

import com.jd.jim.cli.Cluster;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by zhangrui25 on 2018/5/28.
 */
public class InitSingleGitProject {

    public static final ExecutorService THREAD_POOLS = new ThreadPoolExecutor(10, 20,
            2L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());


    private static final String BDP_IDE_BRANCH = "bdp_ide_branch";

    private static final Log logger = LogFactory.getLog(InitSingleGitProject.class);

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private DataDevGitProjectMemberService dataDevGitProjectMemberService;

    @Autowired
    private DataDevScriptDirService dataDevScriptDirService;

    private static final String PROJECT_ALL_PROJECT_KEY = "bdp_data_dev_%s_refresh_all_projects";
    private static final String PROJECT_SINGLE_PROJECT_KEY = "bdp_data_dev_%s_refresh_single_project_%s";


    @Autowired
    private LockUtil lockUtil;

    @Autowired
    private Cluster jimClient;

    @Autowired
    private EnvInit envInit;

    /**
     * 1.init project member 每次都会重新拉去最新
     * <p>
     * <p>
     * 2.may init project tree      只会在第一次初始化的时候拉去第一级目录tree
     * 3.may init project branch    只会在第一次初始化的时候创建 data_dev_branch
     * 4.may init project hooks     只会在第一次初始化的时候创建 项目Hooks
     */
    public void initGitProjects() {
        String refreshSingleProjectKey = String.format(PROJECT_ALL_PROJECT_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"));
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleProjectKey, requestId, 5 * 60);
            if (result) {
                long startTime = System.currentTimeMillis();
                List<DataDevGitProject> allGitProject = dataDevGitProjectService.listAll();
                if (allGitProject != null && allGitProject.size() > 0) {
                    handAllGitProjects(allGitProject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("initGitProjects", e);
        } finally {
            if (result == true) {
                lockUtil.unLock(refreshSingleProjectKey, requestId);
            }
        }
    }

    /**
     * 使用线程POOLS
     *
     * @param allGitProject
     * @throws Exception
     */
    private void handAllGitProjects(List<DataDevGitProject> allGitProject) throws Exception {


        List<Future<Boolean>> runableList = new ArrayList<Future<Boolean>>();
        for (DataDevGitProject gitProject : allGitProject) {
            runableList.add(THREAD_POOLS.submit(new DataDevInitProjectCallable(gitProject)));
        }
        for (Future<Boolean> temp : runableList) {
            temp.get();
        }

    }

    /**
     * 创建Git项目的时候初始化
     *
     * @param dataDevGitProject
     */
    public void initSingleGitProject(DataDevGitProject dataDevGitProject) {
        String refreshSingleProjectKey = String.format(PROJECT_SINGLE_PROJECT_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"), dataDevGitProject.getGitProjectId());
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleProjectKey, requestId, 3 * 60);
            if (result) {
                logger.error("initSingleGitProject===========" + dataDevGitProject.getGitProjectId());
                Thread.sleep(100);
                doInitSingleProject(dataDevGitProject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            if (result == true) {
                lockUtil.unLock(refreshSingleProjectKey, requestId);
            }
        }
    }


    public void doInitSingleProject(DataDevGitProject dataDevGitProject) throws Exception {
        try {

            initProjectMember(dataDevGitProject);

            if (dataDevGitProject.getFinishProjectTreeFlag().equals(DataDevGitInitFlag.NEED_INIT.tocode())) {
                initProjectTree(dataDevGitProject);
            }
        } catch (Exception e) {
            logger.error("doInitSingleProject",e);
            if(e.getMessage().indexOf("404 Project Not Found") != -1){
                //delet
                dataDevGitProjectService.delete(Arrays.asList(dataDevGitProject));
            }
        }
    }

    /**
     * 初始化项目成员
     *
     * @param dataDevGitProject
     */
    public void initProjectMember(DataDevGitProject dataDevGitProject) throws Exception {
        JDGitProjects jdGitProjects = new JDGitProjects();
        Long gitProjectId = dataDevGitProject.getGitProjectId();
        jdGitProjects.setGitProjectId(gitProjectId);
        List<DataDevGitProjectMember> memberList = jdGitProjects.listProjectMembers();
        //删除所有
        dataDevGitProjectMemberService.deleteGitProjectMember(gitProjectId);
        //添加
        if (memberList != null && memberList.size() > 0) {
            dataDevGitProjectMemberService.insert(memberList);
        }

        DataDevGitProject upDataParams = new DataDevGitProject();
        upDataParams.setFinishProjectMemberFlag(DataDevGitInitFlag.INIT_FINISH.tocode());
        upDataParams.setGitProjectId(gitProjectId);
        dataDevGitProjectService.upateDataDevGitProject(upDataParams);

        dataDevGitProject.setFinishProjectMemberFlag(DataDevGitInitFlag.INIT_FINISH.tocode());
    }

    /**
     * 初始化 项目branch 分支 BDP_IDE_BRANCH
     * 初始化 hook
     * 初始化 项目跟路径
     *
     * @param dataDevGitProject
     */
    private void initProjectTree(DataDevGitProject dataDevGitProject) throws Exception {
        //create branch
        createBranch(dataDevGitProject);

        //create hook
        creatGitProjectHook(dataDevGitProject);

        //load Root File Tree
        dataDevScriptDirService.loadGitFileTree(dataDevGitProject, "", 0L, "");
        //FinishProjectTreeFlag
        DataDevGitProject updateParams = new DataDevGitProject();
        updateParams.setGitProjectId(dataDevGitProject.getGitProjectId());
        updateParams.setFinishProjectTreeFlag(DataDevGitInitFlag.INIT_FINISH.tocode());
        dataDevGitProjectService.upateDataDevGitProject(updateParams);
    }


    /**
     * create branch
     * <p>
     * 如果已经存在那么不在新创建 默认从master分支创建
     *
     * @param dataDevGitProject
     */
    private void createBranch(DataDevGitProject dataDevGitProject) {
        try {

            JDGitBranchs jdGitBranchs = new JDGitBranchs();
            jdGitBranchs.setBranch(BDP_IDE_BRANCH);
            jdGitBranchs.setGitProjectId(dataDevGitProject.getGitProjectId());
            if (!jdGitBranchs.checkIdeBranchs()) {
                jdGitBranchs.createBranchs();
            }
            DataDevGitProject updateParams = new DataDevGitProject();
            updateParams.setGitProjectId(dataDevGitProject.getGitProjectId());
            updateParams.setBranch(BDP_IDE_BRANCH);
            dataDevGitProjectService.upateDataDevGitProject(updateParams);

            dataDevGitProject.setBranch(BDP_IDE_BRANCH);

        } catch (Exception e) {
            logger.error(e);

        }

    }

    private void creatGitProjectHook(DataDevGitProject dataDevGitProject) throws Exception {
        JDGitProjects jdGitProjects = new JDGitProjects();
        jdGitProjects.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitProjects.addProjectHook(envInit.getGitProjectHookUrl());
    }

}
