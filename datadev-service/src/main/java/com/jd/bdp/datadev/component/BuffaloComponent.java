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
//        data.put("version", version);

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("appId", appId);
        long timeMillis = System.currentTimeMillis();
        params.put("time", Long.toString(timeMillis));
//        params.put("data", data.toJSONString());
//        params.put("scriptName", scriptName);
//        params.put("projectId", projectId.toString());

        logger.info("-------调度中心-获取脚本内容接口参数：" + params + "; body=" + data);
        buffalo4Prefix = "http://11.91.157.254";
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
//        params.put("data", data.toJSONString());

        logger.info("-------调度中心-获取脚本依赖线上任务列表接口参数：" + params + "; body=" + data);
        buffalo4Prefix = "http://11.91.157.254";
        String entity = HttpUtil.doPostWithParamAndBody(buffalo4Prefix + taskListUrl, params, data);
        logger.info("-------调度中心-获取脚本依赖线上任务列表结果：" + entity);

        JSONObject jsonObject;

        try {
            jsonObject = JSON.parseObject(entity);
        }
        catch(Exception e) {
            logger.error("调度中心-获取脚本依赖线上任务列表接口 返回异常");
            throw new Exception("调度中心-获取脚本依赖线上任务列表接口 返回异常");
        }

        if(jsonObject.getInteger("code") != 0) {
            logger.error("调度中心-获取脚本依赖线上任务列表接口 失败");
            throw new Exception(jsonObject.getString("message"));
        }
        JSONObject res = new JSONObject();
        JSONArray list = jsonObject.getJSONArray("list");
        list = list == null ? new JSONArray() : list;
        int L0 = 0;
        int L1 = 0;
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
        res.put("totalCount", jsonObject.getLong("totalCount"));
        res.put("totalL0", L0);
        res.put("totalL1", L1);
        res.put("list", list);
        return res;
    }

}
