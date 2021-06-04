package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.datadev.component.ProjectSpaceRightComponent;
import com.jd.bdp.datadev.dao.DataDevPlumberArgsDao;
import com.jd.bdp.datadev.dao.DataDevScriptConfigDao;
import com.jd.bdp.datadev.domain.DataDevPlumberArgs;
import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.service.DataDevPlumberArgsService;
import com.jd.bdp.datadev.service.DataDevScriptConfigService;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopMarket;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataDevScriptConfigServiceImpl implements DataDevScriptConfigService {
    private static final Logger logger = Logger.getLogger(DataDevScriptConfigServiceImpl.class);

    @Value("${queryClusterUrl}")
    private String queryClusterUrl;

    @Value("${sc_marketId}")
    private Long scMarketId;

    @Value("${datadev.appId}")
    private String appId;

    @Value("${datadev.token}")
    private String token;

    @Autowired
    private DataDevScriptConfigDao configDao;

    @Autowired
    private DataDevScriptRunDetailService runDetailService;


    @Autowired
    private ClusterJSFInterface jsfInterface;


    @Autowired
    private ClusterJSFInterface clusterJSFInterface;

    @Autowired
    private ProjectSpaceRightComponent projectSpaceRightComponent ;


    @Override
    public List<DataDevScriptConfig> getConfigsByErp(String erp, Long projectSpaceId) throws Exception {
        List<DataDevScriptConfig> list = configDao.getConfigsByErp(erp, projectSpaceId);
        sortByOrder(list);
        return list;
    }

    @Override
    public DataDevScriptConfig getConfigById(Long id) throws Exception {
        return configDao.findById(id);
    }

    @Override
    public void updateConfig(DataDevScriptConfig config) throws Exception {
        DataDevScriptConfig oriConfig = getOriClusterAndMarketByMarketCode(config.getClusterCode(), config.getMarketLinuxUser(), config.getAccountCode());
        if (oriConfig != null) {
            config.setRunMarketLinuxUser(oriConfig.getRunMarketLinuxUser());
            config.setRunClusterCode(oriConfig.getRunClusterCode());
        }
        configDao.updateConfig(config);
    }

    @Override
    public void deleteConfig(Long id) throws Exception {
        configDao.deleteConfig(id);
    }

    @Override
    public void addConfig(DataDevScriptConfig config) throws Exception {
        DataDevScriptConfig oriConfig = getOriClusterAndMarketByMarketCode(config.getClusterCode(), config.getMarketLinuxUser(), config.getAccountCode());
        if (oriConfig != null) {
            config.setRunMarketLinuxUser(oriConfig.getRunMarketLinuxUser());
            config.setRunClusterCode(oriConfig.getRunClusterCode());
        }
        configDao.insertConfig(config);
    }

    @Override
    public void sortByOrder(List<DataDevScriptConfig> list) throws Exception {
        if (list == null) {
            return;
        } else if (list.size() == 1) {
            DataDevScriptConfig dataDevScriptConfig = list.get(0);
            if (dataDevScriptConfig != null && dataDevScriptConfig.getShowOrder() == null) {
                dataDevScriptConfig.setShowOrder(dataDevScriptConfig.getId());
            }
        } else {
            Collections.sort(list, new ConfigComparator());
        }
    }

    public static class ConfigComparator implements Comparator<DataDevScriptConfig> {

        @Override
        public int compare(DataDevScriptConfig o1, DataDevScriptConfig o2) {
            Long order1 = o1.getShowOrder() != null ? o1.getShowOrder() : o1.getId();
            Long order2 = o2.getShowOrder() != null ? o2.getShowOrder() : o2.getId();
            o1.setShowOrder(order1);
            o2.setShowOrder(order2);
            if (order1 == null || order2 == null) {
                return 0;
            } else {
                return order1 > order2 ? -1 : 1;
            }
        }
    }

    public static void main(String[] args) {
        List<DataDevScriptConfig> list = new ArrayList<DataDevScriptConfig>();
        DataDevScriptConfig config1 = new DataDevScriptConfig();
        config1.setShowOrder(2L);

        DataDevScriptConfig config2 = new DataDevScriptConfig();
        config2.setShowOrder(3L);

        DataDevScriptConfig config3 = new DataDevScriptConfig();
        config3.setShowOrder(4L);
        DataDevScriptConfig config4 = new DataDevScriptConfig();
        config4.setId(1L);
        list.add(config4);
        Collections.sort(list, new ConfigComparator());


        for (DataDevScriptConfig config : list) {
            System.out.println(config.getShowOrder());
        }

    }

    @Service
    public static class DataDevPlumberArgsServiceImpl implements DataDevPlumberArgsService {
        @Autowired
        DataDevPlumberArgsDao plumberArgsDao;

        @Override
        public void insertPlumberArgs(DataDevPlumberArgs dataDevPlumberArgs) throws Exception {
            plumberArgsDao.insertPlumberArgs(dataDevPlumberArgs);
        }
    }

    @Override
    public HoldDoubleValue<List<Long>, List<Long>> initForMarketMerge(String marketIds) throws Exception {
        List<Long> marketIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(marketIds)) {
            String[] idStrArray = marketIds.split(",");
            for (String idStr : idStrArray) {
                if (StringUtils.isNotBlank(idStr)) {
                    marketIdList.add(Long.parseLong(idStr));
                }
            }
        }
        if (marketIdList.size() == 0) {
            marketIdList = null;
        }

        List<Long> successList = new ArrayList<>();
        List<Long> failList = new ArrayList<>();
        List<DataDevScriptConfig> configsByMarketIds = configDao.getConfigsByMarketIds(marketIdList);
        for (DataDevScriptConfig config : configsByMarketIds) {
            updateMergeConfig(config, successList, failList);
        }
        for(Long id:marketIdList){
            //runDetailService.updateRunCode(id);
        }
        return new HoldDoubleValue<>(successList, failList);
    }

    public void updateMergeConfig(DataDevScriptConfig config, List<Long> successList, List<Long> failList) {
        DataDevScriptConfig updateConfig = new DataDevScriptConfig();
        updateConfig.setId(config.getId());
        try {
            if (config.getMarketId() != null) {
                logger.error("================1111");
                logger.error("================" + appId + ":" + token + ":" + config.getMarketId() + ":");
                JsfResultDto market = jsfInterface.getHadoopMarketByMarketId(appId, token, new Date().getTime(), config.getMarketId());
                logger.error("market:" + JSONObject.toJSONString(market));
                if (market.getCode() == 0) {
                    ClusterHadoopMarketDto obj = (ClusterHadoopMarketDto) market.getObj();
                    if (obj.getpId() != null && obj.getpId() > 0) {
                        JsfResultDto pMarket = jsfInterface.getHadoopMarketByMarketId(appId, token, new Date().getTime(), obj.getpId());
                        if (pMarket == null || pMarket.getCode() != 0) {
                            return;
                        }
                        ClusterHadoopMarketDto parentObj = (ClusterHadoopMarketDto) pMarket.getObj();
                        DataDevScriptConfig oriClusterAndMarket = getOriClusterAndMarketByMarketCode(parentObj.getClusterCode(), parentObj.getLinuxUser(), config.getAccountCode());
                        updateConfig.setRunClusterCode(oriClusterAndMarket.getRunClusterCode());
                        updateConfig.setRunMarketLinuxUser(oriClusterAndMarket.getRunMarketLinuxUser());
                        updateConfig.setMarketId(parentObj.getMarketId());
                        updateConfig.setMarketLinuxUser(parentObj.getLinuxUser());
                        updateConfig.setClusterCode(parentObj.getClusterCode());

                        ClusterHadoopQueue clusterHadoopQueue = new ClusterHadoopQueue();
                        clusterHadoopQueue.setLinuxUser(parentObj.getLinuxUser());
                        clusterHadoopQueue.setClusterCode(parentObj.getClusterCode());
                        clusterHadoopQueue.setQueueCode(config.getQueueCode());
                        JsfResultDto jsfResultDto = jsfInterface.getQueueInfoByMarketAndCode(appId, token, new Date().getTime(), clusterHadoopQueue);
                        logger.error("queue:" + JSONObject.toJSONString(jsfResultDto));
                        if (jsfResultDto.getCode() == 0) {
                            ClusterHadoopQueue resultDtoObj = (ClusterHadoopQueue) jsfResultDto.getObj();
                            updateConfig.setQueueId(resultDtoObj.getId());
                            ClusterHadoopAccount clusterHadoopAccount = new ClusterHadoopAccount();
                            clusterHadoopAccount.setClusterCode(parentObj.getClusterCode());
                            clusterHadoopAccount.setLinuxUser(parentObj.getLinuxUser());
                            clusterHadoopAccount.setCode(config.getAccountCode());
                            JsfResultDto resultDto = jsfInterface.getProductInfoByMarketAndCode(appId, token, new Date().getTime(), clusterHadoopAccount);
                            logger.error("account:" + JSONObject.toJSONString(resultDto));
                            if (resultDto.getCode() == 0) {
                                ClusterHadoopAccount hadoopAccount = (ClusterHadoopAccount) resultDto.getObj();
                                updateConfig.setAccountId(hadoopAccount.getId());
                                configDao.updateMergeConfig(updateConfig);
                                successList.add(config.getId());
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        failList.add(config.getId());
    }

    @Override
    public DataDevScriptConfig getOriClusterAndMarketByMarketCode(String clusterCode, String martCode, String account) throws Exception {
        try {
            ClusterHadoopMarketDto queryMarket = new ClusterHadoopMarketDto();
            queryMarket.setClusterCode(clusterCode);
            queryMarket.setLinuxUser(martCode);
            JsfResultDto jsfResultDto = jsfInterface.getMarketInfoByClusterCodeAndMartCode(appId, token, new Date().getTime(), queryMarket);
            DataDevScriptConfig dataDevScriptConfig = new DataDevScriptConfig();
            dataDevScriptConfig.setRunMarketLinuxUser(martCode);
            dataDevScriptConfig.setRunClusterCode(clusterCode);
            logger.error("==================="+JSONObject.toJSONString(jsfResultDto));
            if(jsfResultDto.getCode() == 0){
                ClusterHadoopMarket market = (ClusterHadoopMarket)jsfResultDto.getObj();
                logger.error("================"+market.getId()+"================="+scMarketId+"=============="+market.getId().equals(scMarketId));
                if(market.getId()!=null /*&& market.getId().equals(scMarketId)*/){
                    String appId = "123";
                    String token = "123";
                    String time = String.valueOf(new Date().getTime());
                    StringBuilder stringBuilder = new StringBuilder(queryClusterUrl);
                    stringBuilder.append("?appId=").append(appId);
                    stringBuilder.append("&token=").append(token);
                    stringBuilder.append("&time=").append(time);
                    stringBuilder.append("&clusterCode=").append(clusterCode);
                    stringBuilder.append("&martCode=").append(martCode);
                    stringBuilder.append("&account=").append(account);

                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpGet request = new HttpGet(stringBuilder.toString());
                    CloseableHttpResponse response = httpClient.execute(request);
                    HttpEntity entry = response.getEntity();
                    int code = response.getStatusLine().getStatusCode();
                    System.out.println("\tcode:" + code);
                    String result = EntityUtils.toString(entry);
                    HttpClientUtils.closeQuietly(response);
                    HttpClientUtils.closeQuietly(httpClient);
                    logger.error("ugdap url:" + stringBuilder.toString());
                    if (code == 200) {
                        logger.error("ugdap:" + result);
                        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
                        if (jsonObject.get("code") != null && jsonObject.getInt("code") == 0) {
                            net.sf.json.JSONObject dataJson = (net.sf.json.JSONObject) jsonObject.get("data");
                            dataDevScriptConfig.setRunClusterCode(dataJson.getString("physicalClusterCode"));
                            dataDevScriptConfig.setRunMarketLinuxUser(dataJson.getString("authMart"));
                        }
                    }
                }
            }
            return dataDevScriptConfig;
        } catch (Exception e) {
            logger.error("调用ugdap接口报错：" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
