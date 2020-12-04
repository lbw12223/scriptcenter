package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.api.think.dto.MetaDbInfoDto;
import com.jd.bdp.atom.domain.common.JsfResultDTO;
import com.jd.bdp.common.enums.SystemCodeEnum;
import com.jd.bdp.datadev.agent.api.AgentScriptInterface;
import com.jd.bdp.datadev.argsParse.ParseArgsUtil;
import com.jd.bdp.datadev.component.JdqClient;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.*;
import com.jd.bdp.datadev.exception.DependencyDetailNotFoundException;
import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.ScriptJdqMessage;
import com.jd.bdp.datadev.model.ScriptRunDetail;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.util.MD5Util;
import com.jd.bdp.datadev.util.ScriptJdqMessageUtil;
import com.jd.bdp.ide.api.JobServiceInterface;
import com.jd.bdp.ide.domain.IdeClientTypeEnum;
import com.jd.bdp.ide.domain.JobInfo;
import com.jd.bdp.ide.domain.JobResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataDevScriptServiceImpl implements DataDevScriptService {


    @Autowired
    private DataDevClientBaseService clientBaseService;
    @Autowired
    private DataDevScriptFileService scriptFileService;
    @Autowired
    private DynamicJsfService dynamicJsfService;
    @Autowired
    private DataDevScriptRunDetailService runDetailService;
    @Autowired
    private DataDevPlumberArgsService plumberArgsService;
    @Autowired
    private JdqClient jdqClient;
    @Autowired
    private JobServiceInterface jobServiceInterface;
    @Autowired
    private ClusterJSFInterface clusterJSFInterface;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private DataDevDependencyService devDependencyService;
    @Autowired
    private DataDevScriptConfigService configService;

    @Value("${datadev.appId}")
    private String appId;

    @Value("${datadev.token}")
    private String appToken;
    @Value("${datadev.env}")
    private String datadevEnv;


    private static final Logger logger = Logger.getLogger(DataDevScriptServiceImpl.class);


    @Override
    public DataDevScriptRunDetail runScript(DataDevScriptFile file, DataDevScriptRunDetail scriptRunDetail, boolean mergeOriMarket) throws Exception {
        Long rundetailId = null;
        try {
            String targetIp = scriptRunDetail.getIp();
            checkRunParam(file, scriptRunDetail);
            DataDevScriptRunDetail dataDevScriptRunDetail = fillScriptRunDetail(scriptRunDetail, file, mergeOriMarket);
            logger.error("runScript  runDetail==="+JSON.toJSONString(dataDevScriptRunDetail));

            List<DataDevClientBase> baseList = clientBaseService.findAliveClient();
            if (baseList == null || baseList.size() == 0) {
                throw new RuntimeException("没有可运行的客户端");
            }
            logger.error("runScript  client====" + baseList);
            Collections.sort(baseList, new ComparatorClientBase());
            logger.error("runScript  client==== after" + baseList);

            /**
             * 运行之前 到scriptFile 如果是数据预览 不需要保存参数
             */
            if (scriptRunDetail.getRunByDataPreview() == null || scriptRunDetail.getRunByDataPreview() != 1) {
                DataDevScriptFile updateScriptFile = new DataDevScriptFile();
                updateScriptFile.setStartShellPath(scriptRunDetail.getStartShellPath());
                updateScriptFile.setArgs(scriptRunDetail.getArgs());
                scriptFileService.updateDataDevScriptFile(scriptRunDetail.getGitProjectId(), scriptRunDetail.getGitProjectFilePath(), updateScriptFile);
            }
            String args = dataDevScriptRunDetail.getArgs();
            if (StringUtils.isNotBlank(args)) {
                JSONObject params = JSONObject.parseObject(args.trim());
                for (String key : params.keySet()) {
                    params.put(key, ParseArgsUtil.runFunction(params.getString(key)));
                }
                dataDevScriptRunDetail.setArgs(params.toJSONString());
            }
            if (DataDevRunTypeEnum.DependencyRun.tocode().equals(scriptRunDetail.getRunType())) {
                List<DataDevDependencyDetail> list = devDependencyService.getDetails(file.getGitProjectId(), file.getGitProjectFilePath(), null);
                List<DataDevDependencyDetail> newList = devDependencyService.getNewVersionDetails(list, file.getGitProjectId(), file.getGitProjectFilePath());
                List<String> dependencyPaths = new ArrayList<>();
                for (DataDevDependencyDetail devDependencyDetail : newList) {
                    dependencyPaths.add(devDependencyDetail.getDependencyGitProjectFilePath());
                }
                dataDevScriptRunDetail.setDevDependencyDetails(dependencyPaths);
                HoldDoubleValue<Boolean, DataDevDependency> holdDoubleValue = devDependencyService.saveDetails(newList, file.getGitProjectId(), file.getGitProjectFilePath(), scriptRunDetail.getOperator());
                dataDevScriptRunDetail.setDependencyId(holdDoubleValue.b.getId());
                DataDevScriptFile tmpFile = scriptFileService.findTmpByRelationDependencyId(holdDoubleValue.b.getId());
                Long runFileId = null;
                String runFileVersion = "";
                //不是选中内容执行或者旧版本运行并且已经存在打包文件
                if (tmpFile != null && (dataDevScriptRunDetail.isRunByContent() == null || !dataDevScriptRunDetail.isRunByContent())) {
                    dataDevScriptRunDetail.setGitProjectFilePath(tmpFile.getGitProjectFilePath());
                    dataDevScriptRunDetail.setType(DataDevScriptTypeEnum.Zip.toCode());
                    runFileVersion = tmpFile.getVersion();
                    runFileId = tmpFile.getId();
                } else {
                    Map<String, byte[]> replaceMap = new HashMap<>();
                    if (dataDevScriptRunDetail.isRunByContent() != null && dataDevScriptRunDetail.isRunByContent()) {
                        replaceMap.put(dataDevScriptRunDetail.getGitProjectFilePath(), dataDevScriptRunDetail.getContent().getBytes("utf-8"));
                    }
                    byte[] bytes = devDependencyService.packZip(holdDoubleValue.b.getDependencyDetails(), scriptRunDetail.getOperator(), replaceMap);
                    ZtreeNode ztreeNode = scriptFileService.createNewFile(file.getGitProjectId(), "", 4, scriptRunDetail.getOperator(), 1, bytes, "", file.getGitProjectFilePath(), null, null, holdDoubleValue.b.getId(), null);
                    //更新relationDependencyId字段
                    dataDevScriptRunDetail.setGitProjectFilePath(ztreeNode.getPath());
                    runFileVersion = "1000";
                    dataDevScriptRunDetail.setRunByContent(false);
                    runFileId = ztreeNode.getId();
                }
                dataDevScriptRunDetail.setStartShellPath(file.getGitProjectFilePath());
                dataDevScriptRunDetail.setGitProjectId(file.getGitProjectId());
                dataDevScriptRunDetail.setType(DataDevScriptTypeEnum.Zip.toCode());
                if (StringUtils.isBlank(targetIp)) {
                    dataDevScriptRunDetail.setIp(baseList.get(0).getIp());
                }
                runDetailService.insertRunDetail(dataDevScriptRunDetail);
                dataDevScriptRunDetail.setScriptFileId(runFileId);
                dataDevScriptRunDetail.setVersion(runFileVersion);
            } else {
                if (dataDevScriptRunDetail.getDebugFlag() != null && dataDevScriptRunDetail.getDebugFlag() == 1 && StringUtils.isBlank(targetIp)) {
                    dataDevScriptRunDetail.setIp(baseList.get(0).getIp());
                }
                runDetailService.insertRunDetail(dataDevScriptRunDetail);
            }
            if (StringUtils.isNotBlank(dataDevScriptRunDetail.getRunMarketLinuxUser()) && StringUtils.isNotBlank(dataDevScriptRunDetail.getRunClusterCode())) {
                dataDevScriptRunDetail.setClusterCode(dataDevScriptRunDetail.getRunClusterCode());
                dataDevScriptRunDetail.setMarketLinuxUser(dataDevScriptRunDetail.getRunMarketLinuxUser());
            }

            rundetailId = dataDevScriptRunDetail.getId();
            //jdq发送状态
            ScriptRunDetail scriptRunDetailJdq = new ScriptRunDetail();
            scriptRunDetailJdq.setType(dataDevScriptRunDetail.getType());
            scriptRunDetailJdq.setId(dataDevScriptRunDetail.getId());

            scriptRunDetailJdq.setGitProjectId(dataDevScriptRunDetail.getGitProjectId());
            scriptRunDetailJdq.setGitProjectFilePath(dataDevScriptRunDetail.getGitProjectFilePath());

            ScriptJdqMessage dataDevJdqMessage = ScriptJdqMessageUtil.createStatus2JdqMessage(scriptRunDetailJdq, String.valueOf(DataDevScriptRunStatusEnum.Delay.toCode()), 1);
            jdqClient.sendDataDevJdqMessage(dataDevJdqMessage);
            //jdq end
            if (StringUtils.isNotBlank(dataDevScriptRunDetail.getRunClusterCode()) && StringUtils.isNotBlank(dataDevScriptRunDetail.getRunMarketLinuxUser())) {
                dataDevScriptRunDetail.setClusterCode(dataDevScriptRunDetail.getRunClusterCode());
                dataDevScriptRunDetail.setMarketLinuxUser(dataDevScriptRunDetail.getRunMarketLinuxUser());
            }
            logger.error("runScript  runDetail before client==="+JSON.toJSONString(dataDevScriptRunDetail));

            if (dataDevScriptRunDetail.getDebugFlag() == null || dataDevScriptRunDetail.getDebugFlag() == 0) {
                String userToken = urmUtil.UserTokenByErp(null, scriptFileService.getDefaultUserToken());
                dataDevScriptRunDetail.setUserToken(userToken);

                if (StringUtils.isNotBlank(targetIp)) {
                    logger.error("runScript  runDetail before client==="+JSON.toJSONString(dataDevScriptRunDetail));

                    AgentScriptInterface agentScriptInterface = dynamicJsfService.getScriptInterfaceJsfClient(targetIp);
                    try {
                        ApiResult result = agentScriptInterface.exeScriptRunDetail(dataDevScriptRunDetail);
                        if (result.isSuccess()) {
                            return dataDevScriptRunDetail;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                } else {
                    //set sql run params
                    for (DataDevClientBase dataDevClientBase : baseList) {
                        AgentScriptInterface agentScriptInterface = dynamicJsfService.getScriptInterfaceJsfClient(dataDevClientBase.getIp());
                        try {
                            logger.error("runScript  runDetail before client==="+JSON.toJSONString(dataDevScriptRunDetail));

                            ApiResult result = agentScriptInterface.exeScriptRunDetail(dataDevScriptRunDetail);
                            if (result.isSuccess()) {
                                return dataDevScriptRunDetail;
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
                throw new RuntimeException("所有客户端都运行失败");
            } else {
                //以内容方式debug
                if (dataDevScriptRunDetail.isRunByContent() != null && dataDevScriptRunDetail.isRunByContent()) {
                    Map<Long, String> runContentMap = runDetailService.getRunContentMap();
                    runContentMap.put(dataDevScriptRunDetail.getId(), dataDevScriptRunDetail.getContent());
                }
            }
            return dataDevScriptRunDetail;
        } catch (DependencyDetailNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("=========================运行脚本失败,失败原因:" + e);
            if (rundetailId != null) {
                DataDevScriptRunDetail failDetail = new DataDevScriptRunDetail();
                failDetail.setEndTime(new Date());
                failDetail.setId(rundetailId);
                failDetail.setStatus(DataDevScriptRunStatusEnum.Error.toCode());
                runDetailService.updateRuntailStatus(failDetail);
            }
            throw new RuntimeException("运行脚本失败，失败原因：" + e.getMessage());
        }
    }


    @Override
    public void stopScript(DataDevScriptRunDetail scriptRunDetail) throws Exception {
        if (scriptRunDetail.getId() == null) {
            throw new RuntimeException("运行记录id不能为空");
        }
        DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
        if (runDetail == null) {
            throw new RuntimeException("id为" + scriptRunDetail.getId() + "的运行记录不存在");
        }
        //test或者online环境会去杀集群进程
        if (!datadevEnv.equals("dev")) {
            try {
                JobInfo jobInfo = new JobInfo();

                jobInfo.setSysCode("DATADEV");
                jobInfo.setJobId(runDetail.getScriptFileId() != null ? runDetail.getScriptFileId().intValue() : 0);
                jobInfo.setBatchId(scriptRunDetail.getId().intValue());
                jobInfo.setOperator(scriptRunDetail.getOperator());
                jobInfo.setClusterCode(runDetail.getRunClusterCode());
                jobInfo.setMarketCode(runDetail.getRunMarketLinuxUser());
                jobInfo.setQueueCode(runDetail.getQueueCode());
                jobInfo.setAccount(scriptRunDetail.getAccountCode());
                jobInfo.setClientType(IdeClientTypeEnum.HADOOP2.getName());
                logger.error("jobInfo ======================= context" + net.sf.json.JSONObject.fromObject(jobInfo));
                JsfResultDTO<JobResult> jsfResultDTO = jobServiceInterface.stopJob(appId, appToken, System.currentTimeMillis(), jobInfo);
                logger.error("jobInfo ======================== context" + net.sf.json.JSONObject.fromObject(jsfResultDTO));
            } catch (Exception e) {
                logger.error("==================调用jobServiceInterface杀死集群任务失败:" + e.getMessage());
            }
        }
        logger.error("===============================321");
        if (runDetail.getDebugFlag() != null && runDetail.getDebugFlag() == 1) {
            runDetail.setStatus(DataDevScriptRunStatusEnum.Stop.toCode());
            runDetail.setEndTime(new Date());
            runDetail.setStopErp(scriptRunDetail.getStopErp());
            runDetailService.updateRuntailStatus(runDetail);
            return;
        }
        if (runDetail.getStatus() == DataDevScriptRunStatusEnum.Error.toCode() || runDetail.getStatus() == DataDevScriptRunStatusEnum.Stop.toCode()) {
            return;
        }
        if (runDetail.getStatus() == DataDevScriptRunStatusEnum.Delay.toCode() || runDetail.getStatus() == DataDevScriptRunStatusEnum.Running.toCode()) {
            //任务状态为待执行 执行中 才去更新手动停止状态
            runDetail.setStatus(DataDevScriptRunStatusEnum.Stop.toCode());
            runDetail.setEndTime(new Date());
            runDetail.setStopErp(scriptRunDetail.getStopErp());
            runDetailService.updateRuntailStatus(runDetail);
            //任务待执行或者执行中 才去调用客户端
            if (runDetail.getIp() != null && (runDetail.getDebugFlag() == null || runDetail.getDebugFlag() == 0)) {
                AgentScriptInterface agentScriptInterface = dynamicJsfService.getScriptInterfaceJsfClient(runDetail.getIp());
                if (agentScriptInterface == null) {
                    logger.error("================================获取客户端失败");
                    throw new RuntimeException("获取客户端实例失败");
                }
                ApiResult apiResult = agentScriptInterface.stopScriptRunDetail(runDetail);
                logger.error("=================================apiResult.isSuccess()");
                if (!apiResult.isSuccess()) {
                    logger.error("================================运行客户端失败");
                    String message = apiResult.getMessage() != null ? apiResult.getMessage() : "";
                    throw new RuntimeException("运行客户端，脚本停止失败" + message);
                }
            } else {
                throw new RuntimeException("id为" + scriptRunDetail.getId() + "的运行记录没有ip记录");
            }
        } else {
            throw new RuntimeException("id为" + scriptRunDetail.getId() + "的运行记录状态非运行中");
        }
    }

    @Override
    public DataDevScriptRunDetail getRunScriptDetailById(Long id) throws Exception {
        if (id == null) {
            throw new RuntimeException("脚本运行详情id不能为空");
        }
        DataDevScriptRunDetail detail = runDetailService.findById(id);
        return detail;
    }

    @Override
    public DataDevScriptFile getScriptInfo(DataDevScriptFile scriptFile) throws Exception {
        if (scriptFile.getGitProjectId() == null) {
            throw new RuntimeException("脚本gitId(gitProjectId)为空");
        }
        if (StringUtils.isBlank(scriptFile.getGitProjectFilePath())) {
            throw new RuntimeException("脚本路径(gitProjectFilePath)为空");
        }
        DataDevScriptFile dataDevScriptFile = scriptFileService.getScriptByGitProjectIdAndFilePath(scriptFile.getGitProjectId(), scriptFile.getGitProjectFilePath(), scriptFile.getVersion());
        return dataDevScriptFile;
    }

    @Override
    public Long sqlToPlumber(DataDevScriptRunDetail scriptRunDetail, DataDevPlumberArgs plumberArgs) throws Exception {
//        scriptFileService.lockScriptFile(scriptRunDetail.getScriptFileId(), new Date(), scriptRunDetail.getOperator());
        logger.error("====================================start sqltoplumber");
        checkPlumberParam(scriptRunDetail, plumberArgs);
        logger.error("====================clusterCode=" + scriptRunDetail.getClusterCode());
        logger.error("====================MarketLinuxUser=" + scriptRunDetail.getMarketLinuxUser());
        ClusterHadoopMarketDto clusterHadoopMarketDto = new ClusterHadoopMarketDto();
        clusterHadoopMarketDto.setClusterCode(scriptRunDetail.getClusterCode());
        clusterHadoopMarketDto.setLinuxUser(scriptRunDetail.getMarketLinuxUser());
        clusterHadoopMarketDto.setDbName("data_export");
     /*   JsfResultDto jsfResultDto = clusterJSFInterface.getMetaDbInfo(appId, appToken, new Date().getTime(), clusterHadoopMarketDto);
        if (jsfResultDto == null || jsfResultDto.getCode().equals(SystemCodeEnum.SystemError.toCode()) || jsfResultDto.getObj() == null) {
            String messsage = jsfResultDto != null ? jsfResultDto.getMessage() : "返回jsf结果为空";
            throw new RuntimeException("获取集市库信息出错,出错原因:" + messsage);
        }
        MetaDbInfoDto metaDbInfoDto = (MetaDbInfoDto) jsfResultDto.getObj();
        String location = metaDbInfoDto.getDbLocationUri();
        logger.error("=============================location:" + location);
        plumberArgs.setSourceExtend(location);*/
        plumberArgs.setSourceSubprotocol("hive");
        plumberArgs.setSourceDatabase(scriptRunDetail.getDbName());
        plumberArgs.setScriptFileId(scriptRunDetail.getScriptFileId());
        DataDevScriptFile dataDevScriptFile = scriptFileService.getScriptByGitProjectIdAndFilePath(scriptRunDetail.getGitProjectId(), scriptRunDetail.getGitProjectFilePath(), scriptRunDetail.getVersion());
        if (dataDevScriptFile == null) {
            throw new RuntimeException("此脚本不存在");
        }
        List<DataDevClientBase> baseList = clientBaseService.findAliveClient();
        StringBuilder ipBuilder = new StringBuilder();
        if (baseList == null || baseList.size() == 0) {
            throw new RuntimeException("没有可运行的客户端");
        }
        Collections.sort(baseList, new ComparatorClientBase());
        DataDevScriptRunDetail dataDevScriptRunDetail = fillScriptRunDetail(scriptRunDetail, dataDevScriptFile, true);
        dataDevScriptRunDetail.setType(DataDevScriptRunDetailTypeEnum.Plumber.toCode());
        dataDevScriptRunDetail.setDataDevPlumberArgs(plumberArgs);
        runDetailService.insertRunDetail(dataDevScriptRunDetail);
        plumberArgs.setScriptRunDetailId(dataDevScriptRunDetail.getId());
        plumberArgsService.insertPlumberArgs(plumberArgs);

        //jdq发送状态
        logger.error("===========runScript======" + dataDevScriptRunDetail);
        ScriptRunDetail scriptRunDetailJdq = new ScriptRunDetail();
        scriptRunDetailJdq.setType(dataDevScriptRunDetail.getType());
        scriptRunDetailJdq.setId(dataDevScriptRunDetail.getId());
        scriptRunDetailJdq.setGitProjectFilePath(dataDevScriptRunDetail.getGitProjectFilePath());
        scriptRunDetailJdq.setGitProjectId(dataDevScriptRunDetail.getGitProjectId());
        ScriptJdqMessage dataDevJdqMessage = ScriptJdqMessageUtil.createStatus2JdqMessage(scriptRunDetailJdq, String.valueOf(DataDevScriptRunStatusEnum.Delay.toCode()), 1);
        jdqClient.sendDataDevJdqMessage(dataDevJdqMessage);
        //jdq end

        for (DataDevClientBase dataDevClientBase : baseList) {
            ipBuilder.append("," + dataDevClientBase.getIp());
            AgentScriptInterface agentScriptInterface = dynamicJsfService.getScriptInterfaceJsfClient(dataDevClientBase.getIp());
            try {
                logger.error("=========================================" + JSONObject.toJSONString(dataDevScriptRunDetail));
                ApiResult result = agentScriptInterface.exeScriptRunDetail(dataDevScriptRunDetail);
                if (result.isSuccess()) {
                    return dataDevScriptRunDetail.getId();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        throw new RuntimeException("所有客户端都运行失败");

    }

    /**
     * 如果是sql必须有的参数
     *
     * @param scriptFile
     * @param scriptRunDetail
     */
    private void checkRunParam(DataDevScriptFile scriptFile, DataDevScriptRunDetail scriptRunDetail) {


        if (scriptFile.getType() == DataDevScriptTypeEnum.SQL.toCode()) {
            if (StringUtils.isBlank(scriptRunDetail.getClusterCode())) {
                throw new RuntimeException("集群编号clustercode不能为空");
            }
            if (StringUtils.isBlank(scriptRunDetail.getMarketLinuxUser())) {
                throw new RuntimeException("集市linux用户marketLinuxUser不能为空");
            }
            if (StringUtils.isBlank(scriptRunDetail.getQueueCode())) {
                throw new RuntimeException("队列queueCode不能为空");
            }
            if (StringUtils.isBlank(scriptRunDetail.getAccountCode())) {
                throw new RuntimeException("生产账号accountCode不能为空");
            }
        }
    }

    /**
     * 检查plumber运行参数完整性
     *
     * @param runDetail
     * @param plumberArgs
     * @return
     */
    private void checkPlumberParam(DataDevScriptRunDetail runDetail, DataDevPlumberArgs plumberArgs) throws Exception {
        if (runDetail.getScriptFileId() == null || StringUtils.isBlank(runDetail.getDbName())) {
            throw new RuntimeException("scriptFileId  and dbName can't be null");
        }
        DataDevScriptFile dataDevScriptFile = scriptFileService.getScriptByGitProjectIdAndFilePath(runDetail.getGitProjectId(), runDetail.getGitProjectFilePath(), runDetail.getVersion());
        if (dataDevScriptFile.getType() == null || !dataDevScriptFile.getType().equals(DataDevScriptTypeEnum.SQL.toCode())) {
            DataDevScriptTypeEnum scriptTypeEnum = DataDevScriptTypeEnum.enumValueOf(dataDevScriptFile.getType());
            String type = scriptTypeEnum != null ? scriptTypeEnum.toName() : "null";
            throw new RuntimeException("scriptType must be sql when run plumber task ,current type is " + type);
        }
        checkRunParam(dataDevScriptFile, runDetail);
        DataDevScriptTargetTypeEnum typeEnum = DataDevScriptTargetTypeEnum.valuesOf(plumberArgs.getTargetType());
        if (typeEnum == null) {
            throw new RuntimeException("类型不存在" + plumberArgs.getTargetType());
        }
        String type = typeEnum.toName();

        if (plumberArgs.getTargetType() == null || typeEnum == null) {
            throw new RuntimeException("targetType must not be null");
        }
        if (plumberArgs.getTargetType().equals(DataDevScriptTargetTypeEnum.Mysql.toName()) || plumberArgs.getTargetType().equals(DataDevScriptTargetTypeEnum.Phoenix.toName())) {
            if (StringUtils.isBlank(plumberArgs.getTargetHost())) {
                throw new RuntimeException(type + "  host must not be null");
            }
            if (StringUtils.isBlank(plumberArgs.getTargetPort())) {
                throw new RuntimeException(type + "  port must not be null");
            }
            if (StringUtils.isBlank(plumberArgs.getTargetUser())) {
                throw new RuntimeException(type + "  user must not be null");
            }
            if (StringUtils.isBlank(plumberArgs.getTargetPassword())) {
                throw new RuntimeException(type + "  passWord must not be null");
            }
        }
        if (StringUtils.isBlank(plumberArgs.getTargetDatabase())) {
            throw new RuntimeException(type + "  targetDatabase must not be null");
        }
        if (StringUtils.isBlank(plumberArgs.getTargetTableName())) {
            throw new RuntimeException(type + "  targetTableName must not be null");
        }
        if (plumberArgs.getTargetType().equals(DataDevScriptTargetTypeEnum.Elasticsearch.toName())) {
            if (StringUtils.isBlank(plumberArgs.getTargetExtend())) {
                throw new RuntimeException(type + "  targetExtend must not be null");
            }
        }
//        if (StringUtils.isBlank(plumberArgs.getTargetExtend1())) {
//            throw new RuntimeException(type + "  targetExtend1 must not be null");
//        }
        if (StringUtils.isBlank(plumberArgs.getDataDate())) {
            throw new RuntimeException(type + "  dataDate must not be null");
        }
    }

    private DataDevScriptRunDetail fillScriptRunDetail(DataDevScriptRunDetail scriptRunDetail, DataDevScriptFile scriptFile, boolean isMergeMarket) throws Exception {
        scriptRunDetail.setType(scriptFile.getType());
        if (!ScriptTypeEnum.SQL.toCode().equals(scriptRunDetail.getType()) || StringUtils.isBlank(scriptRunDetail.getEngineType())) {
            scriptRunDetail.setEngineType(DataDevScriptEngineTypeEnum.Hive.getValue());
        }
        String version = StringUtils.isNotBlank(scriptRunDetail.getVersion()) ? scriptRunDetail.getVersion() : scriptFile.getVersion();
        scriptRunDetail.setVersion(version);//这里传version 这样每次下载的都是hbase his表
        String startShellPath = "";
        if (StringUtils.isBlank(scriptRunDetail.getStartShellPath())) {
            startShellPath = scriptFile.getStartShellPath();
        } else {
            startShellPath = scriptRunDetail.getStartShellPath();
        }
        if (scriptRunDetail.isRunByContent() != null && scriptRunDetail.isRunByContent() && !MD5Util.getMD5Str(scriptRunDetail.getContent()).equals(scriptFile.getFileMd5())) {
            scriptRunDetail.setRunTmp(1);
        } else {
            scriptRunDetail.setRunTmp(0);
        }
        if (isMergeMarket && StringUtils.isNotBlank(scriptRunDetail.getClusterCode()) && StringUtils.isNotBlank(scriptRunDetail.getMarketLinuxUser()) && StringUtils.isNotBlank(scriptRunDetail.getAccountCode())) {
            DataDevScriptConfig oriClusterAndMarketByMarketCode = configService.getOriClusterAndMarketByMarketCode(scriptRunDetail.getClusterCode(), scriptRunDetail.getMarketLinuxUser(), scriptRunDetail.getAccountCode());
            scriptRunDetail.setRunClusterCode(oriClusterAndMarketByMarketCode.getRunClusterCode());
            scriptRunDetail.setRunMarketLinuxUser(oriClusterAndMarketByMarketCode.getRunMarketLinuxUser());
        }
        scriptRunDetail.setStartShellPath(StringUtils.isNotBlank(startShellPath) ? startShellPath.trim() : "");
        scriptRunDetail.setCreator(StringUtils.isNotBlank(scriptRunDetail.getCreator()) ? scriptRunDetail.getCreator() : "bdp_sys");
        scriptRunDetail.setStatus(DataDevScriptRunStatusEnum.Delay.toCode());
        if (scriptRunDetail.getQueueCode() != null && scriptRunDetail.getQueueCode().startsWith("root.")) {
            scriptRunDetail.setQueueCode(scriptRunDetail.getQueueCode().substring(5));
        } else {
            scriptRunDetail.setQueueCode(scriptRunDetail.getQueueCode());
        }
        scriptRunDetail.setStartTime(new Date());
        scriptRunDetail.setArgsImportType(scriptRunDetail.getArgsImportType() != null ? scriptRunDetail.getArgsImportType() : ArgsImportTypeEnum.STANDARD.toCode());
        scriptRunDetail.setScriptFileId(scriptFile.getId());
        scriptRunDetail.setOperator(StringUtils.isNotBlank(scriptRunDetail.getOperator()) ? scriptRunDetail.getOperator() : "bdp_sys");
        if (scriptRunDetail.getScriptName() == null) {
            scriptRunDetail.setScriptName(scriptFile.getName());
        }
        scriptRunDetail.setGitProjectDirPath(scriptFile.getGitProjectDirPath());

        //cgroup default
        Integer cgroupLimit = 1;
        Integer cgroupMemoryLimit = 10;
        Integer cgroupCpuLimit = 5;
        if (scriptFile.getCgroupArgs() != null) {
            JSONObject cgroupJson = JSONObject.parseObject(scriptFile.getCgroupArgs());
            cgroupLimit = cgroupJson.getInteger("cgroupLimit");
            cgroupMemoryLimit = cgroupJson.getInteger("cgroupMemoryLimit");
            cgroupCpuLimit = cgroupJson.getInteger("cgroupCpuLimit");
        }
        scriptRunDetail.setCgroupLimit(cgroupLimit);
        scriptRunDetail.setCgroupCpuLimit(cgroupCpuLimit);
        scriptRunDetail.setCgroupMemoryLimit(cgroupMemoryLimit);

        return scriptRunDetail;
    }


    /**
     * 按照客户端同时cpu + disk
     * #按照客户端同时执行的任务数从小到大排序
     */
    class ComparatorClientBase implements Comparator<DataDevClientBase> {
        @Override
        public int compare(DataDevClientBase o1, DataDevClientBase o2) {
            if (o1.getActiveScriptRunDetail() != null
                    && o2.getActiveScriptRunDetail() != null &&
                    o1.getDisk() != null &&
                    o1.getCpu() != null &&
                    o2.getDisk() != null &&
                    o2.getCpu() != null
            ) {

                if ((o1.getCpu() + o1.getDisk()) > (o2.getCpu() + o2.getDisk())) {
                    return 1;
                } else {
                    return -1;
                }

            } else {
                return 0;
            }
        }
    }
}
