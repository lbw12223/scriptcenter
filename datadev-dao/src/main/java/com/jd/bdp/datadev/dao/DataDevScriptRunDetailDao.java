package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptRunDetailDao {
    /**
     * @param id
     * @return
     */
    DataDevScriptRunDetail findById(Long id);

    /**
     * @param dataDevScriptRunDetail
     */
    void insertRunDetail(DataDevScriptRunDetail dataDevScriptRunDetail);

    /**
     * @param dataDevScriptRunDetail
     */
    void updateRuntailStatus(DataDevScriptRunDetail dataDevScriptRunDetail);

    /**
     * @param detail
     * @return
     */
    List<DataDevScriptRunDetail> list4page(DataDevScriptRunDetail detail);

    /**
     * @param detail
     * @return
     */
    Long count4page(DataDevScriptRunDetail detail);

    /**
     * 查找某个脚本的最后一次运行记录
     * @param scriptFileId
     * @return
     */
    DataDevScriptRunDetail findLastRunDetail(@Param("scriptFileId") Long scriptFileId,@Param("operator") String operator);




    /**
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param gitNewProjectFilePath
     */
    void updateRunDetailPath(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath , @Param("gitNewProjectFilePath") String gitNewProjectFilePath);



    void updateRunCode(@Param("runClusterCode")String runClusterCode,@Param("runLinuxUser") String runLinuxUser,
                       @Param("clusterCode")String clusterCode,@Param("linuxUser")String linuxUser);

//    /**
//     *
//     * @param id
//     * @param prestoQueryId
//     */
//    void updatePrestoQueryId(@Param("id") Long id, @Param("prestoQueryId")String prestoQueryId);


    List<DataDevScriptRunDetail> listFinishTask(@Param("limit") Long limit);

    /**
     * runListPage
     * @param
     * @return
     */
    List<DataDevScriptRunDetail> runListPage(DataDevScriptRunDetail detail);

    /**
     * runListCount
     * @param
     * @return
     */
    Long runListCount(DataDevScriptRunDetail detail);
}
