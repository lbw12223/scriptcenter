package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.amp.sdk.annotation.AuthChecker;
import com.jd.bdp.datadev.component.HbaseScript;
import com.jd.bdp.datadev.component.ImportScriptManager;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.dao.DataDevScriptFileHisDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevProjectTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.jdgit.*;
import com.jd.bdp.datadev.model.Script;
import com.jd.bdp.datadev.service.DataDevGitProjectMemberService;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptDirService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.web.exception.ParamsException;
import com.jd.bdp.datadev.web.exception.ScriptException;
import com.jd.bdp.domain.authorityCenter.MarketInfoDto;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;


@Controller
@RequestMapping("/scriptcenter/api/")
public class ScriptApiController {

    private static final Logger logger = Logger.getLogger(ScriptApiController.class);
    @Autowired
    private DataDevScriptFileService scriptFileService;
    @Autowired
    private DataDevScriptDirService dirService;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;
    @Autowired
    private HbaseScript hbaseScript;

    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao;

    @Autowired
    private DataDevGitProjectMemberService dataDevGitProjectMemberService;

    @Autowired
    private DataDevScriptDirService dataDevScriptDirService;

    @Autowired
    private DataDevScriptFileService dataDevScriptFileService;

    /**
     * 根据ProjectType获取coding，本地，git项目
     * <p>
     * projectType:coding = 1
     * projectType:git = 2
     * projectType:local = 3
     *
     * @param erp
     * @param projectType
     * @return
     * @throws Exception
     */
    @RequestMapping("/getProjectByErp")
    @ResponseBody
    @AuthChecker
    public JSONObject getProjectByErp(String erp, Integer projectType, String keyword) throws Exception {
        JSONArray jsonArray = new JSONArray();
        try {
            if(StringUtils.isBlank(erp)){
                throw new ParamsException("Erp，不能为NULL");
            }
            if(projectType == null && (projectType < 0  || projectType > 3)){
                throw new ParamsException("projectType取值范围[1,2,3]");
            }

            List<DataDevGitProject> erpProjectBySearch = dataDevGitProjectService.getErpProjectBySearch(erp, keyword, projectType);
            if (erpProjectBySearch != null) {

                for (DataDevGitProject temp : erpProjectBySearch) {
                    JSONObject project = new JSONObject();
                    project.put("projectId", temp.getGitProjectId());
                    project.put("projectName", temp.getGitProjectName());
                    project.put("projectType", projectType);
                    jsonArray.add(project);
                }
            }
            return JSONObjectUtil.getSuccessList(jsonArray);
        } catch (Exception e) {
            logger.error("getProjectByErp", e);
            return JSONObjectUtil.getFailResult(e.getMessage());
        }
    }


    /**
     *
     * @param projectId
     * @param dirId        不传为0时，查询根目录下的dir和file，不为0时，查询p_id为dirId的dir 和 dir_id为dirId的file
     * @return
     * @throws Exception
     */
    @RequestMapping("/getProjectTree")
    @ResponseBody
    @AuthChecker
    public JSONObject getProjectTree(Long projectId, Long dirId) throws Exception {
        JSONObject result = new JSONObject();
        logger.info(String.format("projectId=%s, dirId=%s", projectId, dirId));
        try {
            // 参数校验
            if (projectId == null || projectId <= 0) {
                throw new ParamsException("projectId必填，且必须大于0");
            }
            if (dirId != null && dirId < 0) {
                throw new ParamsException("dirId非必填，但不能小于0");
            }
            if (dirId == null) {
                dirId = 0L;
            }

            List<DataDevScriptDir> dataDevScriptDirs = dataDevScriptDirService.getDataDevScriptDirByPid(projectId, dirId);
            List<DataDevScriptFile> dataDevScriptFiles = dataDevScriptFileService.getScriptsByGitProjectIdAndDirId(projectId, dirId);
            JSONArray jsonArray = new JSONArray();
            for (DataDevScriptDir dataDevScriptDir : dataDevScriptDirs) {
                JSONObject dir = new JSONObject();
                dir.put("id", dataDevScriptDir.getId());
                dir.put("type", "dir");
                dir.put("name", dataDevScriptDir.getName());
                dir.put("fullPath", dataDevScriptDir.getGitProjectDirPath());
                jsonArray.add(dir);
            }
            // file去重
            for (DataDevScriptFile dataDevScriptFile : dataDevScriptFiles) {
                JSONObject file = new JSONObject();
                file.put("id", dataDevScriptFile.getId());
                file.put("type", "file");
                file.put("name", dataDevScriptFile.getName());
                file.put("fullPath", dataDevScriptFile.getGitProjectFilePath());
                jsonArray.add(file);
            }
            return JSONObjectUtil.getSuccessList(jsonArray);
        } catch (Exception e) {
            logger.error("getProjectTree failed", e);
            return JSONObjectUtil.getFailResult(e.getMessage());
        }
    }

    @RequestMapping("/loadScript")
    @ResponseBody
    @AuthChecker
    public void loadScript(HttpServletResponse response, Long scriptId, String version) throws Exception {
        try {
            // 参数校验
            if (scriptId == null || scriptId <= 0) {
                throw new ParamsException("scriptId必填，且必须大于0");
            }

            DataDevScriptFile data = scriptFileService.findById(scriptId);
            if (data == null) {
                throw new RuntimeException("id为" + scriptId + "的脚本不存在");
            }
            if (StringUtils.isBlank(version)) {
                version = data.getVersion();
            }

            byte[] res = null;
            //正常单文件下载
            String fileName = null;
            if (StringUtils.isNotBlank(data.getGitProjectFilePath())) {
                String path = URLDecoder.decode(data.getGitProjectFilePath(), "utf-8");
                DataDevScriptFile scriptFile = scriptFileService.getScriptByGitProjectIdAndFilePath(data.getGitProjectId(), path, version);
                if (scriptFile == null) {
                    throw new RuntimeException("项目id为" + data.getGitProjectId() + "，脚本路径为" + data.getGitProjectFilePath() + (StringUtils.isNotBlank(data.getVersion()) ? ("，版本号为" + data.getVersion()) : "") + "的脚本不存在");
                }
                res = scriptFileService.getScriptBytes(scriptFile.getGitProjectId(), scriptFile.getGitProjectFilePath(), data.getVersion(), urmUtil.getBdpManager(), true);
                fileName = scriptFile.getName();
            }
            response.setStatus(org.apache.http.HttpStatus.SC_OK);
            fileName = StringUtils.isBlank(fileName) ? "script" : fileName;
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());
            response.setContentLength(res != null ? res.length : 0);
            response.getOutputStream().write(res);
            response.getOutputStream().close();
            logger.error("============================下载文件成功，文件：" + fileName + "，大小：" + (res != null ? res.length : 0));
        } catch (Exception e) {
            logger.error("loadScript failed:", e);
        }
    }


    @RequestMapping("/diffScript")
    @ResponseBody
    @AuthChecker
    public void diffScript(Long scriptId, Integer version, Integer diffVersion) throws Exception {
        JSONObject result = new JSONObject();
        try {
        } catch (Exception e) {
            logger.error("uploadScript==============================================================result", e);
            result.put("message", e.getMessage());
            result.put("success", false);
            result.put("code", 1);
        }

    }


    @RequestMapping("/uploadScript")
    @ResponseBody
    @AuthChecker
    public JSONObject uploadScript(String userToken, @RequestParam("file") MultipartFile file, Script data) throws Exception {
        logger.error("uploadScript==============================================================file" + JSONObject.toJSONString(data));
        JSONObject result = new JSONObject();
        try {
            if (file != null && file.getBytes() != null && file.getSize() > 0) {
            } else {
                throw new ScriptException("上传文件为空");
            }
            Long gitProjectId = data.getGitProjectId();

            if (StringUtils.isNotBlank(data.getGitProjectPath()) && (gitProjectId == null || gitProjectId <= 0L)) {
                gitProjectId = dataDevGitProjectService.getGitProjectBy(data.getGitProjectPath()).getGitProjectId();
                data.setGitProjectId(gitProjectId);
            }

            if (gitProjectId == null || gitProjectId <= 0L) {
                gitProjectId = scriptFileService.getDefaultProjectId();
                data.setGitProjectId(gitProjectId);
            }
            DataDevScriptFile resObj = new DataDevScriptFile();
            String erp = urmUtil.getErpByUserToken(userToken);
            dataDevGitProjectService.verifyUserAuthority(erp, gitProjectId);
            scriptFileService.checkAndTransferScriptParam(data);
            String path = "";
            if (StringUtils.isNotBlank(data.getGitProjectFilePath())) {
                path = data.getGitProjectFilePath();
            } else {
                String fullName = DataDevScriptTypeEnum.getFullName(data.getName(), data.getType());
                path = Base64Util.getFullPath(data.getGitProjectDirPath(), fullName);
            }
            if (data.getId() != null || StringUtils.isNotBlank(data.getGitProjectFilePath())) {
                HoldDoubleValue<Boolean, JDGitFiles> holdDoubleValue = scriptFileService.tryUpdateFile(gitProjectId, path, erp, file.getBytes(), null, null, true);
                resObj.setVersion(holdDoubleValue.b.getVersion());
                resObj.setGitProjectFilePath(data.getGitProjectFilePath());
                resObj.setId(data.getId());
                resObj.setGitProjectId(data.getGitProjectId());
            } else {
                ZtreeNode ztreeNode = scriptFileService.createNewFile(gitProjectId, path, data.getType(), erp, 0, file.getBytes(), data.getDescription(), data.getStartShellPath());
                while (ztreeNode.getChildren() != null && ztreeNode.getChildren().size() > 0) {
                    ztreeNode = ztreeNode.getChildren().get(0);
                }
                resObj.setVersion(ztreeNode.getVersion());
                resObj.setGitProjectFilePath(ztreeNode.getPath());
                resObj.setId(ztreeNode.getId());
                resObj.setGitProjectId(ztreeNode.getGitProjectId());
            }
            resObj.setGitProjectId(gitProjectId);
            result.put("message", "上传成功");
            result.put("success", true);
            result.put("code", 0);
            result.put("obj", resObj);
            logger.error("uploadScript==============================================================result" + JSONObject.toJSONString(result));
            return result;
        } catch (Exception e) {
            logger.error("uploadScript==============================================================result", e);
            result.put("message", e.getMessage());
            result.put("success", false);
            result.put("code", 1);
            return result;
        }
    }

    /**
     * 客户端调用
     *
     * @param userToken
     * @param Data
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/downloadScript")
    @ResponseBody
    @AuthChecker
    public JSONObject downloadScript(String userToken, DataDevScriptFile Data, HttpServletResponse response) throws Exception {
        String erp = urmUtil.getErpByUserToken(userToken);
        UrmUserHolder urmUserHolder = new UrmUserHolder();
        urmUserHolder.setErp(erp);
        return downloadScriptNoAuth(urmUserHolder, Data, response, 0, false);
    }

    /**
     * 分四种情况
     * 1：正常单文件下载  downType:0 gitProjectFilePath 不为空
     * 2：正常多文件打包下载，先打包再下载  downType:0 gitProjectDirPath 不为空
     * 3：下载运行时已经打包好的zip文件 downType:1 runDetailId不为空
     * 1：下载上线时已经打包好的zip文件 downType:1 publishId 不为空
     *
     * @param holder
     * @param data
     * @param response
     * @param downType
     * @return
     * @throws Exception
     */
    @RequestMapping("/downloadScriptNoAuth.ajax")
    @ResponseBody
    public JSONObject downloadScriptNoAuth(UrmUserHolder holder, DataDevScriptFile data, HttpServletResponse response
            , @RequestParam(value = "downType", defaultValue = "0") Integer downType,
                                           @RequestParam(value = "isCheckErpRight", defaultValue = "false") Boolean isCheckErpRight) throws Exception {
        logger.error("============================================================downloadScriptNoAuth:" + JSON.toJSONString(data));

        if (holder == null || holder.getErp() == null) {
            holder = new UrmUserHolder();
            holder.setErp(urmUtil.getBdpManager());
        }
        try {
            DataDevScriptFile scriptFile = null;
            String fileName = null;
            if (data.getId() != null) {
                scriptFile = scriptFileService.findById(data.getId());
                if (scriptFile == null) {
                    throw new RuntimeException("id为" + data.getId() + "的脚本不存在");
                }
                data.setGitProjectId(scriptFile.getGitProjectId());
                data.setGitProjectFilePath(scriptFile.getGitProjectFilePath());
            }
            if ((StringUtils.isBlank(data.getGitProjectDirPath()) && StringUtils.isBlank(data.getGitProjectFilePath())) || data.getGitProjectId() == null) {
                throw new RuntimeException("脚本项目id跟脚本路径或者脚本目录不能为空");
            }
            if (isCheckErpRight) {
                dataDevGitProjectService.verifyUserAuthority(holder.getErp(), data.getGitProjectId());
            }
            byte[] res = null;
            //正常单文件下载
            if (StringUtils.isNotBlank(data.getGitProjectFilePath())) {
                String path = URLDecoder.decode(data.getGitProjectFilePath(), "utf-8");
                scriptFile = scriptFileService.getScriptByGitProjectIdAndFilePath(data.getGitProjectId(), path, data.getVersion());
                if (scriptFile == null) {
                    throw new RuntimeException("项目id为" + data.getGitProjectId() + "，脚本路径为" + data.getGitProjectFilePath() + (StringUtils.isNotBlank(data.getVersion()) ? ("，版本号为" + data.getVersion()) : "") + "的脚本不存在");
                }
                res = scriptFileService.getScriptBytes(scriptFile.getGitProjectId(), scriptFile.getGitProjectFilePath(), data.getVersion(), holder.getErp(), true);
                fileName = scriptFile.getName();
            }
            response.setStatus(org.apache.http.HttpStatus.SC_OK);
            fileName = StringUtils.isBlank(fileName) ? "script" : fileName;
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());
            response.setContentLength(res != null ? res.length : 0);
            response.getOutputStream().write(res);
            response.getOutputStream().close();
            logger.error("============================下载文件成功，文件：" + fileName + "，大小：" + (res != null ? res.length : 0));
            return null;
        } catch (Exception e) {
            logger.error("下载异常：" + e.getMessage());
            throw new ScriptException(e.getMessage());
        }
    }

    /**
     * {"code":200,"data":[{"systemName":"JSM","systemDesc":"权限管理系统","itemId":"123","itemName":"集市10000个库表的负责人","remark":"由于后续JSM上
     *
     * @param erp
     * @param optErp
     * @param timestamp
     * @param signature
     * @param appId
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "queryHandOverItem")
    @ResponseBody
    public JSONObject queryHandOverItem(String erp, String optErp, Long timestamp, String signature, String appId, String token) throws Exception {
        JSONObject result = new JSONObject();
        try {


            JSONArray data = new JSONArray();

            if (!("handover.bdp.jd.com").equalsIgnoreCase(appId) ||
                    !("YG4GRNESGW2ZHVJFNXEXPIG3JRE5RJUCJCKRQNA").equalsIgnoreCase(token)) {
                throw new RuntimeException("appId, token 不合法");
            }
            Integer countScript = dataDevScriptFileDao.countErpScriptFile(erp);
            if (countScript > 0) {
                JSONObject scriptFile = new JSONObject();
                scriptFile.put("systemName", "datadev");
                scriptFile.put("systemDesc", "大数据平台-开发平台");
                scriptFile.put("itemId", "datadev-script");
                scriptFile.put("itemName", countScript + "个脚本");
                data.add(scriptFile);
            }


            List<DataDevGitProject> gitProjectList = dataDevGitProjectService.getErpProjectBySearch(erp, "", -1);

            if (gitProjectList != null && gitProjectList.size() > 0) {
                JSONObject gitProject = new JSONObject();
                gitProject.put("systemName", "datadev");
                gitProject.put("systemDesc", "大数据平台-开发平台");
                gitProject.put("itemId", "datadev-gitProject");
                gitProject.put("itemName", (gitProjectList != null ? gitProjectList.size() : 0) + "个Git或Coding项目");
                data.add(gitProject);
            }


            result.put("data", data);
            result.put("code", 200);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", e.getMessage());
            logger.error("queryHandOverItem", e);
        }
        return result;
    }

    /**
     * 交接权限
     *
     * @param sourceErp
     * @param targetErp
     * @param timestamp
     * @param signature
     * @param appId
     * @param token
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "handOverItem", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject handOverItem(String sourceErp, String targetErp, Long timestamp, String signature, String appId, String token, String data) throws Exception {
        JSONObject result = new JSONObject();
        try {
            if (!("handover.bdp.jd.com").equalsIgnoreCase(appId) ||
                    !("YG4GRNESGW2ZHVJFNXEXPIG3JRE5RJUCJCKRQNA").equalsIgnoreCase(token)) {
                throw new RuntimeException("appId, token 不合法");
            }
            handOverItem(sourceErp, targetErp, data);


            result.put("data", "大数据平台-开发平台(交接成功)");
            result.put("code", 200);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", e.getMessage());
            logger.error("queryHandOverItem", e);
        }
        return result;
    }

    @RequestMapping(value = "handOverItemRaw", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject handOverItemRaw(@RequestBody String data) throws Exception {
        JSONObject result = new JSONObject();
        try {
            JSONObject map = JSONObject.parseObject(data);
            String appId = map.get("appId").toString();
            String token = map.get("token").toString();
            String sourceErp = map.get("sourceErp").toString();
            String targetErp = map.get("targetErp").toString();
            String datas = map.get("data").toString();

            if (!("handover.bdp.jd.com").equalsIgnoreCase(appId) ||
                    !("YG4GRNESGW2ZHVJFNXEXPIG3JRE5RJUCJCKRQNA").equalsIgnoreCase(token)) {
                throw new RuntimeException("appId, token 不合法");
            }
            handOverItem(sourceErp, targetErp, datas);


            result.put("data", "大数据平台-开发平台(交接成功)");
            result.put("code", 200);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", e.getMessage());
            logger.error("queryHandOverItem", e);
        }
        return result;
    }

    private void handOverItem(String sourceErp, String targetErp, String datas) {
        boolean doScriptUpdate = false;
        boolean doGitProjectAdd = false;
        if (StringUtils.isEmpty(datas)) {
            doScriptUpdate = true;
            doGitProjectAdd = true;
        } else {
            doScriptUpdate = datas.indexOf("datadev-script") != -1;
            doGitProjectAdd = datas.indexOf("datadev-gitProject") != -1;

        }
        if (doScriptUpdate) {
            dataDevScriptFileDao.updateErpScriptFile(sourceErp, targetErp);
        }
        if (doGitProjectAdd) {
            //给targetErp添加这个项目的权限
            List<DataDevGitProject> gitProjectList = dataDevGitProjectService.getErpProjectBySearch(sourceErp, "", -1);

            for (DataDevGitProject gitProject : gitProjectList) {
                try {
                    List<JDGitMembers> jdGitMembersList = new ArrayList<JDGitMembers>();
                    List<DataDevGitProjectMember> dataDevGitProjectMemberList = new ArrayList<DataDevGitProjectMember>();

                    if (StringUtils.isNotEmpty(targetErp)) {
                        Long gitProjectId = gitProject.getGitProjectId();
                        for (String erp : targetErp.split(",")) {
                            JDGitUser jdGitUser = new JDGitUser();
                            jdGitUser.setName(erp);
                            jdGitUser.setGitProjectId(gitProjectId);
                            JDGitUser searchJdUser = jdGitUser.searchUser();
                            if (searchJdUser == null) {
                                throw new RuntimeException("用户[" + erp + "在git未激活，请在<a href='http://git.jd.com'target='blank'>git.jd.com</a>登录一次进行激活！");
                            }
                            JDGitMembers member = new JDGitMembers();
                            member.setAccessLevel(ImportScriptManager.DEVELOPER);
                            member.setGitUserId(searchJdUser.getId());
                            member.setName(erp);
                            member.setUserName(erp);
                            jdGitMembersList.add(member);

                            DataDevGitProjectMember temp = new DataDevGitProjectMember();
                            temp.setAccessLevel(ImportScriptManager.DEVELOPER);
                            temp.setGitMemberId(searchJdUser.getId());
                            temp.setGitMemberName(erp);
                            temp.setGitMemberUserName(erp);
                            temp.setGitProjectId(gitProjectId);

                            dataDevGitProjectMemberList.add(temp);
                        }
                        JDGitProjects jdGitProjects = new JDGitProjects();
                        jdGitProjects.setGitProjectId(gitProjectId);
                        jdGitProjects.setJdGitMembers(jdGitMembersList);
                        jdGitProjects.addProjectMember();
                        dataDevGitProjectMemberService.insert(dataDevGitProjectMemberList);

                    }
                } catch (Exception e) {
                    logger.error("addProjectMember", e);
                }
            }
        }


    }

}
