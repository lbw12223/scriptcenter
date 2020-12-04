package com.jd.bdp.datadev.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.ImportScriptManager;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.dao.DataDevScriptFileHisDao;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptDirService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.web.worker.InitGitGroup;
import com.jd.bdp.datadev.web.worker.InitGitProject;
import com.jd.bdp.datadev.web.worker.InitSingleGitGroup;
import com.jd.bdp.datadev.web.worker.InitSingleGitProject;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.gd.util.StringUtils;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
@Controller
@RequestMapping("/scriptcenter/redis/")
public class RedisTestController {

    @Autowired
    private InitGitProject initGitProject;

    @Autowired
    private InitSingleGitProject initSingleGitProject;

    @Autowired
    private InitGitGroup initGitGroup;

    @Autowired
    private InitSingleGitGroup initSingleGitGroup;

    @Autowired
    private Cluster jimClient;

    @Autowired
    private DataDevScriptDirService dataDevScriptDirService;

    @Autowired
    private DataDevScriptFileService dataDevScriptFileService;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;


    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao ;

    @Autowired
    private DataDevScriptFileHisDao dataDevScriptFileHisDao ;




    @RequestMapping("fixVersion.ajax")
    @ResponseBody
    public String fixVersionAjax() {
       List<DataDevGitProject> gitProjectList =  dataDevGitProjectService.listAll();
       if(gitProjectList != null && gitProjectList.size() > 0){
           for(DataDevGitProject dataDevGitProject : gitProjectList){
               if(dataDevGitProject.getDeleted() == 1){
                   continue;
               }
               Long gitProjectId = dataDevGitProject.getGitProjectId();
               List<DataDevScriptFile> files = dataDevScriptFileDao.selectAll(gitProjectId);
               for(DataDevScriptFile file : files){
                   if(StringUtils.isBlank(file.getVersion())){
                       dataDevScriptFileDao.fixBugUpdateVersion(file.getId());
                   }

               }
           }
       }
       return "success";
    }


    @RequestMapping("setRedis.ajax")
    @ResponseBody
    public String setRedis() {
        jimClient.set("key", "value");
        return SpringPropertiesUtils.getPropertiesValue("${datadev.env}");
    }

    @RequestMapping("getRedis.ajax")
    @ResponseBody
    public String getRedis() {
        String value = jimClient.get("key");
        return jimClient.get("key");
    }


    @RequestMapping("deletedRedis.ajax")
    @ResponseBody
    public String deletedRedis() {
        jimClient.del("key");
        return "";
    }


    @RequestMapping("refreshAllGitProject.ajax")
    @ResponseBody
    public String refreshAllGitProject() {
        initGitProject.refresh();
        return "success";
    }

    @RequestMapping("doInitAllGitProject.ajax")
    @ResponseBody
    public String doInitAllGitProject() {
        initSingleGitProject.initGitProjects();
        return "success";
    }

    @RequestMapping("doInitSingleGitProject.ajax")
    @ResponseBody
    public String doInitSingleGitProject(Long gitProjectId) {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        initSingleGitProject.initSingleGitProject(dataDevGitProject);
        return "success";
    }


    @RequestMapping("doInitSingleGitProjectMember.ajax")
    @ResponseBody
    public String doInitSingleGitProjectMember(Long gitProjectId) {
        try {
            initSingleGitProject.initProjectMember(dataDevGitProjectService.getGitProjectBy(gitProjectId));
            return "success";
        } catch (Exception e) {
            return "e" + e.getMessage();
        }

    }


    @RequestMapping("refreshAllGitGroup.ajax")
    @ResponseBody
    public String initGitGroup() {
        initGitGroup.initGroup();
        return "success";
    }

    @RequestMapping("doInitAllGitGroup.ajax")
    @ResponseBody
    public String initSingleGitGroup() {
        initSingleGitGroup.initGitGroups();
        return "success";
    }
//
//    @RequestMapping("refreshDir.ajax")
//    @ResponseBody
//    public JSONObject refreshDir(Long gitProjectId, String queryPath) throws Exception {
//        return JSONObjectUtil.getSuccessResult(dataDevScriptDirService.refreshDir(gitProjectId, queryPath));
//    }
//
//
//   @RequestMapping("getScriptContent.ajax")
//   @ResponseBody
//    public String getScriptContent(Long gitProjectId, String queryPath, UrmUserHolder urmUserHolder) throws Exception {
//         return dataDevScriptFileService.getScriptContent(gitProjectId, queryPath, urmUserHolder.getErp());
//    }
//
//
//    @RequestMapping("addScriptFile.ajax")
//    @ResponseBody
//    public JSONObject addScriptFile(UrmUserHolder urmUserHolder, Long gitProjectId, String filePath, String content) throws Exception {
//        DataDevScriptFile dataDevScriptFile = new DataDevScriptFile();
//        dataDevScriptFile.setGitProjectId(gitProjectId);
//        dataDevScriptFile.setCreator(urmUserHolder.getErp());
//        dataDevScriptFile.setGitProjectFilePath(filePath);
//        dataDevScriptFile.setBytes(content.getBytes());
//        dataDevScriptFile.setIsShow(1);
//        dataDevScriptFile.setType(DataDevScriptTypeEnum.SQL.toCode());
//        ZtreeNode ztreeNode = dataDevScriptFileService.createNewFile(gitProjectId, filePath, DataDevScriptTypeEnum.SQL.toCode(), urmUserHolder.getErp(), 0, content.getBytes());
//        return JSONObjectUtil.getSuccessResult(ztreeNode);
//    }
//
//    @RequestMapping("tryUpdateFile.ajax")
//    @ResponseBody
//    public String tryUpdateFile(Long gitProjectId, String queryPath, UrmUserHolder urmUserHolder, String content) throws Exception {
//        return dataDevScriptFileService.tryUpdateFile(gitProjectId, queryPath, urmUserHolder.getErp(), content.getBytes(), "1001", false).toString();
//    }
//
//
//    @RequestMapping("addGitDir.ajax")
//    @ResponseBody
//    public JSONObject addGitDir(UrmUserHolder urmUserHolder, Long gitProjectId, String dirPath) throws Exception {
//        return JSONObjectUtil.getSuccessResult(dataDevScriptDirService.createScriptDir(gitProjectId, dirPath, urmUserHolder.getErp()));
//    }
//
//    @RequestMapping("deleteFile.ajax")
//    @ResponseBody
//    public String deleteFile(UrmUserHolder urmUserHolder, Long gitProjectId, String filePath) throws Exception {
//        dataDevScriptFileService.deleteScriptFile(gitProjectId, filePath, urmUserHolder.getErp());
//        return "success";
//    }
//
//    @RequestMapping("deleteDir.ajax")
//    @ResponseBody
//    public String deleteDir(UrmUserHolder urmUserHolder, Long gitProjectId, String dirPath) throws Exception {
//        dataDevScriptDirService.deleteScriptDir(gitProjectId, dirPath, urmUserHolder.getErp());
//        return "success";
//    }
//
//    @RequestMapping("renameFile.ajax")
//    @ResponseBody
//    public DataDevScriptFile renameFileAjax(UrmUserHolder urmUserHolder, Long gitProjectId, String filePath, String newFileName) throws Exception {
//        return dataDevScriptFileService.renameScriptFile(gitProjectId, filePath, newFileName, urmUserHolder.getErp());
//
//    }
//
//    @RequestMapping("moveDir.ajax")
//    @ResponseBody
//    public JSONObject moveDirAjax(UrmUserHolder urmUserHolder, Long gitProjectId, String filePath, String newDirPath, String newName) throws Exception {
//        return JSONObjectUtil.getSuccessResult(dataDevScriptFileService.moveScriptFile(gitProjectId, filePath, newDirPath, newName, "", urmUserHolder.getErp()));
//    }
//
//    @RequestMapping("scriptHistory.ajax")
//    @ResponseBody
//    public net.sf.json.JSONObject scriptHistoryAjax(UrmUserHolder urmUserHolder, Long gitProjectId, String filePath) throws Exception {
//        PageResultDTO pageResultDTO = new PageResultDTO();
//     /*    DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
//        dataDevScriptFileHis.setGitProjectFilePath(filePath);
//        dataDevScriptFileHis.setGitProjectId(gitProjectId);
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2018, 5, 5, 9, 35, 44);
//        dataDevScriptFileHis.setStartTime(calendar.getTime());
//        // dataDevScriptFileHis.setEndTime(new Date());*/
//        pageResultDTO = dataDevScriptFileService.listScriptHis(gitProjectId, "bdp_ide_branch", filePath, null, null, 1, 20);
//        return AjaxUtil.gridJson(pageResultDTO);
//
//        //    return JSONObjectUtil.getSuccessResult(dataDevScriptFileService.moveScriptFile(gitProjectId, filePath, newDirPath, urmUserHolder.getErp()));
//    }
//
//    @RequestMapping("updateScriptFile.ajax")
//    @ResponseBody
//    public String updateScriptFile(UrmUserHolder urmUserHolder, Long gitProjectId, String filePath) throws Exception {
//        DataDevScriptFile dataDevScriptFile = dataDevScriptFileService.getScriptFile(gitProjectId, filePath);
//        dataDevScriptFile.setGitProjectId(gitProjectId);
//        dataDevScriptFile.setGitProjectFilePath(filePath);
//        dataDevScriptFile.setMender(urmUserHolder.getErp());
//        dataDevScriptFile.setBytes("hello java".getBytes());
//     /*    DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
//        dataDevScriptFileHis.setGitProjectFilePath(filePath);
//        dataDevScriptFileHis.setGitProjectId(gitProjectId);
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2018, 5, 5, 9, 35, 44);
//        dataDevScriptFileHis.setStartTime(calendar.getTime());
//        // dataDevScriptFileHis.setEndTime(new Date());*/
//        HoldDoubleValue<Boolean, JDGitFiles> result = dataDevScriptFileService.updateScriptFileContent(dataDevScriptFile, gitProjectId, false);
//        return result.toString();
//    }


    @RequestMapping({"aceDiff.html"})
    public String demo(HttpServletRequest request, Model model, UrmUserHolder urmUserHolder) {
        return "/scriptcenter/editor/ace_diff";
    }

    @RequestMapping({"aceDiffIndex.html"})
    public String aceDiffIndex(HttpServletRequest request, Model model, UrmUserHolder urmUserHolder) {
        return "/scriptcenter/editor/ace_diff_index";
    }

//    @RequestMapping("pullDir.ajax")
//    @ResponseBody
//    public JSONObject pullDir(UrmUserHolder urmUserHolder, Long gitProjectId, String gitProjectDirPath) throws Exception {
//        return JSONObjectUtil.getSuccessResult(dataDevScriptFileService.pullDir(gitProjectId, gitProjectDirPath, urmUserHolder.getErp()));
//    }


    @Autowired
    private ImportScriptManager importScriptManager;


    @RequestMapping("syncMember.ajax")
    @ResponseBody
    public JSONObject syncMember(UrmUserHolder urmUserHolder, Long gitProjectId, Long appGroupId) throws Exception {
        importScriptManager.syncMember(gitProjectId, appGroupId, true);
        return JSONObjectUtil.getSuccessResult("success");
    }
}
