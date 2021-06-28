package com.jd.bdp.datadev.component;


import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.api.model.ApiResult;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.bdp.planing.domain.bo.ProjectMemberBO;
import com.jd.bdp.planing.domain.bo.ProjectResGroupBO;
import com.jd.bdp.planing.domain.enums.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目空间权限, 一个worker定时，刷新projectSpace中成员
 */
public class ProjectSpaceRightComponent implements InitializingBean {


    private static final Logger logger = LoggerFactory.getLogger(ProjectSpaceRightComponent.class);

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;


    /**
     * CACHE
     */
    private static Map<Long, List<ProjectMemberBO>> CACHE = new HashMap<Long, List<ProjectMemberBO>>();


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 获取有权限的项目空间
     *
     * @param erp
     * @return
     */
    public List<ProjectBO> getProjectSpaces(String erp) {

//        if(true){
//            List<ProjectBO> results = new ArrayList<>();
//            ProjectBO temp1 = new ProjectBO();
//            temp1.setId(10001L);
//            temp1.setName("项目空间一");
//            results.add(temp1);
//            return results;
//        }
        ProjectInterface projectInterface = SpringContextUtil.getBean(ProjectInterface.class);

        ProjectBO projectBO = new ProjectBO();
        projectBO.setErp(erp);
        projectBO.setType(2); //2 开启开发生产隔离
        projectBO.setOpenPrjRes(true);

        com.jd.bdp.planing.api.model.ApiResult<com.jd.bdp.planing.domain.bo.ProjectBO> grantAuthorityProject = projectInterface.getGrantAuthorityProject(appId, appToken, System.currentTimeMillis(), projectBO);

        if (grantAuthorityProject.getCode().equals(0)) {
            return grantAuthorityProject.getList();
        }
        return null;
    }


    public List<DataDevScriptConfig> getDefaultProjectpaceConfig(Long projectId) {
        List<DataDevScriptConfig> defaultProjectSpace = new ArrayList<>();

        try{
            ProjectInterface projectInterface = SpringContextUtil.getBean(ProjectInterface.class);
            ProjectResGroupBO projectResGroupBO = new ProjectResGroupBO();
            projectResGroupBO.setProjectId(projectId);

            ApiResult<ProjectResGroupBO> projectResGroup = projectInterface.getProjectResGroup(appId, appToken, System.currentTimeMillis(), projectResGroupBO);
            if (projectResGroup.getCode().equals(0)) {
                List<ProjectResGroupBO> projectResGroupList = projectResGroup.getList();
                for(ProjectResGroupBO temp : projectResGroupList){
                    DataDevScriptConfig scriptConfig = new DataDevScriptConfig() ;
                    scriptConfig.setId(temp.getId());
                    scriptConfig.setAccountCode(temp.getAccount());
                    scriptConfig.setClusterCode(temp.getLogicComputeClusterCode());
                    scriptConfig.setMarketLinuxUser(temp.getMarketCode());
                    scriptConfig.setQueueCode(temp.getQueueCode());
                    scriptConfig.setRunClusterCode(temp.getLogicComputeClusterCode());
                    scriptConfig.setRunMarketLinuxUser(temp.getMarketCode());
                    defaultProjectSpace.add(scriptConfig);
                }
            }
        }catch (Exception e){
            logger.error("getDefaultProjectpaceConfig",e);
        }
        return defaultProjectSpace;
    }

    /**
     * 定时任务
     */

    public void refreshProjectMember() {
        for (Long projectSpaceId : CACHE.keySet()) {
            setOneProjectSpaceMember(projectSpaceId);
        }
    }

    /**
     * 数据质量的权限，除了 prj_test 测试人员没有权限添加，操作以外，其他的都有
     * <p>
     * <p>
     * https://cf.jd.com/pages/viewpage.action?pageId=357690750
     *
     * @param erp
     * @param projectSpaceId
     * @return
     */

    public boolean hasProjectSpaceRight(String erp, Long projectSpaceId) {

        if (projectSpaceId == null || projectSpaceId <= 0) {
            return false;
        }

        boolean hasRight = false;
        try {
            List<ProjectMemberBO> projectMemberBOS = getMembers(projectSpaceId);
            logger.info("========hasProjectSpaceRight==== {} , {}", JSONObject.toJSONString(projectMemberBOS));

            if (projectMemberBOS != null) {
                for (ProjectMemberBO projectMemberBO : projectMemberBOS) {
                    String role = projectMemberBO.getRole();
                    if (projectMemberBO.getErp().equals(erp) &&
                            (role.indexOf(RoleEnum.ADMIN.toValue()) != -1 ||
                                    role.indexOf(RoleEnum.BIZ_MNG.toValue()) != -1 ||
                                    role.indexOf(RoleEnum.PRJ_MNG.toValue()) != -1 ||
                                    role.indexOf(RoleEnum.PRJ_DEV.toValue()) != -1)) {
                        hasRight = true;
                        if (hasRight) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("hasProjectSpaceRight", e);
        }

        return hasRight;
    }


    /**
     * 获取member
     *
     * @param projectSpaceId
     * @return
     */
    public List<ProjectMemberBO> getMembers(Long projectSpaceId) {
        List<ProjectMemberBO> projectMemberBOS = CACHE.get(projectSpaceId);
        if (projectMemberBOS == null) {
            setOneProjectSpaceMember(projectSpaceId);
            projectMemberBOS = CACHE.get(projectSpaceId);
        }
        return projectMemberBOS;
    }

    /**
     * 设置memeber
     *
     * @param projectSpaceId
     */
    private void setOneProjectSpaceMember(Long projectSpaceId) {
        try {
            ProjectMemberBO projectMemberBO = new ProjectMemberBO();
            projectMemberBO.setProjectId(projectSpaceId);
            ProjectInterface projectInterface = SpringContextUtil.getBean(ProjectInterface.class);

            ApiResult<ProjectMemberBO> projectMemberApiResult = projectInterface.getProjectMember(appId, appToken, System.currentTimeMillis(), projectMemberBO);

            if (projectMemberApiResult != null && projectMemberApiResult.getCode().equals(0)) {
                List<ProjectMemberBO> list = projectMemberApiResult.getList();
                CACHE.put(projectSpaceId, list);
            }

        } catch (Exception e) {
            logger.error("setOneProjectSpaceMember", e);
        }

    }

    public ProjectBO getProjectSpaceById(Long projectSpaceId){
        com.jd.bdp.planing.domain.bo.ProjectBO projectBO  = new ProjectBO();
        projectBO.setId(projectSpaceId);
        ProjectInterface projectInterface = SpringContextUtil.getBean(ProjectInterface.class);
        ApiResult<ProjectBO> result = projectInterface.getProjectById(appId, appToken, System.currentTimeMillis(), projectBO);

        if(result != null && result.getObj() != null){
            return result.getObj();
        }
        return null ;
    }

}
