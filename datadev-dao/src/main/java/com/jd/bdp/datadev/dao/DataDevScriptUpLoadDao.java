package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptUpLoad;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptUpLoadDao {

    void insertScriptUpLoad(DataDevScriptUpLoad dataDevScriptUpLoad);

    Long findAllCount(Long gitProjectId);

    List<DataDevScriptUpLoad> findByPage(@Param("gitProjectId") Long gitProjectId,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime,
                                         @Param("creator") String creator,
                                         @Param("offSet") int offSet,
                                         @Param("pageSize") int pageSize);

}
