package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.EnvInit;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.DataDevHisTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.exception.GitFileNotFoundException;
import com.jd.bdp.datadev.jdgit.*;
import com.jd.bdp.datadev.service.DataDevGitHisDetailService;
import com.jd.bdp.datadev.service.DataDevGitHisService;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.util.MD5Util;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.worker.InitGitProject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.*;


@Controller
@RequestMapping("/scriptcenter/hooks/")
public class GitHookController {

    private static final Logger logger = Logger.getLogger(GitHookController.class);

    private static final String EVENT_HAND = "Push Hook";

    private static String PRIVATE_TOKEN_USER = null;


    @Autowired
    private DataDevScriptFileService dataDevScriptFileService;

    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private EnvInit envInit;


    @Autowired
    private DataDevGitHisService dataDevGitHisService;

    @Autowired
    private DataDevGitHisDetailService dataDevGitHisDetailService;

    //初始化项目的hook
    @ExceptionMessageAnnotation(errorMessage = "Init Git Project Hook")
    @RequestMapping("initGitProjectHook.ajax")
    @ResponseBody
    public JSONObject initGitProjectHook(HttpServletResponse response, HttpServletRequest request) throws Exception {
        List<DataDevGitProject> dataDevGitProjectList = dataDevGitProjectService.listAll();
        StringBuilder stringBuilder = new StringBuilder();
        for (DataDevGitProject dataDevGitProject : dataDevGitProjectList) {
            if (dataDevGitProject.getDeleted().equals(1)) {
                continue;
            }
            try {
                JDGitProjects jdGitProjects = new JDGitProjects();
                jdGitProjects.setGitProjectId(dataDevGitProject.getGitProjectId());
                jdGitProjects.addProjectHook(envInit.getGitProjectHookUrl());
            } catch (Exception e) {
                stringBuilder.append(dataDevGitProject.getGitProjectId() + "  " + dataDevGitProject.getGitProjectPath() + " " + dataDevGitProject.getGitProjectName());
                stringBuilder.append(" " + e.getMessage());
                stringBuilder.append("\r\n<br/>");
            }
        }
        return JSONObjectUtil.getSuccessResult("init success", stringBuilder.toString());
    }

    //初始化项目的hook
    @ExceptionMessageAnnotation(errorMessage = "Init Single Git Project Hook")
    @RequestMapping("initSingleGitProjectHook.ajax")
    @ResponseBody
    public JSONObject initSingleGitProjectHook(Long gitProjectId) throws Exception {

        JDGitProjects jdGitProjects = new JDGitProjects();
        jdGitProjects.setGitProjectId(gitProjectId);
        jdGitProjects.addProjectHook(envInit.getGitProjectHookUrl());

        return JSONObjectUtil.getSuccessResult("init Single success");
    }

    //初始化项目的hook
    @ExceptionMessageAnnotation(errorMessage = "Get All Git Project Hooks")
    @RequestMapping("getAllGitProjectHooks.ajax")
    @ResponseBody
    public JSONObject getAllGitProjectHooks() throws Exception {
        JSONObject datas = new JSONObject();
        List<DataDevGitProject> dataDevGitProjectList = dataDevGitProjectService.listAll();
        StringBuilder stringBuilder = new StringBuilder();
        for (DataDevGitProject dataDevGitProject : dataDevGitProjectList) {
            if (dataDevGitProject.getDeleted().equals(1)) {
                continue;
            }
            try {
                JDGitProjects jdGitProjects = new JDGitProjects();
                jdGitProjects.setGitProjectId(dataDevGitProject.getGitProjectId());
                JSONArray hooks = jdGitProjects.getCurrentPlatformHook();
                datas.put(dataDevGitProject.getGitProjectId() + "", dataDevGitProject.getGitProjectName() + "--" + (hooks != null ? hooks.size() : 0));
            } catch (Exception e) {
                stringBuilder.append(dataDevGitProject.getGitProjectId() + "  " + dataDevGitProject.getGitProjectPath() + " " + dataDevGitProject.getGitProjectName());
                stringBuilder.append(" " + e.getMessage());
                stringBuilder.append("\r\n<br/>");
                logger.error("getAllGitProjectHooks:" + stringBuilder.toString());
            }
        }
        return JSONObjectUtil.getSuccessResult("getAllGitProjectHooks", datas);
    }


    //删除当前项目的hook
    @ExceptionMessageAnnotation(errorMessage = "Deleted Git Project Hook")
    @RequestMapping("deleteProjectHook.ajax")
    @ResponseBody
    public JSONObject deleteGitProjectHook(HttpServletResponse response, HttpServletRequest request) throws Exception {
        List<DataDevGitProject> dataDevGitProjectList = dataDevGitProjectService.listAll();
        StringBuilder stringBuilder = new StringBuilder();
        for (DataDevGitProject dataDevGitProject : dataDevGitProjectList) {
            if (dataDevGitProject.getDeleted().equals(1)) {
                continue;
            }
            try {
                JDGitProjects jdGitProjects = new JDGitProjects();
                jdGitProjects.setGitProjectId(dataDevGitProject.getGitProjectId());
                JSONArray hooks = jdGitProjects.getCurrentPlatformHook();
                jdGitProjects.deleteHooks(hooks);
            } catch (Exception e) {
                stringBuilder.append(dataDevGitProject.getGitProjectId() + "  " + dataDevGitProject.getGitProjectPath() + " " + dataDevGitProject.getGitProjectName());
                stringBuilder.append(" " + e.getMessage());
                stringBuilder.append("\r\n<br/>");
            }
        }
        return JSONObjectUtil.getSuccessResult("delete success", stringBuilder.toString());
    }


    /**
     * 初始化 所有脚本文件当中最新的GitVersion GitMd5
     *
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "InitGitProjectProjectScriptFileGitVersionMd5")
    @RequestMapping("initScriptLastMd5.ajax")
    @ResponseBody
    public JSONObject initScriptLastMd5(HttpServletResponse response, HttpServletRequest request) throws Exception {
        List<DataDevGitProject> dataDevGitProjectList = dataDevGitProjectService.listAll();
        for (DataDevGitProject dataDevGitProject : dataDevGitProjectList) {
            if (dataDevGitProject.getDeleted().equals(1)) {
                continue;
            }
            Long gitProjectId = dataDevGitProject.getGitProjectId();
            List<DataDevScriptFile> dataDevScriptFileList = dataDevScriptFileService.getNoLastGitVersionMd5(gitProjectId);
            for (DataDevScriptFile dataDevScriptFile : dataDevScriptFileList) {
                String gitProjectFilePath = dataDevScriptFile.getGitProjectFilePath();
                if (StringUtils.isBlank(dataDevScriptFile.getGitVersion())) {
                    continue;
                }
                DataDevScriptFile updateParams = new DataDevScriptFile();
                try {
                    boolean canEdit = DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType());
                    JDGitFiles jdGitFiles = new JDGitFiles();
                    jdGitFiles.setGitProjectId(gitProjectId);
                    jdGitFiles.setBranch(InitGitProject.BDP_IDE_BRANCH);
                    jdGitFiles.setFilePath(gitProjectFilePath);
                    jdGitFiles.setBinary(!canEdit);
                    jdGitFiles.loadFileContent();
                    updateParams.setLastGitVersion(jdGitFiles.getLastCommitId());
                    updateParams.setLastGitVersionMd5(StringUtils.isBlank(jdGitFiles.getContent()) ?
                            MD5Util.getMD5(jdGitFiles.getBytes()) :
                            MD5Util.getMD5Str(jdGitFiles.getContent()));
                    updateParams.setGitDeleted(0);

                    dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
                } catch (GitFileNotFoundException e) {
                    // 之前过同步过，但是现在Git已经删除
                    updateParams.setGitDeleted(1);
                    dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
                } catch (Exception e) {
                    logger.error("GitProjectFilePath :" + gitProjectFilePath, e);
                }

            }
        }

        return JSONObjectUtil.getSuccessResult("init success");
    }


    @ExceptionMessageAnnotation(errorMessage = "initProjectLastMd5")
    @RequestMapping("initProjectLastMd5.ajax")
    @ResponseBody
    public JSONObject initProjectLastMd5(HttpServletResponse response, HttpServletRequest request, Long gitProjectId) throws Exception {

        List<DataDevScriptFile> dataDevScriptFileList = dataDevScriptFileService.getNoLastGitVersionMd5(gitProjectId);
        for (DataDevScriptFile dataDevScriptFile : dataDevScriptFileList) {
            String gitProjectFilePath = dataDevScriptFile.getGitProjectFilePath();
            if (StringUtils.isBlank(dataDevScriptFile.getGitVersion())) {
                continue;
            }
            DataDevScriptFile updateParams = new DataDevScriptFile();
            try {
                boolean canEdit = DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType());
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setGitProjectId(gitProjectId);
                jdGitFiles.setBranch(InitGitProject.BDP_IDE_BRANCH);
                jdGitFiles.setFilePath(gitProjectFilePath);
                jdGitFiles.setBinary(!canEdit);
                jdGitFiles.loadFileContent();
                updateParams.setLastGitVersion(jdGitFiles.getLastCommitId());
                updateParams.setLastGitVersionMd5(StringUtils.isBlank(jdGitFiles.getContent()) ?
                        MD5Util.getMD5(jdGitFiles.getBytes()) :
                        MD5Util.getMD5Str(jdGitFiles.getContent()));
                updateParams.setGitDeleted(0);

                dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
            } catch (GitFileNotFoundException e) {
                // 之前过同步过，但是现在Git已经删除
                updateParams.setGitDeleted(1);
                dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
            } catch (Exception e) {
                logger.error("GitProjectFilePath :" + gitProjectFilePath, e);
            }

        }


        return JSONObjectUtil.getSuccessResult("init success");
    }

    @ExceptionMessageAnnotation(errorMessage = "initScriptLastSingleMd5")
    @RequestMapping("initScriptLastSingleMd5.ajax")
    @ResponseBody
    public JSONObject initScriptLastSingleMd5(HttpServletResponse response, HttpServletRequest request, Long gitProjectId, String gitProjectFilePath) throws Exception {

        DataDevScriptFile dataDevScriptFile = dataDevScriptFileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitProjectFilePath);
        DataDevScriptFile updateParams = new DataDevScriptFile();
        boolean canEdit = DataDevScriptTypeEnum.canEdit(dataDevScriptFile.getType());

        try {
            JDGitFiles jdGitFiles = new JDGitFiles();
            jdGitFiles.setGitProjectId(gitProjectId);
            jdGitFiles.setBranch(InitGitProject.BDP_IDE_BRANCH);
            jdGitFiles.setFilePath(gitProjectFilePath);
            jdGitFiles.setBinary(!canEdit);
            jdGitFiles.loadFileContent();
            updateParams.setLastGitVersion(jdGitFiles.getLastCommitId());
            updateParams.setLastGitVersionMd5(StringUtils.isBlank(jdGitFiles.getContent()) ?
                    MD5Util.getMD5(jdGitFiles.getBytes()) :
                    MD5Util.getMD5Str(jdGitFiles.getContent()));
            updateParams.setGitDeleted(0);

            dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
        } catch (GitFileNotFoundException e) {
            // 之前过同步过，但是现在Git已经删除
            updateParams.setGitDeleted(1);
            dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
        } catch (Exception e) {
            logger.error("GitProjectFilePath :" + gitProjectFilePath, e);
            return JSONObjectUtil.getFailResult(e.getMessage());
        }

        return JSONObjectUtil.getSuccessResult("init success");
    }

    /**
     * Git触发上有文件的更新会触发当前的方法
     * <p>
     * 1。只处理Push的情况。
     * 2。线上只处理非 PRIVETE_TOKEN_USER 人员操作的记录，平时通过数据开发平台提交的数据人员都是 PRIVETE_TOKEN_USER
     * 3。处理commit里面的数据，添加git_his , git_his_detail 表
     *
     * <p>
     * header x-gitlab-event:Push Hook
     * header x-gitlab-token:zhangrui156  -- token :设置为PRIVETE_TOKEN_USER
     *
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "Git hook post data")
    @RequestMapping("post")
    @ResponseBody
    public JSONObject postData(HttpServletResponse response, HttpServletRequest request) throws Exception {
        logger.error("=========================================webhook postData 1====================================:");

        try {
            Map<String, String> header = getHeaderData(request);
            logger.error("====webhookpostdataheader====" + JSONObject.toJSONString(header));
            JSONObject postData = getRequestPostData(request);
            logger.error("====webhookpostdatapostData====" + JSONObject.toJSONString(postData));

            if (header.get("x-gitlab-event").equalsIgnoreCase(EVENT_HAND)) {
                String token = header.get("x-gitlab-token");
                if (StringUtils.isNotBlank(token)) {
                    if (isCoding(postData) || token.equals(getPrivateTokenUser())) {
                        handPostData(postData);
                    }
                }


            }
        } catch (Exception e) {
            logger.error("webHook Post", e);
        }

        return JSONObjectUtil.getSuccessResult("call success", null);
    }


    private boolean isCoding(JSONObject postData) {
        return postData.getJSONObject("repository").getString("git_http_url").indexOf("coding.jd.com") != -1;
    }


    /**
     * 1。处理PostData
     *
     * @param postData
     */
    private void handPostData(JSONObject postData) {
        boolean isCoding = isCoding(postData);
        Long gitProjectId = postData.getLong("project_id") + (isCoding ? GitHttpUtil._9YI : 0);
        String ref = postData.getString("ref");

        JSONArray commits = postData.getJSONArray("commits");

        if (commits != null) {
            logger.error("===========webhookpostdatacommits" + gitProjectId + "==========" + commits);
        }


        String author = postData.getString("user_username");
        String email = postData.getString("user_email");
        String beforeCommit = postData.getString("before");
        //只处理非 PRIVATE_TOKEN_USER 操作的数据(其他人在Git上操作)
        //只处理modified的情况 （added.removed 不处理）
        //只处理bdp_ide_branch
        if (ref.indexOf(InitGitProject.BDP_IDE_BRANCH) == -1) {
            return;
        }


        handGitHis(gitProjectId, commits, author, email, beforeCommit);

        if (author.equals(getPrivateTokenUser()) && !isCoding) {
            if (envInit.getEnv().equalsIgnoreCase("online")) {
                return;
            }
        }
        //处理提交历史
        List<DataDevGitHisDetail> insertDataDevGitHisDtails = new ArrayList<DataDevGitHisDetail>();
        for (int index = 0; index < commits.size(); index++) {
            JSONObject commit = commits.getJSONObject(index);
            String commitId = commit.getString("id");
            JSONArray modified = commit.getJSONArray("modified");
            JSONArray removed = commit.getJSONArray("removed");
            JSONArray added = commit.getJSONArray("added");


            handModifedData(modified, commitId, gitProjectId);
            handRemovedData(removed, commitId, gitProjectId);
            handAddData(added, commitId, gitProjectId);


        }


        //获取url
        //logger.error("====================webhook handPostData 4===========" + postData.getString("project"));

        //JSONObject project = postData.getJSONObject("project");
        // String web_url = project.getString("web_url");

        //logger.error("====================webhook web_url 5===========" + web_url);
    }

    /**
     * 初始化 所有脚本文件Commits 信息
     * 1。获取项目
     * 2。删除当前项目的所有历史记录
     * 3。curl --header "PRIVATE-TOKEN: j9e-suFgk6p7di-rgKFg" "http://git.jd.com/api/v4/projects/23759/repository/commits?ref_name=bdp_ide_branch" 获取所有的提交记录
     * 4。curl --header "PRIVATE-TOKEN: j9e-suFgk6p7di-rgKFg" "http://git.jd.com/api/v4/projects/23759/repository/commits/f3fb59bcc846643fed37b6a7b4c754a027b3a141/diff" 获取当前这次提交记录修改
     * 5。生成 GitHis ， GitHisDetail -> database
     *
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "initGitProjectCommits")
    @RequestMapping("initGitProjectCommits.ajax")
    @ResponseBody
    public JSONObject initGitProjectCommits(HttpServletResponse response, HttpServletRequest request) throws Exception {
        List<DataDevGitProject> dataDevGitProjectList = dataDevGitProjectService.listAll();
        String erroMsg = "";
        for (DataDevGitProject dataDevGitProject : dataDevGitProjectList) {
            try {
                if (dataDevGitProject.getDeleted().equals(1)) {
                    continue;
                }
                initGitProjectCommits(dataDevGitProject);
            } catch (Exception e) {
                erroMsg += dataDevGitProject.getGitProjectName() + " " + e.getMessage() + " <br >";
            }

        }

        return JSONObjectUtil.getSuccessResult(erroMsg);
    }

    /**
     * @param response
     * @param request
     * @param gitProjectId
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "initGitSingleProjectCommits")
    @RequestMapping("initGitSingleProjectCommits.ajax")
    @ResponseBody
    public JSONObject initGitSingleProjectCommits(HttpServletResponse response, HttpServletRequest request, Long gitProjectId) throws Exception {

        DataDevGitProject dataDevGitProject = dataDevGitProjectService.getGitProjectBy(gitProjectId);

        if (!dataDevGitProject.getDeleted().equals(1)) {

            initGitProjectCommits(dataDevGitProject);
        }

        return JSONObjectUtil.getSuccessResult(" initGitSingleProjectCommits");
    }

    /**
     * 处理单个gitProjectCommits
     *
     * @param dataDevGitProject
     * @throws Exception
     */
    private void initGitProjectCommits(DataDevGitProject dataDevGitProject) throws Exception {

        Long gitProjectId = dataDevGitProject.getGitProjectId();

        // dataDevGitHisService.deleteDataDevGitHis(gitProjectId);
        dataDevGitHisDetailService.deleteDataDevGitHisDetail(gitProjectId);

        JDGitCommits jdGitCommits = new JDGitCommits();
        jdGitCommits.setBranch(dataDevGitProject.getBranch());
        jdGitCommits.setGitProjectId(gitProjectId);

        List<JDGitCommits> gitProjectCommits = jdGitCommits.allCommits();
        handProjectAllCommits(dataDevGitProject, gitProjectCommits);
    }

    /**
     * 1. 初始化处理commit
     * 1。处理项目的提交历史
     * 2。查询提交历史详情
     *
     * @param dataDevGitProject
     * @param jdGitCommitsList
     */
    private void handProjectAllCommits(DataDevGitProject dataDevGitProject, List<JDGitCommits> jdGitCommitsList) throws Exception {

        //处理所有的Commit
        List<DataDevGitHis> insertCommits = new ArrayList<DataDevGitHis>();
        Collections.reverse(jdGitCommitsList);
        for (JDGitCommits jdGitCommits : jdGitCommitsList) {
            DataDevGitHis dataDevGitHis = new DataDevGitHis();
            dataDevGitHis.setCommitId(jdGitCommits.getShaId());
            dataDevGitHis.setGitProjectId(jdGitCommits.getGitProjectId());
            dataDevGitHis.setSubmitErp(jdGitCommits.getCommitterName());
            dataDevGitHis.setSubmitTime(jdGitCommits.getCommittedDate());
            dataDevGitHis.setComment(jdGitCommits.getMessage());
            dataDevGitHis.setCreated(jdGitCommits.getCommittedDate());
            dataDevGitHis.setBeforeCommitId(jdGitCommits.getBeforeShaId());
            dataDevGitHis.setBranch(jdGitCommits.getBranch());
            dataDevGitHis.setEmail(jdGitCommits.getCommitterEmail());
            dataDevGitHis.setCreator(jdGitCommits.getCommitterName());
            insertCommits.add(dataDevGitHis);
        }
        // dataDevGitHisService.insertDataDevGitHis(insertCommits);

        //每次commit影响文件
        List<DataDevGitHisDetail> insertHisDetails = new ArrayList<DataDevGitHisDetail>();
        for (DataDevGitHis dataDevGitHis : insertCommits) {
            try {
                Long gitProjectId = dataDevGitProject.getGitProjectId();
                JDGitCommits jdGitCommits = new JDGitCommits();
                jdGitCommits.setShaId(dataDevGitHis.getCommitId());
                jdGitCommits.setGitProjectId(gitProjectId);
                JSONArray commitDiffs = jdGitCommits.commitDiff();
                if (commitDiffs != null) {
                    for (int index = 0; index < commitDiffs.size(); index++) {
                        DataDevGitHisDetail hisDetail = new DataDevGitHisDetail();

                        JSONObject oneDiff = commitDiffs.getJSONObject(index);
                        String gitProjectFilePath = oneDiff.getString("new_path");
                        boolean newFile = oneDiff.getBoolean("new_file");
                        boolean deleteFile = oneDiff.getBoolean("deleted_file");
                        int commitType = newFile ?
                                DataDevHisTypeEnum.ADD.tocode() : (deleteFile ? DataDevHisTypeEnum.DELETE.tocode() : DataDevHisTypeEnum.MOIDFY.tocode());

                        hisDetail.setGitProjectId(gitProjectId);
                        hisDetail.setCommitId(dataDevGitHis.getCommitId());
                        hisDetail.setCommitType(commitType);
                        hisDetail.setComment(dataDevGitHis.getComment());
                        hisDetail.setGitProjectFilePath(gitProjectFilePath);
                        hisDetail.setCreator(dataDevGitHis.getCreator());
                        hisDetail.setGitHisId(dataDevGitHis.getId());
                        hisDetail.setCreated(dataDevGitHis.getCreated());
                        hisDetail.setBeforeCommitIds(dataDevGitHis.getBeforeCommitId());
                        hisDetail.setEmail(dataDevGitHis.getEmail());

                        insertHisDetails.add(hisDetail);
                    }
                }
            } catch (Exception e) {
                logger.error("handProjectAllCommit", e);
            }
        }
        dataDevGitHisDetailService.batchInsertDataDevGitHisDetails(insertHisDetails);
    }

    /**
     * 如果是Coding已经添加了 9YI
     * <p>
     * "added": [],
     * "removed": [],
     * "modified": ["yanglin/scene_survey/app_m10_scene_survey_sum_da.py"]
     *
     * @param commitId
     * @param gitProjectId
     */
    private void updateCommitAddModifyRemoved(JSONObject commit, String commitId, Long gitProjectId) {

        try {
            JDGitRepositories jdGitRepositories = new JDGitRepositories();
            jdGitRepositories.setGitProjectId(gitProjectId);
            jdGitRepositories.setCommitId(commitId);
            JSONArray commitDiffs = jdGitRepositories.getCommitChange();

            if (commitDiffs != null) {
                JSONArray added = new JSONArray();
                JSONArray removed = new JSONArray();
                JSONArray modified = new JSONArray();

                for (int index = 0; index < commitDiffs.size(); index++) {
                    JSONObject diff = commitDiffs.getJSONObject(index);

                    Boolean isNewFile = diff.getBoolean("new_file");
                    Boolean isDeleted = diff.getBoolean("deleted_file");
                    String newPath = diff.getString("new_path");
                    String oldPath = diff.getString("old_path");
                    if (isNewFile) {
                        added.add(newPath);
                        continue;
                    }

                    if (isDeleted) {
                        //	"diff": "diff --git a/ide-web/src/main/config/config_online/important.properties b/ide-web/src/main/config/config_online/important.properties\ndeleted file mode 100644\nindex b10369d..0000000\n--- a/ide-web/src/main/config/config_online/important.properties\n+++ /dev/null\n@@ -1,2 +0,0 @@\n-ide.jdbc.username=aIRD6zjX5cQxI/m9DJif5Q==\n-ide.jdbc.password=VhJmkaB0IbNd6gp7RrM4xTincZPeWtav\n\\ No newline at end of file\n",
                        String del = diff.getString("diff");
                        String getDeletePath = del.split(" ")[2].substring(2);
                        removed.add(getDeletePath);
                        continue;
                    }

                    if (!oldPath.equalsIgnoreCase("/dev/null")) {
                        modified.add(oldPath);
                    }
                }
                commit.put("added", added);
                commit.put("removed", removed);
                commit.put("modified", modified);

            }
        } catch (Exception e) {

            logger.error("updateCommitAddModifyRemoved", e);
        }


    }

    /**
     * 1。hook 处理handGitHis
     * 2。首先在gitHis添加一条记录
     * 3。然后解析信息-> gitHisDetail -> databases
     * 4. creator + commits -> 计算出实际的提交人（虚拟账号）
     *
     * @param gitProjectId
     * @param commits
     * @param creator
     */

    private void handGitHis(Long gitProjectId, JSONArray commits, String creator, String email, String beforeCommit) {

        List<DataDevGitHisDetail> insertHisDetails = new ArrayList<DataDevGitHisDetail>();

        for (int index = 0; index < commits.size(); index++) {

            JSONObject oneCommit = commits.getJSONObject(index);
            String commitId = oneCommit.getString("id");

            //因为coding webhook给的数据不全，所有需要重新去调用接口查询这次commit修改的数据
            boolean isCoding = gitProjectId > GitHttpUtil._9YI;
            if (isCoding) {
                updateCommitAddModifyRemoved(oneCommit, commitId, gitProjectId);
                logger.error("===========webhookpostdatacommitsupdate" + gitProjectId + "==========" + commits);
            }

            Date created = JDGitCommits.utcString2Date(oneCommit.getString("timestamp"));
            HoldDoubleValue<String, String> commitNameAndMessage = JDGitCommits.handMessage(oneCommit.getString("message"));
            String message = commitNameAndMessage.b;
            creator = commitNameAndMessage.a != null ? commitNameAndMessage.a : creator;

            DataDevGitHis dataDevGitHis = new DataDevGitHis();
            dataDevGitHis.setCreator(creator);
            dataDevGitHis.setEmail(email);
            dataDevGitHis.setBeforeCommitId(beforeCommit);
            dataDevGitHis.setCreated(created);
            dataDevGitHis.setComment(message);
            dataDevGitHis.setCommitId(commitId);
            dataDevGitHis.setGitProjectId(gitProjectId);
            dataDevGitHis.setCommitByBDPIDE(commitNameAndMessage.a != null);

            insertHisDetails.addAll(getGitHisDetails(oneCommit.getJSONArray("modified"), DataDevHisTypeEnum.MOIDFY, dataDevGitHis));
            insertHisDetails.addAll(getGitHisDetails(oneCommit.getJSONArray("removed"), DataDevHisTypeEnum.DELETE, dataDevGitHis));
            insertHisDetails.addAll(getGitHisDetails(oneCommit.getJSONArray("added"), DataDevHisTypeEnum.ADD, dataDevGitHis));

        }
        logger.error("handGitHis=size" + insertHisDetails.size());
        dataDevGitHisDetailService.batchInsertDataDevGitHisDetails(insertHisDetails);


    }

    private List<DataDevGitHisDetail> getGitHisDetails(JSONArray datas, DataDevHisTypeEnum dataDevHisTypeEnum, DataDevGitHis dataDevGitHis) {
        List<DataDevGitHisDetail> results = new ArrayList<DataDevGitHisDetail>();
        if (datas != null && datas.size() > 0) {
            for (int index = 0; index < datas.size(); index++) {
                DataDevGitHisDetail hisDetail = new DataDevGitHisDetail();
                hisDetail.setGitProjectFilePath(datas.getString(index));
                hisDetail.setCommitType(dataDevHisTypeEnum.tocode());
                hisDetail.setGitProjectId(dataDevGitHis.getGitProjectId());
                hisDetail.setGitProjectId(dataDevGitHis.getGitProjectId());
                hisDetail.setCommitId(dataDevGitHis.getCommitId());
                hisDetail.setComment(dataDevGitHis.getComment());
                hisDetail.setCreator(dataDevGitHis.getCreator());
                hisDetail.setGitHisId(dataDevGitHis.getId());
                hisDetail.setCreated(dataDevGitHis.getCreated());
                hisDetail.setBeforeCommitIds(dataDevGitHis.getBeforeCommitId());
                hisDetail.setEmail(dataDevGitHis.getEmail());
                hisDetail.setCommitByBDPIDE(dataDevGitHis.isCommitByBDPIDE());
                results.add(hisDetail);
            }
        }

        return results;

    }

    /**
     * 处理添加的数据 GitDeleted=0
     *
     * @param added
     * @param lastCommitId
     * @param gitProjectId
     */
    private void handAddData(JSONArray added, String lastCommitId, Long gitProjectId) {
        // logger.error("=======handAddData=====");
        for (int index = 0; added != null && index < added.size(); index++) {
            try {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setLastCommitId(lastCommitId);
                jdGitFiles.setGitProjectId(gitProjectId);
                String gitProjectFilePath = added.getString(index);
                jdGitFiles.setFilePath(gitProjectFilePath);

                DataDevScriptFile updateParams = new DataDevScriptFile();
                updateParams.setLastGitModified(new Date());
                updateParams.setLastGitVersion(lastCommitId);
                updateParams.setGitDeleted(0);
                dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
            } catch (Exception e) {
                logger.error("handAddData", e);
            }
        }
    }

    /**
     * 处理移除的数据更新 GitDeleted=1
     *
     * @param removed
     * @param lastCommitId
     * @param gitProjectId
     */
    private void handRemovedData(JSONArray removed, String lastCommitId, Long gitProjectId) {
        //logger.error("=======handRemovedData=====");
        for (int index = 0; removed != null && index < removed.size(); index++) {
            try {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setLastCommitId(lastCommitId);
                jdGitFiles.setGitProjectId(gitProjectId);
                String gitProjectFilePath = removed.getString(index);
                jdGitFiles.setFilePath(gitProjectFilePath);

                DataDevScriptFile updateParams = new DataDevScriptFile();
                updateParams.setLastGitModified(new Date());
                updateParams.setLastGitVersion(lastCommitId);
                updateParams.setGitDeleted(1);
                dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
            } catch (Exception e) {
                logger.error("handRemovedData", e);
            }
        }
    }

    /**
     * 1。处理修改的通知
     * 2。根据commitID获取到当前文件，并且计算Md5 值，保存到数据库中
     *
     * @param modified
     */
    private void handModifedData(JSONArray modified, String lastCommitId, Long gitProjectId) {
        //logger.error("=======handModifedData=====");

        for (int index = 0; modified != null && index < modified.size(); index++) {
            try {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setLastCommitId(lastCommitId);
                jdGitFiles.setGitProjectId(gitProjectId);
                String gitProjectFilePath = modified.getString(index);
                jdGitFiles.setFilePath(gitProjectFilePath);
                byte[] bytes = jdGitFiles.getFileRaw();
                String md5 = MD5Util.getMD5(bytes);

                /**
                 *    String md5 = null;
                 *                 byte[] bytes = null;
                 *                 if (gitProjectId > GitHttpUtil._9YI) {
                 *                     jdGitFiles.loadFileContent();
                 *                     md5 = jdGitFiles.getFileMd5();
                 *                 } else {
                 *                     bytes = (jdGitFiles.getFileRaw());
                 *                     md5 = MD5Util.getMD5(bytes);
                 *                 }
                 */

                DataDevScriptFile updateParams = new DataDevScriptFile();
                updateParams.setLastGitModified(new Date());
                updateParams.setLastGitVersionMd5(md5);
                updateParams.setLastGitVersion(lastCommitId);
                updateParams.setGitDeleted(0);
                logger.error("=======handModifedData===== gitProjectId " + gitProjectId);
                logger.error("=======handModifedData===== gitProjectFilePath " + gitProjectFilePath);
                logger.error("=======handModifedData===== md5 " + md5);
                logger.error("=======handModifedData===== lastCommitId " + lastCommitId);

                dataDevScriptFileService.updateDataDevScriptFile(gitProjectId, gitProjectFilePath, updateParams);
            } catch (Exception e) {
                logger.error("handModifedData", e);
            }
        }
    }

    /**
     * 1。获取 Private Token User
     *
     * @return
     */

    private String getPrivateTokenUser() {
        if (PRIVATE_TOKEN_USER == null) {
            PRIVATE_TOKEN_USER = GitHttpUtil.getPrivetUser();
        }
        return PRIVATE_TOKEN_USER;
    }

    /**
     * 1。获取header 数据
     *
     * @param request
     * @return
     */
    private Map<String, String> getHeaderData(HttpServletRequest request) {
        Map<String, String> header = new HashMap<String, String>();
        try {
            Enumeration enumeration = request.getHeaderNames();
            while (enumeration.hasMoreElements()) {
                String nextKey = enumeration.nextElement().toString();
                header.put(nextKey, request.getHeader(nextKey));
            }
        } catch (Exception e) {
            logger.error("getHeaderData", e);
        }
        return header;
    }


    /**
     * 1。提取Request中Post的数据
     *
     * @param request
     * @return
     */
    private JSONObject getRequestPostData(HttpServletRequest request) {
        JSONObject postData = null;
        StringBuilder jb = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
            logger.error(" post line:" + jb.toString());
            postData = JSONObject.parseObject(jb.toString());
        } catch (Exception e) {
            postData = new JSONObject();
            logger.error("getRequestPostData", e);
        }
        return postData;
    }

}
