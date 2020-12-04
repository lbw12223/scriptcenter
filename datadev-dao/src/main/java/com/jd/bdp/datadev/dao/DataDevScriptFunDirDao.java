package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevFunDir;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptFunDirDao {

    List<DataDevFunDir> getDirsByDirId(@Param("pId") Long pId);


    DataDevFunDir getFunDirById(@Param("id") Long id) ;

    /**
     * @return
     */
    List<DataDevFunDir> getMinFunDirIds() ;
}
