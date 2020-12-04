package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevClientBase;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DataDevClientBaseDao {
    DataDevClientBase findByIp(String ip);

    void insertClientBase(DataDevClientBase dataDevClientBase);

    void updateRundetailAndLastTime(DataDevClientBase dataDevClientBase);

    List<DataDevClientBase> findByPreTime(Date preTime);

    List<DataDevClientBase> findClientBase();

    List<DataDevClientBase> findClientInfo(String ip);


    void modifyStatus(@Param("id") Long id , @Param("status") Integer status);

    Long count();
    List<DataDevClientBase> list(@Param("start") Integer start,@Param("limit")Integer limit);

}
