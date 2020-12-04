package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.datapreview.domain.DataDevDataPreview;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhanglei68.
 * @date 2019-03-01.
 * @time 17:03.
 */
public interface DataDevDataPreviewDao {

    DataDevDataPreview getByTbInfo(@Param("clusterCode")String clusterCode,
                                   @Param("linuxUser")String linuxUser,
                                   @Param("dbName")String dbName,
                                   @Param("tbName")String tbName);


    void insert(DataDevDataPreview dataDevDataPreview);

    void update(DataDevDataPreview dataDevDataPreview);


}
