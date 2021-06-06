package com.jd.bdp.datadev.component;


import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.PageResultDTO;

import java.util.ArrayList;

public class JSONObjectUtil {
    public static JSONObject getSuccessResultTwoObj(Object object1 ,Object object2 ) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("success", true);
        jsonObject.put("message", "请求成功");
        JSONObject temp = new JSONObject();
        temp.put("obj", object1);
        temp.put("obj2", object2);
        jsonObject.put("data", temp);
        return jsonObject;
    }

    public static JSONObject getSuccessResult(Object object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("success", true);
        jsonObject.put("message", "请求成功");
        jsonObject.put("obj", object);
        return jsonObject;
    }
    public static JSONObject getSuccessList(Object object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("success", true);
        jsonObject.put("message", "请求成功");
        jsonObject.put("list", object);
        return jsonObject;
    }

    public static JSONObject getSuccessResult(String message, Object object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("success", true);
        jsonObject.put("message", message);
        jsonObject.put("obj", object);
        return jsonObject;
    }

    public static JSONObject getFailResult(Object object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 1);
        jsonObject.put("success", false);
        jsonObject.put("message", "请求失败");
        jsonObject.put("obj", object);
        return jsonObject;
    }

    public static JSONObject getFailResult(String messsage, Object object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 1);
        jsonObject.put("success", false);
        jsonObject.put("message", messsage);
        jsonObject.put("obj", object);
        return jsonObject;
    }

    public static JSONObject getFailResult(Integer errorCode, String messsage, Object object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", errorCode);
        jsonObject.put("success", false);
        jsonObject.put("message", messsage);
        jsonObject.put("obj", object);
        return jsonObject;
    }

    public static JSONObject sucessGrid(PageResultDTO pageResultDTO) {

        JSONObject grid = new JSONObject();
        grid.put("totals", pageResultDTO.getRecords());
        grid.put("page", pageResultDTO.getPage());
        grid.put("pageSize", pageResultDTO.getLimit());
        grid.put("status","success");
        grid.put("code",0);
        grid.put("data", pageResultDTO.getRows() == null || pageResultDTO.getRows().size() < 1 ? new ArrayList<Object>() : pageResultDTO.getRows());
        grid.put("message",pageResultDTO.getMessage());
        return grid;
    }
}
