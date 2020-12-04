package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/28.
 */
public class JDGitBranchs implements JSONObjectCovertToGitDomain<JDGitBranchs> {

    private Long gitProjectId;
    private String branch;


    public static void main(String[] args) {
        JDGitBranchs jdGitBranchs = new JDGitBranchs();
        jdGitBranchs.setGitProjectId(900178853L);
        jdGitBranchs.setBranch("bdp_ide_branch");
        jdGitBranchs.checkIdeBranchs();
    }

    /**
     * http://git.jd.com/help/api/branches.md#create-repository-branch
     */
    public void createBranchs() {
        try {
            String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/branches";
            JSONObject params = new JSONObject();
            params.put("branch", branch);
            params.put("ref", "master");
            GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doPost(url, params);
            //如果已经创建，那么直接使用
            if (gitHttpResponse.getResponseMessage().indexOf("Branch already exists") != -1) {
                return;
            } else {
                if (gitHttpResponse.getResponseCode().equals(201) || gitHttpResponse.getResponseCode().equals(200)) {
                    return;
                }
                throw new RuntimeException("创建分支失败!" + JSONObject.parseObject(gitHttpResponse.getResponseMessage()).getString("message"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public boolean checkIdeBranchs() {
        try {
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/branches";
        Map<String,String> params=new HashMap<String,String>();
            List<JDGitBranchs> jdGitBranchs = GitHttpUtil.createClient(gitProjectId).pageAll(url,params,this);
            if(jdGitBranchs!=null&&jdGitBranchs.size()>0){
                for (JDGitBranchs branch:jdGitBranchs){
                    if("bdp_ide_branch".equals(branch.getBranch())){
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public JDGitBranchs covertGitDomain(JSONObject jsonObject){
        JDGitBranchs branch = new JDGitBranchs();
        branch.setBranch(jsonObject.getString("name"));
        branch.setGitProjectId(gitProjectId);
        return branch;
    }


    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
