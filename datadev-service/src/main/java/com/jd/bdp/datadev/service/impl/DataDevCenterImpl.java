package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.HttpRequestDTO;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.dao.DataDevScriptPublishDao;
import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import com.jd.bdp.datadev.enums.DataDevScriptPublishStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.service.DataDevCenterService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.bdp.datadev.util.MD5Util;
import com.jd.bdp.domain.authorityCenter.MarketInfoDto;
import com.jd.bdp.domain.dto.JsfAuthDTO;
import com.jd.bdp.domain.dto.JsfResultDTO;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import com.jd.bdp.domain.urm.right.ApiResultDTO;
import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.domain.bo.ProjectAccountRelBO;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.bdp.planing.domain.bo.ProjectQueueRelBO;
import com.jd.bdp.planing.domain.bo.ProjectResGroupBO;
import com.jd.bdp.rc.api.ApiResult;
import com.jd.bdp.rc.api.ReleaseInterface;
import com.jd.bdp.rc.api.domains.ReleaseInfoFromDevDto;
import com.jd.bdp.rc.domain.bo.ReleaseRecordBo;
import com.jd.jbdp.release.api.ReleaseSubmitInterface;
import com.jd.jbdp.release.model.po.ReleaseWfInfo;
import com.jd.jbdp.release.model.vo.SubmitInfoVo;
import com.jd.jbdp.release.model.vo.SubmitObj;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
public class DataDevCenterImpl implements DataDevCenterService {
    private static final Logger logger = Logger.getLogger(DataDevCenterImpl.class);


    @Autowired
    private DataDevScriptPublishDao publishDao;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private AppGroupUtil appGroupUtil;
    @Value("${buffalo.domain.name}")
    private String buffaloDomain;
    @Value("${smp.app.id}")
    private String smpAppId;
    @Value("${smp.app.token}")
    private String smpAppToken;

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;


    @Autowired
    private DataDevScriptFileService fileService;

    @Autowired
    private ReleaseInterface releaseInterface;

    @Autowired
    private ProjectInterface projectInterface;

    @Autowired
    private ReleaseSubmitInterface releaseSubmitInterface;

    @Autowired
    private BuffaloComponent buffaloComponent;

    public static final String SPLIT = "#-#";

    /**
     * 提交到发布中心
     */
    private void uplineReleaseCenter(JSONObject resObject, String erp, DataDevScriptFile file) {
        ReleaseInfoFromDevDto releaseInfoFromDevDto = new ReleaseInfoFromDevDto();
        releaseInfoFromDevDto.setObjType("script");
        releaseInfoFromDevDto.setProjectId(file.getApplicationId());
        releaseInfoFromDevDto.setReleaseErp(erp);
        releaseInfoFromDevDto.setDesc(file.getVerDescription());


        JSONObject releaseInfo = new JSONObject();
        releaseInfo.put("projectId", file.getApplicationId());
        releaseInfo.put("projectName", file.getApplicationName());
        releaseInfo.put("scriptName", file.getName());
        //releaseInfo.put("scriptId",resObject.getLongValue("fileId"));
        //releaseInfo.put("scriptVersion",resObject.getString("version"));
        releaseInfo.put("fileId", resObject.getLongValue("fileId"));
        releaseInfo.put("version", resObject.getString("version"));
        releaseInfo.put("scriptUrl", "/scriptcenter/api/downloadScriptNoAuth.ajax?id=" + file.getId() + "&version=" + file.getVersion());
        releaseInfo.put("scriptDesc", file.getVerDescription());

        releaseInfoFromDevDto.setShowInfo(releaseInfo);
        releaseInfoFromDevDto.setReleaseInfo(releaseInfo);
        releaseInfoFromDevDto.setObjKey(file.getGitProjectId() + SPLIT + file.getGitProjectFilePath());
        releaseInfoFromDevDto.setReleaseName(file.getName() + "(" + file.getVersion() + ")");
        ApiResult<ReleaseRecordBo> releaseRecordBoApiResult = releaseInterface.submitRelease(appId, appToken, System.currentTimeMillis(), releaseInfoFromDevDto);

        if (!releaseRecordBoApiResult.isSuccess()) {
            throw new RuntimeException("提交发布中心失败," + releaseRecordBoApiResult.getMessage());
        }
    }


    /**
     * 提交发布中心New
     *
     * @param resObject
     * @param erp
     * @param file
     */
    private ReleaseWfInfo uplineReleaseCenterNew(JSONObject resObject, String erp, DataDevScriptFile file, JSONObject preOnline) throws Exception {

        String fileName = file.getName();
        Long projectSpaceId = file.getApplicationId();

        JSONObject currentOnlineInfo = buffaloComponent.getCurrentOnlineInfo(fileName, projectSpaceId);

        String fileType = DataDevScriptTypeEnum.enumValueOf(file.getType()).toName();
        SubmitObj tempSubmitObj = new SubmitObj();
        JSONObject currentTempOnlineInfo = new JSONObject();
        if (currentOnlineInfo != null) {
            currentTempOnlineInfo.put("scriptId", currentOnlineInfo.getLong("fileId"));
            currentTempOnlineInfo.put("fileSize", currentOnlineInfo.getLong("fileSize"));
            currentTempOnlineInfo.put("scriptName", currentOnlineInfo.getString("fileName"));
            currentTempOnlineInfo.put("version", currentOnlineInfo.getString("curVersion"));
            currentTempOnlineInfo.put("md5Code", currentOnlineInfo.getString("md5Code"));
            currentTempOnlineInfo.put("fileType", currentOnlineInfo.getString("fileType"));
        }
        JSONObject devInfo = new JSONObject();
        devInfo.put("scriptId", file.getId());
        devInfo.put("fileSize", file.getSize());
        devInfo.put("scriptName", file.getName());
        devInfo.put("version", file.getVersion());
        devInfo.put("fileType", fileType);
        devInfo.put("preOnline", preOnline);

        String devObjKey = file.getId().toString();
        tempSubmitObj.setOperatorType(currentOnlineInfo != null ? "更新" : "新建");
        tempSubmitObj.setDevInfo(devInfo.toJSONString());
        tempSubmitObj.setDevObjKey(devObjKey);
        // onlineObjKey非首次发布时必传
        tempSubmitObj.setOnlineObjKey(currentOnlineInfo != null ? currentOnlineInfo.getLong("fileId").toString() : null);
        tempSubmitObj.setOnlineInfo(currentTempOnlineInfo.toJSONString());
        tempSubmitObj.setObjType("script");
        tempSubmitObj.setProjectId(projectSpaceId);
        String commitMsg = file.getVerDescription();
        SubmitInfoVo submitInfoVo = new SubmitInfoVo();
        submitInfoVo.setProjectId(projectSpaceId);
        submitInfoVo.setDesc(commitMsg);
        submitInfoVo.setSubmitErp(erp);
        submitInfoVo.setSubmitObj(Arrays.asList(tempSubmitObj));


        JsfResultDTO submit = releaseSubmitInterface.submit(JsfAuthDTO.newInstance(), submitInfoVo);//JSONObject.parseObject("{\"code\":0,\"obj\":{\"submitId\":374,\"wfId\":53893}}", JsfResultDTO.class);
        logger.info("submit result:" + JSONObject.toJSONString(submit));
        // return submit != null && submit.getCode() == 0;
        if (submit.getCode() != 0) {
            throw new RuntimeException(StringUtils.isBlank(submit.getMessage()) ? "发布失败!" : submit.getMessage());
        }
        return (ReleaseWfInfo) submit.getObj();


    }


    @Override
    public DataDevScriptFilePublish upLineScript(DataDevScriptFile file, String erp, DataDevScriptFilePublish oldPublish, Integer runType) throws Exception {

        String userToken = urmUtil.UserTokenByErp(null, erp);
        DataDevScriptFilePublish insertPublish = new DataDevScriptFilePublish();
        insertPublish.setGitProjectId(file.getGitProjectId());
        insertPublish.setGitProjectFilePath(file.getGitProjectFilePath());
        insertPublish.setStatus(DataDevScriptPublishStatusEnum.Publishing.toCode());
        insertPublish.setApplicationId(file.getApplicationId());
        insertPublish.setVersion(file.getVersion());
        insertPublish.setComment(file.getVerDescription());
        insertPublish.setPublisher(erp);
        insertPublish.setPublishSys("buffalo");
        insertPublish.setCreator(erp);
        insertPublish.setMender(erp);
        insertPublish.setCheckMd5(file.getFileMd5());
        insertPublish.setRunType(runType);
        publishDao.insert(insertPublish);
        try {
            file.setBytes(fileService.getScriptBytes(file.getGitProjectId(), file.getGitProjectFilePath(), file.getVersion(), erp));
            String fileName = file.getName();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(Charset.forName("UTF-8"));// 设置请求的编码格式
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
            ContentType contentType = ContentType.create("text/plain", Charset.forName("utf-8"));
            builder.addBinaryBody("file", file.getBytes(), ContentType.MULTIPART_FORM_DATA, fileName);
            Date date = new Date();
            builder.addTextBody("appId", smpAppId, contentType);
            builder.addTextBody("userToken", userToken, contentType);
            builder.addTextBody("time", String.valueOf(date.getTime()), contentType);
            builder.addTextBody("sign", MD5Util.getMD5Str(smpAppToken + userToken + date.getTime()), contentType);
            String url = buffaloDomain + "/api/v2/buffalo4/script/syncScriptFileFromDevForPublish";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jsdAppgroupId", file.getApplicationId());
            if (oldPublish == null) {
                jsonObject.put("managers", erp);
            }
            if (file.getType() == DataDevScriptTypeEnum.Zip.toCode()) {
                jsonObject.put("scriptPackagePath", file.getStartShellPath());
            }
            jsonObject.put("description", file.getDescription());
            jsonObject.put("verDescription", file.getVerDescription());
            jsonObject.put("calEngine", "other");
            jsonObject.put("dataDevProjectId", file.getGitProjectId());
            jsonObject.put("dataDevProjectFilePath", file.getGitProjectFilePath());
            jsonObject.put("dataDevScriptVersion", file.getVersion());
            if (file.getType().equals(DataDevScriptTypeEnum.SQL.toCode())) {
                jsonObject.put("model", "006");
                String args = file.getArgs();
                JSONArray params = new JSONArray();
                if (StringUtils.isNotBlank(args) && !args.trim().equalsIgnoreCase("{}")) {
                    JSONObject argsObject = JSONObject.parseObject(args.trim());
                    for (String key : argsObject.keySet()) {
                        JSONObject param = new JSONObject();
                        param.put("name", key);
                        param.put("value", argsObject.getString(key));
                        params.add(param);
                    }
                }
                jsonObject.put("scriptInfoParams", params);

            }
            builder.addTextBody("data", jsonObject.toJSONString(), contentType);

            HttpPost postMethod = new HttpPost(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            postMethod.setEntity(builder.build());
            String response = HttpUtil.exctueRequest(postMethod);

            JSONObject resObject = JSONObject.parseObject(response);


            logger.info("==================syncScriptFileFromDevForPublish" + JSONObject.toJSONString(resObject));
            if (resObject != null && resObject.getInteger("code") != null && resObject.getInteger("code") == 0) {
                JSONObject obj = resObject.getJSONObject("obj");
                String bdpRequestId = obj.get("bpmRequestId") != null ? obj.get("bpmRequestId").toString() : null;
                String fileId = obj.get("fileId") != null ? obj.get("fileId").toString() : null;
                insertPublish.setBuffaloScriptId(Long.valueOf(fileId));
                insertPublish.setRequestId(bdpRequestId != null ? Long.valueOf(bdpRequestId) : null);
                insertPublish.setStatus(StringUtils.isNotBlank(bdpRequestId) ? DataDevScriptPublishStatusEnum.Auditing.toCode() : DataDevScriptPublishStatusEnum.Success.toCode());
                publishDao.updateStatus(insertPublish);
                uplineReleaseCenter(obj, erp, file);
                return insertPublish;
            } else if (resObject.getInteger("code") == 206) {
                throw new RuntimeException("调度系统在该项目空间下已存在未同步同名文件");
            } else {
                throw new RuntimeException(resObject.getString("message"));
            }
        } catch (Exception e) {
            logger.error("buffalo=====================" + e.getMessage());
            insertPublish.setStatus(DataDevScriptPublishStatusEnum.Failure.toCode());
            publishDao.updateStatus(insertPublish);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ReleaseWfInfo upLineScriptNew(DataDevScriptFile file, String erp) throws Exception {

        String userToken = urmUtil.UserTokenByErp(null, erp);
        DataDevScriptFilePublish insertPublish = new DataDevScriptFilePublish();
        insertPublish.setGitProjectId(file.getGitProjectId());
        insertPublish.setGitProjectFilePath(file.getGitProjectFilePath());
        insertPublish.setStatus(DataDevScriptPublishStatusEnum.Publishing.toCode());
        insertPublish.setApplicationId(file.getApplicationId());
        insertPublish.setVersion(file.getVersion());
        insertPublish.setComment(file.getVerDescription());
        insertPublish.setPublisher(erp);
        insertPublish.setPublishSys("buffalo");
        insertPublish.setCreator(erp);
        insertPublish.setMender(erp);
        insertPublish.setCheckMd5(file.getFileMd5());
        publishDao.insert(insertPublish);

        try {
            /**
             * private Integer projectId;//项目空间ID
             * private String model;//001
             * private String description;//文件描述
             * private String scriptName;
             * private String scriptPackagePath;
             * private String managers;
             * // 数据开发id
             * private Integer dataDevScriptId;
             * private String dataDevScriptVersion;
             * private String erp;//操作人ERP
             *
             */
            //file.setBytes("sss".getBytes());
            file.setBytes(fileService.getScriptBytes(file.getGitProjectId(), file.getGitProjectFilePath(), file.getVersion(), erp));
            String fileName = file.getName();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(Charset.forName("UTF-8"));// 设置请求的编码格式
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
            ContentType contentType = ContentType.create("text/plain", Charset.forName("utf-8"));
            builder.addBinaryBody("file", file.getBytes(), ContentType.MULTIPART_FORM_DATA, fileName);
            Date date = new Date();
            builder.addTextBody("appId", smpAppId, contentType);
            builder.addTextBody("userToken", userToken, contentType);
            builder.addTextBody("time", String.valueOf(date.getTime()), contentType);
            builder.addTextBody("sign", MD5Util.getMD5Str(smpAppToken + userToken + date.getTime()), contentType);
            String url = buffaloDomain + "/api/v2/buffalo4/script/syncScriptFileFromDevForPublish";
            url = buffaloDomain + "/api/v2/buffalo4/script/uploadFileForDevCenter";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("erp", erp);
            if (file.getType() == DataDevScriptTypeEnum.Zip.toCode()) {
                jsonObject.put("scriptPackagePath", file.getStartShellPath());
            }
            jsonObject.put("projectId", file.getApplicationId());
            jsonObject.put("scriptName", file.getName());
            jsonObject.put("description", file.getDescription());
            jsonObject.put("dataDevScriptId", file.getId());
            jsonObject.put("dataDevScriptVersion", file.getVersion());
            jsonObject.put("model", "001");



            builder.addTextBody("data", jsonObject.toJSONString(), contentType);

            HttpPost postMethod = new HttpPost(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            postMethod.setEntity(builder.build());
            String response = HttpUtil.exctueRequest(postMethod);

            JSONObject resObject = JSONObject.parseObject(response);


            logger.info("==================syncScriptFileFromDevForPublish" + JSONObject.toJSONString(resObject));
            if (resObject != null && resObject.getInteger("code") != null && resObject.getInteger("code") == 0) {
                JSONObject obj = resObject.getJSONObject("obj");
                String bdpRequestId = obj.get("bpmRequestId") != null ? obj.get("bpmRequestId").toString() : null;
                String fileId = obj.get("fileId") != null ? obj.get("fileId").toString() : null;
                insertPublish.setBuffaloScriptId(Long.valueOf(fileId));
                insertPublish.setRequestId(bdpRequestId != null ? Long.valueOf(bdpRequestId) : null);
                insertPublish.setStatus(StringUtils.isNotBlank(bdpRequestId) ? DataDevScriptPublishStatusEnum.Auditing.toCode() : DataDevScriptPublishStatusEnum.Success.toCode());

                /**
                 *                     "scriptId":43040,
                 *                     "fileSize":"3",
                 *                     "scriptName":"helloworld.py",
                 *                     "version":"20210604224229",
                 *                     "md5Code":"9f6e6800cfae7749eb6c486619254b9c",
                 *                     "fileType":"py"
                 */
                JSONObject preOnline = new JSONObject();

                preOnline.put("scriptId", obj.getLong("fileId"));
                preOnline.put("fileSize", obj.getLong("fileSize"));
                preOnline.put("scriptName", obj.getString("fileName"));
                preOnline.put("version", obj.getString("version"));
                preOnline.put("md5Code", obj.getString("md5Code"));
                preOnline.put("fileType", obj.getString("fileType"));


                publishDao.updateStatus(insertPublish);
                return uplineReleaseCenterNew(obj, erp, file, preOnline);
            } else if (resObject.getInteger("code") == 206) {
                throw new RuntimeException("调度系统在该项目空间下已存在未同步同名文件");
            } else {
                throw new RuntimeException(resObject.getString("message"));
            }
        } catch (Exception e) {
            logger.error("buffalo=====================" + e.getMessage(), e);
            insertPublish.setStatus(DataDevScriptPublishStatusEnum.Failure.toCode());
            publishDao.updateStatus(insertPublish);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public ApiResultDTO getGrantAuthorityProductionAccountInMarketForBuffalo(String marketUser, String erp, Long spaceProjectId) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();

        List<ClusterHadoopAccount> accountList = new ArrayList<ClusterHadoopAccount>();
        ProjectAccountRelBO projectBO = new ProjectAccountRelBO();
        projectBO.setErp(erp);
        projectBO.setId(spaceProjectId);
        projectBO.setMarketUser(marketUser);
        // projectBO.setEnvType(1);

        com.jd.bdp.planing.api.model.ApiResult<ProjectAccountRelBO> accountApiResult = projectInterface.getGrantAuthorityProductionAccount(appId, appToken, System.currentTimeMillis(), projectBO);
        logger.info("====getGrantAuthorityProductionAccountInMarketForBuffalo=========spaceApiResultresult" + JSONObject.toJSONString(accountApiResult));

        if (accountApiResult.isSuccess()) {
            for (ProjectAccountRelBO bo : accountApiResult.getList()) {
                ClusterHadoopAccount temp = new ClusterHadoopAccount();
                temp.setId(bo.getId());
                temp.setCode(bo.getAccount());
                temp.setName(bo.getAccountName());
                temp.setClusterCode(bo.getClusterCode());
                accountList.add(temp);
            }
        }

        apiResultDTO.setList(accountList);
        apiResultDTO.setSuccess(accountApiResult.getSuccess());
        apiResultDTO.setCode(accountApiResult.getCode());
        apiResultDTO.setMessage(accountApiResult.getMessage());

        return apiResultDTO;
    }

    @Override
    public ApiResultDTO getGrantAuthorityQueueOneAccountInMarketForBuffalo(String marketUser, String accountCode, String erp, Long spaceProjectId) {

        ApiResultDTO apiResultDTO = new ApiResultDTO();

        List<ClusterHadoopQueue> queueList = new ArrayList<ClusterHadoopQueue>();
        ProjectQueueRelBO projectQueueRelBO = new ProjectQueueRelBO();
        projectQueueRelBO.setMarketUser(marketUser);
        projectQueueRelBO.setProductionAccountCode(accountCode);
        projectQueueRelBO.setProjectId(spaceProjectId);
        projectQueueRelBO.setErp(erp);
        //projectQueueRelBO.setEnvType(1);

        com.jd.bdp.planing.api.model.ApiResult<ProjectQueueRelBO> queueApiResult = projectInterface.getGrantAuthorityQueue(appId, appToken, System.currentTimeMillis(), projectQueueRelBO);
        logger.info("====queueresult=" + JSONObject.toJSONString(queueApiResult));

        if (queueApiResult.isSuccess()) {
            for (ProjectQueueRelBO bo : queueApiResult.getList()) {
                ClusterHadoopQueue temp = new ClusterHadoopQueue();
                temp.setId(bo.getId());
                temp.setClusterId(bo.getClusterId() != null ? Long.parseLong(bo.getClusterId()) : -1);
                //temp.setMarketId(bo.getMarketId() != null ? Long.parseLong(bo.getMarketId()) : -1);
                temp.setQueueCode(bo.getQueueCode());
                temp.setQueueName(bo.getQueueName());
                temp.setEngineTypes(bo.getEngineTypes() == null ? "jd-hive,spark,presto" : bo.getEngineTypes());
                queueList.add(temp);
            }
        }

        apiResultDTO.setList(queueList);
        apiResultDTO.setSuccess(queueApiResult.getSuccess());
        apiResultDTO.setCode(queueApiResult.getCode());
        apiResultDTO.setMessage(queueApiResult.getMessage());

        return apiResultDTO;

    }




    @Override
    public ApiResultDTO getGrantAuthorityMarketForBuffalo(String erp, Long spaceProjectId) {
        ApiResultDTO apiResultDTO = new ApiResultDTO();
        List<MarketInfoDto> list = new ArrayList<MarketInfoDto>();




        ProjectBO projectBO = new ProjectBO();
        projectBO.setErp(erp);
        projectBO.setId(spaceProjectId);


        com.jd.bdp.planing.api.model.ApiResult<ProjectAccountRelBO> spaceApiResult = projectInterface.getGrantAuthorityMarket(appId, appToken, System.currentTimeMillis(), projectBO);
        logger.info("==========getGrantAuthorityMarketForBuffalo===spaceApiResult" + JSONObject.toJSONString(spaceApiResult));
        if (spaceApiResult.getSuccess()) {
            for (ProjectAccountRelBO tempBo : spaceApiResult.getList()) {
                MarketInfoDto marketInfoDto = new MarketInfoDto();
                marketInfoDto.setClusterCode(tempBo.getClusterCode());
                marketInfoDto.setMarketCode(tempBo.getMarketUser());
                marketInfoDto.setMarketUser(tempBo.getMarketUser());
                marketInfoDto.setMarketName(tempBo.getMarketName());
                marketInfoDto.setMarketMagagers(tempBo.getMarketManagers());
                list.add(marketInfoDto);
            }
        }

        apiResultDTO.setList(list);
        apiResultDTO.setSuccess(spaceApiResult.getSuccess());
        apiResultDTO.setCode(spaceApiResult.getCode());
        apiResultDTO.setMessage(spaceApiResult.getMessage());
        return apiResultDTO;
    }
}
