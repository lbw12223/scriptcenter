package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.service.impl.DataDevScriptDirServiceImpl;
import com.jd.jsf.gd.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/17.
 */
public class JDGitRepositories implements JSONObjectCovertToGitDomain<JDGitRepositories> {

    private Long gitProjectId;  //参数gitProjectId
    private String filePath;    //参数path
    private String branch;      //参数branch
    private boolean recursive;  //是否递归获取


    private String name;
    private String type;          //active,
    private String path;
    private String commitId ;


    @Override
    public JDGitRepositories covertGitDomain(JSONObject jsonObject) {
        JDGitRepositories jdGitRepositories = new JDGitRepositories();
        jdGitRepositories.setGitProjectId(gitProjectId);
        jdGitRepositories.setBranch(branch);
        jdGitRepositories.setName(jsonObject.getString("name"));
        jdGitRepositories.setPath(jsonObject.getString("path"));
        jdGitRepositories.setType(jsonObject.getString("type"));
        System.out.println(jsonObject);
        return jdGitRepositories;
    }
    /*

     * */

    public static void main(String[] args) throws Exception {
        JDGitRepositories temp = new JDGitRepositories();
        temp.setGitProjectId(23057L);
        // temp.setFilePath();
        temp.setBranch("bdp_ide_branch");
        temp.setRecursive(false);
        temp.setFilePath("");
   /*     String url = "projects/23057/repository/tree";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ref", "master");
        params.put("recursive", "true");*/
        System.out.println(temp.treeAll());
    }

    /**
     * 给定一个Path查询所有的文件或者文件夹
     *
     * @return
     * @throws Exception
     */
    public List<JDGitRepositories> treeAll() throws Exception {
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/tree";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ref", branch);
        params.put("recursive", String.valueOf(recursive));
        if (StringUtils.isNotBlank(filePath)) {
            params.put("path", filePath);
        }
        return filterFileName(GitHttpUtil.createClient(gitProjectId).pageAll(url, params, this));
    }

    /**
     * 获取commit/diff
     * @return
     * @throws Exception
     */
    public JSONArray getCommitChange() throws Exception {
        String url ="projects/"+GitHttpUtil.getRealProjectId(gitProjectId)+"/repository/commits/"+getCommitId()+"/diff";
        HashMap<String, String> params = new HashMap<String, String>();
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doGet(url,null);
        return JSONArray.parseArray(gitHttpResponse.getResponseMessage());
    }


    /**
     * 1。2018-12-17 添加 BDP.TARGET 目录不参与 PULL ， PUSH
     *
     * @param repositories
     * @return
     */
    private List<JDGitRepositories> filterFileName(List<JDGitRepositories> repositories) {
        List<JDGitRepositories> result = new ArrayList<JDGitRepositories>();
        if (repositories != null && repositories.size() > 0) {
            for (JDGitRepositories temp : repositories) {
                if (temp.getPath().startsWith(DataDevScriptDirServiceImpl.TARGET_DIR_NAME)) {
                    continue;
                }
                if (temp.getName().equalsIgnoreCase(".GITKEEP")) {
                    continue;
                }
                result.add(temp);

            }
        }
        return result;
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @Override
    public String toString() {
        return "JDGitRepositories{" +
                "gitProjectId=" + gitProjectId +
                ", filePath='" + filePath + '\'' +
                ", branch='" + branch + '\'' +
                ", recursive=" + recursive +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
