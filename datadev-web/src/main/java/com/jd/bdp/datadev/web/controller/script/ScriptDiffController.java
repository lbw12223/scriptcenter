package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.domain.diff.ReleaseCompareVo;
import com.jd.bdp.datadev.service.DataDevScriptDiffService;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.jbdp.release.model.po.ReleaseObjInfo;
import com.jd.jbdp.release.model.vo.SubmitObj;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/scriptcenter/diff/")
public class ScriptDiffController {
    private static final Logger logger = Logger.getLogger(ScriptDiffController.class);

    @Autowired
    private DataDevScriptDiffService dataDevScriptDiffService;

    /**
     * 获取对比内容
     * @param userHolder
     * @param projectSpaceId
     * @param scriptId
     * @param scriptName
     * @return
     */
    @RequestMapping("/scriptCompare.ajax")
    @ResponseBody
    public JSONObject getFileContentFromBuffalo(UrmUserHolder userHolder, Long projectSpaceId, Long scriptId, String scriptName) {
        ReleaseCompareVo releaseCompareVo = null;
        try {
            releaseCompareVo = dataDevScriptDiffService.compareInfo(projectSpaceId, scriptId, scriptName);
            return JSONObjectUtil.getSuccessResult(releaseCompareVo);
        } catch (Exception e) {
            logger.error("scriptCompare.ajax failed: ", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), releaseCompareVo);
        }
    }

    /**
     * 获取脚本关联任务列表（调度接口不支持分页）
     * @param userHolder
     * @param projectSpaceId
     * @param scriptName
     * @return
     */
    @RequestMapping("/scriptTaskList.ajax")
    @ResponseBody
    public JSONObject scriptTaskList(UrmUserHolder userHolder, Long projectSpaceId, String scriptName) {
        String operator = userHolder.getErp();
        try {
            projectSpaceId = 10109L ;
            scriptName = "python3_demo.py" ;
            JSONObject result = dataDevScriptDiffService.getTaskList(projectSpaceId, scriptName, operator);
            Long totalCount = result.getLong("totalCount");
            Long totalL0 = result.getLong("totalL0");
            Long totalL1 = result.getLong("totalL1");

            JSONArray datas = result.getJSONArray("list");
            PageResultDTO pageResultDTO = new PageResultDTO();
            pageResultDTO.setLimit(1000);
            pageResultDTO.setCode(0);
            pageResultDTO.setPage(1);
            pageResultDTO.setRows(datas);
            pageResultDTO.setRecords(totalCount);
            pageResultDTO.setCode(0);
            return JSONObjectUtil.sucessGrid(pageResultDTO);
        } catch (Exception e) {
            logger.error("scriptCompare.ajax failed: ", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), null);
        }
    }

    /**
     * 校验开发脚本和生产脚本
     * @param userHolder
     * @param projectSpaceId
     * @param scriptId
     * @param scriptName
     * @return
     */
    @RequestMapping("/check.ajax")
    @ResponseBody
    public JSONObject check(UrmUserHolder userHolder, Long projectSpaceId, Long scriptId, String scriptName) {
        String operator = userHolder.getErp();
        try {
            ReleaseCompareVo releaseCompareVo = dataDevScriptDiffService.compareInfo(projectSpaceId, scriptId, scriptName);
            Integer checkCode = dataDevScriptDiffService.check(projectSpaceId, operator, releaseCompareVo);
            String msg = "校验失败";
            if (checkCode == 100) {
                msg = "校验通过";
            } else if (checkCode == 101) {
                msg = "强校验不通过";
            } else if (checkCode == 102) {
                msg = "弱校验不通过";
            }
            return JSONObjectUtil.getSuccessResult(msg, checkCode);
        } catch (Exception e) {
            logger.error("check.ajax failed: ", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), null);
        }
    }


    /**
     * 提交至发布中心
     * 【前置条件：校验成功】
     *
     * @param userHolder
     * @param projectSpaceId
     * @param desc
     * @param submitObj
     * @return
     */
    @RequestMapping("/submit.ajax")
    @ResponseBody
    public JSONObject submit(UrmUserHolder userHolder, Long projectSpaceId, String desc, @RequestBody SubmitObj submitObj) {
        String operator = userHolder.getErp();
//        submitObj = JSONObject.parseObject("{\n" +
//                "        \"devInfo\":{\n" +
//                "            \"scriptId\":74666,\n" +
//                "            \"fileSize\":\"1274\",\n" +
//                "            \"scriptName\":\"python3_demo.py\",\n" +
//                "            \"version\":\"1000\",\n" +
//                "            \"fileType\":\"py\"\n" +
//                "        },\n" +
//                "        \"devObjKey\":\"python3_demo.py\",\n" +
//                "        \"objType\":\"script\",\n" +
//                "        \"onlineInfo\":{\n" +
//                "            \"scriptId\":43020,\n" +
//                "            \"fileSize\":\"2914\",\n" +
//                "            \"scriptName\":\"python3_demo.py\",\n" +
//                "            \"version\":\"20210526115738\",\n" +
//                "            \"md5Code\":\"be701769805c1945adb816485555ec54\",\n" +
//                "            \"fileType\":\"py\"\n" +
//                "        },\n" +
//                "        \"onlineObjKey\":\"python3_demo.py\",\n" +
//                "        \"operatorType\":\"\"\n" +
//                "    }", SubmitObj.class);
        try {
            boolean success = dataDevScriptDiffService.submit2RC(projectSpaceId, desc, operator, submitObj);
            if (success) {
                return JSONObjectUtil.getSuccessResult("提交成功", true);
            }
            return JSONObjectUtil.getFailResult("提交失败", false);
        } catch (Exception e) {
            logger.error("scriptCompare.ajax failed: ", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), false);
        }
    }

    /**
     * 发布上线历史
     * @param userHolder
     * @param projectSpaceId
     * @param scriptName
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/releaseRecord.ajax")
    @ResponseBody
    public JSONObject releaseRecord(UrmUserHolder userHolder,
                                    Long projectSpaceId,
                                    String scriptName,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
        String erp = userHolder.getErp();
        try {
            PageInfo<ReleaseObjInfo> releaseObjInfoPageInfo = dataDevScriptDiffService.releaseRecord(projectSpaceId, scriptName, page, size);
            return JSONObjectUtil.getSuccessResult(releaseObjInfoPageInfo);
        } catch (Exception e) {
            logger.error("releaseRecord.ajax failed: ", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), false);
        }
    }


}
