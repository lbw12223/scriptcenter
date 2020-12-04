package com.jd.bdp.datadev.jdgit;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.component.EnvInit;
import com.jd.bdp.datadev.component.SpringContextUtil;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitNameSpace;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import com.jd.bdp.datadev.enums.DataDevGitOrCodingEnum;
import com.jd.jsf.gd.util.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/5/17.
 */
public class JDGitProjects extends GitConvertToDataDevDomain<DataDevGitProject, JDGitProjects> implements JSONObjectCovertToGitDomain<JDGitProjects> {

    private static Logger log = Logger.getLogger(JDGitProjects.class);

    private Long gitProjectId;
    private String projectPath;
    private String projectName;

    private Long namespaceId;

    private String simple;
    private String visibility; //private , internal , public

    private String webUrl;
    private String sshUrl;
    private String description;
    private String createDate;

    private List<JDGitProjectShareGroups> sharedWithGroups;  //项目分享的groups
    private List<JDGitMembers> jdGitMembers;    //项目成员
    private Long groupId;
    private Integer gitOrCodingCode;
    private String searchNameSpace;


    @Override
    public DataDevGitProject convertDataDevDomain(JDGitProjects jdGitProject) {
        DataDevGitProject temp = new DataDevGitProject();
        temp.setGitProjectId(jdGitProject.getGitProjectId());
        temp.setGitProjectPath(jdGitProject.getProjectPath());
        temp.setGitProjectName(jdGitProject.getProjectName());
        temp.setGroupId(jdGitProject.getGroupId());
        List<JDGitProjectShareGroups> jdGitProjectShareGroupsList = jdGitProject.getSharedWithGroups();
        if (jdGitProjectShareGroupsList != null && jdGitProjectShareGroupsList.size() > 0) {
            JDGitProjectShareGroups jdGitProjectShareGroup = new JDGitProjectShareGroups();
            temp.setSharedGitProjectGroup(jdGitProjectShareGroup.covertDataDevDomainIterable(jdGitProjectShareGroupsList));
        }

        return temp;
    }


    @Override
    public JDGitProjects covertGitDomain(JSONObject jsonObject) {
        JDGitProjects jdGitProjectsTemp = new JDGitProjects();
        Long gitProjectId = jsonObject.getLong("id");
        jdGitProjectsTemp.setGitProjectId(gitProjectId);
        jdGitProjectsTemp.setProjectPath(jsonObject.getString("path_with_namespace"));
        jdGitProjectsTemp.setProjectName(jsonObject.getString("name"));
        jdGitProjectsTemp.setSshUrl(jsonObject.getString("ssh_url_to_repo"));
        jdGitProjectsTemp.setWebUrl(jsonObject.getString("http_url_to_repo"));
        jdGitProjectsTemp.setDescription(jsonObject.getString("description"));
        jdGitProjectsTemp.setCreateDate(JDGitCommits.utcString2YYYYMMDDHHMMSS(jsonObject.getString("created_at")));

        JSONArray sharedWithGroupsArray = jsonObject.getJSONArray("shared_with_groups");
        jdGitProjectsTemp.setSharedWithGroups(JDGitProjectShareGroups.covertGitGroups(gitProjectId, sharedWithGroupsArray));
        //将namespace 添加到sharedWithGroup里面 , 创建在组里面的项目

        JSONObject namespaceJson = jsonObject.getJSONObject("namespace");
        if (namespaceJson != null && namespaceJson.size() > 0) {
//            boolean flag=false;
            if (namespaceJson.containsKey("kind") && namespaceJson.getString("kind").equals("group")) {
                //git
                JDGitProjectShareGroups temp = new JDGitProjectShareGroups();
                temp.setGitProjectId(gitProjectId);
                temp.setJdGroupId(namespaceJson.getLong("id"));
                temp.setGroupName(namespaceJson.getString("name"));
                temp.setGroupAccessLevel(40);
                jdGitProjectsTemp.setGroupId(temp.getJdGroupId());
                jdGitProjectsTemp.getSharedWithGroups().add(temp);
            } else if (DataDevGitOrCodingEnum.CODING.tocode() == DataDevGitOrCodingEnum.checkUrl(jsonObject.getString("web_url")).tocode()) {
                //coding
//                flag=true;
                JDGitProjectShareGroups temp = new JDGitProjectShareGroups();
                temp.setGitProjectId(gitProjectId);
                temp.setJdGroupId(namespaceJson.getLong("id"));
                temp.setGroupName(namespaceJson.getString("name"));
                temp.setGroupAccessLevel(40);
                jdGitProjectsTemp.setGroupId(temp.getJdGroupId());
                jdGitProjectsTemp.getSharedWithGroups().add(temp);

                JDGitGroups groups = new JDGitGroups();
                groups.setGitOrCodingCode(DataDevGitOrCodingEnum.CODING.tocode());
                try {
                    List<DataDevGitGroup> groupList = groups.listGroups(namespaceJson.getString("name"));
                    if (groupList != null && groupList.size() >= 1) {

                        for (DataDevGitGroup group : groupList) {
                            if (temp.getGroupName().equals(group.getName())) {
                                temp.setJdGroupId(group.getGitGroupId());
                            }
                        }
                    }
//                    else {
//                        throw new Exception("通过namespace名称获取组失败");
//                    }
                } catch (Exception ex) {
                    log.error("通过namespace名称获取组失败:", ex);
                }
            }

        }
        jdGitProjectsTemp.setVisibility((jsonObject.containsKey("visibility") ? jsonObject.getString("visibility") : ""));
        return jdGitProjectsTemp;
    }

    /**
     * 创建 coding 项目的时候，默认先添加一个README.md文件到Master
     *
     * @return
     * @throws Exception
     */
    public DataDevGitProject createProject() throws Exception {
        JSONObject params = new JSONObject();
        params.put("name", projectName);


        /**
         * coding 1 用名称去查询namespaceid
         * git 因为group 和 namespace id  一样
         */
        if (gitOrCodingCode.equals(DataDevGitOrCodingEnum.GIT.tocode())) {
            if (namespaceId != null && namespaceId > 0) {
                params.put("namespace_id", GitHttpUtil.getRealGroupId(namespaceId));
            }

        } else {
            if (StringUtils.isNotBlank(searchNameSpace)) {
                JDGitNameSpaces jdGitNameSpaces = new JDGitNameSpaces();
                jdGitNameSpaces.setGitOrCodingCode(gitOrCodingCode);
                List<DataDevGitNameSpace> dataDevGitNameSpaces = jdGitNameSpaces.searchNameSpace(searchNameSpace);
                params.put("namespace_id", dataDevGitNameSpaces.get(0).getId());
            }
        }


        if (StringUtils.isNotEmpty(description)) {
            params.put("description", description);
        }
        if (gitOrCodingCode == DataDevGitOrCodingEnum.GIT.tocode()) {
            params.put("visibility", "private");
        }
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doPost("projects", params);

        if (!gitHttpResponse.getResponseCode().equals(201)) {
            if (gitHttpResponse.getResponseMessage().indexOf("has already been taken") != -1) {
                throw new RuntimeException(projectName + " 在当前组下已经存在!");
            }
            throw new RuntimeException(gitHttpResponse.getResponseMessage());
        }

        JDGitProjects jdGitProjects = covertGitDomain(JSONObject.parseObject(gitHttpResponse.getResponseMessage()));
        handlerId(jdGitProjects, gitOrCodingCode);
        DataDevGitProject dataDevGitProject = convertDataDevDomain(jdGitProjects);


        if (gitOrCodingCode.equals(DataDevGitOrCodingEnum.CODING.tocode())) {
            try {
                JDGitFiles jdGitFiles = new JDGitFiles();
                jdGitFiles.setGitProjectId(dataDevGitProject.getGitProjectId());
                jdGitFiles.setBranch("master");
                jdGitFiles.setFilePath("README.md");
                jdGitFiles.setBinary(true);
                jdGitFiles.setBytes("README.md".getBytes());
                jdGitFiles.setErp(GitRequestClientJdCoding.PRIVETE_TOKEN_USER);
                jdGitFiles.setName(GitRequestClientJdCoding.PRIVETE_TOKEN_USER);
                jdGitFiles.addOrUpdateFile("create", "README.md");
            } catch (Exception e) {
                log.error("create coding project", e);
            }

        }
        return dataDevGitProject;
    }

    public void addProjectMember() throws Exception {
        if (jdGitMembers != null) {
            for (JDGitMembers member : jdGitMembers) {
                addProjectMember(member);
            }
        }
    }

    public void addProjectMember(JDGitMembers member) throws Exception {
        JSONObject params = new JSONObject();
        params.put("id", GitHttpUtil.getRealProjectId(gitProjectId));
        params.put("user_id", member.getGitUserId());
        params.put("access_level", member.getAccessLevel());
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doPost("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/members", params);
        if (!gitHttpResponse.getResponseCode().equals(201)) {
            if (gitHttpResponse.getResponseMessage().indexOf("Member already exists") != -1) {
                throw new RuntimeException(member.getUserName() + " 用户已经存在");
            }
            throw new RuntimeException(gitHttpResponse.getResponseMessage());
        }
    }

    public void addSharedGroup(JDGitProjectShareGroups jdGitProjectShareGroups) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", GitHttpUtil.getRealProjectId(gitProjectId));
        jsonObject.put("group_id", jdGitProjectShareGroups.getJdGroupId());
        jsonObject.put("group_access", jdGitProjectShareGroups.getGroupAccessLevel());
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doPost("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/share", jsonObject);
        if (!gitHttpResponse.getResponseCode().equals(201)) {
            if (gitHttpResponse.getResponseMessage().indexOf("Group already shared with this group") == 12 ||
                    gitHttpResponse.getResponseMessage().indexOf("Project cannot be shared with the group it is in or one of its ancestors") == 12) {
                throw new RuntimeException(jdGitProjectShareGroups.getGroupName() + " 组已经存在");
            }
            throw new RuntimeException(gitHttpResponse.getResponseMessage());
        }
    }

    public void deleteProjectMember(JDGitMembers member) throws Exception {
        try {

            GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doDelete("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/members/" + member.getGitUserId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSharedGroup(JDGitProjectShareGroups jdGitProjectShareGroups) {
        try {
            GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doDelete("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/share/" + jdGitProjectShareGroups.getJdGroupId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        /*get All Project*/
        JDGitProjects temp = new JDGitProjects();
        /*temp.setSimple("false");
        temp.setVisibility("private");
        System.out.println(temp.listAll());*/
        temp.setProjectName("zhangrui156te1s1t1");
        temp.setDescription("description");
        //   temp.setNamespaceId(13780L);
        temp.setGitProjectId(23759L);
        temp.setVisibility("private");
        String urlGit = "http://git.jd.com/wqh/wqh_3.git";
        String proPath = "wqh/wqh_3";
        String proName = "wqh_3";

//        List<DataDevGitProject> list = temp.listAll();

//        System.out.println(list);
//        for( DataDevGitProject dataDevGitProject:list){
//            String webUrlTemp = dataDevGitProject.
//        }
        /*Get Project Member*/
        //  JDGitProjects jdGitProjects = new JDGitProjects();
        //  jdGitProjects.setGitProjectId(23057L);

        // System.out.println(jdGitProjects.listProjectMembers());

        // temp.addProjectHook("http://172.22.213.57:899/testssss");
        JSONArray allHooks = temp.getCurrentPlatformHook();
        temp.deleteHooks(allHooks);
    }

    /**
     * https://gitlab.msu.edu/help/api/projects.md#list-all-projects
     *
     * @return
     * @throws Exception
     */
    public List<DataDevGitProject> listAll(Integer gitOrCodingCode) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtils.isNotBlank(visibility)) {
            params.put("visibility", visibility);
        }
        if (StringUtils.isNotBlank(simple)) {
            params.put("simple", simple);
        }
        List<JDGitProjects> listAll = GitHttpUtil.createClientByCode(gitOrCodingCode).pageAll("projects", params, this);

        /**
         * coding api
         */
        handlerId(listAll, gitOrCodingCode);

        return covertDataDevDomainIterable(listAll);
    }

    /**
     * 获取项目详情
     *
     * @return
     * @throws Exception
     */
    public JDGitProjects gitProjectDetail() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doGet("projects/" + GitHttpUtil.getRealProjectId(gitProjectId), params);
        if (gitHttpResponse.getResponseCode().equals(200)) {
            String responseMessage = gitHttpResponse.getResponseMessage();
            return covertGitDomain(JSONObject.parseObject(responseMessage));
        }
        return null;
    }

    /**
     * 获取当前平台设置hook
     *
     * @return
     * @throws Exception
     */
    public JSONArray getCurrentPlatformHook() throws Exception {
        try {
            JSONArray allHooks = getAllProjectHook();
            JSONArray currentPlatform = new JSONArray();
            String uri = SpringContextUtil.getBean(EnvInit.class).getGitProjectHookUrl();
            for (int index = 0; index < allHooks.size(); index++) {
                if (allHooks.getJSONObject(index).getString("url").indexOf(uri) != -1) {
                    currentPlatform.add(allHooks.getJSONObject(index));
                }
            }
            return currentPlatform;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除hooks
     *
     * @param hooks
     * @throws Exception
     */
    public void deleteHooks(JSONArray hooks) throws Exception {
        if (hooks != null && hooks.size() > 0) {
            for (int index = 0; index < hooks.size(); index++) {
                try {
                    GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doDelete("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/hooks/" + hooks.getJSONObject(index).getLong("id"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 应该去分页查询，现在只查询了一页数据
     *
     * @return
     * @throws Exception
     */
    private JSONArray getAllProjectHook() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("page", String.valueOf(1));
        params.put("per_page", String.valueOf(100));

        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doGet("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/hooks", params);
        if (gitHttpResponse.getResponseCode().equals(200) && StringUtils.isNotBlank(gitHttpResponse.getResponseMessage())) {
            return JSONArray.parseArray(gitHttpResponse.getResponseMessage());
        }
        return new JSONArray();
    }

    /**
     * 1。添加一个Hook
     * 2.为了防止重复，先查询出当前平台有的hooks，然后删除
     * <p>
     * http://git.jd.com/help/api/projects.md#add-project-hook
     */
    public void addProjectHook(String url) throws Exception {


        JDGitProjects jdGitProjects = new JDGitProjects();
        jdGitProjects.setGitProjectId(gitProjectId);
        JSONArray hooks = jdGitProjects.getCurrentPlatformHook();
        jdGitProjects.deleteHooks(hooks);


        JSONObject params = new JSONObject();
        params.put("push_events", true);
        params.put("issues_events", true);
        params.put("merge_requests_events", true);
        params.put("tag_push_events", true);
        params.put("note_events", true);
        params.put("job_events", true);
        params.put("pipeline_events", true);
        params.put("wiki_events", true);
        params.put("enable_ssl_verification", true);

        params.put("token", GitHttpUtil.getPrivetUser());
        params.put("url", url);
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClient(gitProjectId).doPost("projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/hooks", params);
        if (!gitHttpResponse.getResponseCode().equals(201)) {
            throw new RuntimeException("addProjectHook" + gitProjectId + " add hook error" + gitHttpResponse.getResponseMessage());
        }

    }

    /**
     * 查询project member
     * http://git.jd.com/help/api/members.md#list-all-members-of-a-group-or-project
     *
     * @return
     * @throws Exception
     */
    public List<DataDevGitProjectMember> listProjectMembers() throws Exception {
        String url = "projects/" + GitHttpUtil.getRealProjectId(gitProjectId) + "/members";
        JDGitMembers jdGitMembers = new JDGitMembers();
        jdGitMembers.setGitProjectId(GitHttpUtil.getRealProjectId(gitProjectId));
        List<JDGitMembers> result = GitHttpUtil.createClient(gitProjectId).pageAll(url, new HashMap<String, String>(), jdGitMembers);
        if (result != null && result.size() > 0) {
            for (JDGitMembers jdGitMember : result) {
                //这里不改
                jdGitMember.setGitProjectId(gitProjectId);
            }
        }
        JDGitMembers dataDevGitProjectMember = new JDGitMembers();
        return dataDevGitProjectMember.covertDataDevDomainIterable(result);
    }


    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<JDGitProjectShareGroups> getSharedWithGroups() {
        return sharedWithGroups;
    }

    public void setSharedWithGroups(List<JDGitProjectShareGroups> sharedWithGroups) {
        this.sharedWithGroups = sharedWithGroups;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(Long namespaceId) {
        this.namespaceId = namespaceId;
    }

    public List<JDGitMembers> getJdGitMembers() {
        return jdGitMembers;
    }

    public void setJdGitMembers(List<JDGitMembers> jdGitMembers) {
        this.jdGitMembers = jdGitMembers;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getSshUrl() {
        return sshUrl;
    }

    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * 获取项目详情
     *
     * @return
     * @throws Exception
     */
    public JDGitProjects gitProjectDetailByPath(Integer gitOrCodingCode) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        String gitProjectedPathEncode = URL.encode(projectPath);
        params.put("visibility", "private");
        System.err.println("proFileEncode:" + gitProjectedPathEncode);

        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doGet("projects/" + gitProjectedPathEncode, params);
        if (gitHttpResponse.getResponseCode().equals(200)) {
            String responseMessage = gitHttpResponse.getResponseMessage();
            JDGitProjects jdGitProjects = covertGitDomain(JSONObject.parseObject(responseMessage));
            handlerId(jdGitProjects, gitOrCodingCode);
            return jdGitProjects;
        } else if (gitHttpResponse.getResponseCode().equals(404)) {
            return null;
        } else {
            throw new Exception(gitHttpResponse.getResponseCode() + "   " + gitHttpResponse.getResponseMessage());
        }
    }


    /**
     * 如果是coding项目，Id+9亿；
     *
     * @param jdGitProjects
     */
    private void handlerId(JDGitProjects jdGitProjects, Integer gitOrCodingCode) {
        if (gitOrCodingCode == DataDevGitOrCodingEnum.CODING.tocode()) {
            //处理Coding 的 ID
            jdGitProjects.setGitProjectId(jdGitProjects.getGitProjectId() + GitHttpUtil._9YI);
            if (jdGitProjects.getSharedWithGroups().size() > 0) {
                for (JDGitProjectShareGroups groups : jdGitProjects.getSharedWithGroups()) {
                    groups.setGitProjectId(jdGitProjects.getGitProjectId());
                    groups.setJdGroupId(groups.getJdGroupId() + GitHttpUtil._9YI);
                    jdGitProjects.setGroupId(groups.getJdGroupId());
                }
            }
        }
    }

    private void handlerId(List<JDGitProjects> jdGitProjectsList, Integer gitOrCodingCode) {
        if (jdGitProjectsList != null && jdGitProjectsList.size() > 0 && gitOrCodingCode == DataDevGitOrCodingEnum.CODING.tocode()) {
            for (JDGitProjects jdGitProjects : jdGitProjectsList) {
                //处理Coding 的 ID
                jdGitProjects.setGitProjectId(jdGitProjects.getGitProjectId() + GitHttpUtil._9YI);
                if (jdGitProjects.getSharedWithGroups().size() > 0) {
                    for (JDGitProjectShareGroups groups : jdGitProjects.getSharedWithGroups()) {
                        groups.setGitProjectId(jdGitProjects.getGitProjectId());
                        groups.setJdGroupId(groups.getJdGroupId() + GitHttpUtil._9YI);
                        jdGitProjects.setGroupId(groups.getJdGroupId());
                    }
                }
            }
        }
    }


    /**
     * 获取项目详情
     *
     * @return
     * @throws Exception
     */
    public Integer testGitConnect(Integer gitOrCodingCode) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        String gitProjectedPathEncode = URL.encode(projectPath);
        System.err.println("proFileEncode:" + gitProjectedPathEncode);

        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doGet("projects/" + gitProjectedPathEncode, params);
        System.err.println("proFileEncode:" + JSONObject.toJSONString(gitHttpResponse));
        System.err.println("proFileEncode:" + gitProjectedPathEncode);

        return gitHttpResponse.getResponseCode();
    }


    public Integer getGitOrCodingCode() {
        return gitOrCodingCode;
    }

    public void setGitOrCodingCode(Integer gitOrCodingCode) {
        this.gitOrCodingCode = gitOrCodingCode;
    }


    public String getSearchNameSpace() {
        return searchNameSpace;
    }

    public void setSearchNameSpace(String searchNameSpace) {
        this.searchNameSpace = searchNameSpace;
    }

    @Override
    public String toString() {
        return "JDGitProjects{" +
                "gitProjectId=" + gitProjectId +
                ", projectPath='" + projectPath + '\'' +
                ", projectName='" + projectName + '\'' +
                ", namespaceId=" + namespaceId +
                ", simple='" + simple + '\'' +
                ", visibility='" + visibility + '\'' +
                ", webUrl='" + webUrl + '\'' +
                ", sshUrl='" + sshUrl + '\'' +
                ", description='" + description + '\'' +
                ", createDate='" + createDate + '\'' +
                ", sharedWithGroups=" + sharedWithGroups +
                ", jdGitMembers=" + jdGitMembers +
                '}';
    }
}
