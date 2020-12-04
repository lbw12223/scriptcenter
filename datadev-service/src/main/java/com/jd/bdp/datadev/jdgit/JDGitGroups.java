package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevGitGroup;
import com.jd.bdp.datadev.domain.DataDevGitGroupMember;
import com.jd.bdp.datadev.domain.DataDevGitNameSpace;
import com.jd.bdp.datadev.enums.DataDevGitInitFlag;
import com.jd.bdp.datadev.enums.DataDevGitOrCodingEnum;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by zhangrui25 on 2018/5/26.
 */
public class JDGitGroups extends GitConvertToDataDevDomain<DataDevGitGroup, JDGitGroups> implements JSONObjectCovertToGitDomain<JDGitGroups> {

    private static Logger log = Logger.getLogger(JDGitGroups.class);
    private Boolean owned = false;


    private Long jdGroupId;
    private String name;
    private String path;
    private String fullName;
    private String fullPath;
    private String description;
    private Integer gitOrCodingCode;
    private List<JDGitMembers> members;


    @Override
    public DataDevGitGroup convertDataDevDomain(JDGitGroups jdGitGroups) {
        DataDevGitGroup dataDevGitGroup = new DataDevGitGroup();
        dataDevGitGroup.setName(jdGitGroups.getName());
        dataDevGitGroup.setFullName(jdGitGroups.getFullName());
        dataDevGitGroup.setFullPath(jdGitGroups.getFullPath());
        dataDevGitGroup.setGitGroupId(jdGitGroups.getJdGroupId());
        dataDevGitGroup.setFinishProjectMemberFlag(DataDevGitInitFlag.NEED_INIT.tocode());
        return dataDevGitGroup;
    }

    @Override
    public JDGitGroups covertGitDomain(JSONObject jsonObject) {
        JDGitGroups jdGitGroups = new JDGitGroups();
        jdGitGroups.setJdGroupId(jsonObject.getLong("id"));
        jdGitGroups.setPath(jsonObject.getString("path"));
        jdGitGroups.setName(jsonObject.getString("name"));
        jdGitGroups.setFullName(jsonObject.getString("full_name"));
        jdGitGroups.setFullPath(jsonObject.getString("full_path"));
        return jdGitGroups;
    }


    public DataDevGitGroup createGroup() throws Exception {
        JSONObject params = new JSONObject();
        params.put("name", name);
        params.put("description", description);
        params.put("path", name);
        params.put("visibility", "private");
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doPost("groups", params);

        if (gitOrCodingCode.equals(DataDevGitOrCodingEnum.CODING.tocode())) {
            if (!gitHttpResponse.getResponseCode().equals(200)) {
                throw new RuntimeException(gitHttpResponse.getResponseMessage());
            }
        } else {
            if (!gitHttpResponse.getResponseCode().equals(201)) {
                if (gitHttpResponse.getResponseMessage().indexOf("has already been taken") != -1) {
                    throw new RuntimeException("组[" + name + "]已经存在.");
                }
                throw new RuntimeException(gitHttpResponse.getResponseMessage());
            }
        }

        DataDevGitGroup temp = convertDataDevDomain(covertGitDomain(JSONObject.parseObject(gitHttpResponse.getResponseMessage())));
        if (gitOrCodingCode.equals(DataDevGitOrCodingEnum.CODING.tocode())) {
            temp.setGitGroupId(temp.getGitGroupId() +  GitHttpUtil._9YI);
        }
        return temp;
    }

    /**
     * 添加member
     *
     * @throws Exception
     */
    public void addGroupMember() throws Exception {
        if (members != null) {
            for (JDGitMembers member : members) {
                JSONObject params = new JSONObject();
                params.put("id", GitHttpUtil.getRealGroupId(getJdGroupId()));
                params.put("user_id", member.getGitUserId());
                params.put("access_level", member.getAccessLevel());
                GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doPost("groups/" + GitHttpUtil.getRealGroupId(getJdGroupId()) + "/members", params);
                if (!gitHttpResponse.getResponseCode().equals(201) && !gitHttpResponse.getResponseCode().equals(409)) {
                    //409 项目成员已经存在
                    throw new RuntimeException(gitHttpResponse.getResponseMessage());
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        //list groups
        JDGitGroups jdGitGroups = new JDGitGroups();
        jdGitGroups.owned = false;
//        System.out.println(jdGitGroups.listAll());
        jdGitGroups.setJdGroupId(900056056L);
        List<DataDevGitGroupMember> dataDevGitGroupMembers = jdGitGroups.listGroupMembers();
        System.out.println(dataDevGitGroupMembers);

      /*  jdGitGroups.setJdGroupId(14503L);
        JDGitMembers jdGitMembers = new JDGitMembers();
        jdGitMembers.setGitUserId(2697L);
        jdGitMembers.setAccessLevel(40);
        jdGitGroups.setMembers(Arrays.asList(jdGitMembers));
        jdGitGroups.addGroupMember();*/
        // System.out.println(jdGitGroups.createGroup());
    }

    /**
     * http://git.jd.com/help/api/groups.md#list-groups
     *
     * @return
     */
    public List<DataDevGitGroup> listAll() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("owned", String.valueOf(owned));
        List<JDGitGroups> lists = GitHttpUtil.createClientByCode(DataDevGitOrCodingEnum.GIT.tocode()).pageAll("groups", params, this);
        List<JDGitGroups> coding = GitHttpUtil.createClientByCode(DataDevGitOrCodingEnum.CODING.tocode()).pageAll("groups", params, this);

        if (coding != null && coding.size() > 0) {
            for (JDGitGroups codingGroup : coding) {
                codingGroup.setJdGroupId(GitHttpUtil._9YI + codingGroup.getJdGroupId());
            }
        }
        lists.addAll(coding);
        return covertDataDevDomainIterable(lists);
    }

    /**
     * http://git.jd.com/help/api/groups.md#list-groups
     *
     * @return
     */
    public List<DataDevGitGroup> listGroups(String keyWord) throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("search", keyWord);
        params.put("owned", owned.toString());
        params.put("all_available", String.valueOf(true));

        List<JDGitGroups> lists = GitHttpUtil.createClientByCode(gitOrCodingCode).pageAll("groups", params, this);
        return covertDataDevDomainIterable(lists);
    }

    /**
     * http://git.jd.com/help/api/members.md#list-all-members-of-a-group-or-project
     * <p>
     * 13780
     *
     * @return
     */
    public List<DataDevGitGroupMember> listGroupMembers() throws Exception {
        List<DataDevGitGroupMember> result = new ArrayList<DataDevGitGroupMember>();
        JDGitMembers jdGitMembers = new JDGitMembers();
        List<JDGitMembers> list = GitHttpUtil.createClient(jdGroupId).pageAll("groups/" + GitHttpUtil.getRealGroupId(jdGroupId) + "/members", new HashMap<String, String>(), jdGitMembers);
//        log.error("=========项目组"+jdGroupId+"成员："+JSONArray.toJSONString(list));
        if (list != null && list.size() > 0) {
            for (JDGitMembers jdGitMember : list) {
                //这里不修改
                jdGitMember.setGitGroupId(jdGroupId);
                DataDevGitGroupMember dataDevGitGroupMember = new DataDevGitGroupMember();
                dataDevGitGroupMember.setGitGroupId(jdGroupId);
                dataDevGitGroupMember.setAccessLevel(jdGitMember.getAccessLevel());
                dataDevGitGroupMember.setGitMemberId(jdGitMember.getGitUserId());
                dataDevGitGroupMember.setGitMemberName(jdGitMember.getName());
                dataDevGitGroupMember.setGitMemberUserName(jdGitMember.getUserName());
                dataDevGitGroupMember.setState(jdGitMember.getState());
                result.add(dataDevGitGroupMember);
            }
        }
        return result;
    }

    public JDGitGroups getGroupByPath() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("owned", String.valueOf(owned));
        GitHttpResponse gitHttpResponse = GitHttpUtil.createClientByCode(gitOrCodingCode).doGet("groups/" + path, params);
        if (gitHttpResponse.getResponseCode().equals(200)) {
            return covertGitDomain(JSONObject.parseObject(gitHttpResponse.getResponseMessage()));
        } else {
            return null;
        }
    }

    public Long getJdGroupId() {
        return jdGroupId;
    }

    public void setJdGroupId(Long jdGroupId) {
        this.jdGroupId = jdGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JDGitMembers> getMembers() {
        return members;
    }

    public void setMembers(List<JDGitMembers> members) {
        this.members = members;
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }

    public Integer getGitOrCodingCode() {
        return gitOrCodingCode;
    }

    public void setGitOrCodingCode(Integer gitOrCodingCode) {
        this.gitOrCodingCode = gitOrCodingCode;
    }

    @Override
    public String toString() {
        return "JDGitGroups{" +
                "owned=" + owned +
                ", jdGroupId=" + jdGroupId +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", fullName='" + fullName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                '}';
    }
}
