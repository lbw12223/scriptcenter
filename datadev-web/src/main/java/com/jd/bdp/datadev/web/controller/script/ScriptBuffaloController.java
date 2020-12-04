package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.domain.DataDevApplication;
import com.jd.bdp.datadev.domain.DataDevScriptBuffaloJob;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import com.jd.bdp.datadev.enums.DataDevScriptPublishStatusEnum;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptPublishService;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/scriptcenter/buffalo/")
public class ScriptBuffaloController {

    private static final Logger logger = Logger.getLogger(ScriptBuffaloController.class);

    @Autowired
    private AppGroupUtil appGroupUtil;



    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private DataDevScriptPublishService publishService;
    @Autowired
    private DataDevGitProjectService projectService;
    @Autowired
    private ClusterJSFInterface clusterJSFInterface;
    @Autowired
    private ImportScriptManager importScriptManager;

    private static String pattern = "^[0-9a-zA-z\\-_]+\\.(\\w+)$";
    @Value("${buffalo.domain.name}")
    private String buffaloDomain;
    @Value("${buffalo.add.job}")
    private String buffaloAddJobUrl;
    @Value("${buffalo.look.job}")
    private String buffaloLookJobUrl;
    @Value("${buffalo.script.workFlow}")
    private String buffaloScriptWorkFlowUrl;


    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;

    @Autowired
    private AppGroupProjectUtil appGroupProjectUtil ;

    @RequestMapping("updateScriptStatus")
    @ResponseBody
    public JSONObject updateScriptStatus(Long requestId, Integer status) throws Exception {
        logger.error("=====================================requestid status" + requestId + "---------" + status);
        DataDevScriptFilePublish publish = new DataDevScriptFilePublish();
        publish.setRequestId(Long.valueOf(requestId));
        //审批通过
        if (status == 2) {
            publish.setStatus(DataDevScriptPublishStatusEnum.Success.toCode());
        } else if (status == 3) {
            //驳回
            publish.setStatus(DataDevScriptPublishStatusEnum.Reject.toCode());
        } else if (status == 4) {
            //撤回
            publish.setStatus(DataDevScriptPublishStatusEnum.Cancel.toCode());
        }
        publish.setMender("bdp_workflow");
        publishService.updateStatus(publish);
        return JSONObjectUtil.getSuccessResult("更新成功");
    }


    /**
     * @param holder
     * @param file
     * @param scriptId
     * @param relaveScriptStatus 是否关联调度脚本
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "上线脚本")
    @RequestMapping("upLineScript.ajax")
    @ResponseBody
    public JSONObject upLineScript(UrmUserHolder holder, DataDevScriptFile file, Long scriptId
            , @RequestParam(value = "relaveScriptStatus", defaultValue = "0") Integer relaveScriptStatus
            , @RequestParam(value = "runType", defaultValue = "0") Integer runType) throws Exception {
        projectService.verifyUserAuthority(holder.getErp(), file.getGitProjectId());
        DataDevScriptFile old = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        boolean isValid = Pattern.matches(pattern, old.getName());
        /*if (!isValid || old.getName().length() > 255) {
            throw new RuntimeException("上线脚本名字不能带有中文，且长度小于256，请先修改名称再上线！");
        }*/
        if (old == null) {
            throw new RuntimeException("脚本不存在");
        }
        if (StringUtils.isBlank(file.getVersion())) {
            throw new RuntimeException("脚本版本号为空");
        }
        if (file.getApplicationId() == null) {
            throw new RuntimeException("项目空间为空");
        }
        //关联
        if (relaveScriptStatus == 1 && scriptId != null) {
            importScriptManager.callBackScript(scriptId, null, file.getGitProjectId(), file.getGitProjectFilePath(), holder.getErp(), file.getVersion());
        }
        DataDevApplication appInfo = appGroupProjectUtil.getAppInfo(file.getApplicationId());
        old.setVersion(file.getVersion());
        old.setApplicationId(file.getApplicationId());
        old.setApplicationName(appInfo != null ? appInfo.getAppgroupName() : "");
        old.setVerDescription(file.getDescription());
        JSONObject resJson = new JSONObject();
        DataDevScriptFilePublish publish = publishService.findLastNotFail(file.getGitProjectId(), file.getGitProjectFilePath(), file.getApplicationId());
        DataDevScriptFilePublish res = null;
        if (publish != null) {
            resJson.put("isFirst", "false");
        }
        res = publishService.upLineScript(old, holder.getErp(), publish, runType);

        logger.error("==================publish:" + JSONObject.toJSONString(res));
        resJson.put("buffaloAddJobUrl", buffaloAddJobUrl);
        resJson.put("buffaloLookJobUrl", buffaloLookJobUrl);
        resJson.put("scriptName", old.getName());
        if (res.getRequestId() != null) {
            resJson.put("buffaloScriptWorkFlowUrl", buffaloScriptWorkFlowUrl + res.getRequestId());
        }
        return JSONObjectUtil.getSuccessResult("发布成功", resJson);
    }

    @RequestMapping("getJqGridBuffaloTasks.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getJqGridBuffaloTasks(UrmUserHolder holder, DataDevScriptFile file, DataDevScriptBuffaloJob queryJob,
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @RequestParam(value = "rows", defaultValue = "10") int rows) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        String userToken = urmUtil.UserTokenByErp(null, holder.getErp());
        try {
            pageResultDTO = publishService.listBuffaloJobs(file, queryJob, userToken, page, rows);
        } catch (Exception e) {
            pageResultDTO.setSuccess(false);
            pageResultDTO.setRecords(0L);
            pageResultDTO.setMessage("获取列表失败！" + e.getMessage());
        }
        pageResultDTO.setPage(page);
        pageResultDTO.setLimit(rows);
        pageResultDTO.setMessage("获取成功");
//        return AjaxUtil.gridJson(pageResultDTO);
        return AjaxUtil.gridJson(pageResultDTO);
    }

    @ExceptionMessageAnnotation(errorMessage = "获取线上脚本任务数")
    @RequestMapping("getJobNum.ajax")
    @ResponseBody
    public JSONObject getJobNum(UrmUserHolder holder, DataDevScriptFile file, String scriptName) throws Exception {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotBlank(scriptName) && file.getApplicationId() != null) {
            DataDevScriptFilePublish publish = publishService.getScriptInfoByName(file.getApplicationId(), scriptName, holder.getErp());
            jsonObject.put("buffaloInfo", publish);
            DataDevApplication appInfo = appGroupUtil.getAppInfo(file.getApplicationId());
            String appGroupName = (appInfo != null && StringUtils.isNotBlank(appInfo.getAppgroupName())) ? (appInfo.getAppgroupName() + "(" + file.getApplicationId().toString() + ")") : file.getApplicationId().toString();
            jsonObject.put("appGroupName", appGroupName);
        }
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

    @ExceptionMessageAnnotation(errorMessage = "获取项目空间")
    @RequestMapping("getAppByErp.ajax")
    @ResponseBody
    public JSONObject getAppByErp(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath) throws Exception {
//        projectService.verifyUserAuthority(userHolder.getErp(),gitProjectId);
        List<DataDevApplication> list = appGroupUtil.getAppsByErp(userHolder.getErp());
        DataDevScriptFilePublish publish = null;
        if (StringUtils.isNotBlank(gitProjectFilePath) && gitProjectId != null) {
            publish = publishService.findLastNotFail(gitProjectId, gitProjectFilePath, null);
        }
        if (publish != null && publish.getApplicationId() != null) {
            for (DataDevApplication application : list) {
                if (application.getAppgroupId().equals(publish.getApplicationId().intValue())) {
                    application.setDefaultStatus(1);
                    break;
                }
            }
        }
        return JSONObjectUtil.getSuccessResult(list);
    }

    @RequestMapping("checkUpline.ajax")
    @ResponseBody
    public JSONObject checkUpline(Long gitProjectId, String gitProjectFilePath) throws Exception {
        DataDevScriptFilePublish publish = publishService.findLastNotFail(gitProjectId, gitProjectFilePath, null);
        if (publish != null) {
            return JSONObjectUtil.getSuccessResult(1);
        } else {
            return JSONObjectUtil.getSuccessResult(0);
        }
    }

    @ExceptionMessageAnnotation(errorMessage = "获取所有集市")
    @RequestMapping("getAllMarket.ajax")
    @ResponseBody
    public JSONObject getAllMarket() throws Exception {
        JsfResultDto jsfResultDto = clusterJSFInterface.getAllHadoopMarkets(appId, appToken, System.currentTimeMillis());
        return JSONObjectUtil.getSuccessResult(jsfResultDto.getList());
    }


    @RequestMapping("getScriptInfoByName.ajax")
    @ResponseBody
    public JSONObject getScriptInfoByName(String scriptName, Long jsdAppgroupId, UrmUserHolder userHolder) throws Exception {
        try {
            DataDevScriptFilePublish publish = publishService.getScriptInfoByName(jsdAppgroupId, scriptName, userHolder.getErp());
            return JSONObjectUtil.getSuccessResult(publish);
        } catch (Exception e) {
            return JSONObjectUtil.getSuccessResult(null);
        }
    }

}
