package com.jd.bdp.datadev.netty.common;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhangrui25 on 2018/11/8.
 * <p>
 * 客户端返回给 WEB端 执行当前行的信息
 */
public class DebugMessage extends JavaModelToByteJsonAdapter {


    private Integer code;          // 0:成功 非0 失败
    private JSONObject data;
    private DebugCmd debugCmd;
    private String erp;
    private boolean success = true;
    private String message;       //报错时候的message
    private Object obj;
    private String param;
    private Long runDetailId;

    /**
     * 转换 系统返回给页面的ajax请求
     *
     * @return
     */
    public static DebugMessage convertSysAjaxJsonToDebugMessage(JSONObject jsonObject) {

    /*    jsonObject.put("code", 0);
        jsonObject.put("success", true);
        jsonObject.put("message", message);
        jsonObject.put("obj", object);*/
        DebugMessage debugMessage = new DebugMessage();
        debugMessage.setCode(jsonObject.getInteger("code"));
        debugMessage.setMessage(jsonObject.getString("message"));
        debugMessage.setSuccess(jsonObject.getBoolean("success"));
        debugMessage.setObj(jsonObject.get("obj"));
        return debugMessage;
    }

    public Long getRunDetailId() {
        return runDetailId;
    }

    public void setRunDetailId(Long runDetailId) {
        this.runDetailId = runDetailId;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }

    public DebugMessage() {
        this.code = 0;
        this.debugCmd = DebugCmd.DoNothing;
    }

    public DebugCmd getDebugCmd() {
        return debugCmd;
    }

    public void setDebugCmd(DebugCmd debugCmd) {
        this.debugCmd = debugCmd;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "DebugMessage{" +
                "code=" + code +
                ", data=" + data +
                ", debugCmd=" + debugCmd +
                ", erp='" + erp + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", obj=" + obj +
                '}';
    }
}
