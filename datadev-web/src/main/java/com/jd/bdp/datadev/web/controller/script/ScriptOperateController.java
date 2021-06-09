package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.SqlLimitParseResEnum;
import com.jd.bdp.SqlParseLimitUtil;
import com.jd.bdp.SqlParseResult;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.agent.api.AgentCodeFormatInterface;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.RedisComponent;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.*;
import com.jd.bdp.datadev.exception.DependencyDetailNotFoundException;
import com.jd.bdp.datadev.exception.GitFileNotFoundException;
import com.jd.bdp.datadev.jdgit.Base64Util;
import com.jd.bdp.datadev.jdgit.JDGitFiles;
import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.ApiResultCode;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.interceptor.ProjectSpaceIdParam;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.ump.annotation.JProEnum;
import com.jd.ump.annotation.JProfiler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 页面对脚本的各种操作
 */
@Controller
@RequestMapping("/scriptcenter/script/")
public class ScriptOperateController {
    private static final Logger logger = Logger.getLogger(ScriptOperateController.class);
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Autowired
    private DataDevScriptDirService dirService;
    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private DataDevScriptRunDetailService runDetailService;
    @Autowired
    private DataDevScriptService scriptService;
    @Autowired
    private DataDevScriptPublishService publishService;
    @Autowired
    private DataDevGitProjectService projectService;
    @Autowired
    private DataDevScriptTemplateService templateService;
    @Autowired
    private AgentCodeFormatInterface agentCodeFormatInterface;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private DataDevDependencyService devDependencyService;

    @Autowired
    private DataDevScriptUploadService dataDevScriptUploadService;

    @Autowired
    private DataDevGitHisDetailService dataDevGitHisDetailService;

    @Autowired
    private RedisComponent redisComponent;

    public static final ThreadLocal<SimpleDateFormat> SDF_LOCAL = new ThreadLocal<SimpleDateFormat>();

    @RequestMapping("/addDir.ajax")
    @ResponseBody
    public JSONObject addDir(UrmUserHolder userHolder, DataDevScriptDir dir) throws Exception {
        if (StringUtils.isBlank(dir.getName())) {
            throw new RuntimeException("目录名为空");
        }
        projectService.verifyUserAuthority(userHolder.getErp(), dir.getGitProjectId());
        String parentPath = Base64Util.formatName(dir.getGitParentProjectDirPath());
        String name = Base64Util.formatName(dir.getName());
        String path = StringUtils.isNotBlank(parentPath) ? (parentPath + "/" + name) : name;
        ZtreeNode ztreeNode = dirService.createScriptDir(dir.getGitProjectId(), path, userHolder.getErp());
        return JSONObjectUtil.getSuccessResult("添加目录成功", ztreeNode);
    }

    @ExceptionMessageAnnotation(errorMessage = "删除目录")
    @RequestMapping("/deleteDir.ajax")
    @ResponseBody
    public JSONObject deleteDir(DataDevScriptDir scriptDir, UrmUserHolder userHolder) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), scriptDir.getGitProjectId());
        dirService.deleteDir(scriptDir.getGitProjectId(), scriptDir.getGitProjectDirPath(), userHolder.getErp());
        return JSONObjectUtil.getSuccessResult("目录删除成功", null);
    }

    /**
     * 新建临时文件
     *
     * @param userHolder        sso注入用户信息
     * @param type              脚本类型 {@link com.jd.bdp.datadev.enums.DataDevScriptTypeEnum}
     * @param pythonType        1python2  2python3
     * @param gitProjectId      git项目id
     * @param isShow            1临时左边树看不到  0正式 可以看到
     * @param gitProjectDirPath 父目录path
     * @param content           新建脚本默认内容
     * @param templateId        模板id 有大于0的值表示根据模板创建脚本
     * @param isTemplate        true表示创建的是一个模板，false表示创建普通脚本
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "新建临时文件")
    @RequestMapping("/addScript.ajax")
    @ResponseBody
    public JSONObject addScript(UrmUserHolder userHolder, @RequestParam("type") Integer type,
                                @RequestParam(value = "pythonType", defaultValue = "0") Integer pythonType,
                                @RequestParam("gitProjectId") Long gitProjectId,
                                @RequestParam(value = "isShow", defaultValue = "1") Integer isShow,
                                @RequestParam(value = "gitProjectDirPath", defaultValue = "") String gitProjectDirPath,
                                @RequestParam(value = "content", defaultValue = "") String content,
                                @RequestParam(value = "templateId", defaultValue = "0") Long templateId,
                                @RequestParam(value = "isTemplate", defaultValue = "false") Boolean isTemplate) throws Exception {
        if (!isTemplate) {
            projectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);
        } else {
            //模板都统一创建在一个项目下
            gitProjectId = templateService.getTemPlateProjectId();
        }
        if (type == null || DataDevScriptTypeEnum.enumValueOf(type) == null) {
            throw new RuntimeException("新建脚本,脚本类型不能为空");
        }
        byte[] contents;
        String args = "";
        if (templateId > 0) {
            DataDevScriptTemplate scriptTemplate = templateService.getScriptTemplateById(templateId);
            if (scriptTemplate == null) {
                throw new RuntimeException("模板不存在");
            }
            DataDevScriptFile templateScript = fileService.findById(scriptTemplate.getScriptFileId());
            if (templateScript == null) {
                throw new RuntimeException("模板脚本不存在");
            }
            contents = fileService.getScriptBytes(templateScript.getGitProjectId(), templateScript.getGitProjectFilePath(), templateScript.getVersion(), userHolder.getErp(), true);
            args = templateScript.getArgs();
        } else {
            contents = StringUtils.isNotBlank(content) ? content.getBytes("UTF-8") : null;
        }
        ZtreeNode ztreeNode = fileService.createNewFile(gitProjectId, gitProjectDirPath, type, userHolder.getErp(), 1, contents, "", null, gitProjectDirPath, args, null, null);
        if (isTemplate) {
            templateService.insertScriptTemplate(ztreeNode.getName(), type, pythonType, 1, userHolder.getErp(), "", "", 0L, ztreeNode.getId(), 1);
        }
        return JSONObjectUtil.getSuccessResult(ztreeNode);
    }

    @ExceptionMessageAnnotation(errorMessage = "运行脚本")
    @RequestMapping("run.ajax")
    @ResponseBody
    public JSONObject run(UrmUserHolder userHolder, DataDevScriptRunDetail runDetail, String detailToken, @ProjectSpaceIdParam Long projectSpaceId) throws Exception {
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(runDetail.getGitProjectId(), runDetail.getGitProjectFilePath(), runDetail.getVersion());
        if (StringUtils.isNotBlank(detailToken)) {
            String ip = runDetailService.decryptIp(detailToken);
            if (runDetailService.verifyIp(ip)) {
                runDetail.setIp(ip.trim());
            }
        }
        runDetail.setCreator(userHolder.getErp());
        runDetail.setOperator(userHolder.getErp());
        if (file == null) {
            throw new RuntimeException("该脚本不存在");
        }
        runDetailService.verifyMarket(file, runDetail, userHolder.getErp(), projectSpaceId);
        try {

            Pair<Boolean, JSONObject> sqlToolCheck = isSqlToolValidate(file, runDetail, userHolder);

            if (!sqlToolCheck.getLeft()) {
                sqlToolCheck.getRight().put("responseCode", 102);
                return JSONObjectUtil.getSuccessResult("SQL TOOL 验证失败", sqlToolCheck.getRight());
            }
//            sql limit 校验
            SqlParseResult limitCheckRes = isLimitCheck(file, runDetail, userHolder);
            if (limitCheckRes != null && limitCheckRes.getAddLimit() == SqlLimitParseResEnum.ADD_LIMIT_FAIL.toCode()) {
                throw new RuntimeException("根据平台规则，使用presto引擎需要使用limit 1000，请检查sql!");

            }
//            else if(limitCheckRes != null && limitCheckRes.getAddLimit() == SqlLimitParseResEnum.GRAMMER_FAULT.toCode()){
//                throw new RuntimeException("sql语法有错误，请检查sql!");
//            }

            logger.error("controller  runDetail===" + JSON.toJSONString(runDetail));
            runDetail.setArgsImportType(ArgsImportTypeEnum.STANDARD.toCode());
            DataDevScriptRunDetail resultDetail = scriptService.runScript(file, runDetail, false);
            DataDevScriptRunDetail resultObj = new DataDevScriptRunDetail();
            resultObj.setId(resultDetail.getId());
            resultObj.setScriptFileId(file.getId());
            resultObj.setDependencyId(resultDetail.getDependencyId());
            resultObj.setResponseCode(DataDevResponseCodeEnum.Success.toCode().toString());
            resultObj.setDevDependencyDetails(resultDetail.getDevDependencyDetails());
            return JSONObjectUtil.getSuccessResult("运行成功", resultObj);
        } catch (DependencyDetailNotFoundException e) {
            DataDevScriptRunDetail resultObj = new DataDevScriptRunDetail();
            resultObj.setResponseCode(DataDevResponseCodeEnum.DependencyNotFound.toCode().toString());
            return JSONObjectUtil.getSuccessResult(DataDevResponseCodeEnum.DependencyNotFound.toDesc(), resultObj);
        }

    }

    /**
     * check sql 的limit
     *
     * @param file
     * @param runDetail
     * @param urmUserHolder
     * @return
     */
    private SqlParseResult isLimitCheck(DataDevScriptFile file, DataDevScriptRunDetail runDetail, UrmUserHolder urmUserHolder) {
        logger.error("isLimitCheck=====file:" + JSON.toJSONString(file));
        logger.error("isLimitCheck=====runDetail:" + JSON.toJSONString(runDetail));
        SqlParseResult limitResult = null;
        if (file.getType().equals(DataDevScriptTypeEnum.SQL.toCode()) && redisComponent.doLimitCheck() && StringUtils.equalsIgnoreCase(runDetail.getEngineType(), DataDevScriptEngineTypeEnum.Presto.getValue())) {
            try {
                String sqlContent = StringUtils.isEmpty(runDetail.getContent()) ?
                        fileService.getScriptContent(runDetail.getGitProjectId(), runDetail.getGitProjectFilePath(), runDetail.getVersion(), urmUserHolder.getErp()) : runDetail.getContent();
                limitResult = SqlParseLimitUtil.parseSqlLimit(sqlContent, 1000);
                if (limitResult != null) {
                    logger.error("isLimitCheck 结果：" + JSON.toJSONString(limitResult));
                    if (limitResult.getAddLimit() == 0) {
                        //需要加limit但是程序增加limit失败
                    } else if (limitResult.getAddLimit() == SqlLimitParseResEnum.ADD_LIMIT_SUC.toCode() || limitResult.getAddLimit() == SqlLimitParseResEnum.NO_NEED_LIMIT.toCode()) {
                        //有limit的，加limit成功的，不需要加limit的
                        //修改sql
                        String addLimitSql = limitResult.getSqlContent();
                        runDetail.setContent(addLimitSql);
                        runDetail.setRunByContent(true);
                    }
                } else {
                    logger.error("check sql limit 的结果为空。");
                }

            } catch (Exception e) {
                logger.error("check sql limit发生异常，", e);
            }


        } else {
            //其他引擎不需要加limit
            limitResult = new SqlParseResult();
            limitResult.setAddLimit(1);
        }
        logger.error("isLimitCheck:" + (limitResult == null ? "isLimitCheck" : JSON.toJSONString(limitResult)));

        return limitResult;
    }

    /**
     * 1.如果是SQL运行 && 系统设置 && 页面设置 那么就执行检测
     *
     * @param runDetail
     * @return
     */
    private Pair<Boolean, JSONObject> isSqlToolValidate(DataDevScriptFile file, DataDevScriptRunDetail runDetail, UrmUserHolder urmUserHolder) {

        boolean isValidate = true;
        JSONObject responseObj = null;
        JSONArray problems = null;

        try {

            if (file.getType().equals(DataDevScriptTypeEnum.SQL.toCode())
                    && redisComponent.doCheck()
                    && runDetail.getDoSqlToolCheck().equalsIgnoreCase("true")) {


                String sqlContent = StringUtils.isEmpty(runDetail.getContent()) ?
                        fileService.getScriptContent(runDetail.getGitProjectId(), runDetail.getGitProjectFilePath(), runDetail.getVersion(), urmUserHolder.getErp()) : runDetail.getContent();
                Map<String, String> params = new HashMap<String, String>();
                params.put("string", sqlContent);
                JSONObject response = JSONObject.parseObject(HttpUtil.doPost("http://sqltools.jd.com/rulecheck/checksql", params));
                logger.error("sqlContent==" + sqlContent);
                logger.error("response==" + response);

                if (response.getString("code").equals("0")) {
                    JSONObject validateData = response.getJSONObject("data");
                    responseObj = validateData;
                    if (validateData != null) {
                        problems = validateData.getJSONArray("problems");
                        if (problems != null && problems.size() > 0) {
                            isValidate = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("检测sql合法性异常", e);
        }
        return Pair.of(isValidate, responseObj);
    }

    @ExceptionMessageAnnotation(errorMessage = "停止运行脚本")
    @RequestMapping("stop.ajax")
    @ResponseBody
    public JSONObject stop(UrmUserHolder userHolder, Long runDetailId) throws Exception {
        if (StringUtils.isBlank(userHolder.getErp())) {
            throw new RuntimeException("请先登陆");
        }
        try {
            DataDevScriptRunDetail runDetail = runDetailService.findById(runDetailId);
            projectService.verifyUserAuthority(userHolder.getErp(), runDetail.getGitProjectId());
            runDetail.setStopErp(userHolder.getErp());
            runDetail.setOperator(userHolder.getErp());
            scriptService.stopScript(runDetail);
        } catch (Exception e) {
            logger.error("===========================" + runDetailId + "停止失败:" + e.getMessage());
        }
        return JSONObjectUtil.getSuccessResult("停止成功");
    }


    @ExceptionMessageAnnotation(errorMessage = "批量停止运行脚本")
    @RequestMapping("batchStop.ajax")
    @ResponseBody
    public JSONObject batchStop(UrmUserHolder userHolder, String runDetailIds) throws Exception {
        if (StringUtils.isBlank(userHolder.getErp())) {
            throw new RuntimeException("请先登陆");
        }
        for(String runDetailId : runDetailIds.split(",")){
            try {

                DataDevScriptRunDetail runDetail = runDetailService.findById(Long.parseLong(runDetailId));
                projectService.verifyUserAuthority(userHolder.getErp(), runDetail.getGitProjectId());
                runDetail.setStopErp(userHolder.getErp());
                runDetail.setOperator(userHolder.getErp());
                scriptService.stopScript(runDetail);
            } catch (Exception e) {
                logger.error("===========================" + runDetailId + "停止失败:" + e.getMessage());
            }
        }


        return JSONObjectUtil.getSuccessResult("停止成功");
    }

    @RequestMapping("home_open_ide_save.html")
    public String saveHtml(UrmUserHolder userHolder, Model model, Long gitProjectId, Integer scriptType) throws Exception {
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("scriptType", scriptType);

        return "scriptcenter/home/home_open_ide/home_open_ide_save";
    }

    @RequestMapping("home_open_ide_save_as.html")
    public String saveAsHtml(UrmUserHolder userHolder, Model model, Long gitProjectId) throws Exception {
        model.addAttribute("gitProjectId", gitProjectId);
        return "scriptcenter/home/home_open_ide/home_open_ide_save_as";
    }


    @ExceptionMessageAnnotation(errorMessage = "保存脚本")
    @RequestMapping("save.ajax")
    @ResponseBody
    public JSONObject save(UrmUserHolder userHolder, @RequestBody DataDevScriptFile file) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());
        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }

        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(oldFile.getType());
        Long limitSize = typeEnum != null && typeEnum.toLimitSize() != null ? typeEnum.toLimitSize() : 0;
        String content = fileService.dos2unix(StringUtils.isNotBlank(file.getContent()) ? file.getContent() : "");
        byte[] bytes = content.getBytes("utf-8");
        if (bytes.length > limitSize && limitSize > 0) {
            return JSONObjectUtil.getFailResult("此类型文件最大只支持2M大小", null);
        }

        String path = Base64Util.getFullPath(file.getGitProjectDirPath(), file.getName());

        DataDevScriptFile newFile = fileService.getScriptByGitProjectIdAndFilePath(oldFile.getGitProjectId(), path);
        if (newFile != null) {
            throw new RuntimeException("脚本已经存在，请修改脚本名、或者更改目录");
        }
        //更新script_file_his,更新script_run_detail

        // Integer type = file.getType() != null ? file.getType() : DataDevScriptTypeEnum.getFileNameScriptType(file.getName()).toCode();
        // ZtreeNode ztreeNode = fileService.createNewFile(file.getGitProjectId(), path, type, userHolder.getErp(), 0, bytes, file.getDescription(), file.getStartShellPath(), null, oldFile != null ? oldFile.getArgs() : "", null, null);


        fileService.tryUpdateFile(oldFile.getGitProjectId(), oldFile.getGitProjectFilePath(), userHolder.getErp(), bytes, oldFile.getVersion(), oldFile.getGitVersion(), true);
        fileService.tmpFileUpdateHisAndDetail(oldFile.getId(), path, file.getName(), file.getGitProjectId(), oldFile.getGitProjectFilePath());
        //保存文件描述
        DataDevScriptFile params = new DataDevScriptFile();
        params.setDescription(file.getDescription());
        fileService.updateDataDevScriptFile(file.getGitProjectId(), path, params);

        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setErp(userHolder.getErp());
        jdGitFiles.setGitProjectId(oldFile.getGitProjectId());
        jdGitFiles.setFilePath(path);

        ZtreeNode result = dirService.createFileOrDirInDataBase(jdGitFiles, false);

        return JSONObjectUtil.getSuccessResult("保存成功", result);
    }

    @ExceptionMessageAnnotation(errorMessage = "另存为")
    @RequestMapping("saveAs.ajax")
    @ResponseBody
    public JSONObject saveAs(UrmUserHolder userHolder, @RequestBody DataDevScriptFile file) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());
        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }

        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(oldFile.getType());
        Long limitSize = typeEnum != null && typeEnum.toLimitSize() != null ? typeEnum.toLimitSize() : 0;
        String content = fileService.dos2unix(StringUtils.isNotBlank(file.getContent()) ? file.getContent() : "");
        byte[] bytes = content.getBytes("utf-8");
        if (bytes.length > limitSize && limitSize > 0) {
            return JSONObjectUtil.getFailResult("此类型文件最大只支持2M大小", null);
        }

        String path = Base64Util.getFullPath(file.getGitProjectDirPath(), file.getName());

        DataDevScriptFile newFile = fileService.getScriptByGitProjectIdAndFilePath(oldFile.getGitProjectId(), path);
        if (newFile != null) {
            throw new RuntimeException("脚本已经存在，请修改脚本名、或者更改目录");
        }

        Integer type = file.getType() != null ? file.getType() : DataDevScriptTypeEnum.getFileNameScriptType(file.getName()).toCode();
        ZtreeNode ztreeNode = fileService.createNewFile(file.getGitProjectId(), path, type, userHolder.getErp(), 0, bytes, file.getDescription(), file.getStartShellPath(), null, oldFile != null ? oldFile.getArgs() : "", null, null);


        return JSONObjectUtil.getSuccessResult("保存成功", ztreeNode);
    }

    /**
     * todo 调度有线上任务是否允许改变名字
     *
     * @param userHolder
     * @param file
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "移动脚本")
    @RequestMapping("move.ajax")
    @ResponseBody
    public JSONObject move(UrmUserHolder userHolder, DataDevScriptFile file) throws Exception {
//        fileService.checkUplineModifyName(file);
        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());

        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }
//        if (!fileService.canMoveScriptFile(file.getGitProjectId(), file.getGitProjectFilePath())) {
//            throw new RuntimeException("已经同步项目空间，不能进行移动");
//        }
        ZtreeNode ztreeNode = fileService.moveScriptFile(file.getGitProjectId(), file.getGitProjectFilePath(), file.getGitProjectDirPath(), file.getName(), file.getDescription(), userHolder.getErp());
        return JSONObjectUtil.getSuccessResult("保存成功", ztreeNode);
    }

    @ExceptionMessageAnnotation(errorMessage = "修改脚本名字")
    @RequestMapping("scriptRename.ajax")
    @ResponseBody
    public JSONObject scriptRename(UrmUserHolder userHolder, DataDevScriptFile file) throws Exception {

        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());
        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }
        if (StringUtils.isBlank(file.getName())) {
            throw new RuntimeException("脚本名称不能为空");
        }
        if (file.getName().indexOf("/") != -1) {
            throw new RuntimeException("重命名名称不能有/");
        }
        if (!fileService.canMoveScriptFile(file.getGitProjectId(), file.getGitProjectFilePath())) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "该脚本已经上线至任务调度buffalo，不支持重命名操作。");
            jsonObject.put("status", "1");
            return JSONObjectUtil.getSuccessResult(jsonObject);
        }
        DataDevScriptFilePublish publish = publishService.findLastNotFail(file.getGitProjectId(), file.getGitProjectFilePath(), null);
        if (publish != null) {
            throw new RuntimeException("脚本已上线,不能修改脚本名称");
        }
        DataDevScriptFile res = fileService.renameScriptFile(file.getGitProjectId(), file.getGitProjectFilePath(), file.getName(), userHolder.getErp());
        ZtreeNode ztreeNode = new ZtreeNode();
        ztreeNode.setGitProjectId(res.getGitProjectId());
        ztreeNode.setPath(res.getGitProjectFilePath());
        ztreeNode.setParentPath(res.getGitProjectDirPath());
        ztreeNode.setName(res.getName());
        ztreeNode.setType(res.getType());
        ztreeNode.setParChl(1);
        return JSONObjectUtil.getSuccessResult("重命名脚本成功", ztreeNode);

    }

//    @RequestMapping("dirRename.ajax")
//    @ResponseBody
//    public JSONObject dirRename(UrmUserHolder userHolder, DataDevScriptDir dir) throws Exception {
//        if (dir.getId() == DEFAULT_DIR_ID) {
//            throw new RuntimeException("不能修改默认文件夹");
//        }
//        DataDevScriptDir old = dirService.findDirById(dir.getId());
//        if (old == null) {
//            throw new RuntimeException("目录不存在，请刷新目录");
//        }
//        if (StringUtils.isBlank(dir.getName())) {
//            throw new RuntimeException("目录名称不能为空");
//        }
//     /*   if (!appGroupUtil.checkAppAuthority(old.getAppId(), userHolder.getErp())) {
//            throw new RuntimeException(userHolder.getErp() + "无项目空间权限");
//        }*/
//        dirService.updateName(dir.getName(), dir.getId());
//        return JSONObjectUtil.getSuccessResult(dir.getName());
//    }

    /**
     * 1.上线 运行都先保存了saveContent.ajax
     * 2.如果是运行指定版本 && 未修改，直接通过
     *
     * @param userHolder
     * @param scriptFile
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "保存脚本内容")
    @RequestMapping("saveContent.ajax")
    @ResponseBody
    public JSONObject saveContent(UrmUserHolder userHolder, @RequestBody DataDevScriptFile scriptFile) throws Exception {
        JSONObject jsonObject = caculateSaveContent(userHolder, scriptFile, false);
        return JSONObjectUtil.getSuccessResult("保存成功", jsonObject);
    }

    private JSONObject caculateSaveContent(UrmUserHolder userHolder, DataDevScriptFile scriptFile, boolean isDiscover) throws Exception {
        JSONObject jsonObject = new JSONObject();

        projectService.verifyUserAuthority(userHolder.getErp(), scriptFile.getGitProjectId());
        DataDevScriptFile dataBaseScriptFile = fileService.getScriptByGitProjectIdAndFilePath(scriptFile.getGitProjectId(), scriptFile.getGitProjectFilePath());
        if (dataBaseScriptFile == null) {
            throw new RuntimeException("该脚本不存在");
        }
        SimpleDateFormat simpleDateFormat = SDF_LOCAL.get();
        if (simpleDateFormat == null) {
            SDF_LOCAL.set(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            simpleDateFormat = SDF_LOCAL.get();
        }
        jsonObject.put("lastErp", dataBaseScriptFile.getMender());
        String modifiedTimeStr = simpleDateFormat != null ? simpleDateFormat.format(dataBaseScriptFile.getModified()) : (dataBaseScriptFile.getModified() != null ? dataBaseScriptFile.getModified().toString() : "");
        jsonObject.put("lastModified", modifiedTimeStr);
        jsonObject.put("nowErp", userHolder.getErp());

        String content = fileService.dos2unix(StringUtils.isNotBlank(scriptFile.getContent()) ? scriptFile.getContent() : "");
        byte[] bytes = content.getBytes("utf-8");

        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(dataBaseScriptFile.getType());
        Long limitSize = typeEnum != null && typeEnum.toLimitSize() != null ? typeEnum.toLimitSize() : 0;
        if (bytes.length > limitSize && limitSize > 0) {
            throw new RuntimeException("此类型文件最大只支持2M大小");
        }

        dataBaseScriptFile.setBytes(bytes);
        dataBaseScriptFile.setMender(userHolder.getErp());
        dataBaseScriptFile.setVersion(scriptFile.getVersion());
        HoldDoubleValue<Boolean, JDGitFiles> holdDoubleValue = fileService.tryUpdateFile(dataBaseScriptFile.getGitProjectId(), dataBaseScriptFile.getGitProjectFilePath(), userHolder.getErp(), dataBaseScriptFile.getBytes(), scriptFile.getVersion(), scriptFile.getGitVersion(), isDiscover);
//        DataDevScriptFile dataBaseScriptFile = fileService.getScriptByGitProjectIdAndFilePath(scriptFile.getGitProjectId(), scriptFile.getGitProjectFilePath());
        DataDevScriptGitStatusEnum gitStatus = DataDevScriptGitStatusEnum.getGitStatus(StringUtils.isNotBlank(scriptFile.getGitVersion()) ? scriptFile.getGitVersion() : dataBaseScriptFile.getGitVersion(), dataBaseScriptFile.getLastGitVersion(), holdDoubleValue.b.getFileMd5(), dataBaseScriptFile.getLastGitVersionMd5(), dataBaseScriptFile.getGitDeleted());

        DataDevScriptTemplate template = templateService.getTemplateByFileId(dataBaseScriptFile.getId());
        if (template != null && !template.getIncharge().equals(userHolder.getErp())) {
            throw new RuntimeException("无权限操作模板");
        }
        if (template != null) {
            gitStatus = DataDevScriptGitStatusEnum.NON;
        }
        jsonObject.put("gitStatus", gitStatus.toCode());
        jsonObject.put("isDirectCover", holdDoubleValue.a);
        jsonObject.put("serverVersion", holdDoubleValue.b.getVersion());
        jsonObject.put("serverContent", holdDoubleValue.b.getContent());
        jsonObject.put("gitProjectId", dataBaseScriptFile.getGitProjectId());
        jsonObject.put("gitProjectFilePath", dataBaseScriptFile.getGitProjectFilePath());
        jsonObject.put("fileMd5", holdDoubleValue.b.getFileMd5());
        jsonObject.put("name", dataBaseScriptFile.getName());

        jsonObject.put("lastVersion", holdDoubleValue.b.getVersion());
        jsonObject.put("currentVersion", holdDoubleValue.b.getVersion());

        jsonObject.put("lastmender", urmUtil.getErpAndNameByErp(userHolder.getErp()));
        jsonObject.put("lastmodified", SDF_LOCAL.get().format(new Date()));
        return jsonObject;
    }


    @ExceptionMessageAnnotation(errorMessage = "检查脚本是否存在")
    @RequestMapping("checkFileExist.ajax")
    @ResponseBody
    public JSONObject checkFileExist(DataDevScriptFile file) throws Exception {
        String path = StringUtils.isNotBlank(file.getGitProjectFilePath()) ? file.getGitProjectFilePath() : Base64Util.getFullPath(file.getGitProjectDirPath(), file.getName());
        DataDevScriptFile old = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), path);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", old != null ? 1 : 0);
        jsonObject.put("path", path);
        jsonObject.put("isShow", old != null && old.getIsShow() != null ? old.getIsShow() : 0);
        if (old != null) {
            DataDevScriptTemplate template = templateService.getTemplateByFileId(old.getId());
            jsonObject.put("isTemplate", template != null);
        }
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

    @ExceptionMessageAnnotation(errorMessage = "保存上传文件")
    @RequestMapping("saveFile.ajax")
    @ResponseBody
    public JSONObject saveFile(UrmUserHolder userHolder, DataDevScriptFile file, @RequestParam("file") MultipartFile multipartFile) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());
        String path = Base64Util.getFullPath(file.getGitProjectDirPath(), file.getName());
        Integer type = file.getType() != null ? file.getType() : DataDevScriptTypeEnum.getFileNameScriptType(file.getName()).toCode();
        DataDevScriptFile old = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), path);
        if (old != null) {
            HoldDoubleValue<Boolean, JDGitFiles> holdDoubleValue = fileService.tryUpdateFile(file.getGitProjectId(), path, userHolder.getErp(), multipartFile.getBytes(), null, null, true, file.getDescription(), file.getStartShellPath(), null);
            ZtreeNode ztreeNode = dirService.createFilePathZtreeNode(file.getGitProjectId(), path, holdDoubleValue.b.getVersion());
            return JSONObjectUtil.getSuccessResult("保存成功", ztreeNode);
        } else {
            ZtreeNode ztreeNode = fileService.createNewFile(file.getGitProjectId(), path, type, userHolder.getErp(), 0, multipartFile.getBytes(), file.getDescription(), file.getStartShellPath());
            return JSONObjectUtil.getSuccessResult("保存成功", ztreeNode);
        }
    }

    @ExceptionMessageAnnotation(errorMessage = "保存上传文件")
    @RequestMapping("saveMutilFile.ajax")
    @ResponseBody
    public JSONObject saveMutilFile(UrmUserHolder userHolder, DataDevScriptFile file, String fileName, String relativePath, @RequestParam("file") MultipartFile multipartFile) throws Exception {
        String gitProjectFilePath = (StringUtils.isNotBlank(file.getGitProjectDirPath()) ? file.getGitProjectDirPath() + "/" : file.getGitProjectDirPath()) + (StringUtils.isNotBlank(relativePath) ? relativePath : fileName);
        Integer type = file.getType() != null ? file.getType() : DataDevScriptTypeEnum.getFileNameScriptType(fileName).toCode();

        Integer fileCount = fileService.countScriptFile(file.getGitProjectId(), gitProjectFilePath);
        if (fileCount != null && fileCount > 0 && file.getFileUpSamePathOpt() == 0) {
            return JSONObjectUtil.getFailResult(2, "文件重复！", fileCount);
        }
        // fileUpSamePathOpt ;     //上传文件遇到相同的filePath 处理 1：替换 2：保留
        byte[] bytes = multipartFile.getBytes();
        ;
        if (file.getFileUpSamePathOpt() == 0) {
            ZtreeNode ztreeNode = fileService.createNewFile(file.getGitProjectId(), gitProjectFilePath, type, userHolder.getErp(), 0, bytes, file.getDescription(), file.getStartShellPath(), null, null, null, file.getScriptUpLoadId());
            return JSONObjectUtil.getSuccessResult("上传成功！", new JSONObject());
        }

        if (file.getFileUpSamePathOpt() == 1) {
            fileService.tryUpdateFile(file.getGitProjectId(), gitProjectFilePath, userHolder.getErp(), bytes, null, null, true);
            return JSONObjectUtil.getSuccessResult("替换成功！", new JSONObject());
        }

        if (file.getFileUpSamePathOpt() == 2) {
            HoldDoubleValue<String, String> pathAndName = Base64Util.splitFilePath(gitProjectFilePath);
            Long t = fileService.getMaxFileCount(file.getGitProjectId(), gitProjectFilePath);
            gitProjectFilePath = (StringUtils.isNotBlank(pathAndName.a) ? (pathAndName.a + "/") : "") + Base64Util.appendFileName(pathAndName.b, String.valueOf("_" + t));
            fileService.createNewFile(file.getGitProjectId(), gitProjectFilePath, type, userHolder.getErp(), 0, bytes, file.getDescription(), file.getStartShellPath());
            return JSONObjectUtil.getSuccessResult("保留成功！", new JSONObject());
        }


       /* DataDevScriptFile old = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), path);
        if (old != null) {
            HoldDoubleValue<Boolean, JDGitFiles> holdDoubleValue = fileService.tryUpdateFile(file.getGitProjectId(), path, userHolder.getErp(), multipartFile.getBytes(), null, null, true, file.getDescription(), file.getStartShellPath(),null);
            ZtreeNode ztreeNode = dirService.createFilePathZtreeNode(file.getGitProjectId(), path, holdDoubleValue.b.getVersion());
            return JSONObjectUtil.getSuccessResult("保存成功", ztreeNode);
        } else {
            ZtreeNode ztreeNode = fileService.createNewFile(file.getGitProjectId(), path, type, userHolder.getErp(), 0, multipartFile.getBytes(), file.getDescription(), file.getStartShellPath());
            return JSONObjectUtil.getSuccessResult("保存成功", ztreeNode);
        }*/
        return JSONObjectUtil.getSuccessResult("上传成功！", new JSONObject());
    }

  /*  private Integer getMaxFileCount(Long gitProjectId , String filePath) {
        dataDevScriptFileDao.findScriptsByFuzzy(gitProjectId, noSuffixPath);
    }
*/

    @RequestMapping("addScriptUpload.ajax")
    @ResponseBody
    public JSONObject addScriptUpload(UrmUserHolder userHolder, DataDevScriptUpLoad dataDevScriptUpLoad) throws Exception {
        String dirPath = dataDevScriptUpLoad.getGitProjectDirPath().replace("root", "");
        dirPath = dirPath.replaceFirst("/", "");
        dataDevScriptUpLoad.setGitProjectDirPath(dirPath);
        projectService.verifyUserAuthority(userHolder.getErp(), dataDevScriptUpLoad.getGitProjectId());
        dataDevScriptUpLoad.setCreator(userHolder.getErp());
        dataDevScriptUploadService.addDataDevScriptUpLoad(dataDevScriptUpLoad);
        return JSONObjectUtil.getSuccessResult("保存成功", dataDevScriptUpLoad);
    }

    @RequestMapping("saveMutilFile.html")
    public String mutilFile(UrmUserHolder userHolder, Long gitProjectId, String gitProjectDirPath, Model model) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);

        model.addAttribute("dataDevGitProject", projectService.getGitProjectBy(gitProjectId));
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectDirPath", gitProjectDirPath);
        return "scriptcenter/home/home_top/save_mutil_file";
    }

    @ExceptionMessageAnnotation(errorMessage = "复制脚本")
    @RequestMapping("copy.ajax")
    @ResponseBody
    public JSONObject copyScript(UrmUserHolder userHolder, DataDevScriptFile file) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());
        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }
        file.setCreator(userHolder.getErp());
        ZtreeNode res = fileService.copyScriptFile(file);
        return JSONObjectUtil.getSuccessResult(res);
    }

    /**
     * @param userHolder
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping("remove.ajax")
    @ResponseBody
    public JSONObject remove(UrmUserHolder userHolder, DataDevScriptFile file) throws Exception {
        projectService.verifyUserAuthority(userHolder.getErp(), file.getGitProjectId());
        fileService.deleteScriptFile(file.getGitProjectId(), file.getGitProjectFilePath(), userHolder.getErp());
        return JSONObjectUtil.getSuccessResult("删除成功", null);
    }

    @ExceptionMessageAnnotation(errorMessage = "格式化脚本内容")
    @ResponseBody
    @RequestMapping("format.ajax")
    public JSONObject format(UrmUserHolder userHolder, DataDevScriptFile file) throws Exception {
        if (StringUtils.isBlank(file.getContent())) {
            return JSONObjectUtil.getSuccessResult(file);
        }
        if (file.getType() == null) {
            file.setVersion(null);
            DataDevScriptFile tmp = fileService.getScriptByGitProjectIdAndFilePath(file.getGitProjectId(), file.getGitProjectFilePath());
            file.setType(tmp.getType());
        }
        if (file.getType() == DataDevScriptTypeEnum.SQL.toCode() || file.getType() == DataDevScriptTypeEnum.Python2.toCode() || file.getType() == DataDevScriptTypeEnum.Shell.toCode()) {
            // String content = fileService.formatSqlToSrc(file.getContent());
            ApiResult result = agentCodeFormatInterface.formatCode(file);
            if (result.getCode().equals(ApiResultCode.SUCCESS.toCode())) {
                file.setContent(result.getObj().toString());
            }
        }
        return JSONObjectUtil.getSuccessResult(file);
    }

    @RequestMapping("mergeList.html")
    public String mergeList(UrmUserHolder userHolder, DataDevScriptFile file) throws Exception {
        return "scriptcenter/art/mergeList";
    }

    @ExceptionMessageAnnotation(errorMessage = "PULL Git文件夹内容")
    @RequestMapping("pullDir.ajax")
    @ResponseBody
    public JSONObject pullDir(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectDirPath) throws Exception {
        projectService.verifyUserAuthority(urmUserHolder.getErp(), gitProjectId);
        return JSONObjectUtil.getSuccessResult(fileService.pullDir(gitProjectId, gitProjectDirPath, urmUserHolder.getErp()));
    }

    @ExceptionMessageAnnotation(errorMessage = "PULL Git文件内容")
    @RequestMapping("pullFile.ajax")
    @ResponseBody
    public JSONObject pullFile(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectFilePath) throws Exception {
        projectService.verifyUserAuthority(urmUserHolder.getErp(), gitProjectId);
        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }
        DataDevScriptFile file = fileService.pullFile(gitProjectId, gitProjectFilePath, urmUserHolder.getErp());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isMerge", (StringUtils.isNotBlank(file.getNewGitVersion()) && !file.getNewGitVersion().equals(file.getGitVersion())));
        jsonObject.put("newGitVersion", file.getNewGitVersion());
        jsonObject.put("version", file.getVersion());
        jsonObject.put("nowPullErp", urmUserHolder.getErp());//当前操作人
        return JSONObjectUtil.getSuccessResult("pull成功", jsonObject);
    }

    @ExceptionMessageAnnotation(errorMessage = "PUSH Git文件夹内容")
    @RequestMapping("pushDir.ajax")
    @ResponseBody
    public JSONObject pushDir(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectDirPath, String commitMessage) throws Exception {
        projectService.verifyUserAuthority(urmUserHolder.getErp(), gitProjectId);
        List<DataDevScriptFile> list = fileService.pushDir(gitProjectId, gitProjectDirPath, commitMessage, urmUserHolder.getErp());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isMerge", list != null && list.size() > 0);
        jsonObject.put("mergeList", list);
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

    @ExceptionMessageAnnotation(errorMessage = "PUSH Git文件内容")
    @RequestMapping("pushFile.ajax")
    @ResponseBody
    public JSONObject pushFile(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectFilePath, String commitMessage) throws Exception {
        projectService.verifyUserAuthority(urmUserHolder.getErp(), gitProjectId);
        DataDevScriptFile oldFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        if (oldFile == null) {
            throw new RuntimeException("该脚本不存在");
        }
        DataDevScriptFile file = fileService.pushFile(gitProjectId, gitProjectFilePath, commitMessage, urmUserHolder.getErp());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isMerge", file != null);
        jsonObject.put("mergeVersion", file != null ? file.getNewGitVersion() : "");
        jsonObject.put("gitProjectId", oldFile.getGitProjectId());
        jsonObject.put("gitProjectFilePath", oldFile.getGitProjectFilePath());
        jsonObject.put("name", oldFile.getName());
        jsonObject.put("version", oldFile.getVersion());
        jsonObject.put("erp", urmUserHolder.getErp());
        return JSONObjectUtil.getSuccessResult("push成功", jsonObject);
    }

    @ExceptionMessageAnnotation(errorMessage = "打包Zip文件")
    @RequestMapping("packZip.ajax")
    @ResponseBody
    public JSONObject packZip(UrmUserHolder userHolder, Long gitProjectId, String packPath, String gitProjectFilePath, String data, String comment) throws Exception {
        JSONArray jsonArray = JSONArray.parseArray(data);
        Long time1 = System.currentTimeMillis();
        List<DataDevDependencyDetail> dependencyDetails = devDependencyService.transferJsonArray(jsonArray, gitProjectId);
        List<DataDevDependencyDetail> newVersions = devDependencyService.getNewVersionDetails(dependencyDetails, gitProjectId, gitProjectFilePath);
        HoldDoubleValue<Boolean, DataDevDependency> holdDoubleValue = devDependencyService.saveDetails(newVersions, gitProjectId, gitProjectFilePath, userHolder.getErp());
        if (holdDoubleValue.a) {
            DataDevScriptFile updateParam = new DataDevScriptFile();
            updateParam.setDependencyId(holdDoubleValue.b.getId());
            fileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParam);
        }
        Long time2 = System.currentTimeMillis();

        ZtreeNode ztreeNode = fileService.packZip(holdDoubleValue.b.getId(), packPath, userHolder.getErp(), comment);
        Long time3 = System.currentTimeMillis();
        logger.error("=============================================更新最新版本时间：" + (time2 - time1));
        logger.error("=============================================下载时间：" + (time3 - time2));
        return JSONObjectUtil.getSuccessResult(ztreeNode);
    }

    @RequestMapping("packZip.html")
    public String packZip(UrmUserHolder userHolder, Model model, Long gitProjectId, String gitProjectFilePath) throws Exception {
        gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        return "scriptcenter/home/home_top/packZip";
    }

    @RequestMapping("dependency.html")
    public String dependencyHtml(UrmUserHolder userHolder, Model model, Long gitProjectId, String gitProjectFilePath) throws Exception {
        gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        return "scriptcenter/home/home_top/dependency";
    }

    @ExceptionMessageAnnotation(errorMessage = "设置依赖文件")
    @RequestMapping("depSave.ajax")
    @ResponseBody
    public JSONObject depSave(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String data) throws Exception {
        JSONArray jsonArray = JSONArray.parseArray(data);
        List<DataDevDependencyDetail> list = devDependencyService.transferJsonArray(jsonArray, gitProjectId);
        if (list.size() == 0 || list.size() == 1 && list.get(0).getDependencyGitProjectFilePath().equals(gitProjectFilePath)) {
            //说明没有选择其他依赖，这个时候取消依赖
            devDependencyService.deleteDependency(gitProjectId, gitProjectFilePath);
            return JSONObjectUtil.getSuccessResult("设置依赖成功", false);
        } else {
            try {
                List<DataDevDependencyDetail> newList = devDependencyService.getNewVersionDetails(list, gitProjectId, gitProjectFilePath);
                HoldDoubleValue<Boolean, DataDevDependency> holdDoubleValue = devDependencyService.saveDetails(newList, gitProjectId, gitProjectFilePath, userHolder.getErp());
                if (holdDoubleValue.a) {
                    DataDevScriptFile updateParam = new DataDevScriptFile();
                    updateParam.setDependencyId(holdDoubleValue.b.getId());
                    fileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParam);
                }
                return JSONObjectUtil.getSuccessResult("设置依赖成功", true);
            } catch (DependencyDetailNotFoundException e) {
                return JSONObjectUtil.getFailResult(e.getMessage(), null);
            }

        }
    }

//    @RequestMapping("getDepDetails.ajax")
//    @ResponseBody
//    public JSONObject getDepDetails(Long gitProjectId, String gitProjectFilePath) throws Exception {
//        List<DataDevDependencyDetail> list = devDependencyService.getDetails(gitProjectId, gitProjectFilePath, null);
//        return JSONObjectUtil.getSuccessResult(list);
//    }

    /**
     * @param userHolder
     * @param gitProjectId
     * @param gitProjectFilePath 打包之后的zip文件
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping("updateNewVersion.ajax")
    @ResponseBody
    public JSONObject updateNewVersion(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String data) throws Exception {
        JSONArray array = JSONArray.parseArray(data);
        DataDevScriptFile packZip = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        DataDevDependency devDependency = devDependencyService.getById(packZip.getRelationDependencyId());
        List<DataDevDependencyDetail> updateList = devDependencyService.transferJsonArray(array, gitProjectId);
        List<DataDevDependencyDetail> newVersionList = devDependencyService.updateNewVersion(updateList, devDependency);
        HoldDoubleValue<Boolean, DataDevDependency> holdDoubleValue = devDependencyService.saveDetails(newVersionList, gitProjectId, devDependency.getGitProjectFilePath(), userHolder.getErp());
        JSONObject result = new JSONObject();
        logger.error("保存依赖是否成功" + holdDoubleValue.a);
        if (holdDoubleValue.a) {
            ZtreeNode ztreeNode = fileService.packZip(holdDoubleValue.b.getId(), packZip.getGitProjectFilePath(), userHolder.getErp(), null);
            result.put("version", ztreeNode.getVersion());
        }
        result.put("relationDependencyId", holdDoubleValue.b.getId());
        return JSONObjectUtil.getSuccessResult(result);
    }

    @RequestMapping("gitHis.html")
    public String gitHisHtml(UrmUserHolder userHolder, Model model, Long gitProjectId,
                             String gitProjectFilePath,
                             @RequestParam(value = "isDir", defaultValue = "false") boolean isDir) throws Exception {
        gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        model.addAttribute("isDir", isDir);

        return "scriptcenter/home/gitHis/gitHis";
    }

    /**
     * 获取提交历史列表
     *
     * @param userHolder
     * @param page
     * @param rows
     * @param dataDevGitHisDetail
     * @return
     * @throws Exception
     */
    @RequestMapping("gitHis.ajax")
    @ResponseBody
    public net.sf.json.JSONObject gitHisAjax(UrmUserHolder userHolder,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "rows", defaultValue = "10") int rows,
                                             DataDevGitHisDetail dataDevGitHisDetail) throws Exception {

        PageResultDTO pageResultDTO = new PageResultDTO();
        Pageable pageable = new PageRequest(page - 1, rows);
        try {
            pageResultDTO = dataDevGitHisDetailService.queryDataDevGitHisDetail(dataDevGitHisDetail, pageable);
        } catch (Exception e) {
            pageResultDTO.setSuccess(false);
            pageResultDTO.setRecords(0L);
            pageResultDTO.setMessage("获取列表失败！" + e.getMessage());
        }
        pageResultDTO.setPage(page);
        pageResultDTO.setLimit(rows);
        pageResultDTO.setMessage("获取成功");
        return AjaxUtil.gridJson(pageResultDTO);
    }


    /**
     * 比较commit的差异页面
     *
     * @param userHolder
     * @param model
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("gitHisDiff.html")
    public String gitHisDetail(UrmUserHolder userHolder, Model model, Long id) throws Exception {
        try {
            DataDevGitHisDetail dataDevGitHisDetail = dataDevGitHisDetailService.queryDataDevGitHisDetailById(id);
            Long gitProjectId = dataDevGitHisDetail.getGitProjectId();
            String gitProjectFilePath = dataDevGitHisDetail.getGitProjectFilePath();


            String commitId = dataDevGitHisDetail.getCommitId();
            String beforeCommitId = StringUtils.isNotBlank(dataDevGitHisDetail.getBeforeCommitIds()) ? dataDevGitHisDetail.getBeforeCommitIds().split(",")[0] : null;

            DataDevScriptFile dataDevScriptFile = fileService.getScriptByGitProjectIdAndFilePath(dataDevGitHisDetail.getGitProjectId(), dataDevGitHisDetail.getGitProjectFilePath());
            boolean canEdit = DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType());
            model.addAttribute("dataDevGitHisDetail", dataDevGitHisDetail);
            model.addAttribute("canEdit", canEdit);
            model.addAttribute("beforeCommitId", beforeCommitId);
            model.addAttribute("currentCommitId", commitId);

            //修改 显示两边内容差异
            int showSideCount = dataDevGitHisDetail.getCommitType().equals(DataDevHisTypeEnum.MOIDFY.tocode()) ? 2 : 1;
            model.addAttribute("showSideCount", showSideCount);

            if (canEdit) {
                //可编辑文件
                if (showSideCount == 1) {
                    if (dataDevGitHisDetail.getCommitType().equals(DataDevHisTypeEnum.ADD.tocode())) {
                        model.addAttribute("showContent", getGitFileContent(gitProjectId, gitProjectFilePath, commitId).b);
                        model.addAttribute("showCommitId", commitId);
                    }
                    if (dataDevGitHisDetail.getCommitType().equals(DataDevHisTypeEnum.DELETE.tocode())) {
                        model.addAttribute("showContent", getGitFileContent(gitProjectId, gitProjectFilePath, beforeCommitId).b);
                        model.addAttribute("showCommitId", beforeCommitId);
                    }
                } else {
                    model.addAttribute("beforeContent", getGitFileContent(gitProjectId, gitProjectFilePath, beforeCommitId).b);
                    model.addAttribute("currentContent", getGitFileContent(gitProjectId, gitProjectFilePath, commitId).b);
                }
            } else {
                //不可编辑文件 下载
                model.addAttribute("dataDevScriptFile", dataDevScriptFile);
                if (showSideCount == 1) {
                    if (dataDevGitHisDetail.getCommitType().equals(DataDevHisTypeEnum.ADD.tocode())) {
                        model.addAttribute("showCommitId", commitId);
                    }
                    if (dataDevGitHisDetail.getCommitType().equals(DataDevHisTypeEnum.DELETE.tocode())) {
                        model.addAttribute("showCommitId", beforeCommitId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("gitHisDetail", e);
        }
        return "scriptcenter/home/gitHis/gitHisDiff";
    }

    @RequestMapping("downGitScript.ajax")
    @ResponseBody
    public void gitHisCommitTreeHtml(UrmUserHolder userHolder, Long gitHisDetailId, String commitId, HttpServletResponse response) throws Exception {

        DataDevGitHisDetail dataDevGitHisDetail = dataDevGitHisDetailService.queryDataDevGitHisDetailById(gitHisDetailId);
        commitId = StringUtils.isNotBlank(commitId) ? commitId.substring(0, 8) : dataDevGitHisDetail.getCommitId().substring(0, 8);
        DataDevScriptFile dataDevScriptFile = fileService.getScriptByGitProjectIdAndFilePath(dataDevGitHisDetail.getGitProjectId(), dataDevGitHisDetail.getGitProjectFilePath());
        String fileName = dataDevScriptFile.getName();
        if (fileName.lastIndexOf(".") != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + commitId + fileName.substring(fileName.lastIndexOf("."));
        } else {
            fileName = fileName + "_" + commitId;
        }
        response.setStatus(org.apache.http.HttpStatus.SC_OK);
        fileName = StringUtils.isBlank(fileName) ? "script" : fileName;
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setFilePath(dataDevScriptFile.getGitProjectFilePath());
        jdGitFiles.setGitProjectId(dataDevScriptFile.getGitProjectId());
        jdGitFiles.setLastCommitId(StringUtils.isNotBlank(commitId) ? commitId : dataDevGitHisDetail.getCommitId());
        jdGitFiles.setBinary(true);
        jdGitFiles.loadFileContent();

        response.setContentLength(jdGitFiles.getBytes() != null ? jdGitFiles.getBytes().length : 0);
        response.getOutputStream().write(jdGitFiles.getBytes());
        response.getOutputStream().close();

    }


    /**
     * commit 树
     *
     * @param userHolder
     * @param model
     * @param
     * @return
     * @throws Exception
     */

    @RequestMapping("gitHisCommitTree.html")
    public String gitHisCommitTreeHtml(UrmUserHolder userHolder, Model model, Long id, String gitProjectFilePath) throws Exception {

        DataDevGitHisDetail dataDevGitHisDetail = dataDevGitHisDetailService.queryDataDevGitHisDetailById(id);
        DataDevGitProject dataDevGitProject = projectService.getGitProjectBy(dataDevGitHisDetail.getGitProjectId());

        model.addAttribute("dataDevGitProject", dataDevGitProject);
        model.addAttribute("dataDevGitHisDetail", dataDevGitHisDetail);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);

        return "/scriptcenter/home/gitHis/gitHisCommitTree";
    }

    @RequestMapping("gitHisCommitTree.ajax")
    @ResponseBody
    public JSONObject gitHisCommitTreeAjax(UrmUserHolder userHolder, String commit, Long gitProjectId, String gitProjectFilePath) throws Exception {
        List<DataDevGitHisDetail> dataDevGitHisDetailList = dataDevGitHisDetailService.queryDataDevGitHisDetailByCommitId(gitProjectId, commit);

        if (dataDevGitHisDetailList != null && StringUtils.isNotBlank(gitProjectFilePath)) {
            Iterator<DataDevGitHisDetail> it = dataDevGitHisDetailList.iterator();
            while (it.hasNext()) {
                DataDevGitHisDetail dataDevGitHisDetail = it.next();
                if (!dataDevGitHisDetail.getGitProjectFilePath().startsWith(gitProjectFilePath)) {
                    it.remove();
                }
            }
        }

        List<ZtreeNode> ztreeNodeList = covertGitHisDetail2ZtreeNode(dataDevGitHisDetailList);
        return JSONObjectUtil.getSuccessResult(ztreeNodeList);
    }

    /**
     * covert ztreeNode
     *
     * @param dataDevGitHisDetailList
     * @return
     */
    private List<ZtreeNode> covertGitHisDetail2ZtreeNode(List<DataDevGitHisDetail> dataDevGitHisDetailList) {
        Map<String, Long> paths = new HashMap<String, Long>();
        Map<String, DataDevGitHisDetail> detailMap = new HashMap<String, DataDevGitHisDetail>();
        for (DataDevGitHisDetail temp : dataDevGitHisDetailList) {
            String path = temp.getGitProjectFilePath();
            detailMap.put(path, temp);
            String splitPaths[] = path.split("\\/");
            String parent = "";
            for (String split : splitPaths) {
                paths.put(parent + split, 0L);
                parent = StringUtils.isNotBlank(parent) ? parent + split + "/" : split + "/";
            }
            paths.put(path, temp.getId());
        }
        List<ZtreeNode> nodes = new ArrayList<ZtreeNode>();

        for (String path : paths.keySet()) {
            String parentPath = path.lastIndexOf("/") != -1 ? path.substring(0, path.lastIndexOf("/")) : "";
            ZtreeNode ztreeDirNode = new ZtreeNode();
            ztreeDirNode.setParentPath(parentPath);
            ztreeDirNode.setParChl(0);
            ztreeDirNode.setName(path);
            ztreeDirNode.setPath(path);
            ztreeDirNode.setId(paths.get(path));
            if (path.indexOf(".") != -1) {
                ztreeDirNode.setType(DataDevScriptTypeEnum.getFileNameScriptType(path).toCode());
            } else {
                ztreeDirNode.setType(-1);
            }

            ztreeDirNode.setTargetDir(false);
            DataDevGitHisDetail mapDetail = detailMap.get(path);
            if (mapDetail != null) {
                String gitStatus = mapDetail.getCommitType().equals(DataDevHisTypeEnum.ADD.tocode()) ?
                        DataDevScriptGitStatusEnum.ADD.toCode() :
                        mapDetail.getCommitType().equals(DataDevHisTypeEnum.MOIDFY.tocode()) ?
                                DataDevScriptGitStatusEnum.MOD.toCode() : DataDevScriptGitStatusEnum.DEL.toCode();
                ztreeDirNode.setGitStatus(gitStatus);
            }
            nodes.add(ztreeDirNode);
        }
        return nodes;
    }

    @RequestMapping("countGitHisCommit.ajax")
    @ResponseBody
    public JSONObject countGitHisCommitAjax(UrmUserHolder userHolder, String commitId) throws Exception {
        Long countCommit = dataDevGitHisDetailService.countGitHisDetailByCommitId(commitId);
        return JSONObjectUtil.getSuccessResult(countCommit);
    }

    /**
     * 获取可编辑文件内容
     *
     * @param userHolder
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param commitId
     * @return
     * @throws Exception
     */
    @RequestMapping("gitFileContent.ajax")
    @ResponseBody
    public JSONObject gitFileContentAjax(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String commitId) throws Exception {
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setGitProjectId(gitProjectId);
        jdGitFiles.setLastCommitId(commitId);
        jdGitFiles.setFilePath(gitProjectFilePath);
        jdGitFiles.loadFileContent();
        return JSONObjectUtil.getSuccessResult(jdGitFiles.getContent());
    }

    private HoldDoubleValue<Integer, String> getGitFileContent(Long gitProjectId, String gitProjectFilePath, String commitId) throws Exception {
        HoldDoubleValue<Integer, String> result = null;
        try {
            if (StringUtils.isNotBlank(commitId)) {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setGitProjectId(gitProjectId);
                jdGitFiles.setLastCommitId(commitId);
                jdGitFiles.setFilePath(gitProjectFilePath);
                jdGitFiles.loadFileContent();
                result = new HoldDoubleValue<Integer, String>(1, jdGitFiles.getContent());
                return result;
            }
        } catch (GitFileNotFoundException e) {
            return new HoldDoubleValue<Integer, String>(0, "");
        } catch (Exception e) {
            return new HoldDoubleValue<Integer, String>(0, "");
        }
        return result;
    }

    @RequestMapping("initScriptType.ajax")
    @ResponseBody
    public JSONObject initScriptType(UrmUserHolder userHolder, Integer limit) throws Exception {
        if (userHolder.getErp().equals("zhanglei847") || userHolder.getErp().equals("zhangrui156") || userHolder.getErp().equals("bdp_ide") || userHolder.getErp().equals("wangxiaoli76")) {
            Integer size = fileService.initScriptType(limit);
            return JSONObjectUtil.getSuccessResult(size);
        } else {
            return JSONObjectUtil.getFailResult("无权限", "");
        }
    }

    @RequestMapping("initScriptHisType.ajax")
    @ResponseBody
    public JSONObject initScriptHisType(UrmUserHolder userHolder) throws Exception {
        if (userHolder.getErp().equals("zhanglei847") || userHolder.getErp().equals("zhangrui156") || userHolder.getErp().equals("bdp_ide") || userHolder.getErp().equals("wangxiaoli76")) {
            fileService.initScriptHisType();
            return JSONObjectUtil.getSuccessResult("成功");
        } else {
            return JSONObjectUtil.getFailResult("无权限", "");
        }
    }

}
