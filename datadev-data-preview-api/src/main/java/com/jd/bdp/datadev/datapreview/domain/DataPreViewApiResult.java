package com.jd.bdp.datadev.datapreview.domain;

import java.io.Serializable;
import java.util.List;

public class DataPreViewApiResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private Long totalCount;
    private List list;
    private Object obj;
    private Integer page;
    private Integer limit;
    private Boolean success;

    public DataPreViewApiResult() {
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List getList() {
        return this.list;
    }

    public Long getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Object getObj() {
        return this.obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return this.limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean isSuccess() {
        return this.code == 0;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String toString() {
        return "DataPreViewApiResult{code=" + this.code + ", message='" + this.message + '\'' + ", totalCount=" + this.totalCount + ", list=" + this.list + ", obj=" + this.obj + ", page=" + this.page + ", limit=" + this.limit + ", success=" + this.success + '}';
    }

}
