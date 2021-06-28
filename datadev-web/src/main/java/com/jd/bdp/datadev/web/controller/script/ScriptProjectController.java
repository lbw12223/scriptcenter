package com.jd.bdp.datadev.web.controller.script;

//import net.sf.json.JSONObject;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.dao.DataDevGitProjectDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevGitAccessLevelEnum;
import com.jd.bdp.datadev.enums.DataDevGitInitFlag;
import com.jd.bdp.datadev.enums.DataDevGitOrCodingEnum;
import com.jd.bdp.datadev.enums.DataDevScriptGitStatusEnum;
import com.jd.bdp.datadev.jdgit.*;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.worker.InitSingleGitGroup;
import com.jd.bdp.datadev.web.worker.InitSingleGitProject;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhangrui25 on 2018/6/19.
 */
@Controller
@RequestMapping("/scriptcenter/project/")
public class ScriptProjectController {

    private static final Logger logger = Logger.getLogger(ScriptProjectController.class);

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private InitSingleGitProject initSingleGitProject;

    @Autowired
    private InitSingleGitGroup initSingleGitGroup;

    @Autowired
    private DataDevGitGroupService dataDevGitGroupService;
    @Autowired
    private DataDevScriptFileService fileService;

    @Autowired
    private DataDevGitProjectSharedGroupService dataDevGitProjectSharedGroupService;

    @Autowired
    DataDevGitGroupMemberService dataDevGitGroupMemberService;

    @Autowired
    private ImportScriptManager importScriptManager;

    @Autowired
    private DataDevGitProjectMemberService dataDevGitProjectMemberService;

    @Autowired
    private DataDevScriptUploadService dataDevScriptUploadService;

    @Autowired
    private DataDevScriptFileService dataDevScriptFileService;

    @Autowired
    private DataDevGitProjectDao dataDevGitProjectDao;

    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private AppGroupUtil appGroupUtil;
    @Value("${git.private.user}")
    private String SysOwner;
    @Value("${coding.private.user}")
    private String SysOwnerCoding;


    /**
     * 创建项目 成功过后出来 定时程序
     *
     * @param userHolder
     * @param projectName
     * @param description
     * @param groupId
     * @return
     * @throws Exception
     */

    @ExceptionMessageAnnotation(errorMessage = "创建Git|Coding Project")
    @RequestMapping("/createProject.ajax")
    @ResponseBody
    public JSONObject createProject(UrmUserHolder userHolder, String projectName, String description,
                                    Long groupId,
                                    Integer gitOrCodingCode,
                                    String groupName) throws Exception {
        JDGitProjects jdGitProjects = new JDGitProjects();
        jdGitProjects.setDescription(description);
        jdGitProjects.setProjectName(projectName);
        jdGitProjects.setNamespaceId(groupId);
        jdGitProjects.setGitOrCodingCode(gitOrCodingCode);
        jdGitProjects.setSearchNameSpace(groupName);
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.createProject(jdGitProjects);

        DataDevGitProjectSharedGroup sharedGroup = new DataDevGitProjectSharedGroup();
        sharedGroup.setGitProjectId(dataDevGitProject.getGitProjectId());
        sharedGroup.setGroupAccessLevel(ImportScriptManager.OWNER);
        sharedGroup.setGitGroupId(groupId);
        dataDevGitProject.setGroupId(groupId);
        dataDevGitProjectSharedGroupService.insertGitSharedGroup(sharedGroup);

        initSingleGitProject.initSingleGitProject(dataDevGitProject);
        return JSONObjectUtil.getSuccessResult("创建成功", dataDevGitProject);
    }

    @ExceptionMessageAnnotation(errorMessage = "创建本地 Project")
    @RequestMapping("/createLocalProject.ajax")
    @ResponseBody
    public JSONObject createLocalProject(UrmUserHolder userHolder, String projectName, String description) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectDao.getGitProjectByPath(projectName);

        if (dataDevGitProject != null) {
            throw new RuntimeException("项目【" + projectName + "】已经存在！");
        }

        DataDevGitProject insertDataDevGitProject = dataDevGitProjectService.createLocalProject(userHolder.getErp(), projectName, description);
        return JSONObjectUtil.getSuccessResult("创建成功", insertDataDevGitProject);
    }



    /**
     * 添加项目成员
     *
     * @param userHolder
     * @param gitProjectId
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "添加Git项目成员")
    @RequestMapping("/addProjectMember.ajax")
    @ResponseBody
    public JSONObject addProjectMember(UrmUserHolder userHolder, Long gitProjectId, String erps) throws Exception {
        boolean isGitOrCoding = gitProjectId < GitHttpUtil._10YI;
        List<JDGitMembers> jdGitMembersList = new ArrayList<JDGitMembers>();
        List<DataDevGitProjectMember> dataDevGitProjectMemberList = new ArrayList<DataDevGitProjectMember>();
        if (isGitOrCoding) {


            if (StringUtils.isNotEmpty(erps)) {
                for (String erp : erps.split(",")) {
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

        } else {

            if (StringUtils.isNotEmpty(erps)) {
                for (String erp : erps.split(",")) {

                    DataDevGitProjectMember temp = new DataDevGitProjectMember();
                    temp.setAccessLevel(ImportScriptManager.DEVELOPER);
                    DataDevGitProjectMember dataDevGitProjectMember = dataDevGitProjectMemberService.findByErp(gitProjectId, erp);

                    if (dataDevGitProjectMember != null) {
                        throw new RuntimeException("用户【" + erp + "】已经存在！");
                    }
                    temp.setGitMemberName(erp);
                    temp.setGitMemberUserName(erp);
                    temp.setGitProjectId(gitProjectId);

                    dataDevGitProjectMemberList.add(temp);
                }

                dataDevGitProjectMemberService.insert(dataDevGitProjectMemberList);
            }


        }

        return JSONObjectUtil.getSuccessResult("添加人员成功");

    }

    /**
     * 添加共享组
     *
     * @param gitProjectId
     * @param gitGroupId
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "添加Git项目共享组")
    @RequestMapping("/addSharedWithGroups.ajax")
    @ResponseBody
    public JSONObject addSharedWithGroups(UrmUserHolder urmUserHolder,
                                          Long gitProjectId,
                                          Long gitGroupId,
                                          String gitGroupName,
                                          Integer isSyncProjectSpace) throws Exception {

        boolean isGitOrCoding = GitHttpUtil.isCodingOrGit(gitProjectId);
        DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup = new DataDevGitProjectSharedGroup();//創建DataDevGitProjectSharedGroup
        if (isGitOrCoding) {
            JDGitProjectShareGroups jdGitProjectShareGroups = new JDGitProjectShareGroups();//创建JDGitProjectShareGroups
            jdGitProjectShareGroups.setJdGroupId(gitGroupId);
            jdGitProjectShareGroups.setGroupAccessLevel(ImportScriptManager.DEVELOPER);
            jdGitProjectShareGroups.setGroupName(gitGroupName);
            jdGitProjectShareGroups.setGitProjectId(gitProjectId);

            JDGitProjects jdGitProjects = new JDGitProjects();//创建JDGitProjects
            jdGitProjects.setGitProjectId(gitProjectId);
            List<JDGitProjectShareGroups> list = new ArrayList<JDGitProjectShareGroups>();
            list.add(jdGitProjectShareGroups);//JDGitProjects添加JDGitProjectShareGroups
            jdGitProjects.setSharedWithGroups(list);
            jdGitProjects.addSharedGroup(jdGitProjectShareGroups);//添加到git上

            dataDevGitProjectSharedGroup.setGroupAccessLevel(ImportScriptManager.DEVELOPER);
            dataDevGitProjectSharedGroup.setGitGroupId(gitGroupId);
            dataDevGitProjectSharedGroup.setGitProjectId(gitProjectId);
            dataDevGitProjectSharedGroup.setGroupName(gitGroupName);
        } else {
            Long gitProjectGroupId = gitGroupId + GitHttpUtil._10YI;
            boolean isExits = dataDevGitProjectSharedGroupService.isExits(gitProjectId, gitProjectGroupId);
            if (isExits) {
                return JSONObjectUtil.getSuccessResult("该组已经存在！");
            }
            dataDevGitProjectSharedGroup.setGroupAccessLevel(ImportScriptManager.DEVELOPER);
            dataDevGitProjectSharedGroup.setGitGroupId(gitProjectGroupId);
            dataDevGitProjectSharedGroup.setGitProjectId(gitProjectId);
            dataDevGitProjectSharedGroup.setGroupName(gitGroupName);
            dataDevGitProjectSharedGroup.setIsCanSysProjectScript(isSyncProjectSpace != null && isSyncProjectSpace == 1 ? 1 : 0);
        }

        dataDevGitProjectSharedGroupService.insertGitSharedGroup(dataDevGitProjectSharedGroup);//插入數據庫中
        return JSONObjectUtil.getSuccessResult("添加共享组成功");

    }


    /**
     * 删除项目成员
     *
     * @param userHolder
     * @param gitProjectId
     * @param gitMemberId
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "删除Git人员")
    @RequestMapping("/deleteProjectMember.ajax")
    @ResponseBody
    public JSONObject deleteProjectMember(UrmUserHolder userHolder, Long gitProjectId, Long gitMemberId, Long id) throws Exception {
        boolean isGitOrCoding = gitProjectId < GitHttpUtil._10YI;

        if (isGitOrCoding) {
            JDGitMembers jdGitMembers = new JDGitMembers();//创建JDGitMembers
            jdGitMembers.setGitUserId(gitMemberId);

            JDGitProjects jdGitProjects = new JDGitProjects();//创建JDGitProjects
            jdGitProjects.setGitProjectId(gitProjectId);
            jdGitProjects.deleteProjectMember(jdGitMembers);//JDGitProjects执行删除操作
            dataDevGitProjectMemberService.deleteGitProjectMemberById(gitProjectId, gitMemberId);
            return JSONObjectUtil.getSuccessResult("删除人员成功");
        } else {
            DataDevGitProjectMember dataDevGitProjectMember = dataDevGitProjectMemberService.findById(id);
            if (dataDevGitProjectMember.getAccessLevel() == DataDevGitAccessLevelEnum.Owner.toCode()) {
                throw new RuntimeException("创建人不能删除！");
            }
            dataDevGitProjectMemberService.deleteById(id);

            return JSONObjectUtil.getSuccessResult("删除人员成功");
        }


    }

    /**
     * 刪除共享组
     *
     * @param gitProjectId
     * @param jdGroupId
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "删除共享组")
    @RequestMapping("/deleteSharedWithGroups.ajax")
    @ResponseBody
    public JSONObject deleteSharedWithGroups(Long gitProjectId, Long jdGroupId) {
        JDGitProjectShareGroups jdGitProjectShareGroups = new JDGitProjectShareGroups();//创建JDGitProjectShareGroups
        jdGitProjectShareGroups.setJdGroupId(jdGroupId);
        jdGitProjectShareGroups.setGitProjectId(gitProjectId);

        JDGitProjects jdGitProjects = new JDGitProjects();//创建JDGitProjects
        jdGitProjects.setGitProjectId(gitProjectId);

        jdGitProjects.deleteSharedGroup(jdGitProjectShareGroups);//从git上删除共享组信息

        DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup = new DataDevGitProjectSharedGroup();//創建DataDevGitProjectSharedGroup
        dataDevGitProjectSharedGroup.setGitGroupId(jdGroupId);
        dataDevGitProjectSharedGroup.setGitProjectId(gitProjectId);
        dataDevGitProjectSharedGroupService.deleteSharedGroupFromProject(dataDevGitProjectSharedGroup);//从数据库中删除记录
        return JSONObjectUtil.getSuccessResult("删除共享组成功");

    }

    /**
     * 获取用户可以选择的组
     *
     * @param userHolder
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取用户可选组列表")
    @RequestMapping("/listErpGroups.ajax")
    @ResponseBody
    public JSONObject listErpGroups(UrmUserHolder userHolder, Integer gitOrCodingCode) throws Exception {
        return JSONObjectUtil.getSuccessResult(dataDevGitGroupService.listErpGroup(userHolder.getErp(), gitOrCodingCode));
    }

    /**
     * 添加Group -> 默认添加当前的Erp
     *
     * @param userHolder
     * @param groupName
     * @param description
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "创建Git组")
    @RequestMapping("/createGroup.ajax")
    @ResponseBody
    public JSONObject createGroup(UrmUserHolder userHolder, String groupName, String description, String erps, Integer gitOrCodingCode) throws Exception {
        //查询JdUser ID
        Set<String> erpSets = new HashSet<String>();
        for (String erpTemp : erps.split(",")) {
            if (StringUtils.isNotBlank(erpTemp)) {
                erpSets.add(erpTemp);
            }
        }
        erpSets.add(userHolder.getErp());

        List<JDGitMembers> jdGitMembers = new ArrayList<JDGitMembers>();

        for (String erp : erpSets) {
            JDGitUser jdGitUser = new JDGitUser();
            if (StringUtils.isBlank(erp)) {
                continue;
            }
            jdGitUser.setName(erp);
            jdGitUser.setGitOrCodingCode(gitOrCodingCode);
            JDGitUser searchJdUser = jdGitUser.searchUser();
            if (searchJdUser == null) {
                String url = gitOrCodingCode == DataDevGitOrCodingEnum.GIT.tocode() ? "git.jd.com" : "coding.jd.com";
                throw new RuntimeException("用户[" + erp + "未激活，请在<a href='http://" + url + "'target='blank'>" + url + "</a>登录一次进行激活！");
            }
            JDGitMembers temp = new JDGitMembers();
            temp.setName(userHolder.getErp());
            if (erp.equals(userHolder.getErp())) {
                temp.setAccessLevel(ImportScriptManager.OWNER);
            } else {
                temp.setAccessLevel(ImportScriptManager.DEVELOPER);
            }
            temp.setGitUserId(searchJdUser.getId());
            jdGitMembers.add(temp);
            if (gitOrCodingCode == DataDevGitOrCodingEnum.GIT.tocode()) {
                //git
                if (erp.equals(userHolder.getErp())) {
                    temp.setAccessLevel(ImportScriptManager.OWNER);
                } else {
                    temp.setAccessLevel(ImportScriptManager.DEVELOPER);
                }
            } else {
                //coding
                if (erp.equals(userHolder.getErp())) {
                    temp.setAccessLevel(ImportScriptManager.MASTER);
                } else {
                    temp.setAccessLevel(ImportScriptManager.DEVELOPER);
                }
            }

        }

        //create group
        JDGitGroups jdGitGroups = new JDGitGroups();
        jdGitGroups.setDescription(description);
        jdGitGroups.setName(groupName);
        jdGitGroups.setGitOrCodingCode(gitOrCodingCode);
        DataDevGitGroup dataDevGitGroup = dataDevGitGroupService.createGroup(jdGitGroups);


        //add group member
        jdGitGroups.setJdGroupId(dataDevGitGroup.getGitGroupId());
        jdGitGroups.setMembers(jdGitMembers);
        jdGitGroups.addGroupMember();

        //触发自动拉取
        initSingleGitGroup.initSingleGitGroup(dataDevGitGroup.getGitGroupId());


        return JSONObjectUtil.getSuccessResult("创建成功", dataDevGitGroup.getGitGroupId());
    }

    @RequestMapping("/newProjectTips.html")
    public String newProjectTips(UrmUserHolder userHolder) throws Exception {
        return "scriptcenter/home/project/newProjectTips";
    }

    @RequestMapping("/newGitTips.html")
    public String newGitTips(UrmUserHolder userHolder, Model model) throws Exception {
        model.addAttribute("gitPrivateUser", SysOwner);
        return "scriptcenter/home/project/newGitTips";
    }

    @RequestMapping("/newGitProject.html")
    public String newGitProject(UrmUserHolder userHolder) throws Exception {
        return "scriptcenter/home/project/newGitProject";
    }

    @RequestMapping("/newLocalProject.html")
    public String newLocalProject(UrmUserHolder userHolder) throws Exception {
        return "scriptcenter/home/project/newLocalProject";
    }


    @RequestMapping("/newGitProjectGroup.html")
    public String newGitProjectGroup(UrmUserHolder userHolder) throws Exception {
        return "scriptcenter/home/project/newGitProjectGroup";
    }

    @RequestMapping("/newCodingTips.html")
    public String newCodingTips(UrmUserHolder userHolder, Model model) throws Exception {
        model.addAttribute("codingPrivateUser", SysOwnerCoding);
        return "scriptcenter/home/project/newCodingTips";
    }

    @RequestMapping("/newCodingProject.html")
    public String newCodingProject(UrmUserHolder userHolder) throws Exception {
        return "scriptcenter/home/project/newCodingProject";
    }

    @RequestMapping("/newCodingProjectGroup.html")
    public String newCodingProjectGroup(UrmUserHolder userHolder) throws Exception {
        return "scriptcenter/home/project/newCodingProjectGroup";
    }

    @RequestMapping("/tranferScript.html")
    public String tranferScript(String gitProjectDirPath, Long projectId, @RequestParam(value = "scriptId", defaultValue = "0") Long scriptId, @RequestParam(value = "scriptName", defaultValue = "") String scriptName, @RequestParam(value = "scriptVersion", defaultValue = "") String scriptVersion, @RequestParam(value = "jsdAppgroupId", defaultValue = "0") Long jsdAppgroupId, Model model) throws Exception {
        String dirPath = StringUtils.isNotBlank(gitProjectDirPath) ? URLDecoder.decode(gitProjectDirPath, "utf-8") : "";
        model.addAttribute("dirPath", dirPath);
        model.addAttribute("projectId", projectId);
        model.addAttribute("scriptId", scriptId);
        model.addAttribute("scriptName", URLDecoder.decode(scriptName, "utf-8"));
        model.addAttribute("scriptVersion", scriptVersion);
        String jsdAppgroupName = jsdAppgroupId.toString();
        try {
            DataDevApplication appInfo = appGroupUtil.getAppInfo(jsdAppgroupId);
            jsdAppgroupName = appInfo.getAppgroupName();
            model.addAttribute("dirPath", jsdAppgroupName);
            jsdAppgroupName += "(" + appInfo.getAppgroupId() + ")";
        } catch (Exception e) {
            logger.error("=============getAppInfo error:" + e.getMessage());
        }
        model.addAttribute("jsdAppgroupId", jsdAppgroupId);
        model.addAttribute("jsdAppgroupName", jsdAppgroupName);
        return "/scriptcenter/home/transferScript";
    }

    /**
     * 同步脚本
     *
     * @param urmUserHolder
     * @param gitProjectId
     * @param dirPath
     * @return
     * @throws Exception
     */
    @RequestMapping("syncScriptToDataDev.ajax")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject syncScriptToDataDev(UrmUserHolder urmUserHolder,
                                                               Long gitProjectId,
                                                               String dirPath,
                                                               @RequestParam(value = "syncMember", defaultValue = "0") Integer syncMember,
                                                               @RequestParam(value = "scriptId", defaultValue = "0") Long scriptId,
                                                               @RequestParam(value = "scriptName", defaultValue = "") String scriptName,
                                                               @RequestParam(value = "scriptVersion", defaultValue = "") String scriptVersion,
                                                               @RequestParam(value = "jsdAppgroupId", defaultValue = "0") Long jsdAppgroupId,
                                                               @RequestParam(value = "sync", defaultValue = "0") Integer isSync) throws Exception {
        logger.error("insert================syncScriptToDataDev.ajax");
        com.alibaba.fastjson.JSONObject jsonObject = importScriptManager.syncScriptToDataDev(gitProjectId, dirPath, jsdAppgroupId, urmUserHolder.getErp(), syncMember.equals(1), scriptName, scriptId, scriptVersion, isSync != 0);
        return JSONObjectUtil.getSuccessResult("success", jsonObject);
    }

    /**
     * 同步脚本
     *
     * @param urmUserHolder
     * @param gitProjectId
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping("syncScriptToDataDevNew.ajax")
    @ResponseBody
    public JSONObject syncScriptToDataDevNew(UrmUserHolder urmUserHolder,
                                             Long gitProjectId,
                                             @RequestParam(value = "jsdAppgroupId", defaultValue = "0") Long jsdAppgroupId) throws Exception {

//        if(true){
//            jsdAppgroupId = 100144L;
//        }
        String erp = urmUserHolder.getErp();
        net.sf.json.JSONObject jsonObject = importScriptManager.syncScriptToDataDevNew(gitProjectId, jsdAppgroupId, erp);

        return JSONObjectUtil.getSuccessResult("success", jsonObject);
    }


    /**
     * 同步脚本
     *
     * @param urmUserHolder
     * @param gitProjectId
     * @return
     * @throws Exception
     */
    @RequestMapping("syncScriptByMerge.ajax")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject syncScriptByMerge(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectFilePath, @RequestParam(value = "scriptId", defaultValue = "0") Long scriptId, @RequestParam(value = "scriptName", defaultValue = "") String scriptName, @RequestParam(value = "content", defaultValue = "") String content) throws Exception {
        logger.error("insert================syncScriptToDataDev.ajax");
        ZtreeNode ztreeNode = importScriptManager.syncScriptByMerge(gitProjectId, gitProjectFilePath, scriptId, scriptName, content, urmUserHolder.getErp());
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        DataDevScriptGitStatusEnum gitStatus = DataDevScriptGitStatusEnum.getGitStatus(file.getGitVersion(), file.getLastGitVersion(), file.getFileMd5(), file.getLastGitVersionMd5(), file.getGitDeleted());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileMd5", file.getFileMd5());
        jsonObject.put("version", file.getVersion());
        jsonObject.put("gitStatus", gitStatus.toCode());
        return JSONObjectUtil.getSuccessResult("success", jsonObject);
    }

    /**
     * 获取同步进度
     *
     * @param urmUserHolder
     * @param appGroupId
     * @return
     * @throws Exception
     */
    @RequestMapping("syncScriptToDataDevProcess.ajax")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject syncScriptToDataDevProcess(UrmUserHolder urmUserHolder, Long appGroupId) throws Exception {
//        if(true){
//            appGroupId = 100144L ;
//        }
        String value = importScriptManager.getSyncRedisValue(appGroupId);
        if (StringUtils.isNotBlank(value)) {
            return JSONObjectUtil.getSuccessResult("success", JSONObject.parseObject(value));
        }
        return JSONObjectUtil.getSuccessResult("success", "");
    }

    /**
     * 清除同步
     *
     * @param urmUserHolder
     * @param appGroupId
     * @return
     * @throws Exception
     */
    @RequestMapping("clearSync.ajax")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject clearSync(UrmUserHolder urmUserHolder, Long appGroupId) throws Exception {
        importScriptManager.clearSync(appGroupId);
        return JSONObjectUtil.getSuccessResult("success");
    }

    /**
     * 获取需要同步的人员信息
     *
     * @param urmUserHolder
     * @param gitProjectId
     * @param appGroupId
     * @return
     * @throws Exception
     */
    @RequestMapping("getNeedAddMember.ajax")
    @ResponseBody
    public JSONObject syncMember(UrmUserHolder urmUserHolder, Long gitProjectId, Long appGroupId) throws Exception {
        return JSONObjectUtil.getSuccessResult("success", importScriptManager.getSyncProjectMembers(gitProjectId, appGroupId));
    }

    @ExceptionMessageAnnotation(errorMessage = "获取Git成员")
    @RequestMapping("gitMembers.ajax")
    @ResponseBody
    public JSONObject listUser(Long gitProjectId) throws Exception {
        List<DataDevGitProjectMember> projectMembers = dataDevGitProjectMemberService.findAll(gitProjectId);

        if (gitProjectId < GitHttpUtil._10YI) {
            JDGitProjects jdGitProjects = new JDGitProjects();
            jdGitProjects.setGitProjectId(gitProjectId);
            JDGitProjects projectDetail = jdGitProjects.gitProjectDetail();
            List<JDGitProjectShareGroups> groups = projectDetail.getSharedWithGroups();
            List<DataDevGitGroupMember> groupMembers = new ArrayList<DataDevGitGroupMember>();
            for (JDGitProjectShareGroups jdGitProjectShareGroup : groups) {
                Long groupId = jdGitProjectShareGroup.getJdGroupId();
                List<DataDevGitGroupMember> memberUsernames = dataDevGitGroupMemberService.queryFromGroupId(groupId);
                groupMembers.addAll(memberUsernames);
            }
            for (DataDevGitGroupMember dataDevGitGroupMember : groupMembers) {
                DataDevGitProjectMember dataDevGitProjectMember = new DataDevGitProjectMember();
                String erp = dataDevGitGroupMember.getGitMemberUserName();
                dataDevGitProjectMember.setGitMemberUserName(erp);
                projectMembers.add(dataDevGitProjectMember);
            }
        }


        //获取中文名
        urmUtil.covertUserErp2UserName(projectMembers, new ConvertErp2UserName<DataDevGitProjectMember>() {
            @Override
            public String getErp(DataDevGitProjectMember o) {
                return o.getGitMemberUserName();
            }

            @Override
            public void setErpUserName(DataDevGitProjectMember o, String userNames) {
                if (userNames != null) {
                    o.setSystemUserName(userNames + "(" + o.getGitMemberUserName() + ")");
                } else {
                    o.setSystemUserName(o.getGitMemberUserName());
                }
            }
        });
        return JSONObjectUtil.getSuccessResult(removeDuplicate(projectMembers));
    }

    @RequestMapping("groupMember.html")
    public String groupMember(Long groupId, Model model) throws Exception {
        List<DataDevGitGroupMember> groupMembers = dataDevGitGroupMemberService.queryFromGroupId(groupId);
        for (DataDevGitGroupMember dataDevGitGroupMember : groupMembers) {
            String erp = dataDevGitGroupMember.getGitMemberUserName();
            String name = urmUtil.getNameByErp(erp);
            if (name != "") dataDevGitGroupMember.setGitMemberUserName(erp + "(" + name + ")");
            else dataDevGitGroupMember.setGitMemberUserName(erp);
            dataDevGitGroupMember.setAccessLevelRight();
        }

        model.addAttribute("groupMembers", JSONObject.toJSONString(groupMembers));
        return "scriptcenter/art/group_members";
    }


    public List<DataDevGitProjectMember> removeDuplicate(List<DataDevGitProjectMember> list) {
        HashSet<String> set = new HashSet<String>();
        List<DataDevGitProjectMember> result = new ArrayList<DataDevGitProjectMember>();
        for (DataDevGitProjectMember dataDevGitProjectMember : list) {
            String memberName = dataDevGitProjectMember.getGitMemberUserName();
            if (!set.contains(memberName)) {
                result.add(dataDevGitProjectMember);
            }
            set.add(memberName);
        }
        return result;
    }

    @RequestMapping("projectDetail.html")
    public String projectDetail(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectFilePath, Model model) throws Exception {
        boolean isGitOrCoding = gitProjectId < GitHttpUtil._10YI;
        if (isGitOrCoding) {
            JDGitProjects jdGitProjects = new JDGitProjects();
            jdGitProjects.setGitProjectId(gitProjectId);
            JDGitProjects projectDetail = jdGitProjects.gitProjectDetail();
            boolean isGroupMember = false;
            boolean isProjectMember = false;
            boolean hasAuthority = false;
            String erp = urmUserHolder.getErp();

            DataDevGitProjectMember dataDevGitProjectMember = dataDevGitProjectMemberService.findByErp(gitProjectId, erp);

            DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
            Long groupId = dataDevGitProject.getGroupId();//找到项目所属组的id

            List<DataDevGitGroupMember> list = dataDevGitGroupMemberService.queryFromGroupId(groupId);//找到这个组的所有成员
            for (DataDevGitGroupMember dataDevGitGroupMember : list) {
                logger.error("projectDetail list=" + dataDevGitGroupMember.toString());
            }


            if (list != null) {//检查操作人员是否组成员
                for (DataDevGitGroupMember dataDevGitGroupMember : list) {
                    String gitUserName = dataDevGitGroupMember.getGitMemberUserName();
                    if (gitUserName.equals(erp) && dataDevGitGroupMember.getAccessLevel() >= 40) {
                        isGroupMember = true;
                        break;
                    }
                }
            }

            isProjectMember = (dataDevGitProjectMember != null && dataDevGitProjectMember.getAccessLevel() >= 40);//检查操作人员是否项目成员
            logger.error("projectDetail isProjectMember=" + isProjectMember + ";isGroupMember=" + isGroupMember + ";groupId=" + groupId);
            if (isProjectMember || isGroupMember) {
                hasAuthority = true; //如果操作人权限不够，则无权限进行添加和删除操作
            }

            model.addAttribute("projectDetail", JSONObject.toJSONString(projectDetail));
            model.addAttribute("gitProjectId", gitProjectId);
            model.addAttribute("gitProjectFilePath", gitProjectFilePath);
            model.addAttribute("hasAuthority", hasAuthority);
            model.addAttribute("isCodingOrGit", 1);
            model.addAttribute("isCoding", GitHttpUtil.isCoding(gitProjectId) ? 1 : 0);

            return "scriptcenter/art/projectDetail";

        } else {
            boolean hasAuthority = true;
            String erp = urmUserHolder.getErp();
            DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);

            /**
             *     $("#projectName").text(projectDetail.projectPath);
             *     $("#projectId").text(projectDetail.gitProjectId);
             *     $("#HTTP").text(projectDetail.webUrl);
             *     $("#HTTP").attr("href",projectDetail.webUrl);
             *     $("#SSH").text(projectDetail.sshUrl);
             *     $("#creatTime").text(projectDetail.createDate);
             *     $("#description").text(projectDetail.description);
             */
            dataDevGitProject.setGitProjectPath(dataDevGitProject.getGitProjectName());
            model.addAttribute("projectDetail", JSONObject.toJSONString(dataDevGitProject));
            model.addAttribute("gitProjectId", gitProjectId);
            model.addAttribute("gitProjectFilePath", gitProjectFilePath);
            model.addAttribute("hasAuthority", hasAuthority);
            model.addAttribute("isCodingOrGit", 0);
            model.addAttribute("isCoding", 0);
            model.addAttribute("createDate", dataDevGitProject.getRefreshTime());
            model.addAttribute("description", dataDevGitProject.getDescription());
            model.addAttribute("projectName", dataDevGitProject.getGitProjectName());

            return "scriptcenter/art/projectDetail";
        }
    }

    @RequestMapping("addUser.html")
    public String addUser(Long gitProjectId, String gitProjectFilePath, Model model) throws Exception {
        model.addAttribute("gitProjectId", gitProjectId);
        return "scriptcenter/art/addUser";
    }

    @RequestMapping("addGroup.html")
    public String addGroup(UrmUserHolder urmUserHolder, Long gitProjectId, Model model) throws Exception {

        String erp = urmUserHolder.getErp();

        DataDevGitProjectMember localProjcetMaster = dataDevGitProjectMemberService.findLocalProjcetMaster(gitProjectId);

        boolean hasSetProjectOwnerRight = localProjcetMaster != null && localProjcetMaster.getGitMemberName().equalsIgnoreCase(erp);


        model.addAttribute("gitProjectId", gitProjectId);
        model.addAttribute("isCodingOrGit", GitHttpUtil.isCodingOrGit(gitProjectId) ? 1 : 0);
        model.addAttribute("isGit", GitHttpUtil.isGit(gitProjectId) ? 1 : 0);
        model.addAttribute("hasSetProjectOwnerRight", hasSetProjectOwnerRight);

        return "scriptcenter/art/addGroup";
    }

    @RequestMapping("projectMemberInfo.ajax")
    @ResponseBody
    public net.sf.json.JSONObject projectMemberInfoAjax(@RequestParam(value = "gitProjectId", defaultValue = "") Long gitProjectId, @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "20") Integer rows) throws Exception {
        try {
            Pageable pageable = new PageRequest(page - 1, rows);
            Page<DataDevGitProjectMember> members = dataDevGitProjectMemberService.findAllByPage(gitProjectId, pageable);

            //获取中文名
            urmUtil.covertUserErp2UserName(members, new ConvertErp2UserName<DataDevGitProjectMember>() {
                @Override
                public String getErp(DataDevGitProjectMember o) {
                    return o.getGitMemberUserName();
                }

                @Override
                public void setErpUserName(DataDevGitProjectMember o, String userNames) {
                    if (userNames != null) o.setGitMemberUserName(userNames + "(" + o.getGitMemberUserName() + ")");
                    else o.setGitMemberUserName(o.getGitMemberUserName());
                }

            });

            return AjaxUtil.jqGridJson(members);
        } catch (Exception e) {
            return AjaxUtil.failure("查询失败:" + e.getMessage());
        }
    }

    @ExceptionMessageAnnotation(errorMessage = "获取Git项目分享组信息")
    @RequestMapping("sharedWithGroups.ajax")
    @ResponseBody
    public net.sf.json.JSONObject sharedWithGroupsAjax(Long gitProjectId) throws Exception {
        boolean isCoding = GitHttpUtil.isCodingOrGit(gitProjectId);
        JDGitProjects jdGitProjects = new JDGitProjects();
        jdGitProjects.setGitProjectId(gitProjectId);
        JDGitProjects projectDetail = jdGitProjects.gitProjectDetail();
        List<JDGitProjectShareGroups> sharedWithGroups = new ArrayList<JDGitProjectShareGroups>();
        if (isCoding && sharedWithGroups != null && sharedWithGroups.size() > 0) {
            sharedWithGroups = projectDetail.getSharedWithGroups();
            for (JDGitProjectShareGroups jdGitProjectShareGroups : sharedWithGroups) {
                jdGitProjectShareGroups.setGitProjectId(gitProjectId);
                jdGitProjectShareGroups.setJdGroupId(jdGitProjectShareGroups.getJdGroupId() + GitHttpUtil._9YI);
            }
        } else {
            List<DataDevGitProjectSharedGroup> list = dataDevGitProjectSharedGroupService.list(gitProjectId);
            if (list != null) {
                for (DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup : list) {
                    JDGitProjectShareGroups temp = new JDGitProjectShareGroups();
                    temp.setIsCanSysProjectScript(dataDevGitProjectSharedGroup.getIsCanSysProjectScript());
                    temp.setJdGroupId(dataDevGitProjectSharedGroup.getGitGroupId());
                    temp.setGroupAccessLevel(dataDevGitProjectSharedGroup.getGroupAccessLevel());
                    temp.setGroupName(dataDevGitProjectSharedGroup.getGroupName());
                    sharedWithGroups.add(temp);
                }
            }
        }
        Page<JDGitProjectShareGroups> page = new PageImpl<JDGitProjectShareGroups>(sharedWithGroups);
        return AjaxUtil.jqGridJson(page);
    }

    @RequestMapping("/listAllGroupMembers.ajax")
    @ResponseBody
    public net.sf.json.JSONObject listAllGroupMembers(Long gitGroupId) {
        List<DataDevGitGroupMember> list = null;
        try {
            JDGitGroups jdGitGroups = new JDGitGroups();
            jdGitGroups.setJdGroupId(gitGroupId);
            list = jdGitGroups.listGroupMembers();//获取组成员列表
            for (DataDevGitGroupMember dataDevGitGroupMember : list) {
                String erp = dataDevGitGroupMember.getGitMemberUserName();//把组成员的名字改为erp+中文名形式
                String name = urmUtil.getNameByErp(erp);
                if (name != "") dataDevGitGroupMember.setGitMemberUserName(erp + "(" + name + ")");
                else dataDevGitGroupMember.setGitMemberUserName(erp);

                dataDevGitGroupMember.setAccessLevelRight();//设置组成员角色等级
            }
            return AjaxUtil.list2Json(list);
        } catch (Exception e) {
            logger.error("获取组成员失败");
            return AjaxUtil.list2Json(list);
        }
    }

    @ExceptionMessageAnnotation(errorMessage = "Git 提交历史")
    @RequestMapping("projectCommits.ajax")
    @ResponseBody
    public net.sf.json.JSONObject projectDetailAjax(UrmUserHolder userHolder, @RequestParam(value = "startTime", defaultValue = "") String startTime, @RequestParam(value = "endTime", defaultValue = "") String endTime, @RequestParam(value = "gitProjectId", defaultValue = "") Long gitProjectId, @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "20") Integer rows) throws Exception {
        SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        JDGitCommits jdGitCommits = new JDGitCommits();
        jdGitCommits.setPage(page);
        jdGitCommits.setGitProjectId(dataDevGitProject.getGitProjectId());
        jdGitCommits.setBranch(dataDevGitProject.getBranch());
        jdGitCommits.setPageSize(rows);
        if (StringUtils.isNotBlank(startTime)) {
            jdGitCommits.setSince(YYYYMMDDHHMMSS.parse(startTime));
        }
        if (StringUtils.isNotBlank(endTime)) {
            jdGitCommits.setUntil(YYYYMMDDHHMMSS.parse(endTime));
        }
        PageResultDTO pageResultDTO = jdGitCommits.pageCommits();
        List<DataDevScriptFileHis> hislist = (List<DataDevScriptFileHis>) pageResultDTO.getRows();
        urmUtil.covertUserErp2UserName(hislist, new ConvertErp2UserName<DataDevScriptFileHis>() {
            @Override
            public String getErp(DataDevScriptFileHis o) {
                return o.getCreator();
            }

            @Override
            public void setErpUserName(DataDevScriptFileHis o, String userNames) {
                if (StringUtils.isNotBlank(userNames)) {
                    o.setSysCreatorName(userNames + "(" + o.getCreator() + ")");
                } else {
                    o.setSysCreatorName(o.getCreator());
                }
            }
        });
        return AjaxUtil.gridJson(pageResultDTO);
    }

    @RequestMapping("useGitProject.html")
    public String useGitProject(UrmUserHolder userHolder, Model model) throws Exception {
        model.addAttribute("gitPrivateUser", SysOwner);
        return "scriptcenter/home/project/useGitProject";
    }

    @RequestMapping("useCodingProject.html")
    public String useCodingProject(UrmUserHolder userHolder, Model model) throws Exception {
        model.addAttribute("codingPrivateUser", SysOwnerCoding);
        return "scriptcenter/home/project/useCodingProject";
    }

    /**
     * @param projectGitPath 为用户粘贴过来的Git地址
     * @param urmUserHolder
     * @return
     */
    @ExceptionMessageAnnotation(errorMessage = "Git项目导入")
    @RequestMapping("getGitProjectByPath.ajax")
    @ResponseBody
    public JSONObject getGitProjectByPath(String projectGitPath, UrmUserHolder urmUserHolder, Integer gitOrCodingCode) {
        try {
            String proFilePath = getGitProPath(projectGitPath);
            JDGitProjects jdGitProjects = new JDGitProjects();
            jdGitProjects.setProjectPath(proFilePath);
            try {
                //从 git获取到某个 项目
                JDGitProjects jdGitProjectsRes = jdGitProjects.gitProjectDetailByPath(gitOrCodingCode);
                //判断数据库中有没有该项目，如果没有插入，如果有，更新
                if (jdGitProjectsRes != null) {
                    String visibility = jdGitProjectsRes.getVisibility();
                    if (!"private".equals(visibility)) {
                        return JSONObjectUtil.getFailResult("请将项目可见级别设置为Private", null);
                    }
                    Long gitProId = jdGitProjectsRes.getGitProjectId();
                    Long groupGitId = jdGitProjectsRes.getGroupId();
                    //到这里  说明bdp_ide已经具有权限了，接下来检查erp的权限，erp必须是这个项目的master或者owner
                    int authorFlag = 0;//erp是否拥有owner或者master权限
                    int bdpUserFlag = 0;
                    String erp = urmUserHolder.getErp();
                    List<DataDevGitProjectMember> memebers = jdGitProjectsRes.listProjectMembers();
                    for (DataDevGitProjectMember m : memebers) {
                        String name = m.getGitMemberUserName();
                        if (StringUtils.equals(name, erp)) {
                            authorFlag = m.getAccessLevel();
                        }
                        if (gitOrCodingCode == DataDevGitOrCodingEnum.CODING.tocode()) {
                            if (StringUtils.equals(name, SysOwnerCoding)) {
                                bdpUserFlag = m.getAccessLevel();
                            }
                        } else {
                            if (StringUtils.equals(name, SysOwner)) {
                                bdpUserFlag = m.getAccessLevel();
                            }
                        }
                    }
                    if (bdpUserFlag == 0) {
                        String SysOwnerErp = SysOwner;
                        if (gitOrCodingCode == DataDevGitOrCodingEnum.CODING.tocode()) {
                            SysOwnerErp = SysOwnerCoding;
                        }
                        DataDevGitGroupMember dataDevGitGroupMember = dataDevGitGroupMemberService.getDataDevGitGroupMebByErp(SysOwnerErp, groupGitId, gitOrCodingCode);
//                        logger.error("=========项目组成员："+JSONObject.toJSONString(dataDevGitGroupMember));
                        if (dataDevGitGroupMember != null) {
                            bdpUserFlag = dataDevGitGroupMember.getAccessLevel();
                        }
                    }
                    if (bdpUserFlag < 40) {
                        if (gitOrCodingCode == DataDevGitOrCodingEnum.CODING.tocode()) {
                            logger.error("虚拟账号:" + SysOwnerCoding + "在该项目中不是owner或者master或是不在该项目中：" + projectGitPath);
                            return JSONObjectUtil.getFailResult(SysOwnerCoding + "账号角色需设置为Master（项目成员）", null);
                        } else {
                            logger.error("虚拟账号:" + SysOwner + "在该项目中不是owner或者master或是不在该项目中：" + projectGitPath);
                            return JSONObjectUtil.getFailResult(SysOwner + "账号角色需设置为Master（项目成员）或Owner（组成员）", null);
                        }
                    }
                    if (authorFlag == 0) {//项目成员不包括这个erp
                        DataDevGitGroupMember dataDevGitGroupMember = dataDevGitGroupMemberService.getDataDevGitGroupMebByErp(erp, groupGitId, gitOrCodingCode);
                        if (dataDevGitGroupMember != null) {
                            authorFlag = dataDevGitGroupMember.getAccessLevel();
                        }
                    }


                    if (authorFlag < 30) {//这个说明该erp不在项目或组中或者不是owner或者master
                        logger.error("erp:" + erp + "在该项目中不是owner或者master或是不在该项目中：" + projectGitPath);
                        return JSONObjectUtil.getFailResult("当前用户无此项目权限", null);

                    }
                    DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProId);
                    if (dataDevGitProject == null) {
                        dataDevGitProject = jdGitProjectsRes.convertDataDevDomain(jdGitProjectsRes);
                        dataDevGitProjectService.insertOneProject(dataDevGitProject);
                        dataDevGitProjectSharedGroupService.handGitProjectSharedGroup(dataDevGitProject);
                        try {
                            initSingleGitProject.initSingleGitProject(dataDevGitProject);
                            String[] filePathArray = StringUtils.split(proFilePath, "/");
                            String groupName = null;
                            if (filePathArray != null && filePathArray.length == 2) {
                                groupName = filePathArray[0];
                            }
                            if (StringUtils.isNotBlank(groupName)) {
                                JDGitGroups jdGitGroups = new JDGitGroups();
                                jdGitGroups.setPath(groupName);
                                JDGitGroups jdGitGroupRes = jdGitGroups.getGroupByPath();
                                if (jdGitGroupRes != null) {
//                                    List<DataDevGitGroupMember> groupMembers = jdGitGroupRes.listGroupMembers();
                                    DataDevGitGroup dataDevGitGroup = dataDevGitGroupService.getGroupByGroupId(jdGitGroupRes.getJdGroupId());
                                    if (dataDevGitGroup == null) {
                                        dataDevGitGroup = jdGitGroups.convertDataDevDomain(jdGitGroupRes);
                                        dataDevGitGroupService.insertOneGroup(dataDevGitGroup);
                                    }
                                    Long groupId = jdGitGroupRes.getJdGroupId();
                                    initSingleGitGroup.initSingleGitGroup(groupId);

                                }

                            }

                            return JSONObjectUtil.getSuccessResult("添加项目成功", dataDevGitProject);

                        } catch (Exception e) {
                            return JSONObjectUtil.getFailResult("项目已存在，但是同步组和成员的过程中出错，请联系管理员", null);
                        }
                    } else {
                        return JSONObjectUtil.getSuccessResult("项目已存在", dataDevGitProject);
                    }


                } else {
                    return JSONObjectUtil.getFailResult("项目不存在或未添加" + SysOwner + "账号", null);

                }
            } catch (Exception e) {
                logger.error("根据URL：" + projectGitPath + "获取git项目时发生异常，", e);
                return JSONObjectUtil.getFailResult("获取项目时发生异常,请联系管理员", null);

            }

        } catch (Exception e) {
            logger.error("根据URL：" + projectGitPath + "获取git项目时发生异常，" + e.getMessage());
            return JSONObjectUtil.getFailResult("获取项目时发生异常,请联系管理员", null);

        }

    }

    @ExceptionMessageAnnotation(errorMessage = "验证Git项目地址")
    @RequestMapping("testGitConnect.ajax")
    @ResponseBody
    public JSONObject testGitConnect(String projectGitPath, UrmUserHolder urmUserHolder, Integer gitOrCodingCode) {

        try {
            String proFilePath = getGitProPath(projectGitPath);
            JDGitProjects jdGitProjects = new JDGitProjects();
            jdGitProjects.setProjectPath(proFilePath);
            Integer code = jdGitProjects.testGitConnect(gitOrCodingCode);
            if (code.equals(200)) {
                return JSONObjectUtil.getSuccessResult("测试有效性成功", code);

            } else if (code.equals(401)) {
                return JSONObjectUtil.getSuccessResult("无权限访问或未添加" + SysOwner + "账号", code);
//            } else if (code.equals(404)) {
//                return JSONObjectUtil.getSuccessResult("项目不存在或未添加" + SysOwner + "账号", code);
            } else {
                return JSONObjectUtil.getSuccessResult("项目不存在或未添加" + SysOwner + "账号", code);

            }


        } catch (Exception e) {
            logger.error("testGitConnect:异常：", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), null);

        }
    }

    private String getGitProPath(String projectGitPath) throws Exception {
        if (StringUtils.isNotBlank(projectGitPath)) {
            String[] pathArray = null;
            if (projectGitPath.startsWith("git")) {
                pathArray = StringUtils.split(projectGitPath, ":");
                if (pathArray != null && pathArray.length == 2) {
                    String filePath = pathArray[1];
                    if (StringUtils.isNotBlank(filePath) && filePath.endsWith(".git")) {
                        String filepathNoGit = StringUtils.substringBeforeLast(filePath, ".");
                        return filepathNoGit;
                    } else {
                        throw new Exception("请校验地址正确性");

                    }
                } else {
                    throw new Exception("请校验地址正确性");

                }
            } else if (projectGitPath.startsWith("http")) {
                pathArray = StringUtils.split(projectGitPath, "/");
                if (pathArray != null && pathArray.length > 2) {
                    int length = pathArray.length;
                    String groupName = pathArray[length - 2];
                    String proNameGit = pathArray[length - 1];
                    if (StringUtils.isNotBlank(groupName) && StringUtils.isNotBlank(projectGitPath)) {
                        if (StringUtils.contains(proNameGit, ".git")) {
                            String proName = StringUtils.substringBeforeLast(proNameGit, ".");
                            if (StringUtils.isNotBlank(proName)) {
                                String proFilePath = groupName + "/" + proName;
                                return proFilePath;


                            } else {
                                throw new Exception("请校验地址正确性");

                            }
                        } else {
                            throw new Exception("请校验地址正确性");

                        }

                    } else {
                        throw new Exception("请校验地址正确性");

                    }


                } else {
                    throw new Exception("请校验地址正确性");

                }

            } else {
                throw new Exception("请校验地址正确性");

            }


        } else {
            throw new Exception("git项目地址不能为空");

        }
    }

    @RequestMapping("uploadFileHistory.html")
    public String uploadFileHistory(Long gitProjectId, Model model) {
        model.addAttribute("gitProjectId", gitProjectId);
        return "scriptcenter/art/uploadFileHistory";
    }

    @ExceptionMessageAnnotation(errorMessage = "获取文件上传历史")
    @RequestMapping("uploadHis.ajax")
    @ResponseBody
    public net.sf.json.JSONObject uploadHis(DataDevScriptUpLoad dataDevScriptUpLoad, @RequestParam(value = "startTime") String startTime, @RequestParam(value = "endTime") String endTime, @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        Page<DataDevScriptUpLoad> dataDevScriptUpLoadList = null;
        try {
            Pageable pageable = new PageRequest(page - 1, rows);
            dataDevScriptUpLoadList = dataDevScriptUploadService.findByPage(dataDevScriptUpLoad, startTime, endTime, pageable);

            //获取中文名
            urmUtil.covertUserErp2UserName(dataDevScriptUpLoadList, new ConvertErp2UserName<DataDevScriptUpLoad>() {
                @Override
                public String getErp(DataDevScriptUpLoad o) {
                    return o.getCreator();
                }

                @Override
                public void setErpUserName(DataDevScriptUpLoad o, String userNames) {
                    if (StringUtils.isNotBlank(userNames)) {
                        o.setCreator(userNames + "(" + o.getCreator() + ")");
                    } else {
                        o.setCreator(o.getCreator());
                    }
                }
            });

            return AjaxUtil.jqGridJson(dataDevScriptUpLoadList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return AjaxUtil.jqGridJson(dataDevScriptUpLoadList);
        }
    }

    @RequestMapping("fileDetail.html")
    public String fileDetail(Long scriptUpLoadId, Model model) {
        model.addAttribute("scriptUpLoadId", scriptUpLoadId);
        return "scriptcenter/art/fileDetail";
    }

    @ExceptionMessageAnnotation(errorMessage = "上传历史详情")
    @RequestMapping("fileDetail.ajax")
    @ResponseBody
    public net.sf.json.JSONObject fileDetail(Long scriptUpLoadId, @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        Page<DataDevScriptFile> dataDevScriptFileList = null;
        try {
            Pageable pageable = new PageRequest(page - 1, rows);
            dataDevScriptFileList = dataDevScriptFileService.findByScriptUpLoadId(scriptUpLoadId, pageable);
            return AjaxUtil.jqGridJson(dataDevScriptFileList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return AjaxUtil.jqGridJson(dataDevScriptFileList);
        }
    }

    /**
     * @param gits 3:gitId  2:gitGroupId  以,分割
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "根据id获取项目或者项目组")
    @RequestMapping("list_gits_by_ids.ajax")
    @ResponseBody
    public net.sf.json.JSONObject listGitsByIds(String gits) throws Exception {
        String[] idStrArray = gits.split(",");
        List<DataDevGitDto> dtos = new ArrayList<DataDevGitDto>();
        for (String idStr : idStrArray) {
            try {
                if (StringUtils.isBlank(idStr)) {
                    continue;
                }
                String[] categoryAndIds = idStr.split(":");
                if (categoryAndIds == null || categoryAndIds.length != 2) {
                    continue;
                }
                if (categoryAndIds[0].equals("3")) {
                    Long id = Long.parseLong(categoryAndIds[1]);
                    DataDevGitProject project = dataDevGitProjectService.getGitProjectBy(id);
                    DataDevGitDto gitDto = new DataDevGitDto(project);
                    dtos.add(gitDto);
                } else if (categoryAndIds[0].equals("2")) {
                    Long id = Long.parseLong(categoryAndIds[1]);
                    DataDevGitGroup group = dataDevGitGroupService.getGroupByGroupId(id);
                    DataDevGitDto gitDto = new DataDevGitDto(group);
                    dtos.add(gitDto);
                }
            } catch (Exception e) {
                logger.error("===========" + e.getMessage());
            }
        }
        return AjaxUtil.list2Json(dtos);
    }

    /**
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "根据关键字获取项目或者项目组")
    @RequestMapping("list_gits.ajax")
    @ResponseBody
    public net.sf.json.JSONObject listGits(UrmUserHolder urmUserHolder, String keyword) throws Exception {
        List<DataDevGitDto> dtos = new ArrayList<DataDevGitDto>();
        try {
            List<DataDevGitDto> devGitDtos = dataDevGitProjectService.getGitAndGroupByKeyWord(urmUserHolder.getErp(), keyword);
            dtos.addAll(devGitDtos);
        } catch (Exception e) {
            logger.error("根据关键字获取项目或者项目组：" + e.getMessage());
        }
        return AjaxUtil.list2Json(dtos);
    }
}
