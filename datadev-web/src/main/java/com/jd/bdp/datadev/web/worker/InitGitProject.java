package com.jd.bdp.datadev.web.worker;

import com.jd.bdp.datadev.component.LockUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.domain.HoldTreeValue;
import com.jd.bdp.datadev.enums.DataDevGitOrCodingEnum;
import com.jd.bdp.datadev.jdgit.JDGitProjects;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevGitProjectSharedGroupService;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.gd.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangrui25 on 2018/5/26.
 */
public class InitGitProject {

    //新分支名称
    public static final String BDP_IDE_BRANCH = "bdp_ide_branch";

    private static final Log logger = LogFactory.getLog(InitGitProject.class);

    private static final String PROJECT_REFRESH_KEY = "bdp_data_dev_%s_refresh_project_key";

    @Autowired
    private LockUtil lockUtil;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private DataDevGitProjectSharedGroupService dataDevGitProjectSharedGroupService;


    /**
     * 初始化worker
     * <p>
     * 1.project(创建新的分支)
     * 2.project-member
     * 3.project-shared-group
     * <p>
     * <p>
     * 注释：获取当前虚拟账号可以看见的所有的Project
     * 注释：和数据库中的数据做比较，insert(finish_project_tree_flag = 0 ,finish_project_member_flag=0 ) ，update deleted
     * 注释：为了防止多主机同时刷新，使用redisKey 做单例的标志
     */

    @Transactional
    public void refresh() {
        String refreshSingleKey = String.format(PROJECT_REFRESH_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"));
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleKey, requestId, 5 * 60);
            if (result) {
                logger.error("initrefreshproject===========");

                long startTime = System.currentTimeMillis();
                JDGitProjects jdGitProject = new JDGitProjects();
                jdGitProject.setSimple("false");
                jdGitProject.setVisibility("private");
                List<DataDevGitProject> codingRepository = jdGitProject.listAll(DataDevGitOrCodingEnum.CODING.tocode());
                List<DataDevGitProject> gitRepository = jdGitProject.listAll(DataDevGitOrCodingEnum.GIT.tocode());
                if(gitRepository == null || gitRepository.size() < 1 || codingRepository == null || gitRepository.size() < 1){
                    logger.error("codingRepository:" + codingRepository.size() + "  gitRepository:" + gitRepository.size());
                    return;
                }
                gitRepository.addAll(codingRepository);
                List<DataDevGitProject> dataBaseRepository = dataDevGitProjectService.listAll();
                //获取比较结果
                HoldTreeValue<List<DataDevGitProject>, List<DataDevGitProject>, List<DataDevGitProject>> insertAndDelete = compare(gitRepository, dataBaseRepository);
                //处理比较结果
                handInsertAndDelete(insertAndDelete);

                //处理projectSharedGroup
                handGitProjectSharedGroup(gitRepository);

                //notify init single git project
                dataDevGitProjectService.updateAllProjectInitMember();
            }

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            if (result == true) {
                lockUtil.unLock(refreshSingleKey, requestId);
            }
        }
    }

    /**
     * 处理gitProjectSharedGroup
     *
     * @param gitRepository
     */
    private void handGitProjectSharedGroup(List<DataDevGitProject> gitRepository) {
        if (gitRepository != null && gitRepository.size() > 0) {
            for (DataDevGitProject dataDevGitProject : gitRepository) {
                dataDevGitProjectSharedGroupService.handGitProjectSharedGroup(dataDevGitProject);
            }
        }
    }


    //处理比较结果
    private void handInsertAndDelete(HoldTreeValue<List<DataDevGitProject>, List<DataDevGitProject>, List<DataDevGitProject>> insertAndDelete) throws Exception {
        dataDevGitProjectService.insert(insertAndDelete.a);
        if (insertAndDelete.c != null && insertAndDelete.c.size() > 0) {
            for (DataDevGitProject temp : insertAndDelete.c) {
                dataDevGitProjectService.upateDataDevGitProject(temp);
            }
        }
        dataDevGitProjectService.delete(insertAndDelete.b);
    }

    /**
     * 比较Git和dataBase里面的数据
     * <p>
     * 找出 insert ， delete
     *
     * @param gitRepository
     * @param dataBaseRepository
     */
    private HoldTreeValue<List<DataDevGitProject>, List<DataDevGitProject>, List<DataDevGitProject>> compare(List<DataDevGitProject> gitRepository, List<DataDevGitProject> dataBaseRepository) {

        List<DataDevGitProject> insert = new ArrayList<DataDevGitProject>();
        List<DataDevGitProject> delete = new ArrayList<DataDevGitProject>();
        List<DataDevGitProject> update = new ArrayList<DataDevGitProject>();

        //查询在Git里面新添加的project
        for (DataDevGitProject git : gitRepository) {
            if (!isGitProjectExsits(git.getGitProjectId(), dataBaseRepository)) {
                git.setFinishProjectMemberFlag(1);
                git.setFinishProjectTreeFlag(1);
                insert.add(git);
            }
        }
        //需要更新的
        for (DataDevGitProject git : gitRepository) {
            if (isGitProjectDelete(git.getGitProjectId(), dataBaseRepository)) {
                /*git.setFinishProjectMemberFlag(1);
                git.setFinishProjectTreeFlag(1);*/
                git.setDeleted(0);
                update.add(git);
            }
        }
        //查询在database 里面删除的project
        for (DataDevGitProject dataBase : dataBaseRepository) {
            if (!isGitProjectExsits(dataBase.getGitProjectId(), gitRepository)) {
                delete.add(dataBase);
            }
        }
        return new HoldTreeValue<List<DataDevGitProject>, List<DataDevGitProject>, List<DataDevGitProject>>(insert, delete, update);
    }

    private boolean isGitProjectExsits(Long gitProjectId, List<DataDevGitProject> sources) {
        for (DataDevGitProject source : sources) {
            if (gitProjectId.equals(source.getGitProjectId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGitProjectDelete(Long gitProjectId, List<DataDevGitProject> sources) {
        for (DataDevGitProject source : sources) {
            if (gitProjectId.equals(source.getGitProjectId()) && source.getDeleted() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化 Group 人员信息
     * 3.获取Group
     * 4.获取Group-member
     */
    private void refreshGroup() {


    }
}
