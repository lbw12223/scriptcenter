package com.jd.bdp.datadev.service;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevScriptBuffaloJob;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataDevScriptPublishService {


    /**
     * 上线脚本
     *
     * @param file
     * @throws Exception
     */
     DataDevScriptFilePublish upLineScript(DataDevScriptFile file, String erp,DataDevScriptFilePublish oldPublish,Integer runType) throws Exception;


    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    List<DataDevScriptFilePublish> getPushList(Long gitProjectId, String gitProjectFilePath) throws Exception;

    /**
     * 得到最新的发布历史
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    DataDevScriptFilePublish getLastPublish(Long gitProjectId, String gitProjectFilePath , Long applicationId) throws Exception;

    /**
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    List<DataDevScriptFilePublish> getDistinctPushList(Long gitProjectId, String gitProjectFilePath) throws Exception;


    void insert(DataDevScriptFilePublish insertPublish);

    /**
     * 获取buffalo任务的情况
     */
/*
    List<DataDevScriptBuffaloJob> getBuffaloJobs(DataDevScriptFile devScriptFile, String userToken, boolean needVersion) throws Exception;
*/


    public PageResultDTO listBuffaloJobs(DataDevScriptFile file, DataDevScriptBuffaloJob queryJob, String userToken, int page, int rows) throws Exception;


    /**
     * 根据id或者requestId更新
     *
     * @param publish
     * @throws Exception
     */
    void updateStatus(DataDevScriptFilePublish publish) throws Exception;

    /**
     * 寻找状态不是失败的最新一条记录
     * 这里的失败指的是脚本上传失败,脚本驳回跟脚本撤回也是上传成功
     *
     * @param
     * @return
     * @throws Exception
     * @see com.jd.bdp.datadev.enums.DataDevScriptPublishStatusEnum
     */
    DataDevScriptFilePublish findLastNotFail(Long gitProjectId, String gitProjectFilePath, Long applicationId) throws Exception;
    /**
     * 寻找状态成功的最新一条记录
     *
     * @param
     * @return
     * @throws Exception
     * @see com.jd.bdp.datadev.enums.DataDevScriptPublishStatusEnum
     */
    DataDevScriptFilePublish findLastSuccess(Long gitProjectId, String gitProjectFilePath, Long applicationId) throws Exception;


    /**
     * jqgrid使用
     *
     * @param publish
     * @return
     * @throws Exception
     */
    PageResultDTO list4page(DataDevScriptFilePublish publish, Pageable pageable) throws Exception;

    /**
     * 删除上线记录
     *
     * @param appGroupId
     * @param gitProjectId
     * @param gitProjectFilePath
     * @throws Exception
     */
    void deletePublish(Long appGroupId, Long gitProjectId, String gitProjectFilePath) throws Exception;

    DataDevScriptFilePublish findByBuffaloScriptId(Long scriptId) throws Exception;

    DataDevScriptFilePublish getScriptInfoByName(Long jsdAppgroupId, String scriptName, String erp) throws Exception;
}
