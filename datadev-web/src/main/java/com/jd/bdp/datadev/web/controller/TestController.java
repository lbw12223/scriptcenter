package com.jd.bdp.datadev.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.api.ScriptInterface;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.domain.DataDevGitProject;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.enums.DataDevGitOrCodingEnum;
import com.jd.bdp.datadev.jdgit.JDGitProjects;
import com.jd.bdp.datadev.service.*;
import com.jd.bdp.datadev.web.worker.RunningSqlOperationLogCheckWorker;
import com.jd.bdp.urm.sso.UrmUserHolder;

import com.jd.jim.cli.Cluster;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangrui25 on 2018/2/8.
 */
@Controller
@RequestMapping("/scriptcenter/test/")
public class TestController {
    private static final Logger logger = Logger.getLogger(TestController.class);


    @Autowired
    private DataDevScriptRunDetailService dataDevScriptRunDetailService;


    @Autowired
    DataDevScriptFileService scriptFileService;
    @Autowired
    HbaseScript hbaseScript;
    @Autowired
    DataDevScriptUploadService uploadService;

    @Autowired
    DataDevScriptService scriptService;

    @Autowired
    AppGroupUtil appGroupUtil;

    @Autowired
    ScriptInterface scriptInterface;


    @Autowired
    private DataDevGitProjectService dataDevGitProjectService;

    @Autowired
    private LockUtil lockUtil;

    @Autowired
    private Cluster jimClient;


    @Autowired
    private RunningSqlOperationLogCheckWorker runningSqlOperationLogCheckWorker;

    @Autowired
    private ProjectSpaceRightComponent projectSpaceRightComponent;


    @RequestMapping("runningJob.ajax")
    @ResponseBody
    public String runningJob(UrmUserHolder userHolder, String key) throws Exception {
        return "success" + Arrays.toString(runningSqlOperationLogCheckWorker.getRunningJob().toArray());
    }



    @RequestMapping("getTopMeun.ajax")
    @ResponseBody
    public String getTopMeun(UrmUserHolder userHolder, HttpServletRequest httpServletRequest) throws Exception {

         //httpServletRequest.getAttribute("productList");
        return JSONObject.toJSONString(userHolder);
    }
    @RequestMapping("getTopMeun.html")
    @ResponseBody
    public String getTopMeunHtml(UrmUserHolder userHolder, HttpServletRequest httpServletRequest) throws Exception {
        return JSONObject.toJSONString(userHolder);// JSONObject.toJSONString(userHolder);
    }
    @RequestMapping("getUserProjectSpace.ajax")
    @ResponseBody
    public JSONObject getUserProjectSpaceAjax(UrmUserHolder userHolder) throws Exception {
       return JSONObjectUtil.getSuccessResult(projectSpaceRightComponent.getProjectSpaces(userHolder.getErp())) ;
    }

    @RequestMapping("redisSet.ajax")
    @ResponseBody
    public String redisAdd(UrmUserHolder userHolder, String key) throws Exception {
        jimClient.set(key.getBytes(), key.getBytes());
        return "success";
    }

    @RequestMapping("redisDel.ajax")
    @ResponseBody
    public String redisDel(UrmUserHolder userHolder, String key) throws Exception {
        jimClient.del(key);
        return "success";
    }

    @RequestMapping("redisGet.ajax")
    @ResponseBody
    public String redisGet(UrmUserHolder userHolder, String key) throws Exception {
        return jimClient.get(key);
    }

    @RequestMapping("redisSetEx.ajax")
    @ResponseBody
    public String redisSetEx(UrmUserHolder userHolder, String key) throws Exception {
        boolean value = jimClient.set(key, key + 1, 30, TimeUnit.SECONDS, false);
        return "success" + value;
    }

    @RequestMapping("tryLock.ajax")
    @ResponseBody
    public String tryLock(UrmUserHolder userHolder, String key) throws Exception {
        lockUtil.tryLock(key, "12", 2);
        return "success";
    }


    @RequestMapping("index.html")
    public String indexHtml(UrmUserHolder userHolder, Model model) throws Exception {
        return "/scriptcenter/test/upload";
    }




    /*saveFile.ajax*/
    @RequestMapping("saveFile.ajax")
    @ResponseBody
    public JSONObject saveFileTest(UrmUserHolder userHolder, DataDevScriptFile file, @RequestParam("file") MultipartFile multipartFile) throws Exception {
        JSONObject result = new JSONObject();
        result.put("message", "ok");

        if (new Random().nextBoolean()) {
            return JSONObjectUtil.getSuccessResult("保存成功", result);
        }
        return JSONObjectUtil.getFailResult(1, "测试失败!", result);

    }

    /*saveFile.ajax*/
    @RequestMapping("combine/index.html")
    public String combine(UrmUserHolder userHolder) throws Exception {
        return "/combine/index";
    }
    @RequestMapping("combine/iview.html")
    public String iview(UrmUserHolder userHolder) throws Exception {
        return "/combine/iview";
    }

    /**
     * 统计以及完成的内存数据
     *
     * @param limit
     * @return
     * @throws Exception
     */
    @RequestMapping("caculateMemoryUse.ajax")
    @ResponseBody
    public JSONObject caculateMemoryUse(Long limit) throws Exception {


        HoldDoubleValue<Integer, Double> result = dataDevScriptRunDetailService.listFinishTask(limit);
        return JSONObjectUtil.getFailResult(1, "测试失败!", result);
    }





//    @RequestMapping("index.html")
//    public String indexHtml(UrmUserHolder userHolder, Model model) throws Exception {
//        //model.addAttribute("erp",userHolder.getErp());
////        if (1 > 0) {
////            throw new RuntimeException("cess");
////        }
//        return "/scriptcenter/test/index";
//    }
//
//
//    @RequestMapping("testAddHbase.ajax")
//    @ResponseBody
//    public String testAddHbase(UrmUserHolder userHolder, Model model, Long dirId) throws Exception {
//        Long start = System.currentTimeMillis();
//        DataDevScriptFile scriptFile = new DataDevScriptFile();
//        scriptFile.setId(3L);
//        //scriptFile.setContent("select * from user_task");
//        scriptFile.setContentFile(new File("d:/sku_new_zhutu.rar"));
//        hbaseScript.upScriptToHbase(scriptFile, false);
//        Long end = System.currentTimeMillis();
//        return "success" + (start - end) % 1000;
//    }
//
//
////    @RequestMapping("testExecute.ajax")
////    @ResponseBody
////    public String testScriptExecute(UrmUserHolder userHolder, Model model, Long dirId) throws Exception {
////        DataDevScriptRunDetail scriptRunDetail = new DataDevScriptRunDetail();
////        scriptRunDetail.setId(33L);
////        scriptRunDetail.setScriptFileId(26L);
////        scriptRunDetail.setType(DataDevScriptRunDetailTypeEnum.Plumber.toCode());
////        AgentScriptInterface agentScriptInterface = dynamicJsfService.getScriptInterfaceJsfClient(dataDevClientBase.getIp());
////
////        ApiResult apiResult = agentScriptInterface.exeScriptRunDetail(scriptRunDetail);
////        return "success" + apiResult.getMessage();
////    }
//
////    @RequestMapping("testKill.ajax")
////    @ResponseBody
////    public String testKill(UrmUserHolder userHolder, Model model, Long dirId) throws Exception {
////        DataDevScriptRunDetail scriptRunDetail = new DataDevScriptRunDetail();
////        scriptRunDetail.setId(33L);
////        scriptRunDetail.setScriptFileId(26L);
////        scriptRunDetail.setType(DataDevScriptTypeEnum.SQL.toCode());
////        scriptRunDetail.setProcessId("1111111");
////        ApiResult apiResult = agentScriptInterface.stopScriptRunDetail(scriptRunDetail);
////        return "success " + apiResult.getCode() + apiResult.getMessage();
////    }
//
//    @RequestMapping("upload.ajax")
//    public JSONObject upload(MultipartFile file, Script script) throws Exception {
//        System.out.println(1);
//        String suffix = script.getType() != null ? DataDevScriptTypeEnum.enumValueOf(script.getType()).toSuffix() : "sql";
//        File f = File.createTempFile("tmp", suffix);
//        file.transferTo(f);
//        f.deleteOnExit();
//        script.setApplicationId(1L);
//        ApiResult apiResult = ScriptUtil.uploadScript("1", "2", new Date().getTime(), "ss", f, script);
//        JSONObject result = new JSONObject();
//        return JSONObject.fromObject(apiResult, NotNullJsonConfig.getInstance());
//    }
//
//    @RequestMapping("list.ajax")
//    @ResponseBody
//    public JSONObject listAjax(String dirId,
//                               @RequestParam(value = "fileName", defaultValue = "") String fileName,
//                               @RequestParam(value = "page", defaultValue = "1") int page,
//                               @RequestParam(value = "rows", defaultValue = "10") int rows) {
//
//        PageResultDTO pageResultDTO = new PageResultDTO();
//        Pageable pageable = new PageRequest(page - 1, rows);
//        try {
//            pageResultDTO = get(pageable);
//        } catch (Exception e) {
//
//            pageResultDTO.setSuccess(false);
//            pageResultDTO.setRecords(0L);
//            pageResultDTO.setMessage("获取DataDevScriptFile出错，请联系管理员！" + e.getMessage());
//            return AjaxUtil.gridJson(pageResultDTO);
//        }
//        pageResultDTO.setPage(page);
//        pageResultDTO.setLimit(rows);
//        pageResultDTO.setMessage("获取用户组成功");
//        return AjaxUtil.gridJson(pageResultDTO);
//    }
//
//    private PageResultDTO get(Pageable pageable) {
//        PageResultDTO pageResultDTO = new PageResultDTO();
//        Long count = 100l;
//        List<DataDevScriptRunDetail> dataDevScriptRunDetailList = new ArrayList<DataDevScriptRunDetail>();
//        if (count > 0) {
//
//            for (int index = 0; index < pageable.getPageSize(); index++) {
//                DataDevScriptRunDetail temp = new DataDevScriptRunDetail();
//                temp.setArgs("args" + index);
//                temp.setAccountCode("accountCode" + index);
//                temp.setClusterCode("clusterCode" + index);
//                temp.setContent("content" + index);
//                dataDevScriptRunDetailList.add(temp);
//            }
//        }
//        pageResultDTO.setRows(dataDevScriptRunDetailList);
//        pageResultDTO.setSuccess(true);
//        pageResultDTO.setRecords(count);
//        return pageResultDTO;
//    }
//    @RequestMapping("test")
//    @ResponseBody
//    public com.alibaba.fastjson.JSONObject test(String appId , String content, Integer type, DataDevScriptRunDetail tmp) throws Exception {
//        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
//        Long fileScriptId = 0L;
//        try {
//            DataDevScriptFile dataDevScriptFile = new DataDevScriptFile();
//            dataDevScriptFile.setName("test");
//            dataDevScriptFile.setType(type);
//            dataDevScriptFile.setVersion("1000");
//            if (content != null && content.getBytes() != null) {
//                dataDevScriptFile.setBytes(content.getBytes());
//                dataDevScriptFile.setSize(Long.valueOf(content.getBytes().length));
//            } else {
//                throw new ScriptException("上传文件为空");
//            }
//            DataDevScriptFileHis dataDevScriptFileHis = new DataDevScriptFileHis();
//            dataDevScriptFileHis.setHbasePreKey(hbaseScript.getHisPreKey(111L,"1000"));
//            dataDevScriptFile.setHbasePreKey(hbaseScript.getNewPreKey(1111L));
//            dataDevScriptFile.setVersion("1000");
//            if (tmp.getScriptFileId() != null) {
//                fileScriptId = scriptFileService.updateScript(dataDevScriptFile,null);
//            } else {
//                fileScriptId = scriptFileService.insertScriptWithOutLock(dataDevScriptFile);
//            }
//            fileScriptId=dataDevScriptFile.getId();
//            scriptFileService.transFile2HisFile(dataDevScriptFile, dataDevScriptFileHis);
//            scriptFileService.addScriptHis(dataDevScriptFileHis);
//            logger.error("fileScriptId===========================" + fileScriptId);
//            hbaseScript.upScriptToNewHbase(dataDevScriptFile);
//            hbaseScript.upScriptToHisHbase(dataDevScriptFile);
//            uploadService.updateUploadStatus(fileScriptId, DataDevScriptUploadStatusEnum.Success);
//            DataDevScriptRunDetail scriptRunDetail = new DataDevScriptRunDetail();
//            scriptRunDetail.setScriptFileId(fileScriptId);
//            scriptRunDetail.setClusterCode(tmp.getClusterCode());
//            scriptRunDetail.setAccountCode(tmp.getAccountCode());
//            scriptRunDetail.setQueueCode(tmp.getQueueCode());
//            scriptRunDetail.setMarketLinuxUser(tmp.getMarketLinuxUser());
//            scriptRunDetail.setOperator("zhanglei847");
//            scriptRunDetail.setType(type);
//            Map<String,String> args = new HashMap<String,String>();
//            args.put("1","zang              rui");
//            args.put("2","liis");
//            scriptRunDetail.setArgs(com.alibaba.fastjson.JSONObject.toJSONString(args));
//            if(type.equals(DataDevScriptRunDetailTypeEnum.Plumber.toCode())){
//                DataDevPlumberArgs dataDevPlumberArgs = new DataDevPlumberArgs();
//                dataDevPlumberArgs.setTargetCharset("utf-8");
//                dataDevPlumberArgs.setSourceDatabase("fdm.db");
//                dataDevPlumberArgs.setSourceExtend("hdfs://ns7/user/mart_lim/fdm.db");
//                dataDevPlumberArgs.setSourceSubprotocol("hive");
//                dataDevPlumberArgs.setTargetDatabase("bdp_data_dev");
//                dataDevPlumberArgs.setTargetHost("172.22.213.52");
//                dataDevPlumberArgs.setTargetUser("root");
//                dataDevPlumberArgs.setTargetPassword("dd0001");
//                dataDevPlumberArgs.setTargetType("mysql");
//                dataDevPlumberArgs.setTargetPort("3306");
//                dataDevPlumberArgs.setTargetTableName("plumber_test");
//                dataDevPlumberArgs.setTargetExtend("");
//                com.alibaba.fastjson.JSONObject extend1 = new com.alibaba.fastjson.JSONObject();
//                extend1.put("insert", "insert *");
//                extend1.put("cnum", "1");
//                dataDevPlumberArgs.setTargetExtend1(extend1.toJSONString());
//                scriptRunDetail.setDataDevPlumberArgs(dataDevPlumberArgs);
//            }
//            Long resultId = scriptService.runScript(scriptRunDetail);
//            result.put("message", "上传成功");
//            result.put("success", true);
//            result.put("code", 0);
//            result.put("obj", resultId);
//            return result;
//        } catch (HbaseUploadException e) {
//            try {
//                uploadService.updateUploadStatus(fileScriptId, DataDevScriptUploadStatusEnum.Failure);
//            } catch (Exception ex) {
//                logger.error(ex);
//            } finally {
//                result.put("message", e.getMessage());
//                result.put("success", false);
//                result.put("code", 1);
//                return result;
//            }
//        } catch (Exception e) {
//            result.put("message", e.getMessage());
//            logger.error(e);
//            result.put("success", false);
//            result.put("code", 1);
//            return result;
//        }
//    }
//
//
//    @RequestMapping("uploadtest")
//    @ResponseBody
//    public com.alibaba.fastjson.JSONObject uploadtest(@RequestParam("file") MultipartFile file, Script script) throws Exception {
//        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
//        byte[] bytes = file.getBytes();
//        String str = new String(bytes, "utf-8");
//
//        Long fileScriptId = 0L;
//        try {
////            ApiEnv.setEnv("local");
//            logger.error("=======================================" + ApiEnv.getDomain());
//            String appId = "datadev";
//            String appToken = "a664284f9a0048ba206f3ea2629bd9fa";
//            String userToken = "URMd91c37903bab9a5c87a250d7eaa0dc71";
//            Long time = new Date().getTime();
//            String timeStr = Long.toString(time);
//            String sign = MD5Util.getMD5Str(appToken + userToken + timeStr);
//            logger.error("======================appid:" + appId);
//            logger.error("======================userToken:" + userToken);
//            logger.error("======================time:" + time);
//            logger.error("======================sign:" + sign);
//            ApiResult apiResult = ScriptUtil.uploadScript(appId, userToken, time, sign, file.getInputStream(), script);
//            String s = com.alibaba.fastjson.JSONObject.toJSONString(apiResult);
//            return com.alibaba.fastjson.JSONObject.parseObject(s);
//        } catch (HbaseUploadException e) {
//
//        }
//        return null;
//    }
//@RequestMapping("uploadtest")
//@ResponseBody
//public com.alibaba.fastjson.JSONObject uploadtest() throws Exception {
////        String row1 = " 0\t1\t0\t1535167505791\t10.187.101.114\tNULL\tNULL\tNULL\t罗意威（LOEWE）\t0\t0\thttp://www.jd.com/https__pinpai/9187-11851.html\t0\tNULL\t2615,1387 9186,11934,9191,9187,9188,9189,11932\t1512610883.58662\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\t0\t0\t0\t0\t{\"\":null}\t2018-08-25\t1";
//    String row1 = "0\t1\t\t\t\t0\t\t\t1535167505791\t10.187.101.114\tNULL\tNULL\tNULL\t罗意威（LOEWE）\t0\t0\thttp://www.jd.com/https__pinpai/9187-11851.html\t0\tNULL\t2615,1387\t9186,11934,9191,9187,9188,9189,11932\t\t1512610883.58662\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\t\t0\t0\t0\t0\t\t\t\t\t\t\t\t\t\t{\"\":null}\t2018-08-25\t1";
//    ArrayList<String> stringArrayList = new ArrayList<String>();
//    stringArrayList.add(row1);
//
//    List<JSONObject> rowArrays = new ArrayList<JSONObject>();
//    for (String row : stringArrayList) {
//        JSONObject temp = new JSONObject();
//        try {
//            String[] sqlData = row.split("(\\t)");
//            for (int index = 0; index < sqlData.length; index++) {
//                temp.put("" + index, sqlData[index]);
//            }
//
//        } catch (Exception e) {
//        }
//        rowArrays.add(temp);
//    }
//
//    PageResultDTO resultDTO = new PageResultDTO();
//    resultDTO.setPage(1);
//    resultDTO.setLimit(1);
//    Pageable pageable = new PageRequest(1 - 1, 1);
//    List<String> dataRows = new ArrayList<String>();
//    int start = pageable.getOffset() + 1;
//    int end = start + pageable.getPageSize();
//    //sql 的查询多了title在hbase里面
//    resultDTO.setRows(rowArrays);
//    resultDTO.setRecords(1L);
//    resultDTO.setSuccess(true);
//    return com.alibaba.fastjson.JSONObject.parseObject(AjaxUtil.gridJson(resultDTO).toString());
//}
//    @RequestMapping("uploadtest1")
//    @ResponseBody
//    public JSONObject uploadtest1() throws Exception {
////        String row1 = " 0\t1\t0\t1535167505791\t10.187.101.114\tNULL\tNULL\tNULL\t罗意威（LOEWE）\t0\t0\thttp://www.jd.com/https__pinpai/9187-11851.html\t0\tNULL\t2615,1387 9186,11934,9191,9187,9188,9189,11932\t1512610883.58662\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\t0\t0\t0\t0\t{\"\":null}\t2018-08-25\t1";
//        String row1 = "0\t1\t\t\t\t0\t\t\t1535167505791\t10.187.101.114\tNULL\tNULL\tNULL\t罗意威（LOEWE）\t0\t0\thttp://www.jd.com/https__pinpai/9187-11851.html\t0\tNULL\t2615,1387\t9186,11934,9191,9187,9188,9189,11932\t\t1512610883.58662\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\t\t0\t0\t0\t0\t\t\t\t\t\t\t\t\t\t{\"\":null}\t2018-08-25\t1";
//        ArrayList<String> stringArrayList = new ArrayList<String>();
//        stringArrayList.add(row1);
//
//        List<JSONObject> rowArrays = new ArrayList<JSONObject>();
//        for (String row : stringArrayList) {
//            JSONObject temp = new JSONObject();
//            try {
//                String[] sqlData = row.split("(\\t)");
//                for (int index = 0; index < sqlData.length; index++) {
//                    temp.put("" + index, sqlData[index]);
//                }
//
//            } catch (Exception e) {
//            }
//            rowArrays.add(temp);
//        }
//
//        PageResultDTO resultDTO = new PageResultDTO();
//        resultDTO.setPage(1);
//        resultDTO.setLimit(1);
//        Pageable pageable = new PageRequest(1 - 1, 1);
//        List<String> dataRows = new ArrayList<String>();
//        int start = pageable.getOffset() + 1;
//        int end = start + pageable.getPageSize();
//        //sql 的查询多了title在hbase里面
//        resultDTO.setRows(rowArrays);
//        resultDTO.setRecords(1L);
//        resultDTO.setSuccess(true);
//        return AjaxFastJSONUtil.gridJson(resultDTO);
//    }
//    @RequestMapping("testjsf")
//    @ResponseBody
//    public com.alibaba.fastjson.JSONObject run(Long id, String version) throws Exception {
//        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
////
////
////        Long fileScriptId = 0L;
////        try {
////            logger.error("=======================================" + ApiEnv.getDomain());
////            scriptInterface.stopScript("a", "b", 3L, "d", new ScriptRunDetail() {
////                {
////                    setId(276L);
////                }
////            });
////
////        } catch (Exception e) {
////            logger.error(e);
////        }
//        ScriptRunDetail scriptRunDetail=new ScriptRunDetail();
//        scriptRunDetail.setId(1159L);
//        scriptInterface.getRunScriptDetail("a","b",3L,"d",scriptRunDetail);
//        return null;
//    }
}
