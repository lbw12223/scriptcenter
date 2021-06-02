package com.jd.bdp.datadev.service;

import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.api.model.ApiResult;
import com.jd.bdp.planing.domain.bo.*;
import org.springframework.stereotype.Service;

@Service
public class ProjectInterfaceImpl implements ProjectInterface {

    @Override
    public ApiResult<ProjectBO> getProjectById(String s, String s1, Long aLong, ProjectBO projectBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectBO> getGrantAuthorityProject(String s, String s1, Long aLong, ProjectBO projectBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectBO> getAllProject(String s, String s1, Long aLong, ProjectBO projectBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectAccountRelBO> getGrantAuthorityMarket(String s, String s1, Long aLong, ProjectBO projectBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectAccountRelBO> getGrantAuthorityProductionAccount(String s, String s1, Long aLong, ProjectAccountRelBO projectAccountRelBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectQueueRelBO> getGrantAuthorityQueue(String s, String s1, Long aLong, ProjectQueueRelBO projectQueueRelBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult checkAdmin(String s, String s1, Long aLong, String s2) {
        return new ApiResult();
    }

    @Override
    public ApiResult<ProjectMemberBO> getProjectRoleByIdAndErp(String s, String s1, Long aLong, ProjectBO projectBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectMemberBO> getProjectMember(String s, String s1, Long aLong, ProjectMemberBO projectMemberBO) {
        return new ApiResult<>();
    }

    @Override
    public ApiResult<ProjectTNodeRelBO> getGrantAuthorityNode(String s, String s1, Long aLong, ProjectTNodeRelBO projectTNodeRelBO) {
        return new ApiResult<>();
    }
}
