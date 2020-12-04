package com.jd.bdp.datadev.web.worker;

import com.jd.bdp.datadev.component.LockUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.jdgit.JDGitGroups;
import com.jd.bdp.datadev.service.DataDevGitGroupService;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.gd.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;

/**
 * Created by zhangrui25 on 2018/5/26.
 */

public class InitGitGroup {

    //新分支名称
    private static final String BDP_IDE_BRANCH = "bdp_ide_branch";

    private static final Log logger = LogFactory.getLog(InitGitGroup.class);

    private static final String GROUP_REFRESH_KEY = "bdp_data_dev_%s_refresh_group_key";

    @Autowired
    private LockUtil lockUtil;

    @Autowired
    private DataDevGitGroupService dataDevGitGroupService;

    /**
     * 初始化 group worker
     * <p>
     */
    public void initGroup() {
        String refreshSingleKey = String.format(GROUP_REFRESH_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"));
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleKey, requestId, 5*60);
            if (result) {
                long startTime = System.currentTimeMillis();
                JDGitGroups jdGitGroups = new JDGitGroups();
                jdGitGroups.setOwned(true);


                List<DataDevGitGroup> dataDevGitGroupList = jdGitGroups.listAll();


                dataDevGitGroupService.deleteAll();
                dataDevGitGroupService.batchInsert(dataDevGitGroupList);
            }

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }finally {
            if (result == true){
                lockUtil.unLock(refreshSingleKey,requestId);
            }
        }
    }

}
