package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevGitProjectSharedGroup;

/**
 * Created by zhangrui25 on 2018/5/23.
 */
public interface DataDevGitProjectSharedGroupService {


    void handGitProjectSharedGroup(DataDevGitProject dataDevGitProject) ;

    void insertGitSharedGroup(DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup);

    void deleteSharedGroupFromProject(DataDevGitProjectSharedGroup dataDevGitProjectSharedGroup);

}
