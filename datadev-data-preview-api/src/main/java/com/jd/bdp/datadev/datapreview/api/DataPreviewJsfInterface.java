package com.jd.bdp.datadev.datapreview.api;


import com.jd.bdp.datadev.datapreview.domain.DataPreViewApiResult;

/**
 * ide  表数据预览 暴露接口
 *
 * @author lifangli
 * @date 2019-08-01
 */
public interface DataPreviewJsfInterface {
    /**
     * 数据预览
     *
     * @param clusterCode
     * @param marketCode
     * @param dbName
     * @param tableName
     * @return
     */
    DataPreViewApiResult doDataPreview(String clusterCode, String marketCode, String dbName, String tableName , String erp);

    DataPreViewApiResult validData(Long runDetailId);

    DataPreViewApiResult getDataTitle(Long runDetailId);

    DataPreViewApiResult runLogData(Integer page, Integer rows, Long runDetailId);

}
