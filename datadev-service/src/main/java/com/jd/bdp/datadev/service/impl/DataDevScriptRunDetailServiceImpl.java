package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.authorityCenter.AuthorityCenterMarketInfoInterface;
import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterBaseDto;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.common.utils.DESUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.AllMarketComponent;
import com.jd.bdp.datadev.component.HbaseRunDetailLog;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.dao.DataDevScriptRunDetailDao;
import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.enums.*;
import com.jd.bdp.datadev.service.DataDevCenterService;
import com.jd.bdp.datadev.service.DataDevClusterAdminService;
import com.jd.bdp.datadev.service.DataDevScriptConfigService;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.domain.authorityCenter.MarketInfoDto;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopMarket;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import com.jd.bdp.domain.urm.right.ApiResultDTO;
import com.jd.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataDevScriptRunDetailServiceImpl implements DataDevScriptRunDetailService {

    private static final Logger logger = Logger.getLogger(DataDevScriptRunDetailServiceImpl.class);

    private Map<Long, String> CONTENT_MAP = new HashMap<Long, String>();


    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Autowired
    DataDevScriptRunDetailDao runDetailDao;
    @Autowired
    private DataDevScriptFileDao fileDao;
    @Autowired
    private AuthorityCenterMarketInfoInterface marketInfoInterface;

    @Autowired
    private DataDevCenterService dataDevCenterService;

    @Autowired
    private DataDevScriptConfigService configService;
    @Autowired
    private DataDevClusterAdminService dataDevClusterAdminService;
    @Autowired
    private ClusterJSFInterface clusterJSFInterface;

    @Autowired
    private HbaseRunDetailLog hbaseRunDetailLog;

    @Autowired
    private UrmUtil urmUtil;

    @Autowired
    private AllMarketComponent allMarketComponent;

    private static String ENCRYPT_KEY = "CvNvX5Ggmu72dQTj";
    private static Pattern IP_PATTERN = Pattern.compile("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))");

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public DataDevScriptRunDetail findById(Long id) throws Exception {
        DataDevScriptRunDetail detail = runDetailDao.findById(id);
        return detail;
    }

    @Override
    public void insertRunDetail(DataDevScriptRunDetail scriptRunDetail) throws Exception {
        if (StringUtils.isBlank(scriptRunDetail.getArgs())) {
            scriptRunDetail.setArgs("{}");
        }
        if (StringUtils.isBlank(scriptRunDetail.getEnvs())) {
            scriptRunDetail.setEnvs("{}");
        }
        scriptRunDetail.setArgsImportType(scriptRunDetail.getArgsImportType() != null ? scriptRunDetail.getArgsImportType() : ArgsImportTypeEnum.STANDARD.toCode());
        scriptRunDetail.setStartTime(new Date());
        if (scriptRunDetail.getRunType() == null) {
            scriptRunDetail.setRunType(DataDevRunTypeEnum.SingeRun.tocode());
        }
        runDetailDao.insertRunDetail(scriptRunDetail);
    }

    @Override
    public void updateRuntailStatus(DataDevScriptRunDetail scriptRunDetail) throws Exception {
        runDetailDao.updateRuntailStatus(scriptRunDetail);
    }

    @Override
    public PageResultDTO list4page(DataDevScriptRunDetail runDetail, Pageable pageable) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        DataDevScriptFile file = fileDao.getSingleScriptFile(runDetail.getGitProjectId(), runDetail.getGitProjectFilePath());
        if (file == null) {
            throw new RuntimeException("脚本不存在");
        }
        runDetail.setScriptFileId(file.getId());
        Long count = 0L;
        List<DataDevScriptRunDetail> list = new ArrayList<DataDevScriptRunDetail>();
        if (StringUtils.isNotBlank(runDetail.getStatusStr())) {
            String[] strings = runDetail.getStatusStr().trim().split(",");
            runDetail.setStatusList(new ArrayList<Integer>());
            for (String str : strings) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                Integer sta = Integer.parseInt(str);
                if (DataDevScriptRunStatusEnum.enumValueOf(sta) != null) {
                    runDetail.getStatusList().add(sta);
                }
                if (DataDevScriptRunStatusEnum.Running.toCode() == sta) {
                    runDetail.getStatusList().add(DataDevScriptRunStatusEnum.Delay.toCode());
                }
            }
        } else {
            runDetail.setStatusList(null);
        }
        if (runDetail.getStatusList() != null && runDetail.getStatusList().size() == 0) {
            runDetail.setStatusList(null);
        }
        count = runDetailDao.count4page(runDetail);
        if (count > 0) {
            runDetail.setStart(pageable.getOffset());
            runDetail.setLimit(pageable.getPageSize());
            list = runDetailDao.list4page(runDetail);
            for (DataDevScriptRunDetail detail : list) {
                DataDevScriptRunStatusEnum statusEnum = DataDevScriptRunStatusEnum.enumValueOf(detail.getStatus());
                detail.setStatusStr(statusEnum != null ? statusEnum.toDesc() : "");
                if (detail.getStartTime() != null && detail.getEndTime() != null) {
                    Long timeDif = detail.getEndTime().getTime() - detail.getStartTime().getTime();
                    if (timeDif >= 0) {
                        String timeStr = "";
                        if (timeDif / (1000 * 60 * 60) > 0) {
                            timeStr += timeDif / (1000 * 60 * 60) + "h";
                            timeDif %= (1000 * 60 * 60);
                        }
                        if (StringUtils.isNotBlank(timeStr) || timeDif / (1000 * 60) > 0) {
                            timeStr += timeDif / (1000 * 60) + "min";
                            timeDif %= (1000 * 60);
                        }
                        if (timeDif / (1000 * 60) <= 0) {
                            timeStr += timeDif / (1000) + "s";
                        }
                        detail.setTimePeriod(timeStr);
                    }
                }
                if (StringUtils.isNotBlank(detail.getEngineType())) {
                    DataDevScriptEngineTypeEnum engineTypeEnum = DataDevScriptEngineTypeEnum.enumValueOf(detail.getEngineType());
                    if (engineTypeEnum != null) {
                        detail.setEngineType(engineTypeEnum.getName());
                    }
                }
                if (detail.getRunTmp() != null && detail.getRunTmp() == 1) {
                    detail.setVersion("临时版本");
                }
                detail.setRunTypeStr(DataDevRunTypeEnum.enumValueOf(detail.getRunType()).toRunDesc());
            }
        }
        pageResultDTO.setRecords(count);
        pageResultDTO.setSuccess(true);
        pageResultDTO.setRows(list);
        return pageResultDTO;
    }

    @Override
    public DataDevScriptRunDetail findLastRunByFileId(Long fileId, String operator) {
        return runDetailDao.findLastRunDetail(fileId, operator);
    }

    @Override
    public void updateDataDevScriptRunDetailPath(Long gitProjectId, String gitProjectFilePath, String newGitProjectFilePath) {
        runDetailDao.updateRunDetailPath(gitProjectId, gitProjectFilePath, newGitProjectFilePath);
    }


    @Override
    public void verifyMarket(DataDevScriptFile file,
                             DataDevScriptRunDetail runDetail,
                             String erp, Long spaceProjectId) throws Exception {

        if ((file.getType() == DataDevScriptTypeEnum.SQL.toCode() || (StringUtils.isNotBlank(runDetail.getScriptConfigId()) && !"-1".equals(runDetail.getScriptConfigId())))) {
            if (runDetail.getScriptConfigId() == null || StringUtils.isBlank(runDetail.getScriptConfigId()) ) {
                throw new RuntimeException("请选择配置");
            }
            DataDevScriptConfig config = configService.getConfigById(runDetail.getScriptConfigId());
            if (config == null) {
                throw new RuntimeException("配置不存在");
            }
            if (org.apache.commons.lang.StringUtils.isBlank(config.getOwner()) || !config.getOwner().equals(erp)) {
                // throw new RuntimeException(erp + "无配置的使用权限");
            }
            if (org.apache.commons.lang.StringUtils.isBlank(config.getClusterCode())) {
                throw new RuntimeException("该配置的集群code为空");
            }
            if (org.apache.commons.lang.StringUtils.isBlank(config.getMarketLinuxUser())) {
                throw new RuntimeException("该配置的linuxUser为空");
            }
            if (org.apache.commons.lang.StringUtils.isBlank(config.getAccountCode())) {
                throw new RuntimeException("该配置的生产账号code为空");
            }
            if (org.apache.commons.lang.StringUtils.isBlank(config.getQueueCode())) {
                throw new RuntimeException("该配置的队列code为空");
            }
            runDetail.setClusterCode(config.getClusterCode());
            runDetail.setMarketLinuxUser(config.getMarketLinuxUser());
            runDetail.setQueueCode(config.getQueueCode());
            runDetail.setAccountCode(config.getAccountCode());
            runDetail.setEngineType(org.apache.commons.lang.StringUtils.isEmpty(config.getEngineType()) ? DataDevScriptEngineTypeEnum.Hive.getValue() : config.getEngineType());
            runDetail.setRunMarketLinuxUser(config.getRunMarketLinuxUser());
            runDetail.setRunClusterCode(config.getRunClusterCode());
            boolean authority = dataDevClusterAdminService.getClusterAdminByErp(erp);
            if (!authority) {
                ApiResultDTO apiResultDTO = dataDevCenterService.getGrantAuthorityMarketForBuffalo(erp, spaceProjectId);
                if (apiResultDTO.isSuccess()) {
                    for (MarketInfoDto marketInfoDto : (List<MarketInfoDto>) apiResultDTO.getList()) {
                        if ( config.getMarketLinuxUser().equals(marketInfoDto.getMarketUser())) {
                            authority = true;
                            break;
                        }
                    }
                }
                if (!authority) {
                    throw new RuntimeException("无正在使用的集市" + config.getMarketLinuxUser() + "权限");
                }

                ApiResultDTO accountResultDTO = dataDevCenterService.getGrantAuthorityProductionAccountInMarketForBuffalo(runDetail.getMarketLinuxUser(), erp, spaceProjectId);
                if (accountResultDTO.isSuccess()) {
                    for (ClusterHadoopAccount clusterHadoopAccount : (List<ClusterHadoopAccount>) accountResultDTO.getList()) {
                        if (config.getAccountCode().equals(clusterHadoopAccount.getCode())) {
                            authority = true;
                            break;
                        }
                    }
                }
                if (!authority) {
                    throw new RuntimeException("无正在使用的生产账号" + config.getAccountCode() + "权限");
                }
                ClusterHadoopQueue queue = new ClusterHadoopQueue();
                queue.setOperator(erp);
                queue.setProductionAccountCode(config.getAccountCode());
                ApiResultDTO queueResultDTO = dataDevCenterService.getGrantAuthorityQueueOneAccountInMarketForBuffalo(runDetail.getMarketLinuxUser() , runDetail.getAccountCode(), erp, spaceProjectId);
                if (queueResultDTO.isSuccess()) {
                    for (ClusterHadoopQueue clusterHadoopQueue : (List<ClusterHadoopQueue>) queueResultDTO.getList()) {
                        if (config.getQueueCode().equals(clusterHadoopQueue.getQueueCode())) {
                            authority = true;
                            break;
                        }
                    }
                }
                if (!authority) {
                    throw new RuntimeException("无正在使用的队列" + config.getQueueCode() + "权限");
                }
            }
        }
    }

    @Override
    public String encryptIp(String ip) throws Exception {
        return DESUtil.encrypt(ip, ENCRYPT_KEY);
    }

    @Override
    public String decryptIp(String token) throws Exception {
        return DESUtil.decrypt(token, ENCRYPT_KEY);
    }

    @Override
    public boolean verifyIp(String ip) throws Exception {
        if (StringUtils.isNotBlank(ip)) {
            Matcher matcher = IP_PATTERN.matcher(ip);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(DESUtil.encrypt("", ENCRYPT_KEY));
    }

    @Override
    public Map<Long, String> getRunContentMap() throws Exception {
        return CONTENT_MAP;
    }

    @Override
    public void updateRunCode(Long marketId) throws Exception {
        JsfResultDto jsfResultDto = clusterJSFInterface.getHadoopMarketByMarketId(appId, appToken, new Date().getTime(), marketId);
        logger.error("================1111:" + JSONObject.toJSONString(jsfResultDto));
        if (jsfResultDto != null && jsfResultDto.getCode() == 0) {
            ClusterHadoopMarketDto clusterHadoopMarket = (ClusterHadoopMarketDto) jsfResultDto.getObj();
            if (clusterHadoopMarket.getClusterCode().equals("10k")) {
                runDetailDao.updateRunCode("10k", "mart_scr", "10k", clusterHadoopMarket.getLinuxUser());
            } else if (clusterHadoopMarket.getClusterCode().equals("hope")) {
                runDetailDao.updateRunCode("hope", "mart_sch", "hope", clusterHadoopMarket.getLinuxUser());
            } else {
                throw new RuntimeException("集群必须是10k或者hope，集群：" + clusterHadoopMarket.getClusterCode());
            }
        } else {
            throw new RuntimeException(jsfResultDto != null ? jsfResultDto.getMessage() : "集市不存在");
        }
    }

    @Override
    public HoldDoubleValue<Integer, Double> listFinishTask(Long limit) throws Exception {


        List<DataDevScriptRunDetail> runDetailList = runDetailDao.listFinishTask(limit);
        logger.info("==listFinishTask ===" + limit + runDetailList.size());


        double total = 0.0d;
        int numberCount = 0;
        for (DataDevScriptRunDetail dataDevScriptRunDetail : runDetailList) {
            try {
                Integer logCount = dataDevScriptRunDetail.getLogCount();

                String startKey = hbaseRunDetailLog.getKey(dataDevScriptRunDetail, logCount - 3);
                String endKey = hbaseRunDetailLog.getKey(dataDevScriptRunDetail, logCount);


                HoldDoubleValue<HoldDoubleValue<Boolean, Boolean>, List<String>> datas = hbaseRunDetailLog.query(startKey, endKey);


                logger.error(" datas    " + datas.b.size());

                for (String log : datas.b) {

                    //10.198.76.104 内存限制:10.00GB  内存峰值:378.29MB  达到内存限制次数:0
                    if (log.indexOf("内存峰值:") != -1) {
                        String memorys = log.split("内存峰值:")[1].split(" ")[0].toUpperCase();
                        boolean isMb = memorys.indexOf("MB") > 0;
                        boolean isGB = memorys.indexOf("GB") > 0;

                        if (isMb || isGB) {
                            Double number = Double.parseDouble(memorys.replace("MB", "").replace("GB", ""));
                            if (number > 0.0d) {
                                number = isGB ? number * 1024 : number;
                                total += number;
                                numberCount++;
                            }
                        }

                    }
                }

            } catch (Exception e) {
                logger.error("listFinishTask", e);
            }

        }
        return new HoldDoubleValue<>(numberCount, total);
    }

    @Override
    public PageResultDTO runListPage(DataDevScriptRunDetail runDetail, Pageable pageable) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();

        if (StringUtils.isNotBlank(runDetail.getStatusStr())) {
            String[] strings = runDetail.getStatusStr().trim().split(",");
            runDetail.setStatusList(new ArrayList<Integer>());
            for (String str : strings) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                Integer sta = Integer.parseInt(str);
                if (DataDevScriptRunStatusEnum.enumValueOf(sta) != null) {
                    runDetail.getStatusList().add(sta);
                }
                if (DataDevScriptRunStatusEnum.Running.toCode() == sta) {
                    runDetail.getStatusList().add(DataDevScriptRunStatusEnum.Delay.toCode());
                }
            }
        } else {
            runDetail.setStatusList(null);
        }

        Long count = runDetailDao.runListCount(runDetail);
        List<DataDevScriptRunDetail> list = new ArrayList<DataDevScriptRunDetail>();
        if (count > 0) {
            runDetail.setStart(pageable.getOffset());
            runDetail.setLimit(pageable.getPageSize());
            list = runDetailDao.runListPage(runDetail);
            for (DataDevScriptRunDetail item : list) {
                handDataDevScriptRunDetailListItem(item);
                ClusterHadoopMarketDto clusterHadoopMarketDto = allMarketComponent.getMarketById(item.getMarketId());
                if (clusterHadoopMarketDto != null) {
                    item.setMarketName(clusterHadoopMarketDto.getMarketName());
                }
                item.setOperator(urmUtil.getErpAndNameByErp(item.getOperator()));
            }
        }
        pageResultDTO.setRecords(count);
        pageResultDTO.setSuccess(true);
        pageResultDTO.setRows(list);
        return pageResultDTO;
    }

    private void handDataDevScriptRunDetailListItem(DataDevScriptRunDetail detail) {
        DataDevScriptRunStatusEnum statusEnum = DataDevScriptRunStatusEnum.enumValueOf(detail.getStatus());
        detail.setStatusStr(statusEnum != null ? statusEnum.toDesc() : "");
        if (detail.getStartTime() != null && detail.getEndTime() != null) {
            Long timeDif = detail.getEndTime().getTime() - detail.getStartTime().getTime();
            if (timeDif >= 0) {
                String timeStr = "";
                if (timeDif / (1000 * 60 * 60) > 0) {
                    timeStr += timeDif / (1000 * 60 * 60) + "h";
                    timeDif %= (1000 * 60 * 60);
                }
                if (StringUtils.isNotBlank(timeStr) || timeDif / (1000 * 60) > 0) {
                    timeStr += timeDif / (1000 * 60) + "min";
                    timeDif %= (1000 * 60);
                }
                if (timeDif / (1000 * 60) <= 0) {
                    timeStr += timeDif / (1000) + "s";
                }
                detail.setTimePeriod(timeStr);
            }
        }
        if (StringUtils.isNotBlank(detail.getEngineType())) {
            DataDevScriptEngineTypeEnum engineTypeEnum = DataDevScriptEngineTypeEnum.enumValueOf(detail.getEngineType());
            if (engineTypeEnum != null) {
                detail.setEngineType(engineTypeEnum.getName());
            }
        }
        if (detail.getRunTmp() != null && detail.getRunTmp() == 1) {
            detail.setVersion("临时版本");
        }
        detail.setRunTypeStr(DataDevRunTypeEnum.enumValueOf(detail.getRunType()).toRunDesc());
    }
}
