package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptFileHis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptFileHisDao {

    /**
     * @param dataDevScriptFileHis
     */
    void insertHis(DataDevScriptFileHis dataDevScriptFileHis);

    /**
     * @param version
     * @return
     */
    DataDevScriptFileHis findByVersion(@Param("fileId") Long fileId, @Param("version") String version);

    /**
     * @param version
     * @return
     */
    DataDevScriptFileHis findByPathAndVersion(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath, @Param("version") String version);

    /**
     * @param fileHis
     * @return
     */
    Long count4page(DataDevScriptFileHis fileHis);

    /**
     * @param fileHis
     * @return
     */
    List<DataDevScriptFileHis> list4page(DataDevScriptFileHis fileHis);

    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param gitNewProjectFilePath
     */
    void updateDataDevScriptFileHisPath(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath, @Param("gitNewProjectFilePath") String gitNewProjectFilePath);


    void updateScriptFileHis(@Param("fileId") Long fileId,
                             @Param("gitProjectFilePath")  String gitProjectFilePath ,
                             @Param("fileName") String fileName) throws Exception ;


}
