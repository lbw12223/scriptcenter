package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.api.think.meta.MetaInfoInterface;
import com.jd.bdp.datadev.service.DataDevClusterAdminService;
import com.jd.bdp.domain.authorityCenter.DataBaseDto;
import com.jd.bdp.domain.authorityCenter.MarketInfoDto;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopMarket;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import com.jd.bdp.domain.think.meta.MetaDbInfo;
import com.jd.bdp.domain.think.meta.MetaTableInfo;
import com.jd.bdp.domain.urm.right.ApiResultDTO;
import com.jd.bdp.urm2.api.dto.JSFResultDTO;
import com.jd.bdp.urm2.api.right.V2UrmRightInterface;
import com.jd.bdp.urm2.domain.right.RightDto;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class DataDevClusterAdminServiceImpl implements DataDevClusterAdminService {
    private static final Logger logger = Logger.getLogger(DataDevClusterAdminServiceImpl.class);

    @Autowired
    private V2UrmRightInterface v2UrmRightInterface;
    @Autowired
    private ClusterJSFInterface clusterJSFInterface;


    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String token;
    @Value("${cluster.admin.group}")
    private String clusterAdminGroup;
    @Value("${cluster.admin.appCode}")
    private String clusterAdminAppCode;
    private final String defaultOperator = "bjlvyanmeng";
    /**
     * 检查erp是否被授予了集群管理员的角色
     * @param erp
     * @return
     * @throws Exception
     */
    @Override
    public boolean getClusterAdminByErp(String erp) throws Exception {
//        return true;
        boolean isClusterAdmin = false;
        if(1 > 0){
            return false;
        }
        if(StringUtils.isNotBlank(erp)){
            RightDto rightDto = new RightDto();
            rightDto.setGroup(clusterAdminGroup);
            rightDto.setAppCode(clusterAdminAppCode);
            rightDto.setOperator(erp);
            rightDto.setErp(erp);
            logger.error("getClusterAdminByErp验证erp："+erp+"  是否被授予了集群管理员的资源，参数："+JSONObject.fromObject(rightDto).toString());
            JSFResultDTO jsfResultDTO = v2UrmRightInterface.getRight(appId,token,System.currentTimeMillis(),rightDto);
            logger.error("getClusterAdminByErp验证erp："+erp+"  是否被授予了集群管理员的资源，结果："+JSONObject.fromObject(jsfResultDTO).toString());
            int code = jsfResultDTO.getCode();
            if(code == 0  &&  jsfResultDTO.getList() != null && jsfResultDTO.getList().size() > 0){
                //当获取资源成功并且  获取到的资源不为空时说明 该用户被授予了  集群管理员的资源。
                isClusterAdmin = true;
            }else {
                logger.error("getClusterAdminByErp验证erp："+erp+"  是否被授予了集群管理员的资源失败：code："+code+" 错误信息："+jsfResultDTO.getMessage());
//                throw new Exception("getClusterAdminByErp验证erp："+erp+"  是否被授予了集群管理员的资源失败：code："+code+" 错误信息："+jsfResultDTO.getMessage());
            }
        }
        return isClusterAdmin;
    }

    @Override
    public List<MarketInfoDto> getAllMarkets() throws Exception {
        List<MarketInfoDto> allMarketOpen = new ArrayList<MarketInfoDto>();
        JsfResultDto jsfResultDto = clusterJSFInterface.getAllOpenHadoopMarkets(appId,token,System.currentTimeMillis());
        logger.error("getAllMarkets:结果："+JSONObject.fromObject(jsfResultDto).toString());
        int code = jsfResultDto.getCode();
        if(code == 0){
            List<ClusterHadoopMarketDto> list = jsfResultDto.getList();
            if(list != null && list.size()>0){
                for(ClusterHadoopMarketDto clusterHadoopMarket:list){
                    MarketInfoDto marketInfoDto = new MarketInfoDto();
                    marketInfoDto.setClusterCode(clusterHadoopMarket.getClusterCode());
                    marketInfoDto.setClusterId(clusterHadoopMarket.getClusterId());
                    marketInfoDto.setClusterName(clusterHadoopMarket.getClusterName());
                    marketInfoDto.setMarketCode(clusterHadoopMarket.getLinuxUser());
                    marketInfoDto.setMarketUser(clusterHadoopMarket.getLinuxUser());
                    marketInfoDto.setMarketId(clusterHadoopMarket.getMarketId().toString());
                    marketInfoDto.setMarketName(clusterHadoopMarket.getMarketName());
                    marketInfoDto.setMarketMagagers(clusterHadoopMarket.getOwners());
                    marketInfoDto.setIsUgdap((clusterHadoopMarket.getIsUgdap()!=null && clusterHadoopMarket.getIsUgdap()==1)?false:true);
                    allMarketOpen.add(marketInfoDto);
                }
            }else {
                logger.error("getAllMarkets  结果为空");
            }

        }else {
            logger.error("dataDev getAllMarkets发生了异常，code："+code+"  异常信息："+jsfResultDto.getMessage());
            throw new Exception("dataDev getAllMarkets发生了异常，code："+code+"  异常信息："+jsfResultDto.getMessage());
        }
        return allMarketOpen;
    }

    /**
     * 获取某个集市下某个生产账号下的全部队列，不包含父队列
     * @param
     * @param accountId
     * @return
     * @throws Exception
     */
    @Override
    public List<ClusterHadoopQueue> getAllClusterQueueOneMarketOneAccount(Long accountId) throws Exception {
        logger.error("getAllClusterQueueOneMarketOneAccount>>>>>  account:"+accountId);
        List<ClusterHadoopQueue> queuesList = new ArrayList<ClusterHadoopQueue>();
        if(accountId != null){
            JsfResultDto jsfResultDto = clusterJSFInterface.getQueueByAccountId(appId,token,System.currentTimeMillis(),accountId);
            int code = jsfResultDto.getCode();
            if(code == 0){
                if(jsfResultDto.getList() != null){
                    queuesList = jsfResultDto.getList();
                }else {
                    logger.error("getAllClusterQueueOneMarketOneAccount>>>>>  account:"+accountId+"获取队列结果为空");
                }
            }else {
                logger.error("getAllClusterQueueOneMarketOneAccount>>>>>  account:"+accountId+"获取队列失败，code："+code+" message:"+jsfResultDto.getMessage());
                throw new Exception("getAllClusterQueueOneMarketOneAccount>>>>>  account:"+accountId+"获取队列失败，code："+code+" message:"+jsfResultDto.getMessage());
            }
        }else {
            logger.error("getAllClusterQueueOneMarketOneAccount >>>>> accountId 不能为空");
            throw new Exception("getAllClusterQueueOneMarketOneAccount >>>>> accountId 不能为空");
        }
        return queuesList;
    }

    /**
     * 获取集市下所有的生产账号
     * @param marketId
     * @return
     * @throws Exception
     */
    @Override
    public List<ClusterHadoopAccount> getAllClusterHadoopAccountOneMarket(Long marketId) throws Exception {
        logger.error("getAllClusterHadoopAccountOneMarket  集市id："+marketId);
        List<ClusterHadoopAccount> allAccount = new ArrayList<ClusterHadoopAccount>();
        if(marketId != null){
            JsfResultDto jsfResultDto = clusterJSFInterface.getAccountByMarketId(appId,token,System.currentTimeMillis(),marketId);
            int code = jsfResultDto.getCode();
            if(code == 0){
                if(jsfResultDto.getList() != null){
                    allAccount = jsfResultDto.getList();
                }
            }else {
                logger.error("getAllClusterHadoopAccountOneMarket 获取 集市marketId："+marketId+" 下的生产账号失败，code："+code+"  message:"+jsfResultDto.getMessage());
                throw new Exception("getAllClusterHadoopAccountOneMarket 获取 集市marketId："+marketId+" 下的生产账号失败，code："+code+"  message:"+jsfResultDto.getMessage());
            }

        }else {
            logger.error("getAllClusterHadoopAccountOneMarket 获取 集市marketId不能为空");
            throw new Exception("getAllClusterHadoopAccountOneMarket 获取 集市marketId不能为空");
        }
        return allAccount;
    }

    @Override
    public List<DataBaseDto> getMetaDbInfoList(Long marketId, String search) throws Exception {
        List<DataBaseDto> list = new ArrayList<DataBaseDto>();
        logger.error("getMetaDbInfoList>>>>>>marketId:"+marketId+" search:"+search);
        if(marketId != null){
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("marketId",marketId);
          jsonObject.put("key",search);

          JsfResultDto jsfResultDto = clusterJSFInterface.getDbListByMarketId(appId,token,System.currentTimeMillis(),jsonObject.toString());
          int code = jsfResultDto.getCode();
          if(code == 0){
              if(jsfResultDto.getList() != null){
                  list = jsfResultDto.getList();
              }
              return list;
          }else {
              logger.error("getMetaDbInfoList  为集群负责人获取库列表失败，marketId:"+marketId+"  search:"+search+ "  code："+code+" message:"+jsfResultDto.getMessage());
              throw  new Exception("getMetaDbInfoList  为集群负责人获取库列表失败，marketId:"+marketId+"  search:"+search+ "  code："+code+" message:"+jsfResultDto.getMessage());
          }
        }else {
            logger.error("getMetaDbInfoList>>>>>>marketId不能为空。");
            throw new Exception("getMetaDbInfoList>>>>>>marketId不能为空。");
        }
    }

    @Override
    public List<MetaTableInfo> getTableListByMarketAndDb(Long  marketId, String dbName, String search,String operator) throws Exception {
        logger.error("getTableListByMarketAndDb>>>>>>marketId:"+marketId+" search:"+search+"  dbName;"+dbName);
        List<MetaTableInfo> list = new ArrayList<MetaTableInfo>();
        if(marketId != null && StringUtils.isNotBlank(dbName)){
            MetaTableInfo metaTableInfo = new MetaTableInfo();
            metaTableInfo.setMarketId(marketId);
            metaTableInfo.setSerch(search);
            metaTableInfo.setDbName(dbName);
            metaTableInfo.setOperator(operator);
            metaTableInfo.setTableFilter(true);
            logger.error("getTableListByMarketAndDb>>>>>>>>>>>"+JSONObject.fromObject(metaTableInfo).toString());
            JsfResultDto jsfResultDto = clusterJSFInterface.getTbListByMarketIdAndDb(appId,token,System.currentTimeMillis(),metaTableInfo);
            int code = jsfResultDto.getCode();
            if(code == 0){
                if(jsfResultDto.getList() != null){
                    list = jsfResultDto.getList();
                }else {
                    logger.error("getTableListByMarketAndDb>>>>>>marketId:"+marketId +"  dbName;"+dbName+" search:"+search+"获取表为空。");
                }
            }else {
                logger.error("getTableListByMarketAndDb>>>>>>marketId:"+marketId +"  dbName;"+dbName+"获取表失败。code："+code+" message:"+jsfResultDto.getMessage());
                throw new Exception("getTableListByMarketAndDb>>>>>>marketId:"+marketId +"  dbName;"+dbName+"获取表失败。code："+code+" message:"+jsfResultDto.getMessage());
            }
        }else {
            logger.error("getTableListByMarketAndDb>>>>>>marketId:"+marketId +"  dbName;"+dbName+"不能为空。");
            throw new Exception("getTableListByMarketAndDb>>>>>>marketId:"+marketId +"  dbName;"+dbName+"不能为空。");
        }
        logger.error("getTableListByMarketAndDb>>>>>>>"+list.size());
        return list;
    }
}
