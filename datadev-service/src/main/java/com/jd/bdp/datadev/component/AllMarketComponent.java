package com.jd.bdp.datadev.component;

import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.jcm.api.common.util.EntityResult;
import com.jd.bdp.jcm.api.common.util.JDResponse;
import com.jd.bdp.jcm.api.market.model.dto.MarketExternalDTO;
import com.jd.bdp.jcm.api.market.service.external.MarketExternalInterface;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhanglei68.
 * @date 2019-03-04.
 * @time 16:16.
 */
public class AllMarketComponent implements InitializingBean {

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Autowired
    private ClusterJSFInterface jsfInterface;

    @Autowired
    private MarketExternalInterface marketExternalInterface;

    @Value("${jsf.jsm.appId}")
    private String jsmAppId;

    @Value("${jsf.jsm.token}")
    private String jsmToken;

    private Map<Long, ClusterHadoopMarketDto> MARKET_ID_HASHMAP = new HashMap<Long,ClusterHadoopMarketDto>();
    private Map<String,ClusterHadoopMarketDto>  MARKET_Code_HASHMAP = new HashMap<String,ClusterHadoopMarketDto>();

    @Override
    public void afterPropertiesSet() throws Exception {
       // getAllMarket();
    }
    private void getAllMarket(){
        JsfResultDto jsfResultDto = jsfInterface.getAllHadoopMarkets(appId, appToken, new Date().getTime());
        if(jsfResultDto.getCode() == 0){
            List dtoList = jsfResultDto.getList();
            for(ClusterHadoopMarketDto clusterHadoopMarketDto : (List<ClusterHadoopMarketDto>)dtoList){
                MARKET_ID_HASHMAP.put(clusterHadoopMarketDto.getMarketId(),clusterHadoopMarketDto);
                MARKET_Code_HASHMAP.put(clusterHadoopMarketDto.getClusterCode()+":"+clusterHadoopMarketDto.getLinuxUser(),clusterHadoopMarketDto);
            }
        }
    }

    public ClusterHadoopMarketDto getMarketById(Long marketId){
        if(MARKET_ID_HASHMAP != null && MARKET_ID_HASHMAP.containsKey(marketId)){
            return MARKET_ID_HASHMAP.get(marketId);
        }
        return null;
    }
    public ClusterHadoopMarketDto getMarketByCode(String clusterCode,String linuxUser){
        String key = clusterCode + ":"+linuxUser;
        if(MARKET_Code_HASHMAP != null && MARKET_Code_HASHMAP.containsKey(key)){
            return MARKET_Code_HASHMAP.get(key);
        }
        return null;
    }
    public void execute()throws Exception{
        getAllMarket();
    }

    public MarketExternalDTO getMarketInfoByCode(String martCode) {
        long time = System.currentTimeMillis();
        String sign = DigestUtils.md5DigestAsHex((jsmAppId + jsmToken + time).getBytes());
        try {
            JDResponse<EntityResult<MarketExternalDTO>> response = marketExternalInterface.getMarketBasicInfoByMarketIdOrCode(jsmAppId, sign, time, null, martCode);
            if (response != null && response.getCode() == 0) {
                return response.getData().getEntity();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
