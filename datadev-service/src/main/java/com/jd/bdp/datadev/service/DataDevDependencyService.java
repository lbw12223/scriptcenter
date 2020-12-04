package com.jd.bdp.datadev.service;

import com.alibaba.fastjson.JSONArray;
import com.jd.bdp.datadev.domain.DataDevDependency;
import com.jd.bdp.datadev.domain.DataDevDependencyDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.domain.ZtreeNode;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface DataDevDependencyService {

    /**
     * 将前端数据转换成DataDevDependencyDetail
     * @param jsonArray
     * @param gitProjectId
     * @return
     * @throws Exception
     */
    List<DataDevDependencyDetail> transferJsonArray(JSONArray jsonArray,Long gitProjectId)throws Exception;

    /**
     * 保存依赖 会先判断md5是否一样才决定是否真保存
     * @param list
     * @param gitProjectId
     * @param gitProjectFilePath
     * @param erp
     * @return true:更新  false没有更新
     * @throws Exception
     */
    HoldDoubleValue<Boolean,DataDevDependency> saveDetails(List<DataDevDependencyDetail> list, Long gitProjectId, String gitProjectFilePath,String erp)throws Exception;

    /**
     * 通过id获得依赖
     * @param id
     * @return
     * @throws Exception
     */
    DataDevDependency getById(Long id)throws Exception;

    /**
     * 获取文件的依赖列表
     * @param gitProjectId
     * @param gitProjectFilePath
     * @return
     * @throws Exception
     */
    List<DataDevDependencyDetail> getDetails(Long gitProjectId,String gitProjectFilePath,String version)throws Exception;
    List<DataDevDependencyDetail> getDetails(Long dependencyId)throws Exception;

    /**
     * 获取一组依赖的md5
     * @param list
     * @return
     * @throws Exception
     */
    String getMd5ByDependencyDetails(List<DataDevDependencyDetail> list)throws Exception;

    /**
     * 更新依赖为最新版本
     * @param needUpdate 需要更新的部分依赖
     * @throws Exception
     */
    List<DataDevDependencyDetail> updateNewVersion(List<DataDevDependencyDetail> needUpdate,DataDevDependency dataDevDependency)throws Exception;


    /**
     *
     * @param list
     * @param gitProjectId 项目id
     * @param gitProjectFilePath 文件路径
     * @return
     * @throws Exception
     */
    List<DataDevDependencyDetail> getNewVersionDetails(List<DataDevDependencyDetail> list,Long gitProjectId,String gitProjectFilePath)throws Exception;


    /**
     *
     * @param list 依赖列表
     * @param erp
     * @param replaceMap 需要替换的内容  key：脚本路径   value：需要替换的内容字节数组
     * @return
     * @throws Exception
     */
    byte[] packZip(List<DataDevDependencyDetail> list, String erp, Map<String,byte[]> replaceMap)throws Exception;


    void deleteDependency(Long gitProjectId,String gitProjectFilePath) throws Exception;


    void setDisabledStatus(List<ZtreeNode> list,String gitProjectFilePath)throws Exception;

    void dealDeletedFile(List<ZtreeNode> ztreeNodes,String[] selectFilePaths)throws Exception;


    /**
     * 将依赖转化成ztree 返回前端
     * @param list
     * @return
     * @throws Exception
     */

    List<ZtreeNode> getScriptsByDetails(List<DataDevDependencyDetail> list ) throws Exception;

}
