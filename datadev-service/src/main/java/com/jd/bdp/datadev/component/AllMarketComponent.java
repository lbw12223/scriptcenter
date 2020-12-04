package com.jd.bdp.datadev.component;

import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

}
