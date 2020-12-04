package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.api.ScriptInterface;
import com.jd.bdp.datadev.component.HbaseRunDetailData;
import com.jd.bdp.datadev.component.HbaseRunDetailLog;
import com.jd.bdp.datadev.component.UrmUtil;
import com.jd.bdp.datadev.domain.DataDevPlumberArgs;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.enums.ArgsImportTypeEnum;
import com.jd.bdp.datadev.enums.DataDevRunTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.model.ApiResult;
import com.jd.bdp.datadev.model.PlumberTarget;
import com.jd.bdp.datadev.model.Script;
import com.jd.bdp.datadev.model.ScriptRunDetail;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.datadev.service.DataDevScriptService;
import com.jd.jsf.gd.codec.msgpack.JSFMsgPack;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class ScriptServiceImpl implements ScriptInterface {

    private static final Logger logger = Logger.getLogger(ScriptServiceImpl.class);

    @Autowired
    private DataDevScriptService scriptService;
    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private DataDevScriptRunDetailService runDetailService;
    @Autowired
    private DataDevGitProjectService projectService;

    @Autowired
    private HbaseRunDetailLog hbaseRunDetailLog;
    @Autowired
    private UrmUtil urmUtil;

    @Autowired
    private HbaseRunDetailData hbaseRunDetailData;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    @Override
    public ApiResult runScript(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        try {
            logger.error("================APIrunScript"  + JSONObject.toJSONString(scriptRunDetail));

            DataDevScriptFile file = null;
            if (scriptRunDetail.getScriptFileId() != null) {
                file = fileService.findById(scriptRunDetail.getScriptFileId());
            } else {
                file = fileService.getScriptByGitProjectIdAndFilePath(scriptRunDetail.getGitProjectId(), scriptRunDetail.getGitProjectFilePath());
            }
            if (file == null) {
                throw new RuntimeException(scriptRunDetail.getGitProjectId() + ":" + scriptRunDetail.getGitProjectFilePath() + "的脚本不存在");
            }
            String erp = urmUtil.getErpByUserToken(userToken);
            projectService.verifyUserAuthority(erp, file.getGitProjectId());

            if (StringUtils.isBlank(scriptRunDetail.getOperator())) {
                scriptRunDetail.setOperator(erp);
            }
            scriptRunDetail.setCreator(erp);
            //将接口对象转成项目通用对象
            DataDevScriptRunDetail dataDevScriptRunDetail = transScript2DataDevRunDetail(scriptRunDetail, file);
            DataDevScriptRunDetail resultDetail = scriptService.runScript(file, dataDevScriptRunDetail,true);
            logger.error("=====================通过接口运行成功，运行id：" + resultDetail.getId());
     /*       DataDevScriptFile params = new DataDevScriptFile();
            params.setStartShellPath(scriptRunDetail.getStartShellPath());
            params.setArgs(scriptRunDetail.getArgs() != null && scriptRunDetail.getArgs().size() > 0 ? JSONObject.toJSONString(scriptRunDetail.getArgs()) :"");
            fileService.updateDataDevScriptFile(scriptRunDetail.getGitProjectId(),scriptRunDetail.getGitProjectFilePath(),params);*/
            return ApiResult.getSuccessResult("脚本运行成功", resultDetail.getId());
        } catch (Exception e) {
            logger.error("脚本运行失败,失败原因::" , e);
            return ApiResult.getFailResult("脚本运行失败,失败原因:" + e, null);
        }
    }

    @Override
    public ApiResult stopScript(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        try {
            if (scriptRunDetail.getId() == null) {
                throw new RuntimeException("运行记录id为空");
            }
            DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
            if (runDetail == null) {
                throw new RuntimeException("id为" + scriptRunDetail.getId() + "的运行记录不存在");
            }
            String erp = urmUtil.getErpByUserToken(userToken);
            if (StringUtils.isBlank(scriptRunDetail.getOperator())) {
                runDetail.setOperator(erp);
                runDetail.setStopErp(erp);
            }
            projectService.verifyUserAuthority(erp, runDetail.getGitProjectId());
            scriptService.stopScript(runDetail);
            return ApiResult.getSuccessResult("脚本停止成功");
        } catch (Exception e) {
            logger.error("脚本停止失败，脚本运行记录id：" + scriptRunDetail.getId() + "======" + e.getMessage());
            return ApiResult.getFailResult("脚本执行停止失败;" + e.getMessage());
        }
    }

    @Override
    public ApiResult getRunScriptDetail(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        try {
            DataDevScriptRunDetail result = scriptService.getRunScriptDetailById(scriptRunDetail.getId());
            ScriptRunDetail resultObj = transScript2DataDevRunDetail(result);
            return ApiResult.getSuccessResult("获取运行记录详情成功", resultObj);
        } catch (Exception e) {
            logger.error("获取运行详情失败,失败原因:" + e.getMessage());
            return ApiResult.getFailResult("获取运行详情失败,失败原因:" + e.getMessage());
        }
    }


    @Override
    public ApiResult getScriptInfo(String appId, String userToken, Long time, String sign, Script script) {
        try {
            if (script.getId() != null) {
                DataDevScriptFile scriptFile = fileService.findById(script.getId());
                if (scriptFile == null) {
                    throw new RuntimeException("id为" + script.getId() + "的脚本不存在");
                }
                script.setGitProjectFilePath(scriptFile.getGitProjectFilePath());
                script.setGitProjectId(scriptFile.getGitProjectId());
            }
            String erp = urmUtil.getErpByUserToken(userToken);
            projectService.verifyUserAuthority(erp, script.getGitProjectId());
            DataDevScriptFile tmpFile = new DataDevScriptFile();
            tmpFile.setGitProjectId(script.getGitProjectId());
            tmpFile.setGitProjectFilePath(script.getGitProjectFilePath());
            tmpFile.setVersion(script.getVersion());
            DataDevScriptFile scriptFile = scriptService.getScriptInfo(tmpFile);
            if (scriptFile == null) {
                throw new RuntimeException("项目id为" + script.getGitProjectId() + ",文件路径为" + script.getGitProjectFilePath() + (StringUtils.isNotBlank(script.getVersion()) ? ("，版本号为" + script.getVersion()) : "") + "的脚本不存在");
            }
            Script obj = transScriptFile2Script(scriptFile);
            return ApiResult.getSuccessResult("获取脚本详情成功", obj);
        } catch (Exception e) {
            logger.error("获取详情失败，失败原因:" , e);
            return ApiResult.getFailResult("获取详情失败，失败原因:" + e.getMessage());
        }
    }

    @Override
    public ApiResult sqlToPlumber(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail, PlumberTarget plumberTarget) {
        try {
            logger.error("=======================================推数参数" + JSONObject.toJSONString(scriptRunDetail));
            logger.error("=======================================推数参数" + JSONObject.toJSONString(plumberTarget));
            DataDevPlumberArgs args = transPlumber(plumberTarget);
            DataDevScriptFile file = null;
            if (scriptRunDetail.getScriptFileId() != null) {
                file = fileService.findById(scriptRunDetail.getScriptFileId());
                if (file == null) {
                    throw new RuntimeException("id为" + scriptRunDetail.getScriptFileId() + "的脚本不存在");
                }
                scriptRunDetail.setGitProjectId(file.getGitProjectId());
                scriptRunDetail.setGitProjectFilePath(file.getGitProjectFilePath());
            }
            file = fileService.getScriptByGitProjectIdAndFilePath(scriptRunDetail.getGitProjectId(), scriptRunDetail.getGitProjectFilePath(), scriptRunDetail.getVersion());
            if (file == null) {
                throw new RuntimeException("项目id为" + scriptRunDetail.getGitProjectId() + ",文件路径为" + scriptRunDetail.getGitProjectFilePath() + (StringUtils.isNotBlank(scriptRunDetail.getVersion()) ? ("，版本号为" + scriptRunDetail.getVersion()) : "") + "的脚本不存在");
            }
            String erp = urmUtil.getErpByUserToken(userToken);
            projectService.verifyUserAuthority(erp, file.getGitProjectId());
            if (StringUtils.isBlank(scriptRunDetail.getOperator())) {
                scriptRunDetail.setOperator(erp);
            }
            scriptRunDetail.setCreator(erp);
            DataDevScriptRunDetail runDetail = transScript2DataDevRunDetail(scriptRunDetail, file);
            Long runDetailId = scriptService.sqlToPlumber(runDetail, args);
            return ApiResult.getSuccessResult("plmber 任务成功发到客户端", runDetailId);
        } catch (Exception e) {
            logger.error("plumber error:" + e.getMessage(),e);
            return ApiResult.getFailResult(e.getMessage());
        }
    }


    private DataDevScriptRunDetail transScript2DataDevRunDetail(ScriptRunDetail src, DataDevScriptFile file) {
        //这里不要乱传version影响里面判断
        DataDevScriptRunDetail dst = new DataDevScriptRunDetail();
        if (src != null) {
            dst.setClusterCode(src.getClusterCode());
            dst.setMarketLinuxUser(src.getMarketLinuxUser());
            dst.setQueueCode(src.getQueueCode());
            dst.setAccountCode(src.getAccountCode());
            if (src.getArgs() == null) {
                src.setArgs(new HashMap<String, String>());
            }
            if (src.getArgs() != null) {
                dst.setArgs(JSONObject.toJSONString(src.getArgs()));
            }
            dst.setArgsImportType(src.getArgsImportType() != null ? src.getArgsImportType() : ArgsImportTypeEnum.STANDARD.toCode());
            //不允许传环境变量
//            if(src.getEnvs()==null){
//                src.setEnvs(new HashMap<String, String>());
//            }
//            if(src.getEnvs()!=null){
//                dst.setEnvs(JSONObject.toJSONString(src.getEnvs()));
//            }
            dst.setGitProjectId(file.getGitProjectId());
            dst.setGitProjectFilePath(file.getGitProjectFilePath());
            dst.setType(file.getType());
            dst.setScriptFileId(file.getId());
            dst.setVersion(src.getVersion());
            dst.setStartShellPath(src.getStartShellPath());
            dst.setDbName(src.getDbName());
            dst.setOperator(src.getOperator());
            dst.setCreator(src.getCreator());
            dst.setRunType(src.getRunType());
            dst.setEngineType(src.getEngineType());
            if(StringUtils.isNotBlank(src.getSqlContent())){
                dst.setContent(src.getSqlContent());
                dst.setRunByContent(true);
            }

        }
        return dst;
    }

    public ScriptRunDetail transScript2DataDevRunDetail(DataDevScriptRunDetail src) {
        ScriptRunDetail dst = new ScriptRunDetail();
        if (src != null) {
            dst.setId(src.getId());
            dst.setScriptFileId(src.getScriptFileId());
            dst.setVersion(src.getVersion());
            dst.setStartTime(src.getStartTime());
            dst.setEndTime(src.getEndTime());
            dst.setStatus(src.getStatus());
            dst.setClusterCode(src.getClusterCode());
            dst.setMarketLinuxUser(src.getMarketLinuxUser());
            dst.setQueueCode(src.getQueueCode());
            dst.setAccountCode(src.getAccountCode());
            if (StringUtils.isNotBlank(src.getArgs())) {
                //去掉参数里面的
                try{
                    JSONObject obj = JSONObject.parseObject(src.getArgs().replaceAll("\\\\",""));
                    Map<String, String> map = JSONObject.toJavaObject(obj, Map.class);
                    dst.setArgs(map);
                }catch (Exception e){
                }

            }
        }
        return dst;
    }

    public Script transScriptFile2Script(DataDevScriptFile src) {
        Script dst = new Script();
        if (src != null) {
            dst.setGitProjectId(src.getGitProjectId());
            dst.setGitProjectFilePath(src.getGitProjectFilePath());
            dst.setName(src.getName());
            dst.setType(src.getType());
            dst.setSize(src.getSize());
            dst.setVersion(src.getVersion());
            dst.setOwner(src.getOwner() != null ? src.getOwner() : "");
            dst.setDescription(src.getDescription() != null ? src.getDescription() : "");
            dst.setId(src.getId());
            dst.setFileMd5(src.getFileMd5() != null ? src.getFileMd5() : "");
        }
        return dst;
    }

    public DataDevPlumberArgs transPlumber(PlumberTarget plumberTarget) {
        DataDevPlumberArgs args = new DataDevPlumberArgs();
        if (plumberTarget != null) {
            args.setTargetType(plumberTarget.getTargetType());
            args.setTargetHost(plumberTarget.getTargetHost());
            args.setTargetPort(plumberTarget.getTargetPort());
            args.setTargetDatabase(plumberTarget.getTargetDatabase());
            args.setTargetTableName(plumberTarget.getTargetTableName());
            args.setTargetUser(plumberTarget.getTargetUser());
            args.setTargetPassword(plumberTarget.getTargetPassword());
            args.setTargetCharset(plumberTarget.getTargetCharset());
            args.setTargetExtend(plumberTarget.getTargetExtend());
            args.setTargetExtend1(plumberTarget.getTargetExtend1());
            args.setDataDate(simpleDateFormat.format(plumberTarget.getDataDate()));
            args.setDeleteSql(plumberTarget.getDeleteSql());
            args.setTemplateExtend(plumberTarget.getTemplateExtend());
        }
        return args;
    }

    @Override
    public ApiResult runScriptData(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        logger.error("=====================runScriptData" + scriptRunDetail.getId() + " " + scriptRunDetail.getPage() + " " + scriptRunDetail.getRows());
        PageResultDTO resultDTO = new PageResultDTO();
        try {
            DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
            Integer page = scriptRunDetail.getPage();
            Integer rows = scriptRunDetail.getRows();
            resultDTO = hbaseRunDetailData.getRunDetailData(runDetail, page, rows);
            return ApiResult.getSuccessResult("查询成功", AjaxUtil.gridJson(resultDTO));
        } catch (Exception e) {
            resultDTO.setSuccess(false);
            resultDTO.setMessage(e.getMessage());
            return ApiResult.getFailResult("查询失败" + e.getMessage());
        }

    }

    @Override
    public ApiResult runScriptLog(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        logger.error("=====================runScriptLog" + scriptRunDetail.getId() + " " + scriptRunDetail.getPage() + " " + scriptRunDetail.getRows());

        PageResultDTO resultDTO = new PageResultDTO();
        try {
            DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
            if(runDetail == null){
                throw new RuntimeException("运行记录不存在"+scriptRunDetail.getId());
            }
            if (DataDevRunTypeEnum.DependencyRun.tocode().equals(runDetail.getRunType())) {
                runDetail.setType(DataDevScriptTypeEnum.Zip.toCode());
            }
            Integer page = scriptRunDetail.getPage();
            Integer rows = scriptRunDetail.getRows();
            resultDTO = hbaseRunDetailLog.getRunDetailLog(runDetail, page, rows);
            net.sf.json.JSONObject jsonObject = AjaxUtil.gridJson(resultDTO);
//            JSONObject jsonObject1 = JSONObject.parseObject(jsonObject.toString());
            ApiResult apiResult=ApiResult.getSuccessResult("查询成功",jsonObject);
            return apiResult;
        } catch (Exception e) {
            logger.error("查询日志失败：" + e.getMessage(),e);
            resultDTO.setSuccess(false);
            resultDTO.setMessage(e.getMessage());

            return ApiResult.getSuccessResult("查询失败", AjaxUtil.gridJson(resultDTO));
        }
    }

    @Override
    public ApiResult runScriptLogStringData(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        logger.error("=====================runScriptLogStringData" + scriptRunDetail.getId() + " " + scriptRunDetail.getPage() + " " + scriptRunDetail.getRows());

        PageResultDTO resultDTO = new PageResultDTO();
        try {
            DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
            if (DataDevRunTypeEnum.DependencyRun.tocode().equals(runDetail.getRunType())) {
                runDetail.setType(DataDevScriptTypeEnum.Zip.toCode());
            }
            Integer page = scriptRunDetail.getPage();
            Integer rows = scriptRunDetail.getRows();
            resultDTO = hbaseRunDetailLog.getRunDetailLog(runDetail, page, rows);
            ApiResult apiResult = ApiResult.getSuccessResult("查询成功", AjaxUtil.gridJson(resultDTO).toString());
            logger.error("=====================runScriptLogStringData" + JSONObject.toJSONString(apiResult));
            return apiResult ;
        } catch (Exception e) {
            logger.error("查询日志失败：" + e.getMessage(),e);
            resultDTO.setSuccess(false);
            resultDTO.setMessage(e.getMessage());
            return ApiResult.getSuccessResult("查询失败", AjaxUtil.gridJson(resultDTO).toString());
        }
    }

    @Override
    public ApiResult runScriptDataStringData(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        logger.error("=====================runScriptDataStringData" + scriptRunDetail.getId() + " " + scriptRunDetail.getPage() + " " + scriptRunDetail.getRows());
        PageResultDTO resultDTO = new PageResultDTO();
        try {
            DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
            Integer page = scriptRunDetail.getPage();
            Integer rows = scriptRunDetail.getRows();
            resultDTO = hbaseRunDetailData.getRunDetailData(runDetail, page, rows);
            ApiResult apiResult = ApiResult.getSuccessResult("查询成功", AjaxUtil.gridJson(resultDTO).toString());
            logger.error("=====================runScriptDataStringData" + JSONObject.toJSONString(apiResult));
            return apiResult ;
        } catch (Exception e) {
            resultDTO.setSuccess(false);
            resultDTO.setMessage(e.getMessage());
            return ApiResult.getFailResult("查询失败" + e.getMessage());
        }

    }
    @Override
    public ApiResult runScriptTitle(String appId, String userToken, Long time, String sign, ScriptRunDetail scriptRunDetail) {
        logger.error("=====================runScriptTitle" + scriptRunDetail.getId());
        PageResultDTO resultDTO = new PageResultDTO();
        try {
            DataDevScriptRunDetail runDetail = runDetailService.findById(scriptRunDetail.getId());
            resultDTO = hbaseRunDetailData.getRunDetailTitle(runDetail);
            return ApiResult.getSuccessResult("查询成功", resultDTO);
        } catch (Exception e) {
            logger.error("runScriptTitle",e);
            resultDTO.setSuccess(false);
            resultDTO.setMessage(e.getMessage());
            return ApiResult.getFailResult("查询失败" + e.getMessage());
        }

    }

    public static void main(String[] args) {
        try {
            JSFMsgPack jsfMsgPack = new JSFMsgPack();


            //序列化： 请根据实际情况序列化对象，这里是Date
            PageResultDTO resultDTO = new PageResultDTO();
            resultDTO.setPage(1);
            resultDTO.setLimit(10);
            List<String> result = new ArrayList<String>();
            for(int i=0;i<10;i++){
                result.add("fdsfa"+i);
            }
            resultDTO.setRows(result);
            resultDTO.setRecords(20L);
            resultDTO.setSuccess(true);
            net.sf.json.JSONObject jsonObject = AjaxUtil.gridJson(resultDTO);
            ApiResult successResult = ApiResult.getSuccessResult("查询成功", jsonObject);
            byte[] bs = jsfMsgPack.write(successResult);

            //反序列化：请根据实际情况反序列化，这里是Date

            ApiResult apiResult = (ApiResult) jsfMsgPack.read(bs, ApiResult.class);

            System.out.println(apiResult);
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
