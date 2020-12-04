package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.datapreview.domain.DataDevDataPreview;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;

/**
 * @author zhanglei68.
 * @date 2019-02-28.
 * @time 21:00.
 * 数据预览
 */
public interface DataDevDataPreviewService {

    /**
     * 运行脚本 返回预览记录
     * @param clusterCode
     * @param marketCode
     * @param dbName
     * @param tableName
     * @return
     * @throws Exception
     */
    DataDevDataPreview doDataPreview(String clusterCode, String marketCode, String dbName, String tableName , String erp)throws Exception;

    /**
     * 检查是否运行完
     * @param runDetailId
     * @return
     * @throws Exception
     */
    HoldDoubleValue<Boolean,Integer> validData(Long runDetailId)throws Exception;

}
