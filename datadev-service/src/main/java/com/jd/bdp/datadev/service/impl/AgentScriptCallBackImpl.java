package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.agent.api.AgentScriptCallBackInterface;
import com.jd.bdp.datadev.component.JdqClient;
import com.jd.bdp.datadev.dao.DataDevClientBaseDao;
import com.jd.bdp.datadev.dao.DataDevClientInfoDao;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.dao.DataDevScriptRunDetailDao;
import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.enums.DataDevScriptRunStatusEnum;
import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.ScriptJdqMessage;
import com.jd.bdp.datadev.model.ScriptRunDetail;
import com.jd.bdp.datadev.util.ScriptJdqMessageUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by zhangrui25 on 2018/3/9.
 */
public class AgentScriptCallBackImpl implements AgentScriptCallBackInterface {

    private static final Logger logger = Logger.getLogger(AgentScriptCallBackImpl.class);

    @Autowired
    DataDevClientBaseDao clientBaseDao;
    @Autowired
    DataDevClientInfoDao clientInfoDao;
    @Autowired
    DataDevScriptRunDetailDao runDetailDao;
    @Autowired
    DataDevScriptFileDao scriptFileDao;
    @Autowired
    JdqClient jdqClient;

    /**
     * 发送心跳接口
     *
     * @param dataDevClientBase
     * @return
     */
    @Override
    public ApiResult sendHeartbeat(DataDevClientBase dataDevClientBase) {
        try {
            dataDevClientBase.setLastActiveTime(new Date());
            dataDevClientBase.setCreator("bdp_sys");
            dataDevClientBase.setMender("bdp_sys");
            DataDevClientBase clientBase = clientBaseDao.findByIp(dataDevClientBase.getIp());
            logger.error("心跳"+com.alibaba.fastjson.JSONObject.toJSONString(dataDevClientBase));
            if (clientBase == null) {
                dataDevClientBase.setJobLimit(100);
                dataDevClientBase.setStatus(0);
                clientBaseDao.insertClientBase(dataDevClientBase);
                clientBase = dataDevClientBase;
            } else {
                clientBaseDao.updateRundetailAndLastTime(dataDevClientBase);
            }
            dataDevClientBase.setJobLimit(clientBase.getJobLimit());
            clientInfoDao.insertClientInfo(dataDevClientBase);
            if (clientBase != null && clientBase.getStatus() == 0) {
                return ApiResult.getSuccessResult("此ip心跳被禁用", null);
            }
            return ApiResult.getSuccessResult("添加心跳成功", null);
        } catch (Exception e) {
            return ApiResult.getFailResult("添加心跳失败,失败原因:" + e.getMessage(), null);
        }

    }

    /**
     * 任务执行回调
     *
     * @param dataDevScriptRunDetail
     * @return
     */
    @Override
    public ApiResult exeScriptRunDetailCallBack(DataDevScriptRunDetail dataDevScriptRunDetail) {
        try {
            dataDevScriptRunDetail.setEndTime(null);
            dataDevScriptRunDetail.setStartTime(null);
            runDetailDao.updateRuntailStatus(dataDevScriptRunDetail);
            //jdq send status
            logger.error("===========updateCallBackRunning======");
            ScriptRunDetail scriptRunDetail = new ScriptRunDetail();
            scriptRunDetail.setType(dataDevScriptRunDetail.getType());
            scriptRunDetail.setId(dataDevScriptRunDetail.getId());
            scriptRunDetail.setGitProjectFilePath(dataDevScriptRunDetail.getGitProjectFilePath());
            scriptRunDetail.setGitProjectId(dataDevScriptRunDetail.getGitProjectId());

            if(scriptRunDetail.getGitProjectId() == null || scriptRunDetail.getGitProjectId() == 0){
                DataDevScriptRunDetail detail =  runDetailDao.findById(dataDevScriptRunDetail.getId());
                scriptRunDetail.setGitProjectId(detail.getGitProjectId());
                scriptRunDetail.setGitProjectFilePath(detail.getGitProjectFilePath());
            }
            ScriptJdqMessage dataDevJdqMessage = ScriptJdqMessageUtil.createStatus2JdqMessage(scriptRunDetail, String.valueOf(DataDevScriptRunStatusEnum.Running.toCode()), 1);
            jdqClient.sendDataDevJdqMessage(dataDevJdqMessage);
            //jdq end

            return ApiResult.getSuccessResult("任务执行回调成功", null);
        } catch (Exception e) {
            logger.error(e);
            return ApiResult.getFailResult("任务执行回调失败", null);
        }
    }

    /**
     * 任务kill 回调
     *
     * @param dataDevScriptRunDetail
     * @return
     */
    @Override
    public ApiResult stopScriptRunDetailCallBack(DataDevScriptRunDetail dataDevScriptRunDetail) {
        try {
            logger.error("===========updateCallBackStop======" + com.alibaba.fastjson.JSONObject.toJSONString(dataDevScriptRunDetail));
            dataDevScriptRunDetail.setEndTime(null);
            dataDevScriptRunDetail.setEndTime(new Date());
            runDetailDao.updateRuntailStatus(dataDevScriptRunDetail);
            ScriptRunDetail scriptRunDetail = new ScriptRunDetail();
            scriptRunDetail.setType(dataDevScriptRunDetail.getType());
            scriptRunDetail.setId(dataDevScriptRunDetail.getId());
            scriptRunDetail.setGitProjectId(dataDevScriptRunDetail.getGitProjectId());
            scriptRunDetail.setGitProjectFilePath(dataDevScriptRunDetail.getGitProjectFilePath());

            if(scriptRunDetail.getGitProjectId() == null || scriptRunDetail.getGitProjectId() == 0){
                DataDevScriptRunDetail detail =  runDetailDao.findById(dataDevScriptRunDetail.getId());
                scriptRunDetail.setGitProjectId(detail.getGitProjectId());
                scriptRunDetail.setGitProjectFilePath(detail.getGitProjectFilePath());
            }
            ScriptJdqMessage dataDevJdqMessage = ScriptJdqMessageUtil.createStatus2JdqMessage(scriptRunDetail, String.valueOf(dataDevScriptRunDetail.getStatus()), 1);
            jdqClient.sendDataDevJdqMessage(dataDevJdqMessage);

            if(dataDevScriptRunDetail.getResponseCode()!=null){

                ScriptJdqMessage response = ScriptJdqMessageUtil.createResponseCode2JdqMessage(scriptRunDetail, String.valueOf(dataDevScriptRunDetail.getResponseCode()), 1);
                jdqClient.sendDataDevJdqMessage(response);

            }


            return ApiResult.getSuccessResult("任务kill 回调回调成功", null);
        } catch (Exception e) {
            logger.error(e);
            return ApiResult.getFailResult("任务kill 回调回调失败"+e.getMessage(), null);
        }
    }

//    /**
//     * 记录presto的queryID
//     */
//    @Override
//    public ApiResult recordPrestoQueryId(DataDevScriptRunDetail dataDevScriptRunDetail){
//        try {
//            runDetailDao.updatePrestoQueryId(dataDevScriptRunDetail.getId(),dataDevScriptRunDetail.getPrestoQueryId());
//            return ApiResult.getSuccessResult("记录presto的queryID,回调成功", null);
//        } catch (Exception e) {
//            logger.error(e);
//            return ApiResult.getFailResult("记录presto的queryID,回调失败", null);
//        }
//    }
//
//    @Override
//    public ApiResult selectScriptRunDetailCallBack(Long id) {
//        try {
//            DataDevScriptRunDetail scriptRunDetail = runDetailDao.findById(id);
//            return ApiResult.getSuccessResult("记录presto的queryID,回调成功", scriptRunDetail);
//        } catch (Exception e) {
//            logger.error(e);
//            return ApiResult.getFailResult("记录presto的queryID,回调失败", null);
//        }
//    }

}
