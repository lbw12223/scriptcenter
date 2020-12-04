package com.jd.bdp.datadev.jdgit;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.exception.GitFileNotFoundException;
import com.jd.bdp.datadev.util.MD5Util;
import com.jd.jsf.gd.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * Created by zhangrui25 on 2018/5/17.
 * <p>
 * 二进制文件的内容在 bytes
 * 否则在content 里面
 */
public class JDGitFiles {

    private static final String ADD_COMMIT_MESSGE = "[%s] 添加 %s 文件";
    private static final String UPDATE_COMMIT_MESSGE = "[%s] 修改 %s ";

    private static final String DELETE_FILE_COMMIT_MESSGE = "[%s] 删除 %s 文件";
    private static final String DELETE_DIR_COMMIT_MESSGE = "[%s] 删除 %s 文件夹";
    private static final String RENAME_FILE_COMMIT_MESSGE = "[%s] 修改 %s 文件名为 %s";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = Logger.getLogger(JDGitFiles.class);

    private Long gitProjectId;  //参数gitProjectId
    private String filePath;    //参数path
    private String branch;      //参数branch
    private String defaultBranch = "bdp_ide_branch";

    private String lastCommitId; //gitVersion
    private String version;    //hbaseverion
    private String name;
    private Long size;
    private String content;
    private String description;

    private String erp;

    private boolean isBinary;

    private List<String> filePaths;
    private String commitMessage;

    private Integer isShow; //是否为临时文件
    private byte[] bytes;
    private Integer whereIsNew;
    private String startShellPath;
    private String fileMd5;
    private String args;
    private Long scriptUploadId;

    public static void main(String[] args) throws Exception {
        JDGitFiles jdGitFiles = new JDGitFiles();
        jdGitFiles.setGitProjectId(178481 + GitHttpUtil._9YI);
        jdGitFiles.setBranch("master");
        jdGitFiles.setFilePath("README.md");
        jdGitFiles.setBinary(false);
        jdGitFiles.setContent("README.md");
        jdGitFiles.createFile("createProjectAdd");

    }


    /**
     * input stream to byte
     *
     * @param inStream
     * @return
     * @throws IOException
     */
    public static final byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096 * 10];
        int len;
        while ((len = inStream.read(buffer)) > -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        byteArrayOutputStream.flush();
        IOUtils.closeQuietly(inStream);
        return byteArrayOutputStream.toByteArray();
    }


    /**
     * move  需要传入content
     * 改名
     * 移动
     *
     * @param newFilePath
     * @throws Exception
     */
    public String move(String newFilePath) throws Exception {
        byte[] buffer = null;
        if (isBinary) {
            buffer = getFileRaw();
        } else {
            loadFileContent();
            buffer = getContent().getBytes();
        }
        String commitMessage = StringUtils.isNotBlank(this.commitMessage) ? this.commitMessage : String.format(RENAME_FILE_COMMIT_MESSGE, erp, filePath, newFilePath);
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/commits";
        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("author_email", erp);
        params.put("author_name", erp);

        params.put("commit_message", commitMessage);
        JSONArray actions = new JSONArray();
        JSONObject action = new JSONObject();
        action.put("action", "move");
        action.put("file_path", newFilePath);
        action.put("previous_path", filePath);
        if (isBinary) {
            action.put("content", Base64Util.StringToBase64(buffer));
            action.put("encoding", "base64");
        } else {
            action.put("content", new String(buffer));
        }
        actions.add(action);
        params.put("actions", actions);
        GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doPost(url, params);
        if (!result.getResponseCode().equals(201)) {
            throw new RuntimeException(JSONObject.parseObject(result.getResponseMessage()).getString("message"));
        }
        return JSONObject.parseObject(result.getResponseMessage()).getString("id");
    }


    /**
     * 删除当前文件夹下所有的文件，包括子文件夹
     *
     * @throws Exception
     */
    public void deleteDir(String dirPath) throws Exception {
        String commitMessage = String.format(DELETE_DIR_COMMIT_MESSGE, erp, dirPath);
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/commits";
        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("author_email", erp);
        params.put("author_name", erp);
        params.put("commit_message", commitMessage);
        JSONArray actions = new JSONArray();

        for (String tempFilePath : filePaths) {
            JSONObject action = new JSONObject();
            action.put("action", "delete");
            action.put("file_path", tempFilePath);
            actions.add(action);
        }

        params.put("actions", actions);
        GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doPost(url, params);
        if (!result.getResponseCode().equals(201)) {
            throw new RuntimeException(JSONObject.parseObject(result.getResponseMessage()).getString("message"));
        }
    }

    /**
     * 删除文件
     *
     * @throws Exception
     */
    public void deleteFile() throws Exception {

        if (GitHttpUtil.isCoding(gitProjectId)) {
            ///projects/:id/repository/files/
            //curl --request PUT --header 'PRIVATE-TOKEN: 9koXpg98eAheJAdsf5tK' 'http://coding.jd.com/api/v4/projects/13083/repository/files/app%2Fprojectrb%2E?branch=master&author_email=author%40example.com&author_name=Firstname%20Lastname&commit_message=create%20a%20new%20file'
            String commitMessage = String.format(DELETE_FILE_COMMIT_MESSGE, erp, filePath);
            String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/files/" + URLEncoder.encode(filePath, "utf-8");
            url += "?branch=" + URLEncoder.encode(branch, "utf-8");
            url += "&author_email=" + URLEncoder.encode(erp, "utf-8");
            url += "&author_name=" + URLEncoder.encode(erp, "utf-8");
            url += "&commit_message=" + URLEncoder.encode(commitMessage, "utf-8");

            GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doDelete(url);
            if (!result.getResponseCode().equals(201)) {
                throw new RuntimeException(JSONObject.parseObject(result.getResponseMessage()).getString("message"));
            }
        } else {
            String commitMessage = String.format(DELETE_FILE_COMMIT_MESSGE, erp, filePath);
            String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/commits";
            JSONObject params = new JSONObject();
            params.put("branch", branch);
            params.put("author_email", erp);
            params.put("author_name", erp);

            params.put("commit_message", commitMessage);
            JSONArray actions = new JSONArray();

            JSONObject action = new JSONObject();
            action.put("action", "delete");
            action.put("file_path", filePath);
            actions.add(action);

            params.put("actions", actions);
            GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doPost(url, params);
            if (!result.getResponseCode().equals(201)) {
                throw new RuntimeException(JSONObject.parseObject(result.getResponseMessage()).getString("message"));
            }
        }


    }


    /**
     * 添加一个目录(实际在Git上添加一个文件 .gitkeep)
     *
     * @param dirPath dir/sub or /dir/sub/
     * @return
     */
    public JDGitFiles addDir(String dirPath) throws Exception {
        if (dirPath.startsWith("/")) {
            dirPath = dirPath.substring(1);
        }
        if (dirPath.endsWith("/")) {
            dirPath = dirPath.substring(0, dirPath.length() - 1);
        }
        String tempFileName = ".gitkeep";
        setBinary(false);
        setName(tempFileName);
        setFilePath(dirPath + "/" + tempFileName);
        setBytes("".getBytes());
        return addFile(null);
    }


    public void createFile(String commitMessage) throws Exception {

        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("encoding", isBinary ? "base64" : "text");
        params.put("content", content);
        params.put("commit_message", commitMessage);

        GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doPost("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/files/" + filePath, params);

        if (!result.getResponseCode().equals(200)) {
            throw new RuntimeException(result.getResponseMessage());
        }

    }

    public JDGitFiles doCommit(JSONArray actions, String userCommitessage) throws Exception {
        String commitMessage = null;
        if (org.apache.commons.lang.StringUtils.isBlank(userCommitessage)) {
            userCommitessage = "";
        }
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/commits";
        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("author_email", erp);
        params.put("author_name", erp);
        params.put("commit_message", sdf.format(new Date()) + ":" + "[" + erp + "]" + " 提交信息：" + userCommitessage);
        params.put("actions", actions);

        GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doPost(url, params);
        logger.error("========doCommit=========" + result + "===action====" + JSONArray.toJSONString(actions));
        if (result.getResponseCode().equals(201)) {
            JSONObject resultParams = JSONObject.parseObject(result.getResponseMessage());
            JDGitFiles jdGitFiles = new JDGitFiles();
            jdGitFiles.setLastCommitId(resultParams.getString("id"));
            return jdGitFiles;
        }
        if (GitHttpUtil.isCoding(gitProjectId)) {
            if (result.getResponseCode().equals(400) && result.getResponseMessage().indexOf("FILE_NO_CHANGES") != -1) {
                JDGitFiles jdGitFiles = new JDGitFiles();
                return jdGitFiles;
            }
        }
        throw new RuntimeException(JSONObject.parseObject(result.getResponseMessage()).getString("message"));
    }

    public JDGitFiles addOrUpdateFile(String actionStr, String userCommitessage) throws Exception {
        String commitMessage = null;
        if (org.apache.commons.lang.StringUtils.isBlank(userCommitessage)) {
            userCommitessage = "";
        }

        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/commits";
        JSONObject params = new JSONObject();
        params.put("branch", branch);
        params.put("author_email", erp);
        params.put("author_name", erp);

        params.put("commit_message", sdf.format(new Date()) + ":" + "[" + erp + "]" + " 提交信息：" + userCommitessage);
        JSONArray actions = new JSONArray();
        JSONObject action = new JSONObject();
        action.put("action", actionStr);
        action.put("file_path", filePath);
        if (isBinary) {
            action.put("content", Base64Util.StringToBase64(bytes));
            action.put("encoding", "base64");
        } else {
            action.put("content", new String(bytes, "utf-8"));
        }
        actions.add(action);
        params.put("actions", actions);

        GitHttpResponse result = GitHttpUtil.createClient(gitProjectId).doPost(url, params);

        if (result.getResponseCode().equals(201)) {
            JSONObject resultParams = JSONObject.parseObject(result.getResponseMessage());
            JDGitFiles jdGitFiles = new JDGitFiles();
            jdGitFiles.setSize((long) bytes.length);
            //内容不变 文件git版本号不变 通过stats判断文件是否更改
            if (isBinary) {
                jdGitFiles.setLastCommitId(resultParams.getString("id"));
            } else {
                Object stats = resultParams.get("stats");
                if (stats != null) {
                    JSONObject stausTemp = (JSONObject) stats;
                    if (stausTemp.getInteger("total") > 0) {
                        String lastCommitId = resultParams.getString("id");
                        jdGitFiles.setLastCommitId(lastCommitId);
                    }
                }
            }
            jdGitFiles.setFilePath(filePath);
            jdGitFiles.setBranch(branch);
            jdGitFiles.setGitProjectId(gitProjectId);
            jdGitFiles.setName(name);
            jdGitFiles.setErp(erp);
            return jdGitFiles;
        }
        throw new RuntimeException("[" + filePath + "]" + JSONObject.parseObject(result.getResponseMessage()).getString("message"));
    }

    /**
     * 添加 文件
     *
     * @return
     * @throws Exception
     */
    public JDGitFiles addFile(String message) throws Exception {
        return addOrUpdateFile("create", message);
    }

    public JDGitFiles updateFile(String message) throws Exception {
        return addOrUpdateFile("update", message);
    }


    /**
     * http://git.jd.com/help/api/repository_files.md#get-raw-file-from-repository
     * <p>
     * <p>
     * <p>
     * <p>
     * <p>
     * 2020-08-13 coding在获取这个接口报错，修改为在coding情况下取获取内容（base64），通过内容转化为二进制
     *
     * @throws Exception
     */
    public byte[] getFileRaw() throws Exception {

        String ref = StringUtils.isNotBlank(lastCommitId) ? lastCommitId : branch;

        if (gitProjectId > GitHttpUtil._9YI) {
            //coding
            JDGitFiles newRequest = new JDGitFiles();
            newRequest.setGitProjectId(gitProjectId);
            newRequest.setFilePath(filePath);
            newRequest.setLastCommitId(ref);
            newRequest.loadFileContent();
            return newRequest.getContent().getBytes("utf-8");
        }

        String url = "/projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/files/" + URL.encode(filePath) + "/raw?ref=" + ref;
        InputStream fileInputStream = GitHttpUtil.createClient(gitProjectId).getStream(url);
        return input2byte(fileInputStream);


    }


    /**
     * 获取可编辑文件content
     * http://git.jd.com/help/api/repository_files.md#get-file-from-repository
     *
     * @return
     * @throws Exception
     */
    public void loadFileContent() throws Exception {
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/repository/files/" + URL.encode(filePath).replaceAll("\\+", "%20");
        Map<String, String> params = new HashMap<String, String>();
        String ref = StringUtils.isNotBlank(lastCommitId) ? lastCommitId : branch;
        ref = ref == null ? defaultBranch : ref;
        params.put("ref", ref);
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doGet(url, params);
        if (gitHttpResponse.getResponseCode().equals(404)) {
            throw new GitFileNotFoundException("文件不存在" + JSONObject.parseObject(gitHttpResponse.getResponseMessage()).getString("message"));
        }
        JSONObject fileJson = JSONObject.parseObject(gitHttpResponse.getResponseMessage());
        setSize(fileJson.getLong("size"));
        if (StringUtils.isBlank(filePath)) {
            setFilePath(fileJson.getString("file_path"));
        }
        setName(fileJson.getString("file_name"));
        setLastCommitId(fileJson.getString("last_commit_id"));
        setGitProjectId(gitProjectId);
        if (isBinary) {
            byte[] contents = getFileRaw();
            bytes = contents;
            setFileMd5(MD5Util.getMD5(bytes));
        } else {
            if (fileJson.getString("encoding").equals("base64")) {
                String content = Base64Util.base64ToString(fileJson.getString("content"));
                setFileMd5(MD5Util.getMD5Str(content));
                setContent(content);
            } else {
                String content = fileJson.getString("content");
                setFileMd5(MD5Util.getMD5Str(content));
                setContent(content);
            }
        }

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

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public void setBinary(boolean binary) {
        isBinary = binary;
    }


    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }


    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getWhereIsNew() {
        return whereIsNew;
    }

    public void setWhereIsNew(Integer whereIsNew) {
        this.whereIsNew = whereIsNew;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartShellPath() {
        return startShellPath;
    }

    public void setStartShellPath(String startShellPath) {
        this.startShellPath = startShellPath;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Long getScriptUploadId() {
        return scriptUploadId;
    }

    public void setScriptUploadId(Long scriptUploadId) {
        this.scriptUploadId = scriptUploadId;
    }

    @Override
    public String toString() {
        return "JDGitFiles{" +
                "gitProjectId=" + gitProjectId +
                ", filePath='" + filePath + '\'' +
                ", branch='" + branch + '\'' +
                ", lastCommitId='" + lastCommitId + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", content='" + content + '\'' +
                '}';
    }
}
