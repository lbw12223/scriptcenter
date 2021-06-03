package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptTemplate;
import com.jd.bdp.datadev.domain.DataDevScriptTemplateShare;
import com.jd.bdp.datadev.domain.ZtreeNode;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptTemplateService;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/scriptcenter/scriptTemplate/")
public class ScriptTemplateController {
    private static final Logger logger = Logger.getLogger(ScriptTemplateController.class);

    @Autowired
    private DataDevScriptTemplateService templateService;

    @Autowired
    private DataDevScriptFileService fileService;


    @RequestMapping("/templates.html")
    public String templates(UrmUserHolder userHolder, Model model, Integer scriptType, @RequestParam(value = "pythonType", defaultValue = "0") Integer pythonType) throws Exception {
        model.addAttribute("scriptType", scriptType);
        model.addAttribute("pythonType", pythonType);
        return "/scriptcenter/home/script_template";
    }

    @RequestMapping("/script_manage_new_file.html")
    public String scriptManageNewFile(UrmUserHolder userHolder, Model model, Integer scriptType, @RequestParam(value = "pythonType", defaultValue = "0") Integer pythonType) throws Exception {
        model.addAttribute("scriptType", scriptType);
        model.addAttribute("pythonType", pythonType);
        return "scriptcenter/devcenter/left/scripts/script_manage_new_file";
    }

    /**
     * @param userHolder
     * @param scriptType 1 sql 2 shell 3 python
     * @param pythonType 1 python2 2python3
     * @param key
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取脚本模板")
    @RequestMapping("/getTemplates.ajax")
    @ResponseBody
    public JSONObject getTemplates(UrmUserHolder userHolder, Integer scriptType,
                                   @RequestParam(value = "pythonType", defaultValue = "0") Integer pythonType, String key,
                                   @RequestParam(value = "gitProjectId",defaultValue = "0") Integer gitProjectId) throws Exception {
        List<DataDevScriptTemplate> dataDevScriptTemplates = templateService.searchScriptTemplate(scriptType, pythonType, key, userHolder.getErp());
        return JSONObjectUtil.getSuccessResult(dataDevScriptTemplates);
    }

    @RequestMapping("/initSystemTemplate.ajax")
    @ResponseBody
    public JSONObject initSystemTemplate() throws Exception {
        templateService.initScriptTemplate();
        return JSONObjectUtil.getSuccessResult(null);
    }

    /**
     * @param templateId
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取分享信息")
    @RequestMapping("getShareInfos.ajax")
    @ResponseBody
    public JSONObject getShareInfos(UrmUserHolder urmUserHolder, Long templateId) throws Exception {
        JSONObject jsonObject = new JSONObject();
        try {
            DataDevScriptTemplate template = templateService.getScriptTemplateById(templateId);
            if (template == null) {
                throw new RuntimeException("模板不存在");
            }
            if (template.getTemplateType()!=null && template.getTemplateType() ==1 && !template.getIncharge().equals(urmUserHolder.getErp())) {
                throw new RuntimeException("无权限获取模板信息");
            }
            List<DataDevScriptTemplateShare> infos = templateService.getSharesInfos(templateId);
            StringBuilder erpsBuilder = new StringBuilder();
            boolean gitShares = false;
            for (DataDevScriptTemplateShare share : infos) {
                if (share.getShareType() == 1) {
                    erpsBuilder.append(share.getShareTarget()).append(",");
                } else if (share.getShareType() == 2) {
                    gitShares = true;
                }
            }
            jsonObject.put("erps", erpsBuilder.length() > 0 ? erpsBuilder.substring(0, erpsBuilder.length() - 1) : "");
            jsonObject.put("gitShares", gitShares);
            jsonObject.put("name", template.getStatus()!=null && template.getStatus()==1?"":template.getName());
            jsonObject.put("desc", template.getDesc());
            jsonObject.put("scriptType", template.getScriptType());
            jsonObject.put("pythonType", template.getPythonType());
            if(template.getScriptFileId()!=null){
                DataDevScriptFile file = fileService.findById(template.getScriptFileId());
                if(file !=null){
                    jsonObject.put("fileId", file.getId());
                    jsonObject.put("gitProjectId", file.getGitProjectId());
                    jsonObject.put("gitProjectFilePath", file.getGitProjectFilePath());
                }
            }
            return JSONObjectUtil.getSuccessResult(jsonObject);
        } catch (Exception e) {
            logger.error("获取分享信息==========" + e.getMessage());
            return JSONObjectUtil.getSuccessResult(jsonObject);
        }
    }

    @RequestMapping("saveTemplate.ajax")
    @ExceptionMessageAnnotation(errorMessage = "保存模板报错")
    @ResponseBody
    public JSONObject saveTemplate(DataDevScriptTemplate template, UrmUserHolder holder) throws Exception {

        DataDevScriptTemplate returnTemplate = templateService.saveScriptTemplate(template, holder.getErp());
        return JSONObjectUtil.getSuccessResult(returnTemplate);
    }


    @RequestMapping("shareTemplate.ajax")
    @ExceptionMessageAnnotation(errorMessage = "分享模板报错")
    @ResponseBody
    public JSONObject shareTemplate(DataDevScriptTemplate template, UrmUserHolder holder) throws Exception {
        templateService.shareTemplate(template, holder.getErp());
        return JSONObjectUtil.getSuccessResult(template);
    }

    @RequestMapping("checkTemplateNameExist.ajax")
    @ExceptionMessageAnnotation(errorMessage = "查看同名模板")
    @ResponseBody
    public JSONObject checkTemplateNameExist(String templateName, Long templateId) throws Exception {
        boolean exist = templateService.existTemplateName(templateId, templateName);
        return JSONObjectUtil.getSuccessResult(exist ? 1 : 0);
    }

    @ResponseBody
    @ExceptionMessageAnnotation(errorMessage = "删除模板")
    @RequestMapping("deleteTemplate.ajax")
    public JSONObject deleteTemplate(Long templateId,UrmUserHolder urmUserHolder)throws Exception{
        try {
            DataDevScriptTemplate template=templateService.deleteTemplate(urmUserHolder.getErp(),templateId);
            return JSONObjectUtil.getSuccessResult("删除成功",template);
        }catch (Exception e){
            return JSONObjectUtil.getSuccessResult("删除失败："+e.getMessage(),null);
        }
    }

    @ResponseBody
    @ExceptionMessageAnnotation(errorMessage = "置顶模板")
    @RequestMapping("topTemplate.ajax")
    public JSONObject topTemplate(Long templateId,UrmUserHolder urmUserHolder,
                                  @RequestParam(value = "toTop",defaultValue = "true") boolean toTop)throws Exception{
        try {
            DataDevScriptTemplate template=templateService.topTemplate(urmUserHolder.getErp(),templateId,toTop);
            return JSONObjectUtil.getSuccessResult("置顶成功",template);
        }catch (Exception e){
            return JSONObjectUtil.getSuccessResult("置顶失败："+e.getMessage(),null);
        }
    }

}
