package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptPublishDao {

    int insert(DataDevScriptFilePublish publish);

    DataDevScriptFilePublish findLastNotFail(DataDevScriptFilePublish publish);

    void updateStatus(DataDevScriptFilePublish publish);

    void updateDataDevPublish(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath, @Param("dataDevScriptFilePublish") DataDevScriptFilePublish publish);

    DataDevScriptFilePublish findByRequestId(DataDevScriptFilePublish publish);


    Long count4page(DataDevScriptFilePublish publish);

    List<DataDevScriptFilePublish> list4page(DataDevScriptFilePublish publish);

    List<DataDevScriptFilePublish> getPublishList(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);


    /**
     * 得到最新的发布历史
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     */
    DataDevScriptFilePublish getLastPublish(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath ,@Param("applicationId") Long applicationId);

    /**
     * 得到最新某一状态的记录
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param applicationId
     * @return
     */
    DataDevScriptFilePublish findLastByStatus(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath ,@Param("applicationId") Long applicationId,@Param("status")Integer status);

    List<DataDevScriptFilePublish> getDistinctPublishList(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath);


    void deletePublish(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath, @Param("applicationId") Long applicationId);

    DataDevScriptFilePublish findByBuffaloScriptId(@Param("buffaloScriptId") Long buffaloScriptId);

    List<DataDevScriptFilePublish> getMaxVersion(@Param("gitProjectId") Long gitProjectId, @Param("gitProjectFilePath") String gitProjectFilePath, @Param("applicationId") Long applicationId);


}
