package com.jd.bdp.datadev.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.util.HttpUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class DevCenterBuffaloComponent {

    private static final Logger logger = Logger.getLogger(DevCenterBuffaloComponent.class);

    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String token;


    @Value("${devCenter.domain}")
    private String devCenterPrefix;
    @Value("${url.davcenter.envInfo}")
    private String envInfoUrl;

    private String saveUpdateDevCentor = "/devcenter/api/script/updateInfo";


    /**
     * https://cf.jd.com/pages/viewpage.action?pageId=498424815
     * <p>
     * <p>
     * /devcenter/api/script/updateInfo
     * <p>
     * 每次修改的时候 - 保存开发中心接口
     */
    public void savecallBackDevCentor(Long scriptFileId,
                                      String scriptName,
                                      String scriptType,
                                      Integer version,
                                      Long gitProjectId,
                                      String gitProjectFilePath,
                                      Integer fileSize,
                                      String scriptMd5) {

        try {
            /**
             * dataDevScriptId	Integer	脚本ID	是
             * scriptName	String	脚本名称	是
             * scriptType	String	脚本类型	是
             * version	String	版本号	是
             * dataDevProjectId	Integer	项目ID	否
             * dataDevProjectFilePath	String	数据开发脚本项目路径	是
             * fileSize	int	脚本大小	是
             * scriptMd5	String	MD5信息	是
             *
             */
            Map<String, String> params = new HashMap<>();
            params.put("appId", appId);
            params.put("token", token);
            params.put("time", "" + System.currentTimeMillis());


            JSONObject data = new JSONObject();
            data.put("dataDevScriptId", scriptFileId);
            data.put("dataDevScriptVersion",version);
            data.put("md5",scriptMd5);
            data.put("md5Code",scriptMd5);


            data.put("scriptName", scriptName);
            data.put("scriptType", scriptType);
            data.put("version", version);
            data.put("dataDevProjectId", gitProjectId);
            data.put("dataDevProjectFilePath", gitProjectFilePath);
            data.put("fileSize", fileSize);
            data.put("scriptMd5", scriptMd5);


            logger.info("=========更新开发中心内容入参：" + data.toJSONString());
            String entity = HttpUtil.doPostWithParamAndBody(devCenterPrefix + saveUpdateDevCentor, params, data);
            logger.info("=========更新开发中心内容结果：" + entity);
        } catch (Exception e) {

        }
    }

    public JSONArray getDBEnvInfo(String marketCode, String clusterCode) throws Exception {
        JSONObject data = new JSONObject();
        data.put("appId", appId);
        data.put("token", token);
        data.put("time", System.currentTimeMillis());
        data.put("marketCode", marketCode);
        data.put("clusterCode", clusterCode);
        logger.info("=========获取库变量入参：" + data.toJSONString());
        String entity = HttpUtil.doPostWithParamAndBody(devCenterPrefix + envInfoUrl, null, data);
//        entity = "{\n" +
//                "    \"code\": 0,\n" +
//                "    \"list\": [\n" +
//                "        {\n" +
//                "            \"cluseterCode\": \"xx\",\n" +
//                "            \"marketCode\": \"xx\",\n" +
//                "            \"prodDb\": \"MOCK_DB_WCC_SH_1_PROD\",\n" +
//                "            \"variableCode\": \"MOCK_DB_WCC_SH_1\",\n" +
//                "            \"devDb\": \"MOCK_DB_WCC_SH_1_DEV\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"cluseterCode\": \"xx\",\n" +
//                "            \"marketCode\": \"xx\",\n" +
//                "            \"prodDb\": \"MOCK_DB_WCC_SH_2_PROD\",\n" +
//                "            \"variableCode\": \"MOCK_DB_WCC_SH_2\",\n" +
//                "            \"devDb\": \"MOCK_DB_WCC_SH_2_DEV\"\n" +
//                "        }\n" +
//                "    ],\n" +
//                "    \"message\": \"获取集市库变量数据成功\",\n" +
//                "    \"success\": true\n" +
//                "}";
        logger.info("=========获取库变量结果：" + entity);
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(entity);
        } catch (Exception e) {
            logger.error("获取库变量 返回异常");
            throw new Exception("获取库变量 返回异常");
        }
        if (jsonObject.getInteger("code") != 0) {
            logger.error("获取库变量 失败");
            throw new Exception(jsonObject.getString("message"));
        }

        return jsonObject.getJSONArray("list");
    }
}
