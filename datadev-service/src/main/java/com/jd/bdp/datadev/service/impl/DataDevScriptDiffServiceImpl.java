package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
//import com.jd.bdp.buffalo.devcenter.HiveTask;
//import com.jd.bdp.buffalo.devcenter.ParseException;
//import com.jd.bdp.buffalo.devcenter.SqlTableParser;
import com.github.pagehelper.PageInfo;
import com.jd.bdp.datadev.component.BuffaloComponent;
import com.jd.bdp.datadev.domain.diff.DiffInfoVo;
import com.jd.bdp.datadev.domain.diff.DiffPairVo;
import com.jd.bdp.datadev.domain.diff.ReleaseCompareVo;
import com.jd.bdp.datadev.service.DataDevScriptDiffService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.domain.dto.JsfAuthDTO;
import com.jd.bdp.domain.dto.JsfResultDTO;
import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.rc.domain.enums.ReleaseTypeEnum;
import com.jd.jbdp.release.api.ReleaseSubmitInterface;
import com.jd.jbdp.release.model.vo.ReleaseObjRecordVo;
import com.jd.jbdp.release.model.vo.SubmitInfoVo;
import com.jd.jbdp.release.model.vo.SubmitObj;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
//import scala.Tuple2;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

@Service
public class DataDevScriptDiffServiceImpl implements DataDevScriptDiffService {

    private static final Logger logger = Logger.getLogger(DataDevScriptDiffServiceImpl.class);

    @Autowired
    private DataDevScriptFileService scriptFileService;

    @Autowired
    private ProjectInterface projectInterface;

    @Autowired
    private BuffaloComponent buffaloComponent;

    @Autowired
    private ReleaseSubmitInterface releaseSubmitInterface;

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;

    @Override
    public ReleaseCompareVo compareInfo(Long projectSpaceId, Long scriptId, String scriptName) throws Exception {
        // 开发脚本详情
        JSONObject devDetail = null;
        try {
            devDetail = scriptFileService.getScriptDetail(scriptId, null);
        } catch (Exception e) {
            logger.error("获取开发脚本失败", e);
        }
        // 上线脚本详情
        JSONObject onlineDetail = null;
        try {
            onlineDetail = buffaloComponent.scriptGetFileContent(scriptName, projectSpaceId);
        } catch (Exception e) {
            logger.error("获取生产脚本失败", e);
        }

        DiffInfoVo currentLay = new DiffInfoVo();
        DiffInfoVo remoteLay = new DiffInfoVo();
        DiffPairVo diffPairVo = new DiffPairVo();

        //若脚本为zip，jar包，则对比zip，jar包的基本文件信息，否则比较内容
        //zip包名称
        //文件大小
        //md5Code
        //文件类型
        //  有文件就比较文件，没有，就比较基本信息
        //注意开发环境和  线上环境获取到的脚本的信息字段不同，解析时候注意。。
        if(devDetail != null ){
            currentLay.setName(devDetail.getString("name"));
            currentLay.setVersion(devDetail.getString("version"));
            currentLay.setFileType(devDetail.getString("type"));

            String fileContent = devDetail.getString("content");
            if(StringUtils.isNotBlank(fileContent)){
                currentLay.setContent(fileContent);
            }else {
                JSONObject devContent = new JSONObject();
                devContent.put("name",devDetail.getString("name"));
                devContent.put("size",devDetail.getString("size"));
                devContent.put("md5",devDetail.getString("md5"));
                devContent.put("type",devDetail.getString("type"));

                currentLay.setContent(devContent);
            }
        }
        if(onlineDetail != null){
            remoteLay.setName(onlineDetail.getString("scriptName"));
            remoteLay.setVersion(onlineDetail.getString("version"));
            remoteLay.setFileType(onlineDetail.getString("scriptType"));

            String fileContentOnline = onlineDetail.getString("fileContent");
            if(StringUtils.isNotBlank(fileContentOnline)){
                remoteLay.setContent(fileContentOnline);
            }else {
                JSONObject onlineContent = new JSONObject();
                onlineContent.put("name",onlineDetail.getString("scriptName"));
                onlineContent.put("size",onlineDetail.getString("fileSize"));
                onlineContent.put("md5",onlineDetail.getString("md5Code"));
                onlineContent.put("type",onlineDetail.getString("scriptType"));

                remoteLay.setContent(onlineContent);
            }
        }
        diffPairVo.setCurrentInfo(currentLay);
        diffPairVo.setRemoteInfo(remoteLay);
        ReleaseCompareVo releaseCompareVo = new ReleaseCompareVo();
        releaseCompareVo.setCompareType(ReleaseTypeEnum.SCRIPT.getValue());
        releaseCompareVo.setScriptPair(diffPairVo);
        return releaseCompareVo;
    }

    @Override
    public JSONObject getTaskList(Long projectSpaceId, String scriptName, String operator) throws Exception {
        return buffaloComponent.getTaskList(projectSpaceId, scriptName, operator);
    }

    @Override
    public Integer check(Long projectSpaceId, String devContent, ReleaseCompareVo releaseCompareVo) throws Exception {
        //TODO 强校验、弱校验 依赖调度接口
        return 100;
//        DiffInfoVo currentInfo = releaseCompareVo.getScriptPair().getCurrentInfo();
//        // 不比较直接通过
//        if (currentInfo == null || (!isPython(currentInfo.getFileType()) && !isShell(currentInfo.getFileType()))) {
//            return 100;
//        }
//        DiffInfoVo remoteInfo = releaseCompareVo.getScriptPair().getRemoteInfo();

//        Object currentContent = currentInfo.getContent();
//        Object remoteContent = remoteInfo.getContent();
//        String currentSql = new HiveTask("python").getSql(new ByteArrayInputStream(JSONObject.toJSONString(currentContent).getBytes()));
//        Tuple2<List<String>, List<String>> currentTables = SqlTableParser.parse(currentSql);
//
//        String remoteSql = new HiveTask("python").getSql(new ByteArrayInputStream(JSONObject.toJSONString(remoteContent).getBytes()));
//        Tuple2<List<String>, List<String>> remoteTables = SqlTableParser.parse(remoteSql);
//
//        List<String> cInput = currentTables._1();
//        List<String> rInput = remoteTables._1();
//        List<String> cOutPut = currentTables._2();
//        List<String> rOutPut = remoteTables._2();
//        // 强校验不通过
//        if (!cInput.equals(rInput) || !rOutPut.equals(cOutPut)) {
//            return 101;
//        }
//
//        // 根据 projectId 获取 集市code，用来查询表的结构
//        String parentMarketCode = null;
//        ProjectBO projectBO = new ProjectBO();
//        projectBO.setId(projectSpaceId);
//        ApiResult<ProjectBO> apiResult = projectInterface.getProjectById(appId, appToken, System.currentTimeMillis(), projectBO);
//        if (apiResult.getCode().equals(0) && apiResult.getObj() != null) {
////            parentMarketCode = apiResult.getObj().getParentMarketCode();
//        }
//        // 弱校验：校验两边的表结构是否一致
//        if (StringUtils.isNotBlank(parentMarketCode)) {
//
//        }
    }

    /**
     * 检测脚本是否正在发布
     * @param submitInfoVo
     */
    private void checkIsInRelease(SubmitInfoVo submitInfoVo){
        JsfResultDTO haveReleaseing = releaseSubmitInterface.isHaveReleaseing(JsfAuthDTO.newInstance(), submitInfoVo);
        if(haveReleaseing.getCode().equals(-2)) {
            throw new RuntimeException("当前脚本正在审批中，请在发布中心取消后，重新申请!");
        }
        if(haveReleaseing.getCode().equals(-1)){
            throw new RuntimeException("检测脚本是否可以发布"+haveReleaseing.getMessage());
        }
    }


    @Override
    public boolean submit2RC(Long projectSpaceId, String desc, String operator, SubmitObj submitObj) throws Exception {
        SubmitInfoVo submitInfoVo = new SubmitInfoVo();
        submitInfoVo.setProjectId(projectSpaceId);
        submitInfoVo.setDesc(desc);
        submitInfoVo.setSubmitErp(operator);
        submitInfoVo.setSubmitObj(Arrays.asList(submitObj));
        // 检测脚本是否正在发布
        checkIsInRelease(submitInfoVo);

        JsfResultDTO submit = releaseSubmitInterface.submit(JsfAuthDTO.newInstance(), submitInfoVo);
        logger.info("submit result:" + JSONObject.toJSONString(submit));
        return submit != null && submit.getCode() == 0;
    }

    @Override
    public PageInfo<ReleaseObjRecordVo> releaseRecord(Long projectId, String scriptName, Integer page, Integer size) throws Exception {
        SubmitInfoVo submitInfoVo = new SubmitInfoVo();
        if (projectId != null && projectId > 0) {
            submitInfoVo.setProjectId(projectId);
        }
        submitInfoVo.setDevObjKey(scriptName);
        submitInfoVo.setObjType("script");
        submitInfoVo.setPageNum(page);
        submitInfoVo.setPageSize(size);
        JsfResultDTO<PageInfo> pageInfoJsfResultDTO = releaseSubmitInterface.releaseRecord(JsfAuthDTO.newInstance(), submitInfoVo);
        logger.info("releaseRecord pageInfoJsfResultDTO:" + JSONObject.toJSONString(pageInfoJsfResultDTO));
        if (pageInfoJsfResultDTO != null && pageInfoJsfResultDTO.getCode() == 0) {
            return pageInfoJsfResultDTO.getObj();
        }
        throw new Exception("releaseSubmitInterface.releaseRecord failed");

    }

    private boolean isPython(String fileType) {
        return "py".equalsIgnoreCase(fileType) || "python".equalsIgnoreCase(fileType);
    }

    private boolean isShell(String fileType) {
        return "sh".equalsIgnoreCase(fileType) || "shell".equalsIgnoreCase(fileType);
    }
}
