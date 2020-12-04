package com.jd.bdp.datadev.domain;

/**
 * Created by zhangrui25 on 2018/5/29.
 */
public class DataDevGitNameSpace {
/*
     "id": 71,
             "name": "zhangsan",
             "path": "zhangsan",
             "kind": "user",
             "full_path": "xhangsan",
             "parent_id": null,
             "members_count_with_descendants": null
    */

    private Long id ;
    private String name ;
    private String path;
    private String kind ;
    private String fullPath;
    private Long parentId;
    private String membersCcountWithDescendants;

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
}
