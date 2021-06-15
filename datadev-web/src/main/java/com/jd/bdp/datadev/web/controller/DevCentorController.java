package com.jd.bdp.datadev.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevScriptGitStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.jdgit.GitHttpUtil;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.service.impl.DataDevCenterImpl;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.interceptor.ProjectSpaceIdParam;
import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.bdp.rc.api.ApiResult;
import com.jd.bdp.rc.api.ReleaseInterface;
import com.jd.bdp.rc.api.domains.ReleaseInfoFromDevDto;
import com.jd.bdp.rc.domain.bo.ReleaseRecordBo;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.jsf.gd.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/scriptcenter/devcenter/")
public class DevCentorController {

    private static final Logger logger = Logger.getLogger(DevCentorController.class);


    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private DataDevScriptFileService fileService;

    @Autowired
    private DataDevScriptTemplateService templateService;


    @Autowired
    private DataDevScriptDirService dirService;

    @Autowired
    private DataDevScriptRunDetailService detailService;


    @Value("${buffalo.auth.apply.workFlow.ugdap}")
    private String ugdapWorkFlowUrl;

    @Value("${buffalo.auth.apply.workFlow.nor}")
    private String norWorkFlowUrl;

    @Autowired
    private UrmUtil urmUtil ;

    @Autowired
    private ProjectInterface projectInterface ;

    @Autowired
    private DataDevGitProjectService projectService;


    @Autowired
    private ImportScriptManager importScriptManager;

    @Autowired
    private AppGroupProjectUtil appGroupProjectUtil;


    @Autowired
    private DataDevScriptPublishService publishService;

    @Autowired
    private ClusterJSFInterface clusterJSFInterface;

    private static String pattern = "^[0-9a-zA-z\\-_]+\\.(\\w+)$";


    @Autowired
    private DataDevCenterService dataDevCenterService ;


    @Autowired
    private ReleaseInterface releaseInterface ;

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

    @RequestMapping({"script_edit.html"})
    public String openIde(Model model, DataDevScriptFile file, UrmUserHolder urmUserHolder ) throws Exception {
        model.addAttribute("ugdapWorkFlowUrl", ugdapWorkFlowUrl);
        model.addAttribute("norWorkFlowUrl", norWorkFlowUrl);
        if(file.getId() != null ){
            //id 优先
            DataDevScriptFile dataBaseScriptFile =  fileService.findById(file.getId());
            if(dataBaseScriptFile != null){
                file.setGitProjectId(dataBaseScriptFile.getGitProjectId());
                file.setGitProjectFilePath(dataBaseScriptFile.getGitProjectFilePath());
            }
        }
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
        model.addAttribute("isGitOrCoding", file.getGitProjectId() < GitHttpUtil._10YI);

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
        return "scriptcenter/devcenter/script_edit";
    }


    @RequestMapping({"script_manage.html"})
    public String scriptManage(Model model,UrmUserHolder urmUserHolder ) throws Exception {
        model.addAttribute("loginErp", urmUserHolder.getErp());
        return "scriptcenter/devcenter/left/scripts/script_manage";
    }
    @RequestMapping({"table_query.html"})
    public String tableQuery(Model model,UrmUserHolder urmUserHolder) throws Exception {
        return "scriptcenter/devcenter/left/scripts/table_query_all_table";
    }
    @RequestMapping({"function_manage.html"})
    public String functionManage(Model model,UrmUserHolder urmUserHolder) throws Exception {
        return "scriptcenter/devcenter/left/scripts/function_manage";
    }
    @RequestMapping({"function_manage_detail.html"})
    public String functionManageDetail(Model model,UrmUserHolder urmUserHolder) throws Exception {
        return "scriptcenter/devcenter/left/scripts/function_manage_detail";
    }

    @RequestMapping({"move_save_rename_file.html"})
    public String moveSaveRenameFile(Model model,UrmUserHolder urmUserHolder ) throws Exception {
        return "scriptcenter/devcenter/left/scripts/move_save_rename_file";
    }

    @RequestMapping({"create_sql.html"})
    public String createSql(Model model,UrmUserHolder urmUserHolder ) throws Exception {
        return "scriptcenter/devcenter/left/scripts/create_sql";
    }

    @RequestMapping({"table_query_detail.html"})
    public String tableQueryDetail(Model model,UrmUserHolder urmUserHolder ) throws Exception {
        return "scriptcenter/devcenter/left/scripts/table_query_detail";
    }

    @RequestMapping({"table_query_data_preview.html"})
    public String tableDataPreview(Model model,UrmUserHolder urmUserHolder ) throws Exception {
        return "scriptcenter/devcenter/left/scripts/table_query_data_preview";
    }

    @Autowired
    private DataDevScriptDiffService dataDevScriptDiffService;

    @Autowired
    private ProjectSpaceRightComponent projectSpaceRightComponent ;


    @RequestMapping({"uplineArtCheck.html"})
    public String uplineCheck(Model model,UrmUserHolder urmUserHolder ,
                              @ProjectSpaceIdParam Long projectSpaceId ,
                              Long scriptId) throws Exception {

        try {


            DataDevScriptFile dataDevScriptFile = fileService.findById(scriptId);


            JSONObject result = dataDevScriptDiffService.getTaskList(projectSpaceId, dataDevScriptFile.getName(), urmUserHolder.getErp());
            Long totalCount = result.getLong("totalCount");
            Long totalL0 = result.getLong("totalL0");
            Long totalL1 = result.getLong("totalL1");

            ProjectBO projectB0 = projectSpaceRightComponent.getProjectSpaceById(projectSpaceId);
            model.addAttribute("totalL0",totalL0);
            model.addAttribute("totalL1",totalL1);
            model.addAttribute("totalCount",totalCount);
            model.addAttribute("spaceProjectName",projectB0 != null ? projectB0.getName() : "");
            model.addAttribute("dataDevScriptFile",dataDevScriptFile);

        }catch (Exception e){

        }




        return "scriptcenter/devcenter/upline_art_check";
    }






    @RequestMapping("shareTemplate.html")
    public String shareTemplate(UrmUserHolder userHolder, Long templateId , Model model) throws Exception {
        DataDevScriptTemplate dataDevScriptTemplate = templateService.getScriptTemplateById(templateId);
        model.addAttribute("dataDevScriptTemplate", dataDevScriptTemplate); //当前版本号
        model.addAttribute("scriptType", DataDevScriptTypeEnum.enumValueOf(dataDevScriptTemplate.getScriptType()).name()); //当前版本号
        DataDevScriptFile scriptFile = fileService.findById(dataDevScriptTemplate.getScriptFileId());
        dataDevScriptTemplate.setGitProjectFilePath(scriptFile.getGitProjectFilePath());
        dataDevScriptTemplate.setGitProjectId(scriptFile.getGitProjectId());
        List<DataDevScriptTemplateShare> infos = templateService.getSharesInfos(templateId);
        StringBuilder erpsBuilder = new StringBuilder();
        boolean gitShares = false;
        for (DataDevScriptTemplateShare share : infos) {
            if (share.getShareType() == 1) {
                erpsBuilder.append(",").append(share.getShareTarget());
            } else if (share.getShareType() == 2) {
                gitShares = true;
            }
        }

        dataDevScriptTemplate.setShareErps(erpsBuilder.length() > 0 ? erpsBuilder.substring(1) : "");
        dataDevScriptTemplate.setShareGits(gitShares);
        return "scriptcenter/home/share_temlplate";
    }


    @RequestMapping("saveMutilFile.html")
    public String mutilFile(UrmUserHolder userHolder, Long gitProjectId, String gitProjectDirPath, Model model) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);

        model.addAttribute("dataDevGitProject", projectService.getGitProjectBy(gitProjectId));
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectDirPath", gitProjectDirPath);
        return "scriptcenter/devcenter/save_mutil_file";
    }
    @RequestMapping("uplineArtDiff.html")
    public String uplineArtDiff(UrmUserHolder userHolder, String scriptFileId, Model model) throws Exception {
        model.addAttribute("dataDevScriptFile", fileService.findById(Long.parseLong(scriptFileId)));
        model.addAttribute("scriptFileId", scriptFileId);
        return "scriptcenter/devcenter/upline_art_diff";
    }




    private void checkIsInRelease(DataDevScriptFile file){

        ReleaseInfoFromDevDto releaseInfoFromDevDto = new ReleaseInfoFromDevDto();
        releaseInfoFromDevDto.setObjType("script");
        releaseInfoFromDevDto.setObjKey(file.getGitProjectId() + DataDevCenterImpl.SPLIT+ file.getGitProjectFilePath());
        ApiResult<Boolean> haveReleaseing = releaseInterface.isHaveReleaseing(appId, appToken, System.currentTimeMillis(), releaseInfoFromDevDto);

        if(haveReleaseing.getCode().equals(-2)) {
            throw new RuntimeException("当前脚本正在审批中，请在发布中心取消后，重新申请!");
        }
        if(haveReleaseing.getCode().equals(-1)){
            throw new RuntimeException("检测脚本是否可以发布"+haveReleaseing.getMessage());
        }

    }

    @ExceptionMessageAnnotation(errorMessage = "上线脚本")
    @RequestMapping("upLineScript.ajax")
    @ResponseBody
    public JSONObject upLineScript(UrmUserHolder holder, DataDevScriptFile file, Long scriptId
            , @RequestParam(value = "relaveScriptStatus", defaultValue = "0") Integer relaveScriptStatus
            , @RequestParam(value = "runType", defaultValue = "0") Integer runType) throws Exception {
        projectService.verifyUserAuthority(holder.getErp(), file.getGitProjectId());
        DataDevScriptFile old = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());

        if (old == null) {
            throw new RuntimeException("脚本不存在");
        }
        if (org.apache.commons.lang.StringUtils.isBlank(file.getVersion())) {
            throw new RuntimeException("脚本版本号为空");
        }
        if (file.getApplicationId() == null) {
            throw new RuntimeException("项目空间为空");
        }
        /**
         * 检测发布中心是否已经有在流程中的数据
         */
        checkIsInRelease(old);
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

        res = dataDevCenterService.upLineScript(old, holder.getErp(), publish, runType);

        logger.error("==================publish:" + JSONObject.toJSONString(res));
        resJson.put("buffaloAddJobUrl", buffaloAddJobUrl);
        resJson.put("buffaloLookJobUrl", buffaloLookJobUrl);
        resJson.put("scriptName", old.getName());
        if (res.getRequestId() != null) {
            resJson.put("buffaloScriptWorkFlowUrl", buffaloScriptWorkFlowUrl + res.getRequestId());
        }
        return JSONObjectUtil.getSuccessResult("发布成功", resJson);

    }






}
