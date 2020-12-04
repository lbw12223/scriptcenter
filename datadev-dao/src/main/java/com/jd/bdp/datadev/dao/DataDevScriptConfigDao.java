package com.jd.bdp.datadev.dao;

import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDevScriptConfigDao {
    /**
     * 获取erp下的配置
     *
     * @param owner
     * @return
     */
    List<DataDevScriptConfig> getConfigsByErp(@Param("owner")String owner, @Param("projectSpaceId")Long projectSpaceId);

    /**
     *
     * @param id
     * @return
     */
    DataDevScriptConfig findById(Long id);

    /**
     * 增加一条配置
     * @param config
     */
    void insertConfig(DataDevScriptConfig config);

    /**
     * 更新配置信息
     * @param config
     */
    void updateConfig(DataDevScriptConfig config);

    /**
     * 删除配置 deleted字段置1
     * @param id
     */
    void deleteConfig(Long id);

    /**
     *
     * @return
     */
    List<DataDevScriptConfig> getConfigsByMarketIds(@Param("marketIds") List<Long> marketIds);

    /**
     * 更新merge配置
     * @param config
     */
    void updateMergeConfig(DataDevScriptConfig config);

}
