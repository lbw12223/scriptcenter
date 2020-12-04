package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevPlumberArgs;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.model.ApiResult;

/**
 * web 项目调用的运行 停止方法
 */
public interface DataDevScriptService {

    /**
     * 返回运行记录id
     * 如果version不为空,运行指定版本脚本;如果version为空,运行最新版本脚本
     * @param scriptRunDetail
     * @return
     * @throws Exception
     */
    DataDevScriptRunDetail runScript(DataDevScriptFile file, DataDevScriptRunDetail scriptRunDetail,boolean mergeOriMarket) throws Exception;

    void stopScript(DataDevScriptRunDetail scriptRunDetail)throws Exception;

    DataDevScriptRunDetail getRunScriptDetailById(Long  scriptRunDetailId)throws Exception;

    DataDevScriptFile getScriptInfo(DataDevScriptFile scriptFile)throws Exception;

    Long sqlToPlumber( DataDevScriptRunDetail scriptRunDetail, DataDevPlumberArgs plumberArgs)throws Exception;


}
