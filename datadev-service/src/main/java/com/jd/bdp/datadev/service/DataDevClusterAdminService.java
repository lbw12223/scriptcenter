package com.jd.bdp.datadev.service;

import com.jd.bdp.domain.authorityCenter.DataBaseDto;
import com.jd.bdp.domain.authorityCenter.MarketInfoDto;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import com.jd.bdp.domain.think.meta.MetaDbInfo;
import com.jd.bdp.domain.think.meta.MetaTableInfo;

import java.util.List;

public interface DataDevClusterAdminService {

    boolean  getClusterAdminByErp(String erp)throws Exception;

    List<MarketInfoDto> getAllMarkets() throws Exception;

    List<ClusterHadoopQueue> getAllClusterQueueOneMarketOneAccount(Long accountId) throws Exception;

    List<ClusterHadoopAccount> getAllClusterHadoopAccountOneMarket(Long marketId) throws Exception;

    List<DataBaseDto> getMetaDbInfoList(Long marketId, String search) throws Exception;

    List<MetaTableInfo> getTableListByMarketAndDb(Long  marketId, String dbName, String search,String operator)throws Exception;
}
