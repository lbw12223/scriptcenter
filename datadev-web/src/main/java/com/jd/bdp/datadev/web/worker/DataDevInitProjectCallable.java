package com.jd.bdp.datadev.web.worker;


import com.jd.bdp.datadev.component.SpringContextUtil;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Callable;

/**
 * 通知调度DqcAppCall状态
 */
public class DataDevInitProjectCallable implements Callable<Boolean> {


    private static final Log logger = LogFactory.getLog(DataDevInitProjectCallable.class);


    private DataDevGitProject dataDevGitProject;

    public DataDevInitProjectCallable(DataDevGitProject dataDevGitProject) {
        this.dataDevGitProject = dataDevGitProject;
    }


    @Override
    public Boolean call() {
        try {

            InitSingleGitProject initSingleGitProject = SpringContextUtil.getBean(InitSingleGitProject.class);
            initSingleGitProject.initSingleGitProject(dataDevGitProject);
            return true;
        } catch (Exception e) {
            logger.error("DqcTableRuleCalDataDevInitProjectCallablelable==", e);
        }
        return false;
    }


}
