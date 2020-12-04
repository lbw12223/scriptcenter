package com.jd.bdp.datadev.domain;


import java.util.Objects;

public class DataDevGitDto {
    private String name;
    private String path;
    private Long id;
    private Integer category; //   2:gitGroup  3:gitProject
    private String value; //3:gitProjectId  2:gitGroupId

    public DataDevGitDto(){

    }
    public DataDevGitDto(DataDevGitProject project){
        this.name = project.getGitProjectName();
        this.path = project.getGitProjectPath();
        this.id = project.getGitProjectId();
        this.category = 3;
        this.value = "3:"+this.id;
    }
    public DataDevGitDto(DataDevGitGroup gitGroup){
        this.name = gitGroup.getName();
        this.path = gitGroup.getFullPath();
        this.id = gitGroup.getGitGroupId();
        this.category = 2;
        this.value = "2:"+this.id;
    }

    @Override
    public int hashCode() {
        if(value  == null){
            return super.hashCode();
        }
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataDevGitDto gitDto = (DataDevGitDto) o;
        return Objects.equals(value, gitDto.value);
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
