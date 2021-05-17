package com.jd.bdp.datadev.component;


import com.alibaba.fastjson.JSONObject;

public class JSONObjectUtil {
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
}
