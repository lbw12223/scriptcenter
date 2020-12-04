package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.SQLOutput;
import java.util.*;

/**
 * Created by zhangrui25 on 2018/6/19.
 */
public class JDGitUser implements JSONObjectCovertToGitDomain<JDGitUser> {
    private Long id;
    private String name;
    private String state;
    private Long gitProjectId ;
    private Integer gitOrCodingCode ;



    @Override
    public JDGitUser covertGitDomain(JSONObject jsonObject) {
        JDGitUser result = new JDGitUser();
        result.setId(jsonObject.getLong("id"));
        result.setName(jsonObject.getString("name"));
        result.setState(jsonObject.getString("state"));
        return result;
    }

    public static void main(String[] args) throws Exception {
        JDGitUser jdGitUser = new JDGitUser();
        jdGitUser.setName("zhangrui156");
        System.out.println(jdGitUser.searchUser());
    }

    public JDGitUser searchUser() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", name);

        GitRequestClient client = gitProjectId != null ? GitHttpUtil.createClient(gitProjectId) : GitHttpUtil.createClientByCode(gitOrCodingCode);
        GitHttpResponse gitHttpResponse = client.doGet("users", params);
        if (!gitHttpResponse.getResponseCode().equals(200)) {
            throw new RuntimeException(gitHttpResponse.getResponseMessage());
        }
        JSONArray users = JSONArray.parseArray(gitHttpResponse.getResponseMessage());
        if (users != null && users.size() > 0) {
            return covertGitDomain(users.getJSONObject(0));
        }
        throw new RuntimeException("用户 " + name + " 在git未激活，请在<a href='http://git.jd.com'target='blank'>git.jd.com</a>登录一次进行激活！");
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getGitOrCodingCode() {
        return gitOrCodingCode;
    }

    public void setGitOrCodingCode(Integer gitOrCodingCode) {
        this.gitOrCodingCode = gitOrCodingCode;
    }

    @Override
    public String toString() {
        return "JDGitUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
