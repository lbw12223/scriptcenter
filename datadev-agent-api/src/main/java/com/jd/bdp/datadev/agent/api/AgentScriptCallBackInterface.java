package com.jd.bdp.datadev.agent.api;

import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.model.ApiResult;

/**
 * Created by zhangrui25 on 2018/3/6.
 */
public interface AgentScriptCallBackInterface {



    /**
     * 心跳接口
     * @param dataDevClientBase
     * @return
     */
    ApiResult sendHeartbeat(DataDevClientBase dataDevClientBase);


    /**
     * 执行callBack接口
     * @param dataDevScriptRunDetail
     * @return
     */
    ApiResult exeScriptRunDetailCallBack(DataDevScriptRunDetail dataDevScriptRunDetail);


    /**
     *
     * 停止接口
     * @param dataDevScriptRunDetail
     * @return
     */
    ApiResult stopScriptRunDetailCallBack(DataDevScriptRunDetail dataDevScriptRunDetail);

//    /**
//     * 记录引擎为presto的queryID
//     * @param dataDevScriptRunDetail
//     * @return
//     */
//    ApiResult recordPrestoQueryId(DataDevScriptRunDetail dataDevScriptRunDetail);
//
//    /**
//     * 根据id查询脚本运行详情
//     * @param id
//     * @return
//     */
//    ApiResult selectScriptRunDetailCallBack(Long id);

}
