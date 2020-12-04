package com.jd.bdp.datadev.model;

import java.io.Serializable;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class ApiResult  implements Serializable {
    private Boolean success;
    private Integer code;
    private String message;
    private Object obj;
    private static final long serialVersionUID = 7247714666080613254L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public static ApiResult getResult(boolean success, Integer code, String message, Object object) {
        ApiResult apiResult = new ApiResult();
        apiResult.setSuccess(success);
        apiResult.setCode(code);
        apiResult.setMessage(message);
        apiResult.setObj(object);
        return apiResult;
    }

    public static ApiResult getSuccessResult(String message, Object obj) {
        return getResult(true, 0, message, obj);
    }
    public static ApiResult getSuccessResult(String message) {
        return getResult(true, 0, message, null);
    }

    public static ApiResult getFailResult(String message, Object obj) {
        return getResult(false, 1, message, obj);
    }
    public static ApiResult getFailResult(String message) {
        return getResult(false, 1, message, null);
    }

}
