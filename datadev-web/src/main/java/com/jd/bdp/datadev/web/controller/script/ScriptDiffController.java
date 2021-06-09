package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.ImportScriptManager;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.domain.DataDevApplication;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import com.jd.bdp.datadev.domain.diff.ReleaseCompareVo;
import com.jd.bdp.datadev.jdgit.GitHttpUtil;
import com.jd.bdp.datadev.service.DataDevCenterService;
import com.jd.bdp.datadev.service.DataDevScriptDiffService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.impl.DataDevCenterImpl;
import com.jd.bdp.domain.dto.JsfAuthDTO;
import com.jd.bdp.domain.dto.JsfResultDTO;
import com.jd.bdp.rc.api.ApiResult;
import com.jd.bdp.rc.api.domains.ReleaseInfoFromDevDto;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.jbdp.release.api.ReleaseSubmitInterface;
import com.jd.jbdp.release.model.po.ReleaseObjInfo;
import com.jd.jbdp.release.model.vo.ReleaseObjRecordVo;
import com.jd.jbdp.release.model.vo.SubmitObj;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private DataDevScriptFileService fileService;

    @Autowired
    private ReleaseSubmitInterface releaseSubmitInterface;

    @Autowired
    private ImportScriptManager importScriptManager;

    @Autowired
    private DataDevCenterService dataDevCenterService ;

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
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
     * @return
     */
    @RequestMapping("/submit.ajax")
    @ResponseBody
    public JSONObject submit(UrmUserHolder userHolder, Long projectSpaceId, String commitMsg,Long scriptFileId ) throws Exception {

        try {
            String operator = userHolder.getErp();
            preSumit(projectSpaceId,scriptFileId,commitMsg,operator);
            return JSONObjectUtil.getSuccessResult(true);
        } catch (Exception e) {
            logger.error("scriptCompare.ajax failed: ", e);
            return JSONObjectUtil.getFailResult("提交发布失败!" + e.getMessage(), false);
        }
    }


    @RequestMapping("/submit1.ajax")
    @ResponseBody
    public JSONObject submit1(UrmUserHolder userHolder) throws Exception {
        try {
            return JSONObjectUtil.getSuccessResult("提交成功", true);
        } catch (Exception e) {
            logger.error("scriptCompare.ajax failed: ", e);
            return JSONObjectUtil.getFailResult("提交发布失败!", false);
        }
    }




    /**
     * 1.检查是否可以发布（已经有在发布中到流程）
     * 2.强弱校验
     * 3.post git /
     * 4.提交数据到发布中心
     *
     * @param projectSpaceId
     * @param scriptFileId
     * @throws Exception
     */
    private void preSumit(Long projectSpaceId , Long scriptFileId , String commitMsg , String erp) throws Exception{
        DataDevScriptFile old = fileService.findById(scriptFileId);
        old.setVerDescription(commitMsg);
        old.setApplicationId(projectSpaceId);
        if (old == null) {
            throw new RuntimeException("脚本不存在");
        }
        if (org.apache.commons.lang.StringUtils.isBlank(old.getVersion())) {
            throw new RuntimeException("脚本版本号为空");
        }
        if (projectSpaceId == null || projectSpaceId  <= 0L) {
            throw new RuntimeException("项目空间为空");
        }
        Long gitProjectId = old.getGitProjectId();
        String gitProjectFilePath = old.getGitProjectFilePath();

        if(gitProjectId < GitHttpUtil._10YI){
            fileService.pushFileDirect(gitProjectId,gitProjectFilePath,commitMsg,erp);
        }
        checkIsInReleaseNew(old);

        //关联
        importScriptManager.callBackScript(old.getId(), null, old.getGitProjectId(), old.getGitProjectFilePath(), erp, old.getVersion());

        //提交发布中心
        dataDevCenterService.upLineScriptNew(old, erp);



    }

    private void checkIsInRelease(DataDevScriptFile file){

//        ReleaseInfoFromDevDto releaseInfoFromDevDto = new ReleaseInfoFromDevDto();
//        releaseInfoFromDevDto.setObjType("script");
//        releaseInfoFromDevDto.setObjKey(file.getGitProjectId() + DataDevCenterImpl.SPLIT+ file.getGitProjectFilePath());
//        ApiResult<Boolean> haveReleaseing = releaseSubmitInterface.isHaveReleaseing(null, null);
//
//        if(haveReleaseing.getCode().equals(-2)) {
//            throw new RuntimeException("当前脚本正在审批中，请在发布中心取消后，重新申请!");
//        }
//        if(haveReleaseing.getCode().equals(-1)){
//            throw new RuntimeException("检测脚本是否可以发布"+haveReleaseing.getMessage());
//        }

    }

    private void checkIsInReleaseNew(DataDevScriptFile file){
        String devObjKey = file.getId() + DataDevCenterImpl.SPLIT + file.getVersion() ;

        SubmitObj submitObj = new SubmitObj();
        submitObj.setDevObjKey(devObjKey);
        submitObj.setObjType("script");
        JsfResultDTO haveReleaseingByObjKey = releaseSubmitInterface.isHaveReleaseingByObjKey(JsfAuthDTO.newInstance(), submitObj);

        if(haveReleaseingByObjKey.getCode() != 0){
            throw new RuntimeException("发布校验失败!");
        }
        if(haveReleaseingByObjKey.getObj() != null){
            throw new RuntimeException(haveReleaseingByObjKey.getMessage());
        }

        System.out.println(haveReleaseingByObjKey);
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
                                    @RequestParam(value = "projectId", required = false) Long projectId,
                                    @RequestParam(value = "scriptName") String scriptName,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "rows", defaultValue = "10") Integer size) {
        String erp = userHolder.getErp();
        try {
            String SPLIT = "#-#" ;
            scriptName = "test.sh" + SPLIT + "1005";
            PageInfo<ReleaseObjRecordVo> releaseObjInfoPageInfo = dataDevScriptDiffService.releaseRecord(projectId, scriptName, page, size);
            return JSONObjectUtil.getSuccessResult(releaseObjInfoPageInfo);
        } catch (Exception e) {
            logger.error("releaseRecord.ajax failed: ", e);
            return JSONObjectUtil.getFailResult(e.getMessage(), false);
        }
    }


}
