package com.jd.bdp.datadev.jdgit;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.map.HashedMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhangrui25 on 2018/5/11.
 */
public class GitTest {

    /**
     * create project （对应项目空间）
     */
    private static String CREAT_PROJECT = "http://git.jd.com/api/v4/projects/";

    /**
     * create, delete, move, update 文件
     */
    private static String CRUD_FILE = "http://git.jd.com/api/v4/projects/:id/repository/commits";

    /**
     * 获取不同版本的文件
     */
    private static String LOAD_FILE = "http://git.jd.com/api/v4/projects/:id/repository/files/:file_path/raw?ref=:sha";


    private static String getUrl(String uri, Map<String, String> params) {

        return null;
    }

    private final String PROJECT_PATH = URL.encode("bdp_pre/data_dev");

    //private static final String GET_FILE_URL = ""

    private static final SimpleDateFormat NOW_TIME_FORMATE = new SimpleDateFormat("yyyyMMddHHmmss");

    public static void main(String[] args) throws Exception {
        String projectPath = "39301";
        String filePath = "zhangrui.java";
        String url = "projects/" + projectPath + "/members";
        String url2 = "groups/wqh";
        Map<String, String> params = new HashMap<String, String>();
//        params.put("branch", "bdp_ide_branch");
//        params.put("content", "bdp_ide_b111123232323ranch");
//        params.put("commit_message", "测试提交\r\nssssss");
//        params.put("last_commit_id", "785287c445d190258a7945c7dba55d7b97db7944");


        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(1).doGet(url2,params);
        System.out.println(gitHttpResponse);
    }

    private static void createBranch() throws Exception {
        //23057
        String url = "http://git.jd.com/api/v4/projects/23057/repository/branches";
        JSONObject params = new JSONObject();
        params.put("branch", "bdp_ide_branch");
        params.put("ref", "master");
        GitHttpResponse result = GitHttpUtil.createClientByCode(1).doPost(url, params);
        System.out.println(result);

    }

    private static void addProjectMemeber(String projectPath, Long userId) throws Exception {
        String url = "http://git.jd.com/api/v4/projects/" + URL.encode(projectPath) + "/members";
        JSONObject params = new JSONObject();
        params.put("user_id", userId);
        params.put("access_level", "30");
        GitHttpResponse result = GitHttpUtil.createClientByCode(1).doPost(url, params);
        System.out.println(result);
        //curl --header "PRIVATE-TOKEN: 9koXpg98eAheJpvBs5tK" https://gitlab.example.com/api/v4/projects/:id/members

    }

    /**
     * 创建Project
     *
     * @param name
     * @param namespaceId
     * @throws Exception
     */
    public static void createProject(String name, Long namespaceId) throws Exception {
        String url = "http://git.jd.com/api/v4/projects/";
        JSONObject params = new JSONObject();
        params.put("name", name);
        if (namespaceId > 0l) {
            params.put("namespace_id", String.valueOf(namespaceId));
        }
        GitHttpResponse result = GitHttpUtil.createClientByCode(1).doPost(url, params);
        System.out.println(result);
    }

    public static void updateFile(String fileContent, String filePath, String projectPath) throws Exception {
        String commitMessage = "create_new_file";
        String branch = "master";
        filePath = URL.encode(filePath);
        projectPath = URL.encode(projectPath);
        String url = "http://git.jd.com/api/v4/projects/" + projectPath + "/repository/files/" + filePath + "";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("branch", branch);
        params.put("content", fileContent);
        params.put("commit_message", commitMessage);
        params.put("author_email", "author_email");
        params.put("author_name", "author_name");
        params.put("Lastname", "Lastname");
        params.put("last_commit_id", "0eeb0ac103189737f996a645238982fdc59f252a");
 /*       GitHttpResponse result = GitHttpUtil.doPost(url, params);
        System.out.println(result);*/
    }

    public static void upFile(String fileContent, String filePath, String projectPath) throws Exception {
        String commitMessage = "create_new_file";
        String branch = "master";
        filePath = URL.encode(filePath);
        projectPath = URL.encode(projectPath);
        String url = "http://git.jd.com/api/v4/projects/" + projectPath + "/repository/files/" + filePath + "";
        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("content", fileContent);
        params.put("commit_message", commitMessage);
        params.put("author_email", "author_email");
        params.put("author_name", "author_name");
        params.put("Lastname", "Lastname");
        GitHttpResponse result = GitHttpUtil.createClientByCode(1).doPost(url, params);
        System.out.println(result);
    }

    public static void commitFile(String fileContent, String filePath, String projectPath) throws Exception {
        String commitMessage = "create_new_file";
        String branch = "master";


        String url = "projects/" + projectPath + "/repository/commits";

        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("author_email", "wxywxy@.jd.com");
        params.put("author_name", "wxywxy@jd.com");

        params.put("commit_message", commitMessage);
        JSONArray actions = new JSONArray();
        JSONObject action = new JSONObject();
        action.put("action", "create");
        action.put("file_path", filePath);
        action.put("content", fileContent);
        //  action.put("previous_path", filePath);
        //1 action.put("last_commit_id","2a7bd5227d413d7df1f252707b56f4a54df08975");
        //2a7bd5227d413d7df1f252707b56f4a54df08975
        //update file base64
        //action.put("encoding", "base64");
        actions.add(action);
        params.put("actions", actions);

        GitHttpResponse result = GitHttpUtil.createClientByCode(1).doPost(url, params);
        System.out.println(result);
    }


    /**
     * 获取当前文件（最新）
     *
     * @param filePath
     * @param projectPath
     * @return
     * @throws Exception
     */
    private static InputStream getCurrentFile(String filePath, String projectPath) throws Exception {
        return loadFile(filePath, projectPath, "master");
    }

    /**
     * 通过一个Commit获取文件
     *
     * @param filePath
     * @param projectPath
     * @param sha
     * @return
     * @throws Exception
     */
    private static InputStream getCommitFile(String filePath, String projectPath, String sha) throws Exception {
        return loadFile(filePath, projectPath, sha);
    }

    /**
     * 下载文件
     *
     * @param filePath
     * @param projectPath
     * @param sha
     * @return
     * @throws Exception
     */
    private static InputStream loadFile(String filePath, String projectPath, String sha) throws Exception {
    /*    projectPath = URL.encode(projectPath);
        filePath = URL.encode(filePath);
        InputStream inputStream = GitHttpUtil.getStream("http://git.jd.com/api/v4/projects/" + projectPath + "/repository/files/" + filePath + "/raw?ref=" + sha);
        return inputStream;*/
        return null;
    }


    public static String getCommits(String projectPath, String path, String branch) throws Exception {
        projectPath = URL.encode(projectPath);
        Map<String, String> params = new HashMap<String, String>();
        params.put("ref", branch);
        params.put("path", path);
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(1).doGet("projects/" + projectPath + "/repository/commits", params);
        System.out.println("gitHttpResponse " + gitHttpResponse);
        return "";

    }

    public static String fileToBase64(File file) throws Exception {
        Base64 b64 = new Base64();
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        fis.read(buffer);
        fis.close();
        return b64.encodeToString(buffer);
    }

    public static void base64ToFile(String content, String filePath) throws Exception {
        Base64 b64 = new Base64();
        byte[] buffer = b64.decode(content);
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(buffer);
        fos.close();
    }

    public static String base64ToString(String content) throws Exception {
        Base64 b64 = new Base64();
        byte[] buffer = b64.decode(content);
        return new String(buffer);
    }

}
