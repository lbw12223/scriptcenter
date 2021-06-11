package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import com.jd.bdp.domain.urm.right.ApiResultDTO;

public interface DataDevCenterService {
    /**
     * 上线脚本
     * @param file
     * @param erp
     * @param oldPublish
     * @param runType
     * @return
     * @throws Exception
     */
    DataDevScriptFilePublish upLineScript(DataDevScriptFile file, String erp, DataDevScriptFilePublish oldPublish, Integer runType) throws Exception;

    DataDevScriptFilePublish upLineScriptNew(DataDevScriptFile file, String erp) throws Exception;


    ApiResultDTO getGrantAuthorityMarketForBuffalo(String erp , Long spaceProjectId);

    ApiResultDTO getGrantAuthorityProductionAccountInMarketForBuffalo(String marketUser ,String erp ,Long spaceProjectId) ;

    ApiResultDTO getGrantAuthorityQueueOneAccountInMarketForBuffalo( String marketUser , String accountCode  , String erp , Long spaceProjectId) ;






}
