package com.jd.bdp.datadev.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liuyanwei@360buy.com
 * Date: 12-7-29
 * Time: 下午3:02
 * To change this template use File | Settings | File Templates.
 */
public class PageResult {
    public Integer code;     //返回码
    private boolean success=true;
    public List list;       //集合形式的结果
    public Object obj;      //对象形式的结果
    private String message;
    private long totalSize = 0;

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

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
