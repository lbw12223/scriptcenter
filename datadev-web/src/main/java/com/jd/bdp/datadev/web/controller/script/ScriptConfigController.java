package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.api.authorityCenter.AuthorityCenterMarketInfoInterface;
import com.jd.bdp.api.bdptable.MetaDataJSFInterface;
import com.jd.bdp.api.common.JsfResultDto;
import com.jd.bdp.api.think.cluster.ClusterJSFInterface;
import com.jd.bdp.api.think.dto.ClusterHadoopMarketDto;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.MD5Util;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.data.assets.domain.MetaTableColumnBaseInfo;
import com.jd.bdp.datadev.component.AllMarketComponent;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.component.ProjectSpaceRightComponent;
import com.jd.bdp.datadev.datapreview.domain.DataDevDataPreview;
import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.interceptor.ProjectSpaceIdParam;
import com.jd.bdp.domain.authorityCenter.DataBaseDto;
import com.jd.bdp.domain.authorityCenter.MarketInfoDto;
import com.jd.bdp.domain.authorityCenter.TableInfoDto;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopAccount;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopMarket;
import com.jd.bdp.domain.think.clusterBase.ClusterHadoopQueue;
import com.jd.bdp.domain.think.meta.MetaTableInfo;
import com.jd.bdp.domain.urm.right.ApiResultDTO;
import com.jd.bdp.urm.sso.UrmUserHolder;
import com.jd.oss.search.service.CommonSearchService;
import com.jd.oss.search.service.TableFieldsDataJsfInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.util.*;

@Controller
@RequestMapping("/scriptcenter/config/")
public class ScriptConfigController {
    private static final Logger logger = Logger.getLogger(ScriptConfigController.class);
    private static final String MYSQL_CLUSTER = "rdbms"; //关系型数据库过滤掉


    @Value("${datadev.env}")
    private String env;
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String appToken;
    @Value("${sc_marketId}")
    private Long scMarketId;
    @Autowired
    private DataDevScriptConfigService configService;
    @Autowired
    private AuthorityCenterMarketInfoInterface marketInfoInterface;
    @Autowired
    private DataDevClusterAdminService dataDevClusterAdminService;
    @Autowired
    private DataDevDataPreviewService dataPreviewService;
    @Autowired
    private MetaDataJSFInterface metaDataJSFInterface;
    @Autowired
    private AllMarketComponent allMarketComponent;
    @Autowired
    private ClusterJSFInterface jsfInterface;

    @Autowired
    private DataDevCenterService dataDevCenterService;

    @Autowired
    private CommonSearchService commonSearchService;

    @Autowired
    private TableFieldsDataJsfInterface tableFieldsDataJsfInterface;

    @Value("${xingtu.appId}")
    private String xingtuAppId;
    @Value("${xingtu.token}")
    private String xingtuToken;

    @Autowired
    private ProjectSpaceRightComponent projectSpaceRightComponent;

    @ExceptionMessageAnnotation(errorMessage = "获取运行配置信息")
    @RequestMapping("/getConfigByErp.ajax")
    @ResponseBody
    public JSONObject getConfigByErp(UrmUserHolder userHolder, @ProjectSpaceIdParam Long projectSpaceId) throws Exception {
//        userHolder.setErp("bjyuanz");
        List<DataDevScriptConfig> list = configService.getConfigsByErp(userHolder.getErp(), projectSpaceId);
        List<DataDevScriptConfig> defaultProjectpaceConfig = new ArrayList<DataDevScriptConfig>(); //projectSpaceRightComponent.getDefaultProjectpaceConfig(projectSpaceId);
        return JSONObjectUtil.getSuccessResultTwoObj(list, null);
    }


    @ExceptionMessageAnnotation(errorMessage = "获取集市列表")
    @RequestMapping("/getMarketByErp.ajax")
    @ResponseBody
    public JSONObject getMarketByErp(UrmUserHolder userHolder, @ProjectSpaceIdParam Long projectSpaceId) throws Exception {
        try {

            ApiResultDTO apiResultDTO = dataDevCenterService.getGrantAuthorityMarketForBuffalo(userHolder.getErp(), projectSpaceId);
            logger.error("========================getMarketByErp" + JSONObject.toJSONString(apiResultDTO));
            if (apiResultDTO.isSuccess()) {
                List<MarketInfoDto> list = new ArrayList<MarketInfoDto>();
                for (MarketInfoDto marketInfoDto : (List<MarketInfoDto>) apiResultDTO.getList()) {
                    if (!marketInfoDto.getClusterCode().equals(MYSQL_CLUSTER)) {
                        list.add(marketInfoDto);
                    }
                }
                return JSONObjectUtil.getSuccessResult(list);
            }


        } catch (Exception e) {
            logger.error("==================getMarketByErp:", e);
        }
        return JSONObjectUtil.getSuccessResult(new ArrayList<MarketInfoDto>());
    }

    @ExceptionMessageAnnotation(errorMessage = "获取队列列表")
    @RequestMapping("/getQueueByErp.ajax")
    @ResponseBody
    public JSONObject getQueueByErp(UrmUserHolder userHolder, ClusterHadoopQueue clusterHadoopQueue, @ProjectSpaceIdParam Long projectSpaceId) throws Exception {
        try {

            String marketUser = clusterHadoopQueue.getLinuxUser();
            String acountCode = clusterHadoopQueue.getProductionAccountCode();

            clusterHadoopQueue.setOperator(userHolder.getErp());
            ApiResultDTO apiResultDTO = dataDevCenterService.getGrantAuthorityQueueOneAccountInMarketForBuffalo(marketUser, acountCode, userHolder.getErp(), projectSpaceId);
            if (apiResultDTO.isSuccess()) {
                logger.error(JSONObject.toJSONString(apiResultDTO.getList()));
                return JSONObjectUtil.getSuccessResult(apiResultDTO.getList());
            }

        } catch (Exception e) {
            logger.error("==================getQueueByErp:" + e.getMessage());
        }
        return JSONObjectUtil.getSuccessResult(new ArrayList<ClusterHadoopQueue>());
    }

    @ExceptionMessageAnnotation(errorMessage = "获取生成账号")
    @RequestMapping("/getAccountByErp.ajax")
    @ResponseBody
    public JSONObject getAccountByErp(UrmUserHolder userHolder, ClusterHadoopAccount clusterHadoopAccount, @ProjectSpaceIdParam Long projectSpaceId) throws Exception {
        try {
            String marketUser = clusterHadoopAccount.getLinuxUser();

            clusterHadoopAccount.setOperator(userHolder.getErp());
            ApiResultDTO apiResultDTO = dataDevCenterService.getGrantAuthorityProductionAccountInMarketForBuffalo(marketUser, userHolder.getErp(), projectSpaceId);
            if (apiResultDTO.isSuccess()) {
                return JSONObjectUtil.getSuccessResult(apiResultDTO.getList());
            }
        } catch (Exception e) {
            logger.error("==================getAccountByErp:" + e.getMessage());
        }
        return JSONObjectUtil.getSuccessResult(new ArrayList<ClusterHadoopAccount>());
    }

    @RequestMapping("/getDbByErp.ajax")
    @ResponseBody
    public JSONObject getDbByErp(UrmUserHolder userHolder, DataBaseDto dataBaseDto) throws Exception {
        try {
            Long marketId = dataBaseDto.getMarketId();
            ApiResultDTO apiResultDTOMarket = marketInfoInterface.getMarketByIdKnowUgdap(appId, appToken, System.currentTimeMillis(), marketId);
            int mCode = apiResultDTOMarket.getCode();
            if (mCode == 0) {
                if (apiResultDTOMarket.getObj() != null) {
                    ClusterHadoopMarket clusterHadoopMarket = (ClusterHadoopMarket) apiResultDTOMarket.getObj();
                    boolean ugdapFlag = (clusterHadoopMarket.getIsUgdap() == 1 ? false : true);
                    boolean isClusterAdmin = dataDevClusterAdminService.getClusterAdminByErp(userHolder.getErp());
                    if (!ugdapFlag && isClusterAdmin) {
                        //只有非ugdap并且是管理员才从这里拿
                        logger.error("getDbByErp  " + userHolder.getErp() + "是集群管理员");
                        List<DataBaseDto> list = dataDevClusterAdminService.getMetaDbInfoList(marketId, "");
                        return JSONObjectUtil.getSuccessResult(list);

                    } else {
                        dataBaseDto.setOperator(userHolder.getErp());
                        logger.error("=======================" + JSONObject.toJSONString(dataBaseDto));
                        ApiResultDTO apiResultDTO = marketInfoInterface.getGrantAuthorityDBOneAccountInMarket(appId, appToken, System.currentTimeMillis(), dataBaseDto);
                        logger.error("=======================" + JSONObject.toJSONString(apiResultDTO));
                        if (apiResultDTO.isSuccess()) {
                            return JSONObjectUtil.getSuccessResult(apiResultDTO.getList());
                        }
                    }
                } else {
                    logger.error("getDbByErp   根据marketId :" + marketId + "获取集市信息失败，成功，但是集市不存在。");
                    throw new Exception("getDbByErp   根据marketId :" + marketId + "获取集市信息失败，成功，但是集市不存在。");
                }

            } else {
                logger.error("getDbByErp   根据marketId :" + marketId + "获取集市信息失败，code：" + apiResultDTOMarket.getCode() + " message:" + apiResultDTOMarket.getMessage());
                throw new Exception("getDbByErp   根据marketId :" + marketId + "获取集市信息失败，code：" + apiResultDTOMarket.getCode() + " message:" + apiResultDTOMarket.getMessage());
            }


        } catch (Exception e) {
            logger.error("==================getDbByErp:" + e.getMessage());
        }
        return JSONObjectUtil.getSuccessResult(new ArrayList<DataBaseDto>());
    }

    @RequestMapping("/getTableByErp.ajax")
    @ResponseBody
    public JSONObject getTableByErp(UrmUserHolder userHolder, TableInfoDto tableInfoDto) throws Exception {
        try {
            Long marketId = tableInfoDto.getMarketId();
            ApiResultDTO apiResultDTOMarket = marketInfoInterface.getMarketByIdKnowUgdap(appId, appToken, System.currentTimeMillis(), marketId);
            int mCode = apiResultDTOMarket.getCode();
            if (mCode == 0) {
                if (apiResultDTOMarket.getObj() != null) {
                    ClusterHadoopMarket clusterHadoopMarket = (ClusterHadoopMarket) apiResultDTOMarket.getObj();
                    boolean ugdapFlag = (clusterHadoopMarket.getIsUgdap() == 1 ? false : true);
                    boolean isClusterAdmin = dataDevClusterAdminService.getClusterAdminByErp(userHolder.getErp());
                    if (!ugdapFlag && isClusterAdmin) {
                        logger.error("getTableByErp  " + userHolder.getErp() + "是集群管理员");
                        List<MetaTableInfo> list = dataDevClusterAdminService.getTableListByMarketAndDb(marketId, tableInfoDto.getDbName(), tableInfoDto.getTbName(), userHolder.getErp());
                        return JSONObjectUtil.getSuccessResult(list);
                    } else {
                        tableInfoDto.setOpretor(userHolder.getErp());
                        logger.error("=======================" + JSONObject.toJSONString(tableInfoDto));
                        if (StringUtils.isBlank(tableInfoDto.getTbName())) {
                            tableInfoDto.setTbName("");
                        }
                        ApiResultDTO apiResultDTO = marketInfoInterface.getGrantAuthorityTBOneAccountInMarket(appId, appToken, System.currentTimeMillis(), tableInfoDto);
                        logger.error("=======================" + JSONObject.toJSONString(apiResultDTO));
                        if (apiResultDTO.isSuccess()) {
                            return JSONObjectUtil.getSuccessResult(apiResultDTO.getList());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("==================getTableByErp:", e);
        }
        return JSONObjectUtil.getSuccessResult(new ArrayList<TableInfoDto>());
    }


    @RequestMapping("/getColumns.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getColumns(UrmUserHolder userHolder,
                                             Long marketId,
                                             String accountCode,
                                             String dbName,
                                             String tbName,
                                             String searchWord,
                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "rows", defaultValue = "20") Integer rows) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        try {
            List<MetaTableColumnBaseInfo> list = new ArrayList<MetaTableColumnBaseInfo>();
            if (marketId != null && marketId > 0 && StringUtils.isNotBlank(dbName) && StringUtils.isNotBlank(tbName)) {

                JsfResultDto apiResultDTO = metaDataJSFInterface.findMetaColumn(appId, appToken, System.currentTimeMillis(), marketId, dbName, tbName, 1, 100000);

                // ApiResultDTO apiResultDTO = marketInfoInterface.getTableColumnInfos(appId, appToken, System.currentTimeMillis(), metaColumnInfo);
                logger.error("===================getColumns==" + JSONObject.toJSONString(apiResultDTO));
                if (apiResultDTO.getCode().equals(0)) {
                    list = apiResultDTO.getList();
                }
                if (StringUtils.isNotBlank(searchWord)) {
                    List<MetaTableColumnBaseInfo> resList = new ArrayList<MetaTableColumnBaseInfo>();
                    for (int j = 0; j < list.size(); j++) {
                        MetaTableColumnBaseInfo columnInfo = list.get(j);
                        String name = columnInfo.getColumnName();
                        String desc = columnInfo.getComment();

                        if (name.indexOf(searchWord) != -1 || desc.indexOf(searchWord) != -1) {
                            resList.add(columnInfo);
                        }
                    }
                    list = resList;
                }
            }
            pageResultDTO.setRecords((long) list.size());
            pageResultDTO.setSuccess(true);
            pageResultDTO.setRows(list);
        } catch (Exception e) {
            logger.error("=========================" + e.getMessage());
            pageResultDTO.setSuccess(false);
            pageResultDTO.setRecords(0L);
            pageResultDTO.setMessage("获取列表失败！" + e.getMessage());
        }
        pageResultDTO.setPage(page);
        pageResultDTO.setLimit(rows);
        pageResultDTO.setMessage("获取成功");
        return AjaxUtil.gridJson(pageResultDTO);
    }

    @RequestMapping("/getAllDbs.ajax")
    @ResponseBody
    public JSONObject getAllDbs(Long marketId) throws Exception {
        try {
            if (marketId != null && marketId > 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("marketId", marketId);
                jsonObject.put("limitSize", 10000);
                JsfResultDto jsfResultDto = jsfInterface.getDbListByMarketId(appId, appToken, System.currentTimeMillis(), jsonObject.toJSONString());
                int mCode = jsfResultDto.getCode();
                if (mCode == 0) {
                    List<DataBaseDto> dataBaseDtos = jsfResultDto.getList();
                    return JSONObjectUtil.getSuccessResult(dataBaseDtos);
                }
            }
        } catch (Exception e) {
            logger.error("==================getTableByErp:" + e.getMessage());
        }
        return JSONObjectUtil.getSuccessResult(new ArrayList<DataBaseDto>());
    }

    @RequestMapping("/getAllTablesOld.ajax")
    @ResponseBody
    public JSONObject getAllTables(Long marketId, String dbName, String searchWord, @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        JSONObject result = new JSONObject();
        List<com.jd.bdp.data.assets.domain.api.MetaTableInfo> resultTbs = new ArrayList<com.jd.bdp.data.assets.domain.api.MetaTableInfo>();
        try {
            JsfResultDto jsfResultDto = metaDataJSFInterface.findMetaTable(appId, appToken, System.currentTimeMillis(), marketId, dbName, searchWord, pageNumber, pageSize);
            if (jsfResultDto != null) {
                logger.error("===============查询所有表结果：" + JSONObject.toJSONString(jsfResultDto));
            }
            if (jsfResultDto != null && jsfResultDto.getCode() == 0) {
                resultTbs = jsfResultDto.getList();
                for (com.jd.bdp.data.assets.domain.api.MetaTableInfo tableInfo : resultTbs) {
                    ClusterHadoopMarketDto marketDto = allMarketComponent.getMarketByCode(tableInfo.getClusterCode(), tableInfo.getLinuxUser());
                    if (marketDto != null) {
                        tableInfo.setMarketId(marketDto.getMarketId().toString());
                        tableInfo.setMarketName(marketDto.getMarketName());
                    } else {
                        tableInfo.setMarketName("--");
                    }
                    if (StringUtils.isBlank(tableInfo.getMemo())) {
                        tableInfo.setMemo("");
                    }
                }
                result.put("total", jsfResultDto.getTotalSize());
                result.put("num", resultTbs.size());
            } else {
                result.put("total", 0);
                result.put("num", 0);
            }
        } catch (Exception e) {
            logger.error("===========获取所有表报错：" + e.getMessage());
            result.put("total", 0);
            result.put("num", 0);
        }

//
//        for (int i = pageNumber * pageSize; i < (pageNumber + 1) * pageSize; i++) {
//            com.jd.bdp.data.assets.domain.api.MetaTableInfo metaTableInfo = new com.jd.bdp.data.assets.domain.api.MetaTableInfo();
//            metaTableInfo.setTbName("wxl_test_c_erp_dataassets_20190124_03");
//            metaTableInfo.setMemo("fsdfsdfs表描述" + i);
//            metaTableInfo.setMarketName("商城集市");
//            metaTableInfo.setDbName("dev");
//            metaTableInfo.setMarketId("591");
//            metaTableInfo.setLinuxUser("mart_sc");
//            metaTableInfo.setClusterCode("sc");
//            resultTbs.add(metaTableInfo);
//        }
//        result.put("total", 113);
//        result.put("num", 10);
//
        result.put("list", resultTbs == null ? new ArrayList<com.jd.bdp.data.assets.domain.api.MetaTableInfo>() : resultTbs);
        return JSONObjectUtil.getSuccessResult(result);
    }

    @RequestMapping("/getAllTables.ajax")
    @ResponseBody
    public JSONObject getAllTablesNew(@RequestParam(value = "searchWord", defaultValue = "*") String searchWord,
                                      @RequestParam(value = "martCode", defaultValue = "") String martCode,
                                      @RequestParam(value = "dbName", defaultValue = "") String dbName,
                                      @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        JSONObject result = new JSONObject();
        JSONArray list = new JSONArray();
        try {
            JSONObject params = new JSONObject();
            params.put("page", pageNumber);
            params.put("pageSize", pageSize);
            params.put("query", searchWord);
            if (StringUtils.isNotBlank(martCode)) {
                params.put("martCode", martCode);
            }
            if (StringUtils.isNotBlank(dbName)) {
                params.put("dbName", dbName);
            }
            Long time = System.currentTimeMillis();
            String sign = MD5Util.getMD5Str(xingtuAppId + xingtuToken + time);
            logger.error("===============xingtuAppId:" + xingtuAppId);
            logger.error("===============xingtuToken:" + xingtuToken);
            logger.error("===============sign:" + sign);
            logger.error("===============time:" + time);
            logger.error("===============data:" + params.toJSONString());
            JSONObject apiResult;
            // 使用mock jsf接口
            apiResult = commonSearchService.search(xingtuAppId, sign, time, params.toJSONString());
            logger.error("===============查询所有表结果：" + apiResult);
            if (apiResult != null && apiResult.getInteger("code") == 0) {
                JSONObject data = apiResult.getJSONObject("data");
                list = data.getJSONArray("datas");
                for (Object o : list) {
                    JSONObject tableInfo = (JSONObject) o;
                    ClusterHadoopMarketDto marketDto = allMarketComponent.getMarketByCode(tableInfo.getString("cluster"), tableInfo.getString("martCode"));
                    if (marketDto != null) {
                        tableInfo.put("marketId", marketDto.getMarketId().toString());
                        tableInfo.put("marketName", marketDto.getMarketName());
                    } else {
                        tableInfo.put("marketName", "--");
                    }
                    tableInfo.put("memo", tableInfo.getString("tbComment") == null ? "" : tableInfo.getString("tbComment"));
                }

                result.put("total", data.getInteger("totalRecord"));
                result.put("num", list.size());
            } else {
                result.put("total", 0);
                result.put("num", 0);
            }
        } catch (Exception e) {
            logger.error("===========获取所有表报错：" + e.getMessage());
            result.put("total", 0);
            result.put("num", 0);
        }
        result.put("list", list);
        return JSONObjectUtil.getSuccessResult(result);
    }

    @RequestMapping("/getAllColumnsOld.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getAllColumns(UrmUserHolder userHolder, Long marketId, String dbName, String tbName,
                                                @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "20") Integer rows,
                                                @RequestParam(value = "searchWord", defaultValue = "") String searchWord) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        try {
            List<MetaTableColumnBaseInfo> list = new ArrayList<MetaTableColumnBaseInfo>();

            logger.error("====getAllColumns : marketId :" + marketId + " dbName:" + dbName + " tbName:" + tbName);
            JsfResultDto apiResultDTO = metaDataJSFInterface.findMetaColumn(appId, appToken, System.currentTimeMillis(), marketId, dbName, tbName, 1, 100000);
            if (apiResultDTO.getCode() == 0) {
                list = apiResultDTO.getList();
            }
            logger.error("==================:" + JSONObject.toJSONString(apiResultDTO));
            List<MetaTableColumnBaseInfo> searchList = list;
            if (StringUtils.isNotBlank(searchWord)) {
                searchList = new ArrayList<MetaTableColumnBaseInfo>();
                for (MetaTableColumnBaseInfo metaColumnInfo : list) {
                    if (StringUtils.isNotBlank(metaColumnInfo.getColumnName()) && metaColumnInfo.getColumnName().indexOf(searchWord) != -1
                            || StringUtils.isNotBlank(metaColumnInfo.getComment()) && metaColumnInfo.getComment().indexOf(searchWord) != -1) {
                        searchList.add(metaColumnInfo);
                    }
                }
            }
            int from = (page - 1) * rows;
            int to = (page * rows) > searchList.size() ? searchList.size() : (page * rows);
            List<MetaTableColumnBaseInfo> result = (page - 1) * rows > searchList.size() ? new ArrayList<MetaTableColumnBaseInfo>() : searchList.subList(from, to);
            pageResultDTO.setRecords(Long.valueOf(searchList.size()));
            pageResultDTO.setSuccess(true);
            pageResultDTO.setRows(result);
        } catch (Exception e) {
            logger.error("=========================" + e.getMessage());
            pageResultDTO.setSuccess(false);
            pageResultDTO.setRecords(0L);
            pageResultDTO.setMessage("获取列表失败！" + e.getMessage());
        }

        pageResultDTO.setPage(page);
        pageResultDTO.setLimit(rows);
        pageResultDTO.setMessage("获取成功");
        return AjaxUtil.gridJson(pageResultDTO);
    }

    @RequestMapping("/getAllColumns.ajax")
    @ResponseBody
    public net.sf.json.JSONObject getAllColumnsNew(UrmUserHolder userHolder, String martCode, String cluster, String dbName, String tbName,
                                                   @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "rows", defaultValue = "20") Integer rows,
                                                   @RequestParam(value = "searchWord", defaultValue = "") String searchWord) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        try {
//            List<MetaTableColumnBaseInfo> list = new ArrayList<MetaTableColumnBaseInfo>();
            logger.error("====getAllColumns : martCode :" + martCode + " cluster:" + cluster + " dbName:" + dbName + " tbName:" + tbName);

            JSONObject params = new JSONObject();
            params.put("martCode", martCode);
            params.put("cluster", cluster);
            params.put("dbName", dbName);
            params.put("tbName", tbName);
            Long time = System.currentTimeMillis();
            String sign = MD5Util.getMD5Str(xingtuAppId + xingtuToken + time);
            logger.error("===========xingtuAppId:" + xingtuAppId);
            logger.error("===========xingtuToken:" + xingtuToken);
            logger.error("===========sign:" + sign);
            logger.error("===========time:" + time);
            logger.error("===========data:" + params.toJSONString());
            JSONObject apiResult;
            apiResult = tableFieldsDataJsfInterface.queryTBFields(xingtuAppId, sign, time, params.toJSONString());
            logger.error("===============查询表字段：" + apiResult);

            JSONArray columns = new JSONArray();
            if (apiResult != null && apiResult.getInteger("code") == 0) {
                JSONObject data = apiResult.getJSONObject("data");
                JSONArray tmp = data.getJSONArray("columns");
                if (tmp != null) {
                    for (Object o : tmp) {
                        JSONObject json = (JSONObject) o;
                        JSONObject object = new JSONObject();
                        object.put("columnName", json.getString("name"));
                        object.put("comment", json.getString("comment"));
                        object.put("columnType", json.getString("type"));
                        columns.add(object);
                    }
                }
            }
            List<JSONObject> searchList = columns.toJavaList(JSONObject.class);
            if (StringUtils.isNotBlank(searchWord)) {
                searchList = new ArrayList<JSONObject>();
                for (Object column : columns) {
                    JSONObject json = (JSONObject) column;
                    String columnName = json.getString("columnName");
                    String columnComment = json.getString("comment");
                    if (StringUtils.isNotBlank(columnName) && columnName.indexOf(searchWord) != -1
                            || StringUtils.isNotBlank(columnComment) && columnComment.indexOf(searchWord) != -1) {
                        searchList.add(json);
                    }
                }
            }
            int from = (page - 1) * rows;
            int to = (page * rows) > searchList.size() ? searchList.size() : (page * rows);
            List<JSONObject> result = (page - 1) * rows > searchList.size() ? new ArrayList<JSONObject>() : searchList.subList(from, to);
            pageResultDTO.setRecords(Long.valueOf(searchList.size()));
            pageResultDTO.setSuccess(true);
            pageResultDTO.setRows(result);
        } catch (Exception e) {
            logger.error("=========================" + e.getMessage());
            pageResultDTO.setSuccess(false);
            pageResultDTO.setRecords(0L);
            pageResultDTO.setMessage("获取列表失败！" + e.getMessage());
        }

        pageResultDTO.setPage(page);
        pageResultDTO.setLimit(rows);
        pageResultDTO.setMessage("获取成功");
        return AjaxUtil.gridJson(pageResultDTO);
    }

    @RequestMapping("/save.ajax")
    @ResponseBody
    public JSONObject save(UrmUserHolder userHolder, @RequestBody List<DataDevScriptConfig> configs) throws Exception {
        List<DataDevScriptConfig> result = new ArrayList<DataDevScriptConfig>();
        for (DataDevScriptConfig config : configs) {
            if (config.getStatus() == null) {
                result.add(config);
                continue;
            } else if (config.getStatus() == 1) {
                config.setOriId(config.getId());
                config.setCreator(userHolder.getErp());
                config.setMender(userHolder.getErp());
                config.setOwner(userHolder.getErp());
                configService.addConfig(config);
                config.setStatus(null);
                result.add(config);
            } else if (config.getStatus() == 2) {
                configService.deleteConfig(config.getId());
            } else if (config.getStatus() == 3) {
                config.setMender(userHolder.getErp());
                configService.updateConfig(config);
                config.setStatus(null);
                result.add(config);
            }
        }
        configService.sortByOrder(result);

        return JSONObjectUtil.getSuccessResult("保存成功", result);
    }

    @RequestMapping("initMergeConfig.ajax")
    @ResponseBody
    public JSONObject initMergeConfig(UrmUserHolder urmUserHolder, String marketIds) throws Exception {
//        if(urmUserHolder.getErp().equals("bjyuanz")||urmUserHolder.getErp().equals("zhanglei847") || urmUserHolder.getErp().equals("zhangrui156") || urmUserHolder.getErp().equals("wangxiaoli76")){
        if (true) {
            logger.error("initMergeConfig==marketIds:" + marketIds);
            HoldDoubleValue<List<Long>, List<Long>> listListHoldDoubleValue = configService.initForMarketMerge(marketIds);
            return JSONObjectUtil.getSuccessResult("更新成功：" + JSONObject.toJSONString(listListHoldDoubleValue.a) + "条，====================失败：" + JSONObject.toJSONString(listListHoldDoubleValue.b) + "条", null);
        } else {
            return JSONObjectUtil.getFailResult("无权限操作", null);
        }
    }


    @RequestMapping("/getSqlTipSearchTableName.ajax")
    @ResponseBody
    public JSONObject getSqlTipSearchTableName(UrmUserHolder userHolder, final String tableName, final Long marketId, final String dbName) throws Exception {

        List<com.jd.bdp.data.assets.domain.api.MetaTableInfo> resultTbs = new ArrayList<com.jd.bdp.data.assets.domain.api.MetaTableInfo>();
        if (true) {
            try {
                JsfResultDto jsfResultDto = metaDataJSFInterface.findMetaTable(appId, appToken, new Date().getTime(), marketId, dbName, tableName, 1, 10);
                if (jsfResultDto != null && jsfResultDto.getCode() == 0) {
                    logger.error("===============getSqlTipSearchTableName：" + JSONObject.toJSONString(jsfResultDto));
                    resultTbs = jsfResultDto.getList();
                    for (com.jd.bdp.data.assets.domain.api.MetaTableInfo tableInfo : resultTbs) {
                        ClusterHadoopMarketDto marketDto = allMarketComponent.getMarketByCode(tableInfo.getClusterCode(), tableInfo.getLinuxUser());
                        if (marketDto != null) {
                            tableInfo.setMarketId(marketDto.getMarketId().toString());
                            tableInfo.setMarketName(marketDto.getMarketName());
                        }
                        if (StringUtils.isBlank(tableInfo.getMemo())) {
                            tableInfo.setMemo("");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("===========获取所有表报错：" + e.getMessage());
            }
        }
        if (false) {
            resultTbs.add(new com.jd.bdp.data.assets.domain.api.MetaTableInfo() {
                {
                    setMarketName("电子文娱");
                    setMarketId(String.valueOf(marketId));
                    setTbName(tableName + "1");
                    setDbName(dbName);
                }
            });
            resultTbs.add(new com.jd.bdp.data.assets.domain.api.MetaTableInfo() {
                {
                    setMarketName("电子文娱");
                    setMarketId(String.valueOf(marketId));
                    setTbName(tableName + "2");
                    setDbName(dbName);
                }
            });
            resultTbs.add(new com.jd.bdp.data.assets.domain.api.MetaTableInfo() {
                {
                    setMarketName("电子文娱");
                    setMarketId(String.valueOf(marketId));
                    setTbName(tableName + "3");
                    setDbName(dbName);
                }
            });
        }

        return JSONObjectUtil.getSuccessResult("success", resultTbs);
    }

    @RequestMapping("/getSqlTipSearchTableColumns.ajax")
    @ResponseBody
    public JSONObject getSqlTipSearchTableColumns(UrmUserHolder userHolder, String tableName, Long marketId, String dbName) throws Exception {
        ClusterHadoopMarketDto clusterHadoopMarketDto = allMarketComponent.getMarketById(marketId);


        List<MetaTableColumnBaseInfo> list = new ArrayList<MetaTableColumnBaseInfo>();
        logger.error("====getSqlTipSearchTableColumns : marketId :" + marketId + " dbName:" + dbName + " tbName:" + tableName);

        JsfResultDto apiResultDTO = metaDataJSFInterface.findMetaColumn(appId, appToken, System.currentTimeMillis(), marketId, dbName, tableName, 1, 100000);
        if (apiResultDTO.getCode() == 0) {
            list = apiResultDTO.getList();
        }
        logger.error("==================:" + JSONObject.toJSONString(apiResultDTO));

        if (false) {
            for (int index = 0; index < 20; index++) {
                final int _index = index;
                list.add(new MetaTableColumnBaseInfo() {
                    {
                        setColumnType("string" + _index);
                        setColumnName("list" + _index);
                    }
                });
            }
            list.add(new MetaTableColumnBaseInfo() {
                {
                    setColumnType("string");
                    setColumnName("list");
                }
            });
            list.add(new MetaTableColumnBaseInfo() {
                {
                    setColumnType("string");
                    setColumnName("age");
                }
            });
            list.add(new MetaTableColumnBaseInfo() {
                {
                    setColumnType("string");
                    setColumnName("users");
                }
            });
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("market", clusterHadoopMarketDto);
        result.put("columns", list);
        return JSONObjectUtil.getSuccessResult("success", result);

    }

    @RequestMapping("/dataPreview.ajax")
    @ResponseBody
    public JSONObject doDataPreview(UrmUserHolder userHolder, String clusterCode, String linuxUser, String
            dbName, String tbName) {
        try {
            DataDevDataPreview preview = dataPreviewService.doDataPreview(clusterCode, linuxUser, dbName, tbName, userHolder.getErp());
            return JSONObjectUtil.getSuccessResult(preview.getRunDetailId());
        } catch (Exception e) {
            return JSONObjectUtil.getSuccessResult(null);
        }
    }

    @RequestMapping("/validResult.ajax")
    @ResponseBody
    public JSONObject validResult(UrmUserHolder userHolder, Long runDetailId) {
        JSONObject resultObj = new JSONObject();
        try {
            HoldDoubleValue<Boolean, Integer> holdDoubleValue = dataPreviewService.validData(runDetailId);
            resultObj.put("result", holdDoubleValue.a);
            resultObj.put("dataCount", holdDoubleValue.b);
            resultObj.put("error", false);
            return JSONObjectUtil.getSuccessResult(resultObj);
        } catch (Exception e) {
            resultObj.put("error", true);
            return JSONObjectUtil.getSuccessResult(resultObj);
        }
    }


    public static void main(String[] args) throws Exception {
//        String ss="%E5%94%AE%E5%90%8E";
        String ss = "%25E5%2594%25AE%25E5%2590%258E";
        System.out.println(URLDecoder.decode(ss, "utf-8"));
//        System.out.println(System.currentTimeMillis());
    }

}
