package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.datadev.component.UrmStaticUtil;
import com.jd.bdp.datadev.dao.DataDevDataPreviewDao;
import com.jd.bdp.datadev.datapreview.domain.DataDevDataPreview;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.ArgsImportTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptEngineTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptRunStatusEnum;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.util.HttpUtil;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopMarket;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author zhanglei68.
 * @date 2019-02-28.
 * @time 21:03.
 */
@Service
public class DataDevDataPreviewServiceImpl implements DataDevDataPreviewService {

    @Value("${checkTableUrl}")
    private String checkTableUrl;
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Value("${sc_marketId}")
    private Long scMarketId;

    @Value("${queryClusterUrl}")
    private String queryClusterUrl;

    private String dataPreviewFilePath = "DataPreview/dataPreview.sql";

    private Long EXPIRE_TIME = 2 * 24 * 3600 * 1000L;
    private Long RUN_TIME = 5 * 60 * 1000L;

    @Autowired
    private ClusterJSFInterface clusterJSFInterface;

    @Autowired
    private DataDevScriptTemplateService templateService;

    @Autowired
    private DataDevScriptRunDetailService runDetailService;

    @Autowired
    private DataDevDataPreviewDao dataPreviewDao;

    @Autowired
    private DataDevScriptService scriptService;

    @Autowired
    private DataDevScriptFileService fileService;

    private static final Logger logger = Logger.getLogger(DataDevDataPreviewServiceImpl.class);


    @Override
    public DataDevDataPreview doDataPreview(String clusterCode, String marketCode, String dbName, String tableName , String erp) throws Exception {
        //数据预览表两天内里有记录，并且运行结束，或者五分钟内还没运行结束的 不需要重新运行
        boolean needRun = false;
        DataDevDataPreview dataPreview = dataPreviewDao.getByTbInfo(clusterCode, marketCode, dbName, tableName);
        DataDevScriptRunDetail resultDetail = null;
        if (dataPreview == null) {
            needRun = true;
            logger.info("根据库表信息查询不到数据预览记录");
        } else {
            resultDetail = runDetailService.findById(dataPreview.getRunDetailId());
            logger.error("==============rudnetail" + JSONObject.toJSONString(resultDetail));
        }
        if (!needRun) {
            Long nowTime = System.currentTimeMillis();
            Long preTime = nowTime - EXPIRE_TIME;
            logger.error("==============dataPreview" + JSONObject.toJSONString(dataPreview));
            if (dataPreview.getModified().getTime() < preTime || resultDetail == null || resultDetail.getStatus().equals(DataDevScriptRunStatusEnum.Error.toCode()) || resultDetail.getStatus().equals(DataDevScriptRunStatusEnum.Stop.toCode())) {
                needRun = true;
                logger.error("运行记录超时");
            }
        }
        if (!needRun) {
            Long nowTime = System.currentTimeMillis();
            Long preTime = nowTime - RUN_TIME;
            if (resultDetail.getCreated().getTime() < preTime && !resultDetail.getStatus().equals(DataDevScriptRunStatusEnum.Success.toCode())) {
                needRun = true;
                logger.error("运行记录运行超时");
            }
        }
        logger.error("是否需要重新运行===============================" + needRun);
        if (needRun) {
            DataDevScriptRunDetail runDetail = getRundetailCode(clusterCode, marketCode, dbName, tableName);
            Long gitProjectId = templateService.getTemPlateProjectId();
            String gitProjectFilePath = dataPreviewFilePath;
            DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
            runDetail.setCreator(UrmStaticUtil.getBdpManager());
            runDetail.setOperator(erp);
            runDetail.setRunByDataPreview(1);
            runDetail.setGitProjectId(gitProjectId);
            runDetail.setGitProjectFilePath(gitProjectFilePath);
            if (file == null) {
                throw new RuntimeException("该脚本不存在");
            }
            runDetail.setArgsImportType(ArgsImportTypeEnum.STANDARD.toCode());
            runDetail.setEngineType(DataDevScriptEngineTypeEnum.Hive.getValue());
            JSONObject argsObj = new JSONObject();
            argsObj.put("dbName", dbName);
            argsObj.put("tbName", tableName);
            runDetail.setArgs(argsObj.toJSONString());
            logger.error("===============1111" + JSONObject.toJSONString(runDetail));
            resultDetail = scriptService.runScript(file, runDetail, false);
            logger.error("===============2222" + JSONObject.toJSONString(runDetail));
            if (dataPreview == null) {
                dataPreview = new DataDevDataPreview();
                dataPreview.setClusterCode(clusterCode);
                dataPreview.setLinuxUser(marketCode);
                dataPreview.setDbName(dbName);
                dataPreview.setTbName(tableName);
                dataPreview.setRunDetailId(resultDetail.getId());
                dataPreview.setCreator(erp);
                dataPreviewDao.insert(dataPreview);
            } else {
                DataDevDataPreview preview = new DataDevDataPreview();
                preview.setId(dataPreview.getId());
                preview.setRunDetailId(resultDetail.getId());
                dataPreviewDao.update(preview);
            }
            dataPreview.setRunDetailId(resultDetail.getId());
        }
        return dataPreview;

    }

    @Override
    public HoldDoubleValue<Boolean,Integer> validData(Long runDetailId) throws Exception {
        DataDevScriptRunDetail runDetail = runDetailService.findById(runDetailId);
        if (runDetail.getDataCount() != null && runDetail.getDataCount() > 0) {
            return new HoldDoubleValue<>(true,runDetail.getDataCount()-1);
        }
        if (runDetail.getStatus().equals(DataDevScriptRunStatusEnum.Stop.toCode()) || runDetail.getStatus().equals(DataDevScriptRunStatusEnum.Error.toCode())) {
            logger.error("=========运行id：" + runDetailId + "运行出错");
            throw new RuntimeException("运行出错");
        }
        return new HoldDoubleValue<>(false,-1);
    }

    private DataDevScriptRunDetail getRundetailCode(String clusterCode, String marketCode, String dbName, String tableName) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = new DataDevScriptRunDetail();
        ClusterHadoopMarket market = getMarket(clusterCode, marketCode);
        logger.error("=========market:" + JSONObject.toJSONString(market));
        UgdapTableInfo tableInfo = checkTable(clusterCode, marketCode, dbName, tableName);
        logger.error("=========tableInfo:" + JSONObject.toJSONString(tableInfo));

        ClusterHadoopAccount account = getAccount(market, tableInfo, marketCode);
        logger.error("=========account:" + JSONObject.toJSONString(account));

        ClusterHadoopQueue queue = getQueue(account);
        logger.error("=========queue:" + JSONObject.toJSONString(queue));
        if (tableInfo != null && StringUtils.isNotBlank(tableInfo.getOwner())) {
            account.setCode(tableInfo.getOwner());
        }
        dataDevScriptRunDetail.setClusterCode(clusterCode);
        dataDevScriptRunDetail.setMarketLinuxUser(marketCode);
        dataDevScriptRunDetail.setAccountCode(account.getCode());
        dataDevScriptRunDetail.setQueueCode(queue.getQueueCode());
        //if (market.getId().equals(scMarketId)) {
            changeRunCode(dataDevScriptRunDetail);
        //}
        return dataDevScriptRunDetail;
    }

    private DataDevScriptRunDetail changeRunCode(DataDevScriptRunDetail dataDevScriptRunDetail) throws Exception {
        String appId = "123";
        String token = "123";
        String time = String.valueOf(System.currentTimeMillis());
        Map<String, String> map = new HashMap<>();
        map.put("appId", appId);
        map.put("token", token);
        map.put("time", time);
        map.put("clusterCode", dataDevScriptRunDetail.getClusterCode());
        map.put("martCode", dataDevScriptRunDetail.getMarketLinuxUser());
        map.put("account", dataDevScriptRunDetail.getAccountCode());
        String responseStr = HttpUtil.doPost(queryClusterUrl, map);
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(responseStr);
        if (jsonObject.get("code") != null && jsonObject.getInt("code") == 0) {
            net.sf.json.JSONObject dataJson = (net.sf.json.JSONObject) jsonObject.get("data");
            dataDevScriptRunDetail.setRunClusterCode(dataJson.getString("physicalClusterCode"));
            dataDevScriptRunDetail.setRunMarketLinuxUser(dataJson.getString("authMart"));
        } else {
            dataDevScriptRunDetail.setRunClusterCode(dataDevScriptRunDetail.getClusterCode());
            dataDevScriptRunDetail.setRunMarketLinuxUser(dataDevScriptRunDetail.getMarketLinuxUser());
        }
        return dataDevScriptRunDetail;
    }

    private ClusterHadoopMarket getMarket(String clusterCode, String marketCode) {
        ClusterHadoopMarketDto clusterHadoopMarketDto = new ClusterHadoopMarketDto();
        clusterHadoopMarketDto.setClusterCode(clusterCode);
        clusterHadoopMarketDto.setLinuxUser(marketCode);
        JsfResultDto jsfResultDto = clusterJSFInterface.getMarketInfoByClusterCodeAndMartCode(appId, appToken, new Date().getTime(), clusterHadoopMarketDto);
        if (jsfResultDto.getCode() != 0) {
            throw new RuntimeException("获取集市报错:" + jsfResultDto.getMessage());
        }
        ClusterHadoopMarket market = (ClusterHadoopMarket) jsfResultDto.getObj();
        if (market == null) {
            throw new RuntimeException("集市不存在，clusterCode:" + clusterCode + ",marketCode:" + marketCode);
        }
        return market;
    }

    private ClusterHadoopAccount getAccount(ClusterHadoopMarket market, UgdapTableInfo tableInfo, String marketCode) {
        ClusterHadoopAccount firstAccount = null;
        ClusterHadoopAccount secondAccount = null;
        ClusterHadoopAccount thirdAccount = null;
        String tableOwner = tableInfo.getOwner();

        JsfResultDto resultDto = clusterJSFInterface.getAccountByMarketId(appId, appToken, new Date().getTime(), market.getId());
        if (resultDto.getCode() != 0) {
            throw new RuntimeException("集市获取账号报错" + JSONObject.toJSONString(market));
        }
        List<ClusterHadoopAccount> clusterHadoopAccountList = resultDto.getList();
        for (ClusterHadoopAccount clusterHadoopAccount : clusterHadoopAccountList) {
            if (StringUtils.isNotBlank(tableOwner) && clusterHadoopAccount.getCode().equals(tableOwner)) {
                firstAccount = clusterHadoopAccount;
                break;
            }
            if (StringUtils.isNotBlank(tableInfo.getBusinessLine()) && clusterHadoopAccount.getCode().equals(tableInfo.getBusinessLine())) {
                secondAccount = clusterHadoopAccount;
                continue;
            }
            if (clusterHadoopAccount.getCode().equals(marketCode)) {
                thirdAccount = clusterHadoopAccount;
                continue;
            }
        }


        if (firstAccount == null && secondAccount == null && thirdAccount == null) {
            throw new RuntimeException("表找不到账号：" + JSONObject.toJSONString(tableInfo));
        }
        return firstAccount != null ? firstAccount : secondAccount != null ? secondAccount : thirdAccount;
    }

    private ClusterHadoopQueue getQueue(ClusterHadoopAccount account) throws Exception {
        JsfResultDto jsfResultDto = clusterJSFInterface.getQueueByAccountId(appId, appToken, new Date().getTime(), account.getId());
        if (jsfResultDto == null || jsfResultDto.getCode() == null || jsfResultDto.getCode() != 0) {
            throw new RuntimeException("获取账号的队列失败：" + jsfResultDto.getMessage());
        }
        List<ClusterHadoopQueue> queues = jsfResultDto.getList();
        ClusterHadoopQueue realQueue = null;
        Map<Long, List<ClusterHadoopQueue>> map = new HashMap<>();
        for (ClusterHadoopQueue queue : queues) {
            if (queue.getParentQueueId() == null) {
                continue;
            }
            if (map.containsKey(queue.getParentQueueId())) {
                List<ClusterHadoopQueue> list = map.get(queue.getParentQueueId());
                list.add(queue);
            } else {
                List<ClusterHadoopQueue> list = new ArrayList<>();
                list.add(queue);
                map.put(queue.getParentQueueId(), list);
            }
        }
        ClusterHadoopQueue best = null;
        ClusterHadoopQueue better = null;
        for (ClusterHadoopQueue queue : queues) {
            if (map.containsKey(queue.getId())) {
                continue;
            }
            if (queue.getQueueCode().indexOf(account.getCode()) != -1) {
                best = (queue);
                break;
            }
            better = (queue);
        }
        realQueue = best != null ? best : better != null ? better : (queues != null && queues.size() > 0) ? (queues.get(0)) : null;
        if (realQueue == null) {
            throw new RuntimeException("根据账号获取不到队列：" + JSONObject.toJSONString(account));
        }
        return realQueue;
    }


    private UgdapTableInfo checkTable(String clusterCode, String marketCode, String dbName, String tableName) throws Exception {
        Map<String, String> map = new HashMap<>();
        JSONObject dataParamObj = new JSONObject();
        dataParamObj.put("clusterCode", clusterCode);
        dataParamObj.put("martCode", marketCode);
        dataParamObj.put("tbls", dbName + "." + tableName);
        map.put("data", dataParamObj.toJSONString());
        logger.error("=====================" + JSONObject.toJSONString(map));
        logger.error("=====================" + checkTableUrl);
        String s = HttpUtil.doPost(checkTableUrl, map);
        UgdapTableInfo tableInfo = new UgdapTableInfo();
        logger.error("=====================sssss:" + s);
        JSONObject jsonObject = JSONObject.parseObject(s);
        if (jsonObject.get("code") != null && jsonObject.getInteger("code") == 0) {
            JSONObject dataObj = jsonObject.getJSONObject("data");
            if (dataObj != null && dataObj.get("tbls") != null) {
                JSONArray array = dataObj.getJSONArray("tbls");
                for (int i = 0; i < array.size(); i++) {
                    JSONObject table = array.getJSONObject(i);
                    if (table.getInteger("exist") == 1) {
                        tableInfo.setOwner(table.getString("owner"));
                        tableInfo.setPhysicalClusterCode(table.getString("physicalClusterCode"));
                        tableInfo.setBusinessLine(table.getString("business_line"));
                        break;
                    }
                }
            }
        }
        return tableInfo;
    }

    public static void main(String[] args) throws Exception {
        String data = "{'clusterCode':'${clusterCode}'},'martCode':'${marketCode}','tbls':'${tbls}'";
        data = data.replaceAll("\\$\\{clusterCode}", "10k").replaceAll("\\$\\{marketCode}", "mart_sc").replaceAll("\\$\\{tbls}", "dev" + "." + "ta");
        Map<String, String> map = new HashMap<>();
        map.put("data", data);
        String s = HttpUtil.doPost("http://ugdap.jd.com/ugdapRestApi/DbTableApi/queryTableExist.ajax", map);

    }
}


