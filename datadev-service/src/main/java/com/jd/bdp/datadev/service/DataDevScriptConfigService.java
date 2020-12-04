package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.datadev.domain.HoldDoubleValue;

import java.util.List;

public interface DataDevScriptConfigService {

    /**
     * 根据erp查找配置信息
     * 排序规则 如果order为空 就以id为主 如果id也为空order默认最低
     * @param erp
     * @return
     * @throws Exception
     */
    List<DataDevScriptConfig> getConfigsByErp(String erp, Long projectSpaceId)throws Exception;

    /**
     * 根据id查找
     * @param id
     * @return
     * @throws Exception
     */
    DataDevScriptConfig getConfigById(Long id)throws Exception;

    /**
     * 更新配置
     * @return
     * @throws Exception
     */
    void updateConfig(DataDevScriptConfig config)throws Exception;

    /**
     * 删除配置
     * @throws Exception
     */
    void deleteConfig(Long id)throws Exception;

    void addConfig(DataDevScriptConfig config)throws Exception;


    void sortByOrder(List<DataDevScriptConfig> list)throws Exception;



    HoldDoubleValue<List<Long>,List<Long>> initForMarketMerge(String marketIds)throws Exception;

    /**
     * 调用ugdap接口查询生产账号合并之前的集群code与集市code 分别置于runClusterCode 与 runMarketLinuxUser 字段
     * @param clusterCode
     * @param martCode
     * @param account
     * @return
     * @throws Exception
     */
    DataDevScriptConfig getOriClusterAndMarketByMarketCode(String clusterCode,String martCode,String account)throws Exception;


}
