package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;

@Controller
@RequestMapping("/scriptcenter/scriptFile/")
public class ScriptInfoController {
    private static final Logger logger = Logger.getLogger(ScriptInfoController.class);

    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private DataDevScriptRunDetailService runDetailService;
    @Autowired
    private DataDevScriptPublishService publishService;
    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;
    @Autowired
    private DataDevScriptTemplateService templateService;
    @Autowired
    private UrmUtil urmUtil;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @ExceptionMessageAnnotation(errorMessage = "保存脚本描述信息")
    @RequestMapping("/saveDescription.ajax")
    @ResponseBody
    public JSONObject saveDescription(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String description) throws Exception {
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId,gitProjectFilePath);
        if(file !=null){
            DataDevScriptFile params = new DataDevScriptFile();
            params.setDescription(description);
            fileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, params);
            DataDevScriptTemplate template = templateService.getTemplateByFileId(file.getId());
            if(template != null){
                DataDevScriptTemplate updateTemplate = new DataDevScriptTemplate();
                updateTemplate.setId(template.getId());
                updateTemplate.setDesc(description);
                templateService.updateTemplate(updateTemplate.getId(),updateTemplate);
            }

        }
        return JSONObjectUtil.getSuccessResult("success");
    }

    @ExceptionMessageAnnotation(errorMessage = "保存脚本参数")
    @RequestMapping("/saveArgs.ajax")
    @ResponseBody
    public JSONObject saveArgs(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath, String args , String startShellPath,String description , String cgroupArgs) throws Exception {
        DataDevScriptFile params = new DataDevScriptFile();
        params.setArgs(args);
        params.setCgroupArgs(cgroupArgs);
        params.setStartShellPath(startShellPath);
        if(StringUtils.isNotBlank(description)){
            params.setDescription(description);
        }
        fileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, params);
        return JSONObjectUtil.getSuccessResult("success");
    }

    @ExceptionMessageAnnotation(errorMessage = "获取Git Project详细信息")
    @RequestMapping("/getInfo.ajax")
    @ResponseBody
    public JSONObject getInfo(UrmUserHolder userHolder, Long gitProjectId, String gitProjectFilePath) throws Exception {
        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        if (file != null) {
            file.setCreatorName(urmUtil.getNameByErp(file.getCreator()));
            String time = sdf.format(file.getCreated());
            file.setCreatedStr(time);
            file.setApplicationName(dataDevGitProject.getGitProjectPath());
            DataDevScriptTemplate template = templateService.getTemplateByFileId(file.getId());
            if(template != null){
                file.setTemplateDesc(template.getDesc());
                file.setTemplateName(template.getName());
            }
            return JSONObjectUtil.getSuccessResult(file);

        } else {
            return JSONObjectUtil.getFailResult("脚本不存在", file);
        }
    }


    @RequestMapping("/getRunHistory.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getRunHistory(DataDevScriptRunDetail runDetail, @RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "rows", defaultValue = "10") int rows) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        Pageable pageable = new PageRequest(page - 1, rows);
        try {
            pageResultDTO = runDetailService.list4page(runDetail, pageable);
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

    @RequestMapping("/getOperateHistory.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getOperateHistory(DataDevScriptFileHis fileHis,
                                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                                    @RequestParam(value = "rows", defaultValue = "10") int rows) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        Pageable pageable = new PageRequest(page - 1, rows);
        try {
            pageResultDTO = fileService.list4page(fileHis, pageable);
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

    @RequestMapping("/getOnlineHistory.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getOnlineHistory(DataDevScriptFilePublish publish, @RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "rows", defaultValue = "10") int rows) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        Pageable pageable = new PageRequest(page, rows);
        try {
            pageResultDTO = publishService.list4page(publish, pageable);
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



}
