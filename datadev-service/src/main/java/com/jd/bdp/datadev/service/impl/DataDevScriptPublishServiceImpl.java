package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.AppGroupUtil;
import com.jd.bdp.datadev.component.BuffaloComponent;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.dao.DataDevScriptPublishDao;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevRunTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptPublishStatusEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptFunService;
import com.jd.bdp.datadev.service.DataDevScriptPublishService;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.bdp.datadev.util.MD5Util;
import com.jd.bdp.rc.api.ApiResult;
import com.jd.bdp.rc.api.ReleaseInterface;
import com.jd.bdp.rc.api.domains.ReleaseInfoFromDevDto;
import com.jd.bdp.rc.domain.bo.ReleaseRecordBo;
import com.jd.bdp.rc.domain.common.PageResult;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataDevScriptPublishServiceImpl implements DataDevScriptPublishService {

    @Autowired
    private DataDevScriptPublishDao publishDao;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private AppGroupUtil appGroupUtil;
    private static final Logger logger = Logger.getLogger(DataDevScriptPublishServiceImpl.class);
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
    private ReleaseInterface releaseInterface ;

    @Autowired
    private BuffaloComponent buffaloComponent;

    @Override
    public List<DataDevScriptFilePublish> getPushList(Long gitProjectId, String gitProjectFilePath) throws Exception {
        return publishDao.getPublishList(gitProjectId, gitProjectFilePath);
    }

    @Override
    public DataDevScriptFilePublish getLastPublish(Long gitProjectId, String gitProjectFilePath, Long applicationId) throws Exception {
        return publishDao.getLastPublish(gitProjectId, gitProjectFilePath, applicationId);
    }

    @Override
    public List<DataDevScriptFilePublish> getDistinctPushList(Long gitProjectId, String gitProjectFilePath) throws Exception {
        return publishDao.getDistinctPublishList(gitProjectId, gitProjectFilePath);
    }

    /**
     *
     * 提交到发布中心
     */
    private void uplineReleaseCenter( JSONObject resObject  , String erp , DataDevScriptFile file){

        ReleaseInfoFromDevDto releaseInfoFromDevDto = new ReleaseInfoFromDevDto();
        releaseInfoFromDevDto.setObjType("script");
        releaseInfoFromDevDto.setProjectId(file.getApplicationId());
        releaseInfoFromDevDto.setReleaseErp(erp);
        releaseInfoFromDevDto.setDesc(file.getVerDescription());





        JSONObject releaseInfo = new JSONObject();
        releaseInfo.put("projectId",file.getApplicationId());
        releaseInfo.put("projectName",file.getApplicationName());
        releaseInfo.put("scriptName",file.getName());
        releaseInfo.put("scriptId",file.getId());
        releaseInfo.put("scriptVersion",file.getVersion());
        releaseInfo.put("scriptUrl","scriptUrl");
        releaseInfo.put("scriptDesc",file.getVerDescription());

        releaseInfoFromDevDto.setShowInfo(releaseInfo);
        releaseInfoFromDevDto.setReleaseInfo(releaseInfo);

        releaseInfoFromDevDto.setObjKey(file.getGitProjectId() + "_" + file.getGitProjectFilePath() + "_" + file.getVersion());
        releaseInfoFromDevDto.setReleaseName(erp+"_"+file.getGitProjectId() + "_" + file.getGitProjectFilePath() + "_" + file.getVersion());
        ApiResult<ReleaseRecordBo> releaseRecordBoApiResult = releaseInterface.submitRelease(appId, appToken, System.currentTimeMillis(), releaseInfoFromDevDto);

        if(!releaseRecordBoApiResult.isSuccess()){
            throw new RuntimeException("提交发布中心失败," + releaseRecordBoApiResult.getMessage());
        }
    }

    /**
     * 添加发布中心接口
     * @param file
     * @param erp
     * @param oldPublish
     * @param runType
     * @return
     * @throws Exception
     */
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
            String url = buffaloDomain + "/api/v2/buffalo4/script/syncScriptFileFromDev";
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
                //sql类型上线 2018-01-28
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

            logger.error("=====================================upline data:" + jsonObject.toJSONString());
            logger.error("=====================================upline url:" + url);
            logger.error("=====================================upline size:" + file.getBytes().length);
            HttpPost postMethod = new HttpPost(url);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            postMethod.setEntity(builder.build());
            String response = HttpUtil.exctueRequest(postMethod);
            logger.error("=====================================upline response:" + response);

            JSONObject resObject = JSONObject.parseObject(response);


            if (resObject != null && resObject.getInteger("code") != null && resObject.getInteger("code") == 0) {
                JSONObject obj = resObject.getJSONObject("obj");
                String bdpRequestId = obj.get("bpmRequestId") != null ? obj.get("bpmRequestId").toString() : null;
                String fileId = obj.get("fileId") != null ? obj.get("fileId").toString() : null;
                insertPublish.setBuffaloScriptId(Long.valueOf(fileId));
//                insertPublish.setBuffaloScriptVersion(Long.valueOf(version));
                insertPublish.setRequestId(bdpRequestId != null ? Long.valueOf(bdpRequestId) : null);
                insertPublish.setStatus(StringUtils.isNotBlank(bdpRequestId) ? DataDevScriptPublishStatusEnum.Auditing.toCode() : DataDevScriptPublishStatusEnum.Success.toCode());
                publishDao.updateStatus(insertPublish);
                uplineReleaseCenter(resObject,erp,file);
                return insertPublish;
            } else if (resObject.getInteger("code") == 206) {
                logger.error("buffalo=====================" + resObject);
                throw new RuntimeException("调度系统在该项目空间下已存在未同步同名文件");
            } else {
                logger.error("buffalo=====================" + resObject);
                throw new RuntimeException(resObject.getString("message"));
            }
        } catch (Exception e) {
            logger.error("buffalo=====================" + e.getMessage());
            insertPublish.setStatus(DataDevScriptPublishStatusEnum.Failure.toCode());
            publishDao.updateStatus(insertPublish);
            throw new RuntimeException(e.getMessage());
        }

    }


    public PageResultDTO listBuffaloJobs(DataDevScriptFile file, DataDevScriptBuffaloJob queryJob, String userToken, int page, int rows) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        List<DataDevScriptBuffaloJob> resList = new ArrayList<DataDevScriptBuffaloJob>();
        Integer start = (page - 1) * rows;
        Integer end = page * rows;
        Integer count = 0;
//        String url = buffaloDomain + "/api/v2/buffalo4/script/getAllTaskList";
//        Date date = new Date();
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("appId", smpAppId);
//        map.put("userToken", userToken);
//        map.put("time", String.valueOf(date.getTime()));
//        map.put("sign", MD5Util.getMD5Str(smpAppToken + userToken + date.getTime()));
//        JSONObject dataParam = new JSONObject();
//        if (file.getGitProjectId() != null) {
//            dataParam.put("dataDevProjectId", file.getGitProjectId());
//        }
//        if (StringUtils.isNotBlank(file.getGitProjectFilePath())) {
//            dataParam.put("dataDevProjectFilePath", file.getGitProjectFilePath());
//            if(file.getGitProjectFilePath().endsWith(".sql")){
//                dataParam.put("model", "006");
//            }
//        }
//        if (queryJob.getAppGroupId() != null) {
//            dataParam.put("jsdAppgroupId", queryJob.getAppGroupId());
//        }
//        if (StringUtils.isNotBlank(file.getName())) {
//            dataParam.put("scriptName", file.getName());
//            if(file.getName().endsWith(".sql")){
//                dataParam.put("model", "006");
//            }
//        }
//        map.put("data", dataParam.toJSONString());
//        logger.info("==================================================" + url + ":==========================" + JSONObject.toJSONString(map));
//        String res = HttpUtil.doPost(url, map);
//        logger.info("==================================================job num :" + res);
        JSONObject jsonObject = buffaloComponent.getTaskList(queryJob.getAppGroupId(), file.getName(), null);

        List<DataDevScriptFilePublish> maxVersionList = publishDao.getMaxVersion(file.getGitProjectId(), file.getGitProjectFilePath(), file.getApplicationId());
        Map<Long, String> versionMap = new HashMap<Long, String>();
        for (DataDevScriptFilePublish publish : maxVersionList) {
            versionMap.put(publish.getApplicationId(), publish.getVersion());
        }
        if (jsonObject != null && jsonObject.get("list") != null) {
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject objItem = jsonArray.getJSONObject(i);
                DataDevScriptBuffaloJob buffaloJob = new DataDevScriptBuffaloJob();
                buffaloJob.setTaskId(objItem.getInteger("taskId"));
                buffaloJob.setTaskVersion(objItem.getString("taskVersion"));
                if (StringUtils.isNotBlank(queryJob.getTaskVersion()) && !queryJob.getTaskVersion().equals(buffaloJob.getTaskVersion())) {
                    continue;
                }
                Long approveStatus = objItem.getLong("approveStatus");
                approveStatus = approveStatus != null ? approveStatus : -1;
                buffaloJob.setApproveStatus(approveStatus);
                String approveStatusStr = "";
                /**
                 * 3.5审批状态 1测试任务 2线上任务 3待上线审批 4待下线审批 5过期任务
                 * 4.0审批状态 1-测试任务 2-上线审批中 3-线上任务 4-下线审批中 5-过期任务
                 */
                if ("1".equals(buffaloJob.getTaskVersion())) {
                    switch (approveStatus.intValue()) {
                        case 1:
                            approveStatusStr = "测试任务";
                            break;
                        case 2:
                            approveStatusStr = "上线审批中";
                            break;
                        case 3:
                            approveStatusStr = "线上任务";
                            break;
                        case 4:
                            approveStatusStr = "下线审批中";
                            break;
                        case 5:
                            approveStatusStr = "过期任务";
                            break;
                        default:
                            break;
                    }
                    buffaloJob.setTaskVersionStr("任务调度4.0");
                } else if ("2".equals(buffaloJob.getTaskVersion())) {
                    switch (approveStatus.intValue()) {
                        case 1:
                            approveStatusStr = "测试任务";
                            break;
                        case 2:
                            approveStatusStr = "线上任务";
                            break;
                        case 3:
                            approveStatusStr = "待上线审批";
                            break;
                        case 4:
                            approveStatusStr = "待下线审批";
                            break;
                        case 5:
                            approveStatusStr = "过期任务";
                            break;
                        default:
                            break;
                    }
                    buffaloJob.setTaskVersionStr("任务调度3.5");
                }
                buffaloJob.setApproveStatusStr(approveStatusStr);
                buffaloJob.setAppGroupId(objItem.getLong("appGroupId"));
                buffaloJob.setAppGroupName(objItem.getString("appGroupName") + "(" + buffaloJob.getAppGroupId() + ")");
                buffaloJob.setTaskName(objItem.getString("taskName"));
                buffaloJob.setManagers(objItem.getString("managers"));
                buffaloJob.setManagersName(urmUtil.getErpAndNameByErp(buffaloJob.getManagers()));
                String description = objItem.getString("description");
                if (StringUtils.isNotBlank(description)) {
                    description = description.replaceAll("[\n\r]", "  ");
                }
                buffaloJob.setDescription(description);
                String version = versionMap.get(buffaloJob.getAppGroupId());
                buffaloJob.setDatadevVersion(StringUtils.isNotBlank(version) ? version : "1000");
                if (StringUtils.isNotBlank(queryJob.getBuffaloKeyWord())) {
                    String queryWord = queryJob.getBuffaloKeyWord().trim().toUpperCase();
                    if (StringUtils.isNotBlank(queryWord)) {
                        if (!((buffaloJob.getDatadevVersion() != null && buffaloJob.getDatadevVersion().toString().toUpperCase().contains(queryWord))
                                || (StringUtils.isNotBlank(buffaloJob.getTaskName()) && buffaloJob.getTaskName().toUpperCase().contains(queryWord))
                                || (StringUtils.isNotBlank(buffaloJob.getDescription()) && buffaloJob.getDescription().toUpperCase().contains(queryWord)))) {
                            continue;
                        }
                    }
                }
                if (count >= start && count < end) {
                    resList.add(buffaloJob);
                }
                count++;
            }
            pageResultDTO.setRecords(Long.valueOf(count));
            pageResultDTO.setSuccess(true);
            pageResultDTO.setRows(resList);
        } else {
            pageResultDTO.setRecords(0L);
            pageResultDTO.setSuccess(true);
            pageResultDTO.setRows(resList);
        }
        return pageResultDTO;
    }

    @Override
    public void updateStatus(DataDevScriptFilePublish publish) throws Exception {
        if (publish.getId() != null) {
            publishDao.updateStatus(publish);
        } else if (publish.getRequestId() != null) {
            DataDevScriptFilePublish old = publishDao.findByRequestId(publish);
            publish.setId(old.getId());
            publishDao.updateStatus(publish);
        } else {
            throw new RuntimeException("id requestid不能同时为空");
        }
    }


    @Override
    public DataDevScriptFilePublish findLastNotFail(Long gitProjectId, String gitProjectFilePath, Long applicationId) throws Exception {
        DataDevScriptFilePublish publish = new DataDevScriptFilePublish();
        publish.setGitProjectId(gitProjectId);
        publish.setGitProjectFilePath(gitProjectFilePath);
        publish.setApplicationId(applicationId);
        publish.setStatus(DataDevScriptPublishStatusEnum.Failure.toCode());
        return publishDao.findLastNotFail(publish);
    }

    @Override
    public DataDevScriptFilePublish findLastSuccess(Long gitProjectId, String gitProjectFilePath, Long applicationId) throws Exception {
        return publishDao.findLastByStatus(gitProjectId, gitProjectFilePath, applicationId, DataDevScriptPublishStatusEnum.Success.toCode());
    }

    @Override
    public PageResultDTO list4page(DataDevScriptFilePublish publish, Pageable pageable) throws Exception {
        ReleaseRecordBo releaseRecordBo = new ReleaseRecordBo();
        releaseRecordBo.setObjType("script");
        releaseRecordBo.setObjKey(publish.getGitProjectId() + DataDevCenterImpl.SPLIT + publish.getGitProjectFilePath());
        releaseRecordBo.setPageNum(pageable.getPageNumber());
        releaseRecordBo.setPageSize(pageable.getPageSize());
        ApiResult<PageResult<ReleaseRecordBo>> apiResult = releaseInterface.releaseRecord(appId, appToken, System.currentTimeMillis(), releaseRecordBo);
        PageResult<ReleaseRecordBo> result = apiResult.getObj();
        PageResultDTO pageResultDTO = new PageResultDTO();
        pageResultDTO.setRecords(result.getTotalCount());
        pageResultDTO.setSuccess(true);
        pageResultDTO.setRows(result.getList());
        return pageResultDTO;
    }

    @Override
    public void insert(DataDevScriptFilePublish insertPublish) {
        publishDao.insert(insertPublish);
    }

    @Override
    public void deletePublish(Long appGroupId, Long gitProjectId, String gitProjectFilePath) throws Exception {
        publishDao.deletePublish(gitProjectId, gitProjectFilePath, appGroupId);
    }

    @Override
    public DataDevScriptFilePublish findByBuffaloScriptId(Long scriptId) throws Exception {
        return publishDao.findByBuffaloScriptId(scriptId);
    }

    @Override
    public DataDevScriptFilePublish getScriptInfoByName(Long jsdAppgroupId, String scriptName, String erp) throws Exception {
        String userToken = urmUtil.UserTokenByErp(null, erp);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName("UTF-8"));// 设置请求的编码格式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
        ContentType contentType = ContentType.create("text/plain", Charset.forName("utf-8"));
        String url = "";
        Date date = new Date();
        builder.addTextBody("appId", smpAppId, contentType);
        builder.addTextBody("userToken", userToken, contentType);
        builder.addTextBody("time", String.valueOf(date.getTime()), contentType);
        builder.addTextBody("sign", MD5Util.getMD5Str(smpAppToken + userToken + date.getTime()), contentType);
        url = buffaloDomain + "/api/v2/buffalo4/script/getScriptInfoByName";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsdAppgroupId", jsdAppgroupId);
        jsonObject.put("scriptName", scriptName);
        builder.addTextBody("data", jsonObject.toJSONString(), contentType);
        logger.error("=====================================getBuffaloScriptInfo data:" + jsonObject.toJSONString());
        logger.error("=====================================getBuffaloScriptInfo url:" + url);
        HttpPost postMethod = new HttpPost(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        postMethod.setEntity(builder.build());
        String response = HttpUtil.exctueRequest(postMethod);
        logger.error("=====================================getBuffaloScriptInfo response:" + response);
        JSONObject resObject = JSONObject.parseObject(response);
        DataDevScriptFilePublish publish = null;
        if (resObject.get("list") != null) {
            JSONArray array = resObject.getJSONArray("list");
            if (array.size() > 0) {
                JSONObject obj = array.getJSONObject(0);
                publish = new DataDevScriptFilePublish();
                if (obj.get("dataDevProjectId") != null) {
                    publish.setGitProjectId(obj.getLong("dataDevProjectId"));
                }
                if (obj.get("dataDevProjectFilePath") != null) {
                    publish.setGitProjectFilePath(obj.getString("dataDevProjectFilePath"));
                }
                if (obj.get("fileId") != null) {
                    publish.setBuffaloScriptId(obj.getLong("fileId"));
                }
            }
        }
        return publish;
    }

}
