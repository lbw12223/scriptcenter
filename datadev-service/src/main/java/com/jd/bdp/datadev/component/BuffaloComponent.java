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
public class BuffaloComponent {

    private static final Logger logger = Logger.getLogger(BuffaloComponent.class);
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String token;

    @Value("${buffalo.domain.name}")
    private String buffalo4Prefix;
    @Value("${url.buffalo4.script.getFileContent}")
    private String scriptGetFileContent;
    @Value("${url.buffalo4.script.taskList}")
    private String taskListUrl;


    /**
     * 调度中心-获取生产侧的脚本内容接口  fileContent
     */
    public JSONObject scriptGetFileContent(String scriptName, Long projectId)throws Exception {
        JSONObject data = new JSONObject();

        data.put("scriptName", scriptName);
        data.put("projectId", projectId);

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("appId", appId);
        long timeMillis = System.currentTimeMillis();
        params.put("time", Long.toString(timeMillis));

        logger.info("-------调度中心-获取脚本内容接口参数：" + params + "; body=" + data);
        String entity = HttpUtil.doPostWithParamAndBody(buffalo4Prefix + scriptGetFileContent, params, data);
        logger.info("-------调度中心-获取脚本内容结果：" + entity);
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(entity);
        }
        catch(Exception e) {
            logger.error("调度中心-获取脚本内容接口 返回异常");
            throw new Exception("调度中心-获取脚本内容接口 返回异常");
        }

        if(jsonObject.getInteger("code") != 0) {
            logger.error("调度中心-获取脚本内容接口 失败");
            throw new Exception(jsonObject.getString("message"));
        }
        JSONObject obj = jsonObject.getJSONObject("obj");
        //JSONObject fileContent = obj.getJSONObject("fileContent");
        return obj;
    }

    public JSONObject getTaskList(Long projectSpaceId, String scriptName, String operator)throws Exception {
        JSONObject data = new JSONObject();

        data.put("scriptName", scriptName);
        data.put("jsdAppgroupId", projectSpaceId);
        data.put("operator", operator);

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("appId", appId);
        long timeMillis = System.currentTimeMillis();
        params.put("time", Long.toString(timeMillis));

        logger.info("-------调度中心-获取脚本依赖线上任务列表接口参数：" + params + "; body=" + data);
        String entity = HttpUtil.doPostWithParamAndBody(buffalo4Prefix + taskListUrl, params, data);
        logger.info("-------调度中心-获取脚本依赖线上任务列表结果：" + entity);
        JSONObject jsonObject = JSON.parseObject(entity);
        JSONObject res = new JSONObject();
        JSONArray list = jsonObject.containsKey("list") ? jsonObject.getJSONArray("list") : new JSONArray();
        list = list == null ? new JSONArray() : list;
        int totalCount = 0;
        int L0 = 0;
        int L1 = 0;
        if(list != null && list.size() > 0){
            for (Object o : list) {
                JSONObject json = (JSONObject) o;
                if (json != null) {
                    if ("L0".equals(json.getString("priorityDesc"))) {
                        L0++;
                    } else if ("L1".equals(json.getString("priorityDesc"))) {
                        L1++;
                    }
                }
            }
            totalCount = jsonObject.getInteger("totalCount");
        }

        res.put("totalCount",totalCount);
        res.put("totalL0", L0);
        res.put("totalL1", L1);
        res.put("list", list);
        return res;
    }

    public JSONObject getCurrentOnlineInfo(String scriptName, Long projectId) throws Exception {
        try {
            JSONObject data = new JSONObject();

            /**
             * https://cf.jd.com/pages/viewpage.action?pageId=511882696
             *
             * projectId	Integer	项目空间ID	是
             * scriptName	String	脚本名称	是
             * model	S
             */
            data.put("scriptName", scriptName);
            data.put("projectId", projectId);
            data.put("model", "001");


            Map<String, String> params = new HashMap<>();
            params.put("token", token);
            params.put("appId", appId);
            long timeMillis = System.currentTimeMillis();
            params.put("time", Long.toString(timeMillis));
            logger.info("-------调度中心-获取getCurrentOnlineInfo容接口参数：" + params + "; body=" + data);
            String entity = HttpUtil.doPostWithParamAndBody(buffalo4Prefix + "/api/v2/buffalo4/script/findCurrentVersionByParam", params, data);
            logger.info("-------调度中心-获取getCurrentOnlineInfo容结果：" + entity);
            JSONObject jsonObject = JSON.parseObject(entity);

            if (jsonObject.getInteger("code") != 0) {
                throw new RuntimeException(jsonObject.getString("errorMsg"));
            }

            JSONObject obj = jsonObject.getJSONObject("obj");
            return obj;

        } catch (Exception e) {
            //throw new Exception(e.getMessage());
        }
        return new JSONObject();

    }
}
