package com.jd.bdp.datadev.api;

import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.PlumberTarget;
import com.jd.bdp.datadev.model.Script;
import com.jd.bdp.datadev.model.ScriptRunDetail;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public interface ScriptInterface {

    /**
     * 运行脚本
     *
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult runScript(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

    /**
     * 获取脚本运行详情
     *
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult getRunScriptDetail(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

    /**
     * 停止脚本
     *
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult stopScript(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

    /**
     * 获取脚本信息
     *
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param script
     * @return
     */
    ApiResult getScriptInfo(String appId, String userToken, Long time, String sign, Script script);


    /**
     * 将Sql 推到 Plumber
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @param plumberTarget
     * @return
     */
    ApiResult sqlToPlumber(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail, PlumberTarget plumberTarget);


    /**
     * 查询日志(脚本执行完成后调用)
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult runScriptLog(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

    /**
     * 查询结果(脚本执行完成后调用)
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult runScriptData(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

    /**
     * 查询日志(脚本执行完成后调用)
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult runScriptLogStringData(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

    /**
     * 查询结果(脚本执行完成后调用)
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult runScriptDataStringData(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);


    /**
     * 查询结果Title(SQL 执行,脚本执行完成后调用)
     * @param appId
     * @param userToken
     * @param time
     * @param sign
     * @param scriptRunDetail
     * @return
     */
    ApiResult runScriptTitle(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail);

}
