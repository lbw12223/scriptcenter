package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.domain.DataDevGitNameSpace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/5/26.
 */
public class JDGitNameSpaces extends GitConvertToDataDevDomain<DataDevGitNameSpace, JDGitNameSpaces> implements JSONObjectCovertToGitDomain<JDGitNameSpaces> {

    private Boolean owned = false;


    private Long id;
    private String name;
    private String path;
    private String kind;
    private String fullPath;
    private Long parentId;
    private String membersCcountWithDescendants;


    private Integer gitOrCodingCode ;



    @Override
    public DataDevGitNameSpace convertDataDevDomain(JDGitNameSpaces jdGitNameSpaces) {
        DataDevGitNameSpace dataDevGitNameSpace = new DataDevGitNameSpace();
        dataDevGitNameSpace.setName(jdGitNameSpaces.getName());
        dataDevGitNameSpace.setFullPath(jdGitNameSpaces.getFullPath());
        dataDevGitNameSpace.setKind(jdGitNameSpaces.getKind());
        dataDevGitNameSpace.setId(jdGitNameSpaces.getId());
        dataDevGitNameSpace.setMembersCcountWithDescendants(jdGitNameSpaces.getMembersCcountWithDescendants());
        dataDevGitNameSpace.setParentId(jdGitNameSpaces.getParentId());
        return dataDevGitNameSpace;
    }

    @Override
    public JDGitNameSpaces covertGitDomain(JSONObject jsonObject) {
        JDGitNameSpaces jdGitNameSpaces = new JDGitNameSpaces();
        jdGitNameSpaces.setId(jsonObject.getLong("id"));
        jdGitNameSpaces.setPath(jsonObject.getString("path"));
        jdGitNameSpaces.setName(jsonObject.getString("name"));
        jdGitNameSpaces.setParentId(jsonObject.getLong("parent_id"));
        jdGitNameSpaces.setKind(jsonObject.getString("kind"));
        jdGitNameSpaces.setFullPath(jsonObject.getString("full_path"));
        jdGitNameSpaces.setMembersCcountWithDescendants(jsonObject.getString("members_count_with_descendants"));
        return jdGitNameSpaces;
    }


    public List<DataDevGitNameSpace> searchNameSpace(String searchKeyWord) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("search", searchKeyWord);
        List<JDGitNameSpaces> listAll = GitHttpUtil.createClientByCode(gitOrCodingCode).pageAll("namespaces", params, this);
        return covertDataDevDomainIterable(listAll);
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMembersCcountWithDescendants() {
        return membersCcountWithDescendants;
    }

    public void setMembersCcountWithDescendants(String membersCcountWithDescendants) {
        this.membersCcountWithDescendants = membersCcountWithDescendants;
    }


    public Integer getGitOrCodingCode() {
        return gitOrCodingCode;
    }

    public void setGitOrCodingCode(Integer gitOrCodingCode) {
        this.gitOrCodingCode = gitOrCodingCode;
    }
}
