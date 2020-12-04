package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;

import java.util.Map;

/**
 * Created by zhangrui25 on 2018/5/18.
 */
public class GitHttpResponse {

    private Integer responseCode;

    private String responseMessage;

    private Map<String, String> header;

    private HttpEntity entity;

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "GitHttpResponse{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                ", header=" + header +
                '}';
    }

    /**
     * @param httpResponse
     * @param gitHttpResponseCovert
     * @param <T>
     * @return
     */
    public static <T> T hanGitHttpResponse(GitHttpResponse httpResponse, GitHttpResponseCovert<T> gitHttpResponseCovert) {
        if (!httpResponse.getResponseCode().equals(HttpStatus.SC_OK)) {
            throw new RuntimeException("请求Git失败" + JSONObject.parseObject(httpResponse.getResponseMessage()).getString("message") );
        }
        return gitHttpResponseCovert.covertHttpResponse(httpResponse);
    }

}
