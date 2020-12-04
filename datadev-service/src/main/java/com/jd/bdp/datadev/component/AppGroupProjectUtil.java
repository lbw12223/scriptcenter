package com.jd.bdp.datadev.component;

import com.jd.bdp.datadev.domain.DataDevApplication;
import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.api.model.ApiResult;
import com.jd.bdp.planing.domain.ProjectMember;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.bdp.planing.domain.bo.ProjectMemberBO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目空间替换为项目空间后
 */
@Component
public class AppGroupProjectUtil {
    private static final Logger logger = Logger.getLogger(AppGroupProjectUtil.class);


    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;

    @Autowired
    private ProjectInterface projectInterface;


    public List<DataDevApplication> getAppsByErp(String erp) throws Exception {

        List<DataDevApplication> result = new ArrayList<DataDevApplication>();
        ProjectBO projectBO = new ProjectBO();
        projectBO.setErp(erp);
        ApiResult<ProjectBO> apiResult = projectInterface.getGrantAuthorityProject(appId, appToken, System.currentTimeMillis(), projectBO);
        logger.info("getGrantAuthorityProject.... {} " + com.alibaba.fastjson.JSONObject.toJSONString(apiResult));
        List<ProjectBO> list = apiResult.getList();
        if (list != null && list.size() > 0) {
            for (ProjectBO temp : list) {
                DataDevApplication application = new DataDevApplication();
                application.setAppgroupName(temp.getName());
                application.setAppgroupId(Integer.parseInt(temp.getId() + ""));
                result.add(application);
            }
        }
        return result;
    }

    /**
     * 获取App Info
     *
     * @param id
     * @return
     * @throws Exception
     */

    public DataDevApplication getAppInfo(Long id) throws Exception {
        DataDevApplication dataDevApplication = new DataDevApplication();
        ProjectBO projectBO = new ProjectBO();
        projectBO.setId(id);
        ApiResult<ProjectBO> apiResult = projectInterface.getProjectById(appId, appToken, System.currentTimeMillis(), projectBO);
        if (apiResult.getCode().equals(0) && apiResult.getObj() != null) {
            dataDevApplication.setAppgroupId(Integer.parseInt(apiResult.getObj().getId() + ""));
            dataDevApplication.setAppgroupName(apiResult.getObj().getName());
        }
        return dataDevApplication; //getAppInfo(appId, appToken, domain, id);
    }

    /**
     * 获取app 人员
     *
     * @param id
     * @return
     * @throws Exception
     */
    public List<String> getErpsByAppId(Long id) {

        List<String> erps = new ArrayList<String>();
        ProjectMemberBO projectMemberBO = new ProjectMemberBO();
        projectMemberBO.setProjectId(id);
        ApiResult<ProjectMemberBO> projectMemberApiResult = projectInterface.getProjectMember(appId, appToken, System.currentTimeMillis(), projectMemberBO);
        if (projectMemberApiResult != null && projectMemberApiResult.getCode().equals(0)) {

            List<ProjectMemberBO> list = projectMemberApiResult.getList();
            for (ProjectMember projectMember : list) {
                erps.add(projectMember.getErp());
            }
        }
        return erps;
    }


}
