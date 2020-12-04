package com.jd.bdp.datadev.service;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface DataDevScriptRunDetailService {
    /**
     * 根据id查找运行记录
     *
     * @param id
     * @return
     * @throws Exception
     */
    DataDevScriptRunDetail findById(Long id) throws Exception;

    /**
     * 插入一条运行记录
     *
     * @param scriptRunDetail
     * @throws Exception
     */
    void insertRunDetail(DataDevScriptRunDetail scriptRunDetail) throws Exception;

    /**
     * 根据运行记录id客户端调用更新运行记录的运行状态
     *
     * @param scriptRunDetail
     * @throws Exception
     */
    void updateRuntailStatus(DataDevScriptRunDetail scriptRunDetail) throws Exception;

    /**
     * @param runDetail
     * @param pageable
     * @return
     * @throws Exception
     */
    PageResultDTO list4page(DataDevScriptRunDetail runDetail, Pageable pageable) throws Exception;

    /**
     * 根据git跟path查找最后运行记录
     *
     * @param
     * @param
     * @return
     */
    DataDevScriptRunDetail findLastRunByFileId(Long fileId, String operator);

    /**
     * 更新运行记录的Path
     *
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param newGitProjectFilePath
     */
    void updateDataDevScriptRunDetailPath(Long gitProjectId, String gitProjectFilePath, String newGitProjectFilePath);


    /**
     * 验证运行参数
     *
     * @param runDetail
     * @param erp
     * @throws Exception
     */
    void verifyMarket(DataDevScriptFile file, DataDevScriptRunDetail runDetail, String erp , Long spaceProjectId) throws Exception;

    /**
     * 加密ip
     *
     * @throws Exception
     */
    String encryptIp(String ip) throws Exception;

    /**
     * 解密ip
     *
     * @throws Exception
     */
    String decryptIp(String token) throws Exception;

    /**
     * 验证是不是ip
     *
     * @param ip
     * @return
     * @throws Exception
     */
    boolean verifyIp(String ip) throws Exception;

    Map<Long, String> getRunContentMap() throws Exception;


    void updateRunCode(Long marketId) throws Exception;


    HoldDoubleValue<Integer, Double> listFinishTask(Long limit) throws Exception;
}
