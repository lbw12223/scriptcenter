package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevDependency;
import com.jd.bdp.datadev.domain.DataDevDependencyDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevDependencyDao {
    /**
     * 获取某次依赖所有具体依赖文件
     * @param dependencyId
     * @return
     */
    List<DataDevDependencyDetail> getDependencyDetailsByDependId(@Param("dependencyId") Long dependencyId);

    /**
     * 通过id或者依赖
     * @param id
     * @return
     */
    DataDevDependency getById(@Param("id") Long id);

    /**
     * 保存依赖
     * @param dataDevDependency
     */
    void saveDependency(DataDevDependency dataDevDependency);


    /**
     * 保存依赖细节
     * @param list
     */
    void saveDependencyDetails(List<DataDevDependencyDetail> list);

    /**
     * 获取某次依赖某个文件细节
     * @return
     */
    DataDevDependencyDetail getDetailByDependencyIdAndPath(@Param("dependencyId") Long dependencyId,@Param("gitProjectId")Long gitProjectId,@Param("gitProjectFilePath")String gitProjectFilePath);


}
