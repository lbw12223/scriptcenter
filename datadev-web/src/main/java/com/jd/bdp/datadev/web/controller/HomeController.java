package com.jd.bdp.datadev.web.controller;

import com.alibaba.fastjson.JSON;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevGitAccessLevelEnum;
import com.jd.bdp.datadev.enums.DataDevOpenScriptTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptGitStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.api.model.ApiResult;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.gd.util.StringUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by zhangrui25 on 2017/12/8.
 */
@Controller
public class HomeController {
    private static final Logger logger = Logger.getLogger(HomeController.class);

    private static final String NEW_USER_GUIDE_KEY = "datadev_index_guide_%s_%s"; //

    @Autowired
    private DataDevScriptFileService fileService;

    @Autowired
    private DataDevScriptDirService dirService;

    @Autowired
    private DataDevScriptRunDetailService detailService;

    @Autowired
    private DataDevScriptPublishService publishService;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private ImportScriptManager importScriptManager;

    @Autowired
    private DataDevGitProjectMemberService projectMemberService;

    @Autowired
    private DataDevGitGroupMemberService groupMemberService;

    @Autowired
    private DataDevScriptTemplateService templateService;

    @Autowired
    private ProjectInterface projectInterface ;

    @Value("${buffalo.auth.apply.workFlow.ugdap}")
    private String ugdapWorkFlowUrl;

    @Value("${buffalo.auth.apply.workFlow.nor}")
    private String norWorkFlowUrl;
    @Value("${git.private.user}")
    private String gitUser;

    @Autowired
    private Cluster jimClient;

    @Autowired
    private UrmUtil urmUtil;


    @Value("${datadev.appId}")
    private String appId;

    @Value("${datadev.token}")
    private String appToken;


    private String getGuideRedisKey(String erp) {
        String env = SpringPropertiesUtils.getPropertiesValue("${datadev.env}");
        return (String.format(NEW_USER_GUIDE_KEY, env, erp));
    }

    @RequestMapping({"/scriptcenter/showGuide.ajax"})
    @ResponseBody
    public String showGuide(UrmUserHolder userHolder) {
        jimClient.del(getGuideRedisKey(userHolder.getErp()));
        return "success";
    }

    @RequestMapping({"/scriptcenter/blank.html"})
    public String blank(UrmUserHolder userHolder) {
        return "/scriptcenter/home/blank";
    }


    @RequestMapping({"/scriptcenter/guideStep.ajax"})
    @ResponseBody
    public com.alibaba.fastjson.JSONObject guideStep(UrmUserHolder userHolder, String step) {
        JSONObject guideStep = new JSONObject();
        guideStep.put("step", step);
        guideStep.put("isFinish", "0");
        jimClient.getSet(getGuideRedisKey(userHolder.getErp()), JSON.toJSONString(guideStep));
        return JSONObjectUtil.getSuccessResult("请求成功");
    }

    @RequestMapping({"/scriptcenter/finishGuide.ajax"})
    @ResponseBody
    public com.alibaba.fastjson.JSONObject finishGuide(UrmUserHolder userHolder, String step) {
        JSONObject guideStep = new JSONObject();
        guideStep.put("step", step);
        guideStep.put("isFinish", "1");
        jimClient.getSet(getGuideRedisKey(userHolder.getErp()), JSON.toJSONString(guideStep));
        return JSONObjectUtil.getSuccessResult("请求成功");
    }

    @RequestMapping({"/scriptcenter/guide_index.html"})
    public String indexGuide() {
        return "/scriptcenter/guide/guide_index";
    }


    @RequestMapping({"/scriptcenter/index.html"})
    public String index(UrmUserHolder userHolder, Model model,
                        @RequestParam(value = "operatorType", defaultValue = "edit") String operatorType,
                        @RequestParam(value = "gitProjectId", defaultValue = "0") Long gitProjectId,
                        @RequestParam(value = "gitProjectFilePath", defaultValue = "") String gitProjectFilePath,
                        @RequestParam(value = "scriptId", defaultValue = "0") Long scriptId,
                        @RequestParam(value = "scriptName", defaultValue = "") String scriptName,
                        @RequestParam(value = "scriptVersion", defaultValue = "") String scriptVersion,
                        @RequestParam(value = "jsdAppgroupId", defaultValue = "0") Long jsdAppgroupId,
                        @RequestParam(value = "ip", defaultValue = "") String ip,
                        @RequestParam(value = "cg", defaultValue = "false") String cg) throws Exception {
        gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");

        if (StringUtils.isNotBlank(ip)) {
            String ipToken = detailService.encryptIp(ip);
            model.addAttribute("detailToken", ipToken);
        }
        model.addAttribute("cg", cg);

        Integer openScriptType = DataDevOpenScriptTypeEnum.Normal.toCode();
        if (gitProjectId <= 0L) {
            DataDevScriptFilePublish publish = publishService.findByBuffaloScriptId(scriptId);
            if (publish != null) {
                gitProjectId = publish.getGitProjectId();
                gitProjectFilePath = publish.getGitProjectFilePath();
            } else if (scriptId > 0) {
                openScriptType = DataDevOpenScriptTypeEnum.BuffaloImport.toCode();
            }
        } else if (StringUtils.isNotBlank(gitProjectFilePath)) {
            //调度 会显项目
            openScriptType = DataDevOpenScriptTypeEnum.OpenScript.toCode();
            String erp = userHolder.getErp();
            DataDevGitProject targetProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
            List<DataDevGitProject> projectList = dataDevGitProjectService.getErpProject(erp);
            boolean hasRight = false;
            for (DataDevGitProject project : projectList) {
                if (project.getGitProjectId().equals(gitProjectId)) {
                    hasRight = true;
                    break;
                }
            }
            if (targetProject == null || targetProject.getDeleted() == 1) {
                openScriptType = DataDevOpenScriptTypeEnum.IdeNoAuth.toCode();
                model.addAttribute("targetGitPath", targetProject != null ? targetProject.getGitProjectPath() : "");
            } else if (!hasRight) {
//            } else if (true) {
                StringBuilder stringBuilder = new StringBuilder();
                List<DataDevGitProjectMember> memberList = projectMemberService.findAll(targetProject.getGitProjectId());
                List<DataDevGitGroupMember> gitGroupMembers = groupMemberService.queryFromGroupId(targetProject.getGroupId());
                for (DataDevGitProjectMember projectMember : memberList) {
                    if (projectMember.getAccessLevel() != null && projectMember.getAccessLevel() >= DataDevGitAccessLevelEnum.Master.toCode() && !gitUser.equalsIgnoreCase(projectMember.getGitMemberUserName())) {
                        String oenerErp = projectMember.getGitMemberUserName();
                        String name = urmUtil.getNameByErp(oenerErp);
                        if (StringUtils.isNotBlank(name)) {
                            stringBuilder.append(name).append("(").append(oenerErp).append("),");
                        } else {
                            stringBuilder.append(oenerErp).append(",");
                        }
                    }
                }
                for (DataDevGitGroupMember gitGroupMember : gitGroupMembers) {
                    if (gitGroupMember.getAccessLevel() != null && gitGroupMember.getAccessLevel() == DataDevGitAccessLevelEnum.Owner.toCode() && !gitUser.equalsIgnoreCase(gitGroupMember.getGitMemberUserName())) {
//                    if(true){
                        String oenerErp = gitGroupMember.getGitMemberUserName();
                        String name = urmUtil.getNameByErp(oenerErp);
                        if (StringUtils.isNotBlank(name)) {
                            stringBuilder.append(name).append("(").append(oenerErp).append("),");
                        } else {
                            stringBuilder.append(oenerErp).append(",");
                        }
                    }
                }
                model.addAttribute("ownerErps", stringBuilder.length() > 0 ? stringBuilder.substring(0, stringBuilder.length() - 1) : "");
                openScriptType = DataDevOpenScriptTypeEnum.UserNoAuth.toCode();
            } else if (openScriptType.equals(DataDevOpenScriptTypeEnum.OpenScript.toCode()) && scriptId > 0) {
                DataDevScriptFile targetFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
                if (targetFile == null) {
                    openScriptType = DataDevOpenScriptTypeEnum.ReBuild.toCode();
                } else if (jsdAppgroupId != null) {
                    DataDevScriptFilePublish dataDevScriptFilePublish = publishService.findLastSuccess(gitProjectId, gitProjectFilePath, jsdAppgroupId);
                    if (dataDevScriptFilePublish != null && !targetFile.getVersion().equals(dataDevScriptFilePublish.getVersion())) {
                        openScriptType = DataDevOpenScriptTypeEnum.MergeScript.toCode();
                        model.addAttribute("buffaloCurrentVersion", dataDevScriptFilePublish.getVersion());
                        model.addAttribute("datadevLastVersion", targetFile.getVersion());
                        model.addAttribute("mergeScriptType", targetFile.getType());
                    }
                }
            }
        }
//        //test
//        DataDevScriptFile testFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId,gitProjectFilePath);
//        if(testFile!=null){
//            openScriptType = DataDevOpenScriptTypeEnum.MergeScript.toCode();
//            model.addAttribute("buffaloCurrentVersion", "1003");
//            model.addAttribute("datadevLastVersion", testFile.getVersion());
//            model.addAttribute("mergeScriptType", testFile.getType());
//        }


        //test

        model.addAttribute("ugdapWorkFlowUrl", ugdapWorkFlowUrl);
        model.addAttribute("norWorkFlowUrl", norWorkFlowUrl);
        model.addAttribute("operatorType", operatorType);
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        model.addAttribute("scriptId", scriptId);
        if (scriptId > 0L) {
            JSONObject jsonObject = importScriptManager.getOneScript(userHolder.getErp(), scriptId);
            model.addAttribute("scriptName", jsonObject.getString("fileName"));
        } else {
            model.addAttribute("scriptName", scriptName);
        }
        model.addAttribute("scriptVersion", scriptVersion);
        model.addAttribute("jsdAppgroupId", jsdAppgroupId);
        model.addAttribute("loginErp", userHolder.getErp());
        model.addAttribute("scriptTypeMap", DataDevScriptTypeEnum.toCodeNameMap());
        model.addAttribute("openScriptType", openScriptType);

        //获取项目空间

        ProjectBO projectBO = new ProjectBO();
        projectBO.setErp(userHolder.getErp());
        ApiResult<ProjectBO> apiResult = projectInterface.getGrantAuthorityProject(appId, appToken, System.currentTimeMillis(), projectBO);
        logger.info("getGrantAuthorityProject.... {} " + com.alibaba.fastjson.JSONObject.toJSONString(apiResult));
        model.addAttribute("projectBos", apiResult.getList());


        String showNewUserGuide = "{isFinish:1}";//jimClient.get(getGuideRedisKey(userHolder.getErp()));
        if (StringUtils.isBlank(showNewUserGuide)) {
            return "/scriptcenter/guide/guide_index";
        } else {
            try {
                com.alibaba.fastjson.JSONObject temp = JSON.parseObject(showNewUserGuide);
                if (temp.getString("isFinish").equals("1")) {
                    return "/scriptcenter/home/home_index";
                } else {
                    model.addAttribute("firstStep", temp.getString("step"));
                    return "/scriptcenter/guide/guide_index";
                }
            } catch (Exception e) {
                logger.error(e);
            }
            return "/scriptcenter/guide/guide_index";
        }
    }

    @RequestMapping({"/scriptcenter/demo.html"})
    public String demo(HttpServletRequest request, Model model, UrmUserHolder urmUserHolder) {
        return "/scriptcenter/home/demo";
    }

    @RequestMapping({"/scriptcenter/"})
    public String indexDataDev(HttpServletRequest request, Model model, UrmUserHolder urmUserHolder) {
        return "redirect:/scriptcenter/index.html";
    }

    /**
     * @param
     * @param model
     * @param
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping({"/scriptcenter/home/home_open_ide.html"})
    public String openIde(Model model, DataDevScriptFile file, UrmUserHolder urmUserHolder) throws Exception {
        dataDevGitProjectService.verifyUserAuthority(urmUserHolder.getErp(), file.getGitProjectId());
        String path = URLDecoder.decode(file.getGitProjectFilePath(), "utf-8");
        file.setGitProjectFilePath(path);
        DataDevScriptFile scriptFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (scriptFile == null) {
            throw new RuntimeException("脚本不存在");
        }
        DataDevScriptTemplate template = templateService.getTemplateByFileId(scriptFile.getId());
        if (template != null && !template.getIncharge().equals(urmUserHolder.getErp())) {
            throw new RuntimeException("无权限操作模板");
        }
        file.setName(scriptFile.getName());
        file.setType(scriptFile.getType());
        DataDevScriptRunDetail runDetail = detailService.findLastRunByFileId(scriptFile.getId(), urmUserHolder.getErp());
        if (runDetail != null) {
            model.addAttribute("configId", runDetail.getScriptConfigId());
        }
        if (scriptFile != null && scriptFile.getArgs() != null) {
            model.addAttribute("args", scriptFile.getArgs());
        }
        model.addAttribute("templateId", template != null ? template.getId() : -1);
        model.addAttribute("scriptFileId", scriptFile.getId());
        model.addAttribute("gitProjectFilePath", scriptFile.getGitProjectFilePath());
        model.addAttribute("version", scriptFile.getVersion());
        model.addAttribute("gitProjectFilePath", scriptFile.getGitProjectFilePath());
        model.addAttribute("scriptType", scriptFile.getType());
        model.addAttribute("canEdit", DataDevScriptTypeEnum.canEdit(scriptFile.getType()));
        model.addAttribute("canRun", DataDevScriptTypeEnum.canRun(scriptFile.getType()));
        model.addAttribute("pythonType", template != null ? template.getPythonType() : file.getPythonType());
        model.addAttribute("gitProjectId", scriptFile.getGitProjectId());
        model.addAttribute("gitProjectDirPath", scriptFile.getGitProjectDirPath());
        model.addAttribute("isShow", scriptFile.getIsShow());
        model.addAttribute("startShellPath", scriptFile.getStartShellPath());
        model.addAttribute("fileMd5", scriptFile.getFileMd5());
        model.addAttribute("fileName", scriptFile.getName());
        model.addAttribute("runType", (scriptFile.getDependencyId() != null && scriptFile.getDependencyId() > 0) ? 1 : 0);
        model.addAttribute("relationDependencyId", scriptFile.getRelationDependencyId());
        model.addAttribute("lastmender", urmUtil.getErpAndNameByErp(scriptFile.getMender()));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("lastmodified", dateFormat.format(scriptFile.getModified()));
        DataDevScriptGitStatusEnum gitStatusEnum = DataDevScriptGitStatusEnum.NON;
        if (!dirService.isBelongTarget(scriptFile.getGitProjectFilePath())) {
            model.addAttribute("filePosition", "general");
            gitStatusEnum = DataDevScriptGitStatusEnum.getGitStatus(scriptFile.getGitVersion(), scriptFile.getLastGitVersion(), scriptFile.getFileMd5(), scriptFile.getLastGitVersionMd5(), scriptFile.getGitDeleted());
        } else {
            model.addAttribute("filePosition", "target");
        }
        if (template != null) {

        }
        model.addAttribute("gitStatus", template != null ? DataDevScriptGitStatusEnum.NON : gitStatusEnum.toCode());

        //test

        String currentVersion = StringUtils.isBlank(file.getVersion()) ? scriptFile.getVersion() : file.getVersion();
        DataDevScriptFile currentFile = scriptFile;
        if (StringUtils.isNotBlank(scriptFile.getVersion()) && !currentVersion.equals(scriptFile.getVersion())) {
            currentFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath(), currentVersion);
        }
        model.addAttribute("currentRelationDependencyId", currentFile != null ? currentFile.getRelationDependencyId() : "");
        //version
        model.addAttribute("lastVersion", scriptFile.getVersion());//最新版本号
        model.addAttribute("version", currentVersion); //当前版本号
        logger.error("here.........");
        return "scriptcenter/home/home_open_ide/home_open_ide";
    }


}
