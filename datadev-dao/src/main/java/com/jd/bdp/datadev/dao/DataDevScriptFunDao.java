package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevFunDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptFunDao {

    /**
     *
     * @param funDirId
     * @return
     */
    List<DataDevFunDetail> getDetailByDirId(@Param("funDirId") Long funDirId);

    /**
     *
     * @param id
     * @return
     */
    DataDevFunDetail findById(@Param("id") Long id);


    /**
     * @param keyword
     * @return
     */
    List<DataDevFunDetail> getDetailByKeyword(@Param("keyword") String keyword);


    List<String> getDataDevFunNameList();

}
