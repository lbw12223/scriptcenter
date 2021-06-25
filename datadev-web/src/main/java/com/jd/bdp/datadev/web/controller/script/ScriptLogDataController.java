package com.jd.bdp.datadev.web.controller.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.*;
import com.jd.bdp.datadev.domain.*;
import com.jd.bdp.datadev.enums.*;
import com.jd.bdp.datadev.service.DataDevGitProjectService;
import com.jd.bdp.datadev.service.DataDevScriptConfigService;
import com.jd.bdp.datadev.service.DataDevScriptFileService;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.component.HightChartData;
import com.jd.bdp.datadev.web.worker.RunningSqlOperationLogCheckWorker;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhangrui25 on 2018/4/12.
 */
@Controller
public class ScriptLogDataController {


    @Autowired
    private DataDevScriptRunDetailService dataDevScriptRunDetailService;
    @Autowired
    private DataDevScriptConfigService configService;

    @Autowired
    private DataDevScriptFileService fileService;
    @Autowired
    private DataDevGitProjectService projectService;

    @Autowired
    private HbaseRunDetailData hbaseRunDetailData;

    @Autowired
    private HbaseRunDetailLog hbaseRunDetailLog;
    @Autowired
    private UrmUtil urmUtil;
    @Autowired
    private RunningSqlOperationLogCheckWorker runningSqlOperationLogCheckWorker;



    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss,SSS";

    private static final SimpleDateFormat simpleDateFormate = new SimpleDateFormat(dateFormat);


    private static final String DEFAULT_NAME = "script";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Logger logger = Logger.getLogger(ScriptInfoController.class);

    private static final Integer maxHasFinishRequestTimes = 60;

    @RequestMapping({"/scriptcenter/home/home_open_ide_runData.html"})
    public String openIdeRunData(Model model, UrmUserHolder urmUserHolder, Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        model.addAttribute("cols", JSONObject.toJSONString(hbaseRunDetailData.getRunDetailDataTableTitle(dataDevScriptRunDetail)));
        model.addAttribute("dataDevScriptRunDetail", dataDevScriptRunDetail);
        return "scriptcenter/home/home_open_ide/home_open_ide_runData";
    }

    /**
     * 实时日志，页面自己去判断当前是否执行完成。
     * <p>
     * <p>
     * 2020-7-14 : 修改为isResult JDQ升级为两个Q，修改为 判断状态 && 获取hbase中最后一条结果是否存在
     * ： 页面需要自己去判断isResult, 如果状态成功，但是一直没有结果，那么执行5次过后，直接打开结果，无论有无
     *
     * @param userHolder
     * @param runDetailId
     * @param currentLogIndex
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "实时获取脚本运行日志")
    @RequestMapping("/scriptcenter/script/runTimeLog.ajax")
    @ResponseBody
    public JSONObject runTimeLogAjax(UrmUserHolder userHolder,
                                     @RequestParam(value = "runDetailId") Long runDetailId,
                                     @RequestParam(value = "currentLogIndex") Integer currentLogIndex,
                                     @RequestParam(value = "hasFinishRequestTimes", defaultValue = "0") Integer hasFinishRequestTimes) throws Exception {

        DataDevScriptRunDetail runDetail = dataDevScriptRunDetailService.findById(runDetailId);
        runningSqlOperationLogCheckWorker.setJobRunning(runDetail);

        runDetail.setCurrentLogIndex(currentLogIndex);
        Map<String, Object> result = new HashedMap();

        boolean canOpenResult = false ;     //是否可以打开结果
        boolean isLastLog = false;          //是否最后一行日志




        //判断是否是SQL
        logger.info("========runDetail.getDataCount()====" + runDetail.getDataCount());
        if (runDetail.getDataCount() != null && runDetail.getDataCount() > 0) {
            if (hasFinishRequestTimes >= maxHasFinishRequestTimes || hbaseRunDetailData.hasDataDevScriptRunDetailDataFinish(runDetail))
                canOpenResult = true;
        }

        boolean isFinish = (runDetail.getStatus() != DataDevScriptRunStatusEnum.Delay.toCode() && runDetail.getStatus() != DataDevScriptRunStatusEnum.Running.toCode()) ? true : false;

        HoldDoubleValue<HoldDoubleValue<Boolean, Boolean>, List<String>> holdDoubleValue = hbaseRunDetailLog.getRunTimeRunDetailLog(runDetail);
        result.put("logs", holdDoubleValue.b);

        // isLastLog : 控制页面是否一直请求 isLastLog = 当前页面请求的日志index == 已经等于运行记录上等日志行数
        isLastLog = holdDoubleValue.a.a;

        if (runDetail.getType().equals(DataDevScriptTypeEnum.SQL.toCode()) &&
                runDetail.getStatus().equals(DataDevScriptRunStatusEnum.Success.toCode()) &&
                isLastLog) {
            //sql脚本一直等待结果存到Hbase完成
            isLastLog = canOpenResult;
        }


        result.put("isErrorLog", holdDoubleValue.a.b);
        result.put("isLastLog", isLastLog);
        //是否可以打开结果
        result.put("canOpenResult", canOpenResult );
        result.put("runDetailId", runDetailId);
        result.put("runDetail", runDetail);
        result.put("isFinish", isFinish);

        return JSONObjectUtil.getSuccessResult(result);
    }

    /**
     * 取最后一条日志
     *
     * @param userHolder
     * @param runDetailId
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取最后一条运行日志")
    @RequestMapping("/scriptcenter/script/getLastLog.ajax")
    @ResponseBody
    public JSONObject getLastLog(UrmUserHolder userHolder,
                                 @RequestParam(value = "runDetailId") Long runDetailId) throws Exception {
        DataDevScriptRunDetail runDetail = dataDevScriptRunDetailService.findById(runDetailId);
//        projectService.verifyUserAuthority(userHolder.getErp(), runDetail.getGitProjectId());
        Date date = runDetail.getEndTime() != null ? runDetail.getEndTime() : new Date();
        String stopErp = StringUtils.isNotBlank(runDetail.getStopErp()) ? runDetail.getStopErp() : "";
        String daTimeStr = date != null ? simpleDateFormate.format(date) : "";
        //2018-08-16 15:40:03,849   172.22.178.133(bjyuanz) 手动停止脚本成功...
        String log = daTimeStr + "   " + (runDetail.getIp() != null ? runDetail.getIp() : "") + "(" + stopErp + ")" + " 手动停止脚本成功...";
        JSONObject result = new JSONObject();
        result.put("log", log);
        result.put("runDetailId", runDetailId);
        return JSONObjectUtil.getSuccessResult(result);
    }

    /**
     * 运行日志，执行完成后调用
     *
     * @param userHolder
     * @param page
     * @param rows
     * @param runDetailId
     * @return
     * @throws Exception
     */
    @ExceptionMessageAnnotation(errorMessage = "获取脚本运行日志")
    @RequestMapping("/scriptcenter/script/runLog.ajax")
    @ResponseBody
    public net.sf.json.JSONObject runLogAjax(UrmUserHolder userHolder,
                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "rows", defaultValue = "20") Integer rows,
                                             Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        if (DataDevRunTypeEnum.DependencyRun.tocode().equals(dataDevScriptRunDetail.getRunType())) {
            dataDevScriptRunDetail.setType(DataDevScriptTypeEnum.Zip.toCode());
        }
//        projectService.verifyUserAuthority(userHolder.getErp(), dataDevScriptRunDetail.getGitProjectId());
        return AjaxUtil.gridJson(hbaseRunDetailLog.getRunDetailLog(dataDevScriptRunDetail, page, rows));
    }


    @ExceptionMessageAnnotation(errorMessage = "计算图表数据")
    @RequestMapping("/scriptcenter/script/chartData.ajax")
    @ResponseBody
    public net.sf.json.JSONObject chartData(UrmUserHolder userHolder,
                                            String data) throws Exception {
        JSONObject params = JSON.parseObject(data);
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(params.getLong("runDetailId"));
//        projectService.verifyUserAuthority(userHolder.getErp(), dataDevScriptRunDetail.getGitProjectId());
        List<JSONObject> baseDatas = hbaseRunDetailData.getAllDataDevScriptRunDetailData(dataDevScriptRunDetail);
       /* JSONArray datas = JSON.parseObject(FileUtils.readFileToString(new File("d:/hightChartData/datas.txt"))).getJSONArray("rows");
        baseDatas = new ArrayList<JSONObject>();
        for (int index = 0; index < datas.size(); index++) {
            JSONObject temp = datas.getJSONObject(index);
            baseDatas.add(temp);
        }
        logger.error(baseDatas);*/
        return AjaxUtil.success("success", handChartData(baseDatas, params));

    }

    /**
     * series: [{
     * name: '安装，实施人员',
     * data: [43934, 52503, 57177, 69658, 97031, 119931, 137133, 154175]
     * }],
     * categories:[x轴 按照顺序来]
     *
     * @param baseDatas
     * @param params
     * @return
     */
    private JSONObject handChartData(List<JSONObject> baseDatas, JSONObject params) {
        JSONObject result = new JSONObject();
        /**
         * [{
         *     columnIndex:
         *     name:
         * }]
         */
        final JSONArray categoryLabel = params.getJSONArray("categoryLabel");
        final JSONArray xLabel = params.getJSONArray("xLabel");
        final JSONArray yLable = params.getJSONArray("yLabel");
        final JSONArray zLabel = params.getJSONArray("zLabel");
        final String chartType = params.getString("chartType");
        final String categoriesColumnIndex = chartType.equals("qpt_chart") ? categoryLabel.getJSONObject(0).getString("columnIndex") : xLabel.getJSONObject(0).getString("columnIndex");

        List<JSONObject> sortBaseData = sortAndCovertYLable(categoryLabel, xLabel, yLable, zLabel, baseDatas, chartType);
        HoldDoubleValue<JSONArray, JSONArray> allAndGroupCategories = getCategories(sortBaseData, categoriesColumnIndex);

        boolean hasAllGroup = false;
        for (int index = 0; index < yLable.size(); index++) {
            hasAllGroup = hasAllGroup || yLable.getJSONObject(index).getInteger("sort") == 0;
        }

        result.put("categories", hasAllGroup ? allAndGroupCategories.a : allAndGroupCategories.b);
        result.put("series", getSeries(baseDatas, params, allAndGroupCategories.b));

        return result;
    }

    /**
     * 排序 && 转换YLabel的值为数值
     *
     * @param xLabel
     * @param yLabel
     * @param baseDatas
     * @return
     */
    private List<JSONObject> sortAndCovertYLable(JSONArray categoryLabel, JSONArray xLabel, JSONArray yLabel, JSONArray zLabel, List<JSONObject> baseDatas, String chartType) {
        boolean tempIsDesc = false;
        String tempCategoriesColumnIndex = null;
        if (chartType.equals("qpt_chart")) {
            tempIsDesc = categoryLabel.getJSONObject(0).getString("sort").equals("2");
            tempCategoriesColumnIndex = categoryLabel.getJSONObject(0).getString("columnIndex");
        } else {
            tempIsDesc = xLabel.getJSONObject(0).getString("sort").equals("2");
            tempCategoriesColumnIndex = xLabel.getJSONObject(0).getString("columnIndex");
        }

        final boolean isDesc = tempIsDesc;
        final String categoriesColumnIndex = tempCategoriesColumnIndex;
        /*排序*/
        Collections.sort(baseDatas, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    String o1Value = o1.getString(categoriesColumnIndex);
                    String o2Value = o2.getString(categoriesColumnIndex);
                    if (HightChartData.isNumeric(o1Value) && HightChartData.isNumeric(o2Value)) {
                        double o1DoubleValue = HightChartData.covertNumber(o1Value).doubleValue();
                        double o2DoubleValue = HightChartData.covertNumber(o2Value).doubleValue();
                        if (o2DoubleValue == o1DoubleValue) {
                            return 0;
                        }
                        if (isDesc) {
                            return o2DoubleValue > o1DoubleValue ? 1 : -1;
                        } else {
                            return o2DoubleValue > o1DoubleValue ? -1 : 1;
                        }
                    } else {
                        if (isDesc) {
                            return o2.getString(categoriesColumnIndex).compareTo(o1.getString(categoriesColumnIndex));
                        } else {
                            return o1.getString(categoriesColumnIndex).compareTo(o2.getString(categoriesColumnIndex));
                        }
                    }

                } catch (Exception e) {
                    logger.error(e);
                }
                return 0;
            }
        });
        /**
         * 转换Y轴需要的值 double
         */
        Set<String> yLabelColumnIndex = new HashSet<String>();
        if (chartType.equals("qpt_chart")) {
            if (xLabel != null && xLabel.size() > 0) {
                for (int index = 0; index < xLabel.size(); index++) {
                    yLabelColumnIndex.add(xLabel.getJSONObject(index).getString("columnIndex"));
                }
            }
        }
        if (yLabel != null && yLabel.size() > 0) {
            for (int index = 0; index < yLabel.size(); index++) {
                yLabelColumnIndex.add(yLabel.getJSONObject(index).getString("columnIndex"));
            }
        }
        if (zLabel != null && zLabel.size() > 0) {
            for (int index = 0; index < zLabel.size(); index++) {
                yLabelColumnIndex.add(zLabel.getJSONObject(index).getString("columnIndex"));
            }
        }

        for (JSONObject temp : baseDatas) {
            /*Y 轴的数据转换为数字*/
            for (String needCovertColumnIndex : yLabelColumnIndex) {
                temp.put(needCovertColumnIndex, HightChartData.covertNumber(temp.getString(needCovertColumnIndex)));
            }
        }

        return baseDatas;
    }

    /**
     * 处理中位数，平均数，最大值，最小值
     *
     * @param sortDatas
     * @param params
     */
    private JSONArray getSeries(List<JSONObject> sortDatas, JSONObject params, JSONArray groupCategories) {
        final JSONArray categoryLabel = params.getJSONArray("categoryLabel");
        final JSONArray xLabel = params.getJSONArray("xLabel");
        final JSONArray yLable = params.getJSONArray("yLabel");
        final JSONArray zLabel = params.getJSONArray("zLabel");
        final String chartType = params.getString("chartType");
        final String categoriesColumnIndex = chartType.equals("qpt_chart") ? categoryLabel.getJSONObject(0).getString("columnIndex") : xLabel.getJSONObject(0).getString("columnIndex");
        /**
         * 按照  categoriesColumnIndex 分组过后的数据
         */
        Map<String, List<JSONObject>> fixDatas = new HashMap<String, List<JSONObject>>();
        for (int index = 0; index < sortDatas.size(); index++) {
            JSONObject temp = sortDatas.get(index);
            String key = temp.getString(categoriesColumnIndex);
            List<JSONObject> datas = fixDatas.get(key);
            datas = datas != null ? datas : new ArrayList<JSONObject>();
            datas.add(temp);
            fixDatas.put(key, datas);
        }
        if (chartType.equals("qpt_chart")) {
            return HightChartData.getBubbleSeries(fixDatas, xLabel, yLable, zLabel, groupCategories);
        } else {
            for (int index = 0; index < yLable.size(); index++) {
                if (chartType.equalsIgnoreCase("bt_chart")) {
                    JSONArray datas = HightChartData.getPieSeries(fixDatas, yLable.getJSONObject(index), sortDatas, categoriesColumnIndex, groupCategories);
                    yLable.getJSONObject(index).put("data", datas);
                } else {
                    JSONArray datas = HightChartData.getOneSeries(fixDatas, yLable.getJSONObject(index), sortDatas, categoriesColumnIndex, groupCategories);
                    yLable.getJSONObject(index).put("data", datas);
                }
            }
        }

        return yLable;
    }


    /**
     * 获取 categories ,
     * <p>
     * 如果Y Label 里面包含有
     *
     * @param sortDatas             (已经排序过)
     * @param categoriesColumnIndex
     * @return
     */
    private HoldDoubleValue<JSONArray, JSONArray> getCategories(List<JSONObject> sortDatas, String categoriesColumnIndex) {
        JSONArray allCategories = new JSONArray();
        //平铺
        for (int dataIndex = 0; dataIndex < sortDatas.size(); dataIndex++) {
            allCategories.add(sortDatas.get(dataIndex).getString(categoriesColumnIndex));
        }

        JSONArray groupCategories = new JSONArray();
        //(分组)
        LinkedHashSet<String> linkedSet = new LinkedHashSet<String>();
        for (int dataIndex = 0; dataIndex < sortDatas.size(); dataIndex++) {
            JSONObject data = sortDatas.get(dataIndex);
            linkedSet.add(data.getString(categoriesColumnIndex));
        }
        for (String temp : linkedSet) {
            groupCategories.add(temp);
        }
        return new HoldDoubleValue<JSONArray, JSONArray>(allCategories, groupCategories);
    }


    @ExceptionMessageAnnotation(errorMessage = "获取脚本运行结果")
    @RequestMapping("/scriptcenter/script/runData.ajax")
    @ResponseBody
    public JSONObject runLogData(UrmUserHolder userHolder,

                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "rows", defaultValue = "20") Integer rows,
                                 Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
//        projectService.verifyUserAuthority(userHolder.getErp(), dataDevScriptRunDetail.getGitProjectId());
        PageResultDTO pageResultDTO = hbaseRunDetailData.getRunDetailData(dataDevScriptRunDetail, page, rows);
     /*   net.sf.json.JSONObject result = net.sf.json.JSONObject.fromObject(FileUtils.readFileToString(new File("d:/hightChartData/datas.txt")));
        result.getJSONArray("rows").getJSONObject(0).put("0", new JSONObject());
        return result;*/
        return AjaxFastJSONUtil.gridJson(pageResultDTO);
    }

    @ExceptionMessageAnnotation(errorMessage = "获取脚本运行结果标题")
    @RequestMapping({"/scriptcenter/script/title.ajax"})
    @ResponseBody
    public JSONObject getDataTitle(Model model, UrmUserHolder urmUserHolder, Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
//        projectService.verifyUserAuthority(urmUserHolder.getErp(), dataDevScriptRunDetail.getGitProjectId());
        Map<String, String> map = hbaseRunDetailData.getRunDetailDataTableTitle(dataDevScriptRunDetail);

     /*   map.put("0", "afs_ser_bill_id");
        map.put("1", "afs_ser_bill_apply_tm");
        map.put("2", "cust_expect_proc_mode_cd");
        map.put("3", "cust_acct");
        map.put("4", "invoice_time");
        return JSONObject.parseObject(FileUtils.readFileToString(new File("d:/hightChartData/title.txt")));*/
        return JSONObjectUtil.getSuccessResult(map);


    }

    @RequestMapping({"/scriptcenter/home/home_log.html"})
    public String homeLog(Model model, UrmUserHolder urmUserHolder, Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        DataDevScriptRunStatusEnum statusEnum = DataDevScriptRunStatusEnum.enumValueOf(dataDevScriptRunDetail.getStatus());
        model.addAttribute("status", statusEnum != null ? statusEnum.toDesc() : "");
        model.addAttribute("id", dataDevScriptRunDetail.getId());
        model.addAttribute("runDetailId", runDetailId);
        return "scriptcenter/home/home_open_ide/home_log";
    }

    @RequestMapping({"/scriptcenter/home/home_data.html"})
    public String homeData(Model model, UrmUserHolder urmUserHolder, Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        if (dataDevScriptRunDetail.getType() != DataDevScriptTypeEnum.SQL.toCode()) {
            throw new RuntimeException("非sql任务无法查看结果");
        }
        if (dataDevScriptRunDetail.getStatus() != DataDevScriptRunStatusEnum.Success.toCode()) {
            throw new RuntimeException("此任务失败无法查看结果");
        }
        DataDevScriptRunStatusEnum statusEnum = DataDevScriptRunStatusEnum.enumValueOf(dataDevScriptRunDetail.getStatus());
        model.addAttribute("status", statusEnum != null ? statusEnum.toDesc() : "");
        model.addAttribute("id", dataDevScriptRunDetail.getId());
        model.addAttribute("runDetailId", runDetailId);
        return "scriptcenter/home/home_open_ide/home_data";
    }

    @RequestMapping({"/scriptcenter/home/home_data_log_test.html"})
    public String homeDataLogTest(Model model, Long runDetailId, Long dataLog) throws Exception {
        model.addAttribute("runDetailId", runDetailId);
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        if (dataDevScriptRunDetail == null) {
            throw new RuntimeException("运行记录不存在");
        }
        if (dataDevScriptRunDetail.getType() == DataDevScriptTypeEnum.SQL.toCode() && dataDevScriptRunDetail.getStatus() == DataDevScriptRunStatusEnum.Success.toCode()) {
            model.addAttribute("hasData", 1);
        } else {
            model.addAttribute("hasData", 0);
        }
        model.addAttribute("dataLog", dataLog);
        return "/scriptcenter/home/home_data_log_test";
    }

    @RequestMapping({"/scriptcenter/home/home_data_log.html"})
    public String homeDataLog(Model model, Long runDetailId, Long dataLog) throws Exception {
        model.addAttribute("runDetailId", runDetailId);
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        if (dataDevScriptRunDetail == null) {
            throw new RuntimeException("运行记录不存在");
        }
        if (dataDevScriptRunDetail.getType() == DataDevScriptTypeEnum.SQL.toCode()
                && dataDevScriptRunDetail.getDataCount() != null && dataDevScriptRunDetail.getDataCount() > 0) {
            model.addAttribute("hasData", 1);
        } else {
            model.addAttribute("hasData", 0);
        }

        model.addAttribute("dataLog", dataLog);
        model.addAttribute("scriptType", dataDevScriptRunDetail.getType());
        return "scriptcenter/home/home_open_ide/home_data_log";
    }

    @RequestMapping({"/scriptcenter/devcenter/home_data_log.html"})
    public String devCenterHomeDataLog(Model model, Long runDetailId, Long dataLog) throws Exception {
        model.addAttribute("runDetailId", runDetailId);
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
        if (dataDevScriptRunDetail == null) {
            throw new RuntimeException("运行记录不存在");
        }
        if (dataDevScriptRunDetail.getType() == DataDevScriptTypeEnum.SQL.toCode()
                && dataDevScriptRunDetail.getDataCount() != null && dataDevScriptRunDetail.getDataCount() > 0) {
            model.addAttribute("hasData", 1);
        } else {
            model.addAttribute("hasData", 0);
        }

        model.addAttribute("dataLog", dataLog);
        model.addAttribute("scriptType", dataDevScriptRunDetail.getType());
        return "scriptcenter/devcenter/home_data_log";
    }

    @ExceptionMessageAnnotation(errorMessage = "获取运行详细信息")
    @RequestMapping({"/scriptcenter/home/basicInfo.ajax"})
    @ResponseBody
    public net.sf.json.JSONObject basicInfo(UrmUserHolder userHolder, Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
//        projectService.verifyUserAuthority(userHolder.getErp(), dataDevScriptRunDetail.getGitProjectId());
        if (dataDevScriptRunDetail == null) {
            throw new RuntimeException("运行记录不存在");
        }
        DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(dataDevScriptRunDetail.getGitProjectId(), dataDevScriptRunDetail.getGitProjectFilePath());
        if (file == null) {
            throw new RuntimeException("脚本不存在");
        }
        if (dataDevScriptRunDetail.getScriptConfigId() != null) {
            DataDevScriptConfig config = configService.getConfigById(dataDevScriptRunDetail.getScriptConfigId());
            if (config != null) {
                dataDevScriptRunDetail.setClusterCode(config.getClusterCode());
                dataDevScriptRunDetail.setMarketLinuxUser(config.getMarketLinuxUser());
                dataDevScriptRunDetail.setAccountCode(config.getAccountCode());
                dataDevScriptRunDetail.setQueueCode(config.getQueueCode());
            }
        }
        if (StringUtils.isNotBlank(dataDevScriptRunDetail.getOperator())) {
            dataDevScriptRunDetail.setOperator(urmUtil.getNameByErp(dataDevScriptRunDetail.getOperator()) + "(" + dataDevScriptRunDetail.getOperator() + ")");
        }
        if (dataDevScriptRunDetail.getStartTime() != null && dataDevScriptRunDetail.getEndTime() != null) {
            Long timeDif = dataDevScriptRunDetail.getEndTime().getTime() - dataDevScriptRunDetail.getStartTime().getTime();
            if (timeDif >= 0) {
                String timeStr = "";
                if (timeDif / (1000 * 60 * 60) > 0) {
                    timeStr += timeDif / (1000 * 60 * 60) + "h";
                    timeDif %= (1000 * 60 * 60);
                }
                if (com.jd.common.util.StringUtils.isNotBlank(timeStr) || timeDif / (1000 * 60) > 0) {
                    timeStr += timeDif / (1000 * 60) + "min";
                    timeDif %= (1000 * 60);
                }
                if (timeDif / (1000 * 60) <= 0) {
                    timeStr += timeDif / (1000) + "s";
                }
                dataDevScriptRunDetail.setTimePeriod(timeStr);
            }
        }
        DataDevScriptEngineTypeEnum engineTypeEnum = DataDevScriptEngineTypeEnum.enumValueOf(dataDevScriptRunDetail.getEngineType());
        if (engineTypeEnum != null) {
            dataDevScriptRunDetail.setEngineType(engineTypeEnum.getName());
        }
        DataDevScriptRunStatusEnum statusEnum = DataDevScriptRunStatusEnum.enumValueOf(dataDevScriptRunDetail.getStatus());
        dataDevScriptRunDetail.setStatusStr(statusEnum.toDesc());
        net.sf.json.JSONObject resObj = AjaxUtil.fromObject(dataDevScriptRunDetail);
        DataDevGitProject project = projectService.getGitProjectBy(file.getGitProjectId());
        if (project != null) {
            resObj.put("gitProjectPath", project.getGitProjectPath());
        }
        return resObj;
    }


    @RequestMapping("/scriptcenter/script/downLoad.ajax")
    public ModelAndView downLoad(Model model, UrmUserHolder userHolder, Long runDetailId) throws Exception {
        DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
//        projectService.verifyUserAuthority(userHolder.getErp(), dataDevScriptRunDetail.getGitProjectId());
        if (dataDevScriptRunDetail.getType() != DataDevScriptTypeEnum.SQL.toCode()) {
            throw new RuntimeException("非sql脚本暂不支持下载结果");
        }
        if (dataDevScriptRunDetail.getDataCount() == null || dataDevScriptRunDetail.getDataCount() <= 0L) {
            throw new RuntimeException("脚本任务没有执行结果,不能下载.");
        }
        Map<String, String> map = hbaseRunDetailData.getRunDetailDataTableTitle(dataDevScriptRunDetail);
//        logger.error("===============================1111111111111111=================="+JSONObject.toJSONString(map));
        List<String> titleList = new ArrayList<String>();
        Integer index = 0;
        while (map.get(index.toString()) != null) {
            titleList.add(map.get(index.toString()));
            index++;
        }

//        logger.error("===============================22222222222222222==================="+JSONObject.toJSONString(titleList));

        List<List<String>> dataList = hbaseRunDetailData.getDataList(dataDevScriptRunDetail);

        String fileName = DEFAULT_NAME;
        Long gitProjectId = dataDevScriptRunDetail.getGitProjectId();
        String gitFilePath = dataDevScriptRunDetail.getGitProjectFilePath();
        if (gitProjectId != null && StringUtils.isNotBlank(gitFilePath)) {
            DataDevScriptFile file = fileService.getScriptByGitProjectIdAndFilePath(gitProjectId, gitFilePath);
            fileName = (file != null && StringUtils.isNotBlank(file.getName())) ? DataDevScriptTypeEnum.getNoSuffixName(file.getName()) : DEFAULT_NAME;
        }
        fileName += "_" + sdf.format(new Date());
        return new ModelAndView(new ExcelView(titleList, dataList, fileName));
    }

    /**
     * https://blog.csdn.net/dengsilinming/article/details/7352054
     */
    @RequestMapping("/scriptcenter/highchart/downLoad.ajax")
    @ResponseBody
    public JSONObject doPost(String type, String svg, String filename, HttpServletResponse response)
            throws ServletException, IOException {
        filename = StringUtils.isEmpty(filename) ? "datadev_chart" : filename;
        if (null != type && null != svg) {
            svg = svg.replaceAll(":rect", "rect");
            String ext = "";
            Transcoder t = null;
            if (type.equals("image/png")) {
                ext = "png";
                t = new PNGTranscoder();
            } else if (type.equals("image/jpeg")) {
                ext = "jpg";
                t = new JPEGTranscoder();
//            }else if (type.equals("application/pdf")) {
//                ext = "pdf";
//                t = new PDFTranscoder();
            } else if (type.equals("image/svg+xml")) {
                ext = "svg";
            }
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(filename + "." + ext, "UTF-8"));
            response.setContentType(type);
            if (null != t) {
                TranscoderInput input = new TranscoderInput(new StringReader(svg));
                TranscoderOutput output = new TranscoderOutput(response.getOutputStream());

                try {
                    t.transcode(input, output);
                } catch (TranscoderException e) {
                    response.getOutputStream().print("Problem transcoding stream. See the web logs for more details.");
                    e.printStackTrace();
                }
            } else if (ext.equals("svg")) {
                OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
                writer.append(svg);
                writer.close();
            } else
                response.getOutputStream().print("Invalid type: " + type);
        } else {
            response.addHeader("Content-Type", "text/html");
            response.getOutputStream().println("Usage: Parameter [svg]Parameter [type]\n\t 参数为空，下载失败");
        }
        response.getOutputStream().flush();
        response.getOutputStream().close();
        return null;
    }
}
