package com.jd.bdp.datadev.agent.api;

import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.model.ApiResult;

/**
 * Created by zhangrui25 on 2018/3/6.
 */
public interface AgentScriptInterface {


    /**
     * 执行
     *
     * @param dataDevScriptRunDetail
     * @return
     */
    ApiResult exeScriptRunDetail(DataDevScriptRunDetail dataDevScriptRunDetail);

    /**
     * 停止
     *
     * @param dataDevScriptRunDetail
     * @return
     */
    ApiResult stopScriptRunDetail(DataDevScriptRunDetail dataDevScriptRunDetail);

}
