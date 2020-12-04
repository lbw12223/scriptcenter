package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.ImportScriptManager;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevScriptGitStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.jdgit.JDGitFiles;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.util.MD5Util;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/scriptcenter/script/")
public class ScriptManageController {
    private static final Logger logger = Logger.getLogger(ScriptManageController.class);
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Autowired
    private DataDevScriptDirService dirService;
    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;
    @Autowired
    private ImportScriptManager importScriptManager;
    @Autowired
    private DataDevDependencyService devDependencyService;
    @Autowired
    private DataDevScriptTemplateService templateService;

    @Value("${datadev.env}")
    private String env;

    @ExceptionMessageAnnotation(errorMessage = "获取当前用户可访问Git Project")
    @RequestMapping("/getAppByErp.ajax")
    @ResponseBody
    public JSONObject getApps(UrmUserHolder userHolder, String keyword) throws Exception {
        String erp = userHolder.getErp();
        if(StringUtils.isNotBlank(keyword)){
            keyword=keyword.replaceAll("\\_","\\\\_");
            keyword=keyword.replaceAll("\\%","\\\\%");
        }
        List<DataDevApplication> list = new ArrayList<DataDevApplication>();//appGroupUtil.getAppsByErp(erp);

        List<DataDevGitProject> dataDevGitProjectList = dataDevGitProjectService.getErpProjectBySearch(erp, keyword);
        if (dataDevGitProjectList != null && dataDevGitProjectList.size() > 0) {
            for (DataDevGitProject temp : dataDevGitProjectList) {
                DataDevApplication dataDevApplication = new DataDevApplication();
                dataDevApplication.setAppgroupId(Integer.parseInt(temp.getGitProjectId() + ""));
                dataDevApplication.setAppgroupName(temp.getGitProjectPath());
                list.add(dataDevApplication);
            }
        }
        return JSONObjectUtil.getSuccessResult(list);
    }

    /**
     * 得到目录下面的脚本和子目录
     * dirService.getDirFiles
     *
     * @param range          0全部文件  1只获取目录
     * @param
     * @param path
     * @param gitProjectId
     * @param gitProjectId   项目id
     * @param openDirs       保持打开状态的目录
     * @param selectFilePath 选择的脚本
     * @param selectDirPath  选择的目录
     * @param range          0全部文件  1只获取目录
     * @param isRootSelect   是否选择根目录文件,不重复选择根目录文件，因为一开始就加载了，只加载缺的文件
     * @param targetRange    0：不选择target目录及子文件目录  1：所有文件  2：只选择target目录及子文件目录
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取目录下脚本")
    @RequestMapping("/getScripsByDirId.ajax")
    @ResponseBody
    public JSONObject getScripsByDirId(UrmUserHolder holder, String path, Long gitProjectId, @RequestParam(value = "range", defaultValue = "0") Integer range, @RequestParam(value = "selectFilePath", defaultValue = "") String selectFilePath, @RequestParam(value = "selectDirPath", defaultValue = "") String selectDirPath, @RequestParam(value = "openDirs", defaultValue = "") String openDirs, @RequestParam(value = "targetRange", defaultValue = "0") Integer targetRange, @RequestParam(value = "isSelectRoot", defaultValue = "true") boolean isRootSelect) throws Exception {
        dataDevGitProjectService.verifyUserAuthority(holder.getErp(), gitProjectId);
        String[] dirs = openDirs.equals("") ? new String[0] : openDirs.split(",");
        List<ZtreeNode> res = dirService.getDirFiles(gitProjectId, path, dirs, new String[]{selectFilePath}, selectDirPath, range, isRootSelect, targetRange);

        return JSONObjectUtil.getSuccessResult(res);
    }

    @RequestMapping("/getScripsByTarget.ajax")
    @ResponseBody
    public JSONObject getScripsByTarget(UrmUserHolder holder, Long gitProjectId) throws Exception {
        dataDevGitProjectService.verifyUserAuthority(holder.getErp(), gitProjectId);
        List<ZtreeNode> res = dirService.getTargetDirs(gitProjectId);
        return JSONObjectUtil.getSuccessResult(res);
    }

    /**
     * 获取文件依赖的文件11
     *
     * @param holder
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取脚本依赖关系")
    @RequestMapping("/getScripsByDependency.ajax")
    @ResponseBody
    public JSONObject getScripts(UrmUserHolder holder, String gitProjectFilePath, Long gitProjectId) throws Exception {
        dataDevGitProjectService.verifyUserAuthority(holder.getErp(), gitProjectId);
        List<DataDevDependencyDetail> dependencyDetails = devDependencyService.getDetails(gitProjectId, gitProjectFilePath, null);
        Integer size = dependencyDetails != null ? dependencyDetails.size() : 0;
        String[] selectFilePaths = new String[size];
        int i = 0;
        for (DataDevDependencyDetail detail : dependencyDetails) {
            selectFilePaths[i++] = detail.getDependencyGitProjectFilePath();
        }
        List<ZtreeNode> res = dirService.getDirFiles(gitProjectId, "", null, selectFilePaths, null, 0, true, 0);
        devDependencyService.dealDeletedFile(res, selectFilePaths);
        devDependencyService.setDisabledStatus(res, gitProjectFilePath);
        return JSONObjectUtil.getSuccessResult(res);
    }

    /**
     * 获取文件依赖的文件
     *
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取依赖文件列表")
    @RequestMapping("/getScriptsByDependencyId.ajax")
    @ResponseBody
    public JSONObject getScriptsByDependencyId(UrmUserHolder holder, Long dependencyId, Long gitProjectId, String gitProjectFilePath, String version) throws Exception {
        List<DataDevDependencyDetail> dependencyDetails = null;
        Long start = System.currentTimeMillis();
        if (dependencyId != null) {
            dependencyDetails = devDependencyService.getDetails(dependencyId);
        } else {
            dependencyDetails = devDependencyService.getDetails(gitProjectId, gitProjectFilePath, version);
        }
        Long start1 = System.currentTimeMillis();
        List<ZtreeNode> res = devDependencyService.getScriptsByDetails(dependencyDetails);
        Long start2 = System.currentTimeMillis();
        logger.error((start1 - start) + "=======1111==========222222======" + (start2 - start1));
        return JSONObjectUtil.getSuccessResult(res);
    }


    /**
     * 得到项目下的所有目录
     *
     * @param
     * @param gitProjectId
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取项目下所有的目录")
    @RequestMapping("/getAllDirs.ajax")
    @ResponseBody
    public JSONObject getAllDirs(UrmUserHolder holder, Long gitProjectId, @RequestParam(value = "isTargetSelect", defaultValue = "false") boolean isTargetSelect) throws Exception {
        List<ZtreeNode> res = dirService.getAllDataDevScriptDir(gitProjectId, isTargetSelect);
        return JSONObjectUtil.getSuccessResult(res);
    }

    @ExceptionMessageAnnotation(errorMessage = "搜索脚本")
    @RequestMapping("/search.ajax")
    @ResponseBody
    public JSONObject getScripsBySearch(UrmUserHolder userHolder, Long gitProjectId, String query, String fileTypes, Integer limit) throws Exception {
        dataDevGitProjectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);
        DataDevScriptFile filter = new DataDevScriptFile();
        if (StringUtils.isNotBlank(fileTypes)) {
            filter.setTypeStr("(" + fileTypes + ")");
        }
        filter.setGitProjectId(gitProjectId);
        filter.setKeyWord(query);
        filter.setLimit(limit);
        List<DataDevScriptFile> files = fileService.findScriptsByFilter(filter);
        return JSONObjectUtil.getSuccessResult(files);
    }

    @ExceptionMessageAnnotation(errorMessage = "搜索脚本")
    @RequestMapping("/searchArrays.ajax")
    @ResponseBody
    public JSONArray getScripsBySearchArray(UrmUserHolder userHolder, Long gitProjectId, String query, String fileTypes, Integer limit) throws Exception {
        dataDevGitProjectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);

        DataDevScriptFile filter = new DataDevScriptFile();
        List<Integer> typeList = null;
        if (fileTypes != null && StringUtils.isNotBlank(fileTypes)) {
            typeList = new ArrayList<Integer>();
            for (String type : fileTypes.split(",")) {
                if (StringUtils.isNotBlank(type)) {
                    typeList.add(Integer.parseInt(type));
                }
            }
        }

        filter.setTypeList(typeList != null && typeList.size() > 0 ? typeList : null);
        filter.setGitProjectId(gitProjectId);
        filter.setKeyWord(query);
        filter.setLimit(limit);
        JSONArray result = new JSONArray();

        List<DataDevScriptFile> files = fileService.findScriptsByFilter(filter);
        if (files != null && files.size() > 0) {
            for (DataDevScriptFile dataDevScriptFile : files) {
                JSONObject temp = new JSONObject();
                temp.put("label", dataDevScriptFile.getName());
                temp.put("gitProjectId", dataDevScriptFile.getGitProjectId());
                temp.put("gitProjectFilePath", dataDevScriptFile.getGitProjectFilePath());
                result.add(temp);
            }

        }
        return result;
    }


//    /**
//     * 得到第一层目录
//     *
//     * @param userHolder
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping("/getDirsByAppId.ajax")
//    @ResponseBody
//    public JSONObject getDirs(UrmUserHolder userHolder, Long gitProjectId, String path) throws Exception {
//        List<ZtreeNode> res = dirService.getDirFiles(gitProjectId, path,null);
//        return JSONObjectUtil.getSuccessResult(res);
//    }

    /**
     * 获取脚本内容
     *
     * @param userHolder
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取脚本内容")
    @RequestMapping("/getScript.ajax")
    @ResponseBody
    public JSONObject getScript(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String version) throws Exception {
        if(env.equals("dev")){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("res", "res");
            jsonObject.put("md5", "oldFile != null ? oldFile.getFileMd5() : MD5Util.getMD5Str(res)");
            jsonObject.put("gitStatus", 1);
            return JSONObjectUtil.getSuccessResult(jsonObject);
        }
        dataDevGitProjectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);
        DataDevScriptFile oldFile = null;
        if (StringUtils.isNotBlank(version)) {
            oldFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath, version);
        } else {
            oldFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        }
        if (oldFile == null) {
            throw new RuntimeException("脚本不存在");
        }
        DataDevScriptTemplate template = templateService.getTemplateByFileId(oldFile.getId());
        if(template !=null && !template.getIncharge().equals(userHolder.getErp())){
            throw new RuntimeException("无权限操作模板");
        }
        DataDevScriptGitStatusEnum gitStatus = template!=null?DataDevScriptGitStatusEnum.NON:DataDevScriptGitStatusEnum.getGitStatus(oldFile.getGitVersion(), oldFile.getLastGitVersion(), oldFile.getFileMd5(), oldFile.getLastGitVersionMd5(), oldFile.getGitDeleted());
        if(template!=null){
            gitStatus = DataDevScriptGitStatusEnum.NON;
        }
        String res = fileService.getScriptContent(gitProjectId, gitProjectFilePath, version, userHolder.getErp());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("res", res);
        jsonObject.put("md5", oldFile != null ? oldFile.getFileMd5() : MD5Util.getMD5Str(res));
        jsonObject.put("gitStatus", gitStatus.toCode());
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

    @RequestMapping({"aceDiff.html"})
    public String aceDiff(Model model, Long gitProjectId, String gitProjectFilePath) throws Exception {
        if (StringUtils.isNotBlank(gitProjectFilePath)) {
            gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");
        }
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        return "/scriptcenter/editor/ace_diff";
    }

    @RequestMapping({"aceDiffChoice.html"})
    public String aceDiffChoice(Model model, Long gitProjectId, String gitProjectFilePath, String currentVersion, String lastVersion, String scriptType,
                                @RequestParam(value = "choice", defaultValue = "0") Integer choice, @RequestParam(value = "modifiedStatus", defaultValue = "0") Integer modifiedStatus) throws Exception {
        if (StringUtils.isNotBlank(gitProjectFilePath)) {
            gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");
        }
        DataDevScriptFile currentVersionFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId,gitProjectFilePath,currentVersion);
        DataDevScriptFile lastVersionFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId,gitProjectFilePath,lastVersion);
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("lastVersion", lastVersion);
        model.addAttribute("scriptType", scriptType);
        model.addAttribute("canEdit", DataDevScriptTypeEnum.canEdit(Integer.parseInt(scriptType)));
        model.addAttribute("choice", choice);
        model.addAttribute("modifiedStatus", modifiedStatus);
        model.addAttribute("currentRelationDependencyId", currentVersionFile!=null?currentVersionFile.getRelationDependencyId():null);
        model.addAttribute("lastRelationDependencyId", lastVersionFile!=null?lastVersionFile.getRelationDependencyId():null);
        return "/scriptcenter/editor/ace_diff_choice";
    }

    /**
     * git与hbase冲突
     *
     * @param userHolder
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param version
     * @param gitVersion
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取需要合并脚本内容")
    @RequestMapping("getMergeContent.ajax")
    @ResponseBody
    public JSONObject getMergeContent(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String version, String gitVersion, String oldVersion) throws Exception {
        String hbaseContent = "";
        String gitContent = "";
        JSONObject jsonObject = new JSONObject();
        dataDevGitProjectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);

        if (StringUtils.isNotBlank(version)) {
            hbaseContent = fileService.getScriptContent(gitProjectId, gitProjectFilePath, version, userHolder.getErp());
        }
        if (StringUtils.isNotBlank(gitVersion)) {
            try {
                JDGitFiles jdGitFiles = fileService.getGitContent(gitProjectId, gitProjectFilePath, gitVersion);
                gitContent = jdGitFiles.getContent();
            } catch (Exception e) {
                gitContent = "";
                logger.error("git 获取内容失败：" + e.getMessage());
            }
        }
        if (StringUtils.isNotBlank(oldVersion)) {
            String oldContent = fileService.getScriptContentFromHbase(gitProjectId, gitProjectFilePath, oldVersion);
            jsonObject.put("oldContent", oldContent);
        }
        jsonObject.put("hbaseContent", hbaseContent);
        jsonObject.put("gitContent", gitContent);
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

    /**
     * 调度与hbase冲突
     *
     * @param userHolder
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取调度与开发平台需要合并脚本内容")
    @RequestMapping("getImportMergeContent.ajax")
    @ResponseBody
    public JSONObject getImportMergeContent(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, Long scriptId) throws Exception {
        String hbaseContent = "";
        String buffaloContent = "";

        DataDevScriptFile dataDevScriptFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        hbaseContent = fileService.getScriptContentFromHbase(gitProjectId, gitProjectFilePath, dataDevScriptFile.getVersion());

        if (scriptId != null && scriptId > 0) {
            byte[] bytes = importScriptManager.loadFile(userHolder.getErp(), scriptId, null);
            buffaloContent = new String(bytes, "utf-8");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hbaseContent", hbaseContent);
        jsonObject.put("buffaloContent", buffaloContent);
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

    @RequestMapping({"scriptContent.html"})
    public String scriptContent(UrmUserHolder userHolder, Model model, Long gitProjectId, String gitProjectFilePath, String version) throws Exception {
        gitProjectFilePath = URLDecoder.decode(gitProjectFilePath, "utf-8");
        dataDevGitProjectService.verifyUserAuthority(userHolder.getErp(), gitProjectId);
        DataDevScriptFile dataDevScriptFile = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath, version);
        if (dataDevScriptFile == null) {
            dataDevScriptFile = fileService.getScriptByGitProjectIdAndFilePathIgnoreDelete(gitProjectId, gitProjectFilePath, version);
        }
        String hbaseContent = fileService.getScriptContentFromHbase(dataDevScriptFile.getId(), version);
        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("gitProjectFilePath", gitProjectFilePath);
        model.addAttribute("hbaseContent", hbaseContent);
        Integer type = DataDevScriptTypeEnum.getFileNameScriptType(gitProjectFilePath).toCode();
        model.addAttribute("scriptType", type);
        model.addAttribute("canEdit", DataDevScriptTypeEnum.canEdit(type) ? 1 : 0);
        model.addAttribute("version", version);
        model.addAttribute("relationDependencyId", dataDevScriptFile.getRelationDependencyId() != null ? dataDevScriptFile.getRelationDependencyId() : -1);
        return "scriptcenter/home/home_open_ide/script_content";
    }

    @RequestMapping("/getPushNum.ajax")
    @ResponseBody
    public JSONObject getPushNum(UrmUserHolder holder, Long gitProjectId, String gitProjectDirPath, String gitProjectFilePath) throws Exception {
        JSONObject jsonObject = dirService.getPushNum(gitProjectId, gitProjectDirPath, gitProjectFilePath);
        jsonObject.put("erp", holder.getErp());
        return JSONObjectUtil.getSuccessResult(jsonObject);
    }

}
