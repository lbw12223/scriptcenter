package com.jd.bdp.datadev.web.worker;

import com.jd.bdp.datadev.component.AppGroupUtil;
import com.jd.bdp.datadev.component.LockUtil;
import com.jd.bdp.datadev.component.SpringPropertiesUtils;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.enums.DataDevScriptRunStatusEnum;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.datadev.service.DataDevScriptService;
import com.jd.jim.cli.Cluster;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 1.需要解决用户，在退出浏览器的时候去kill任务。
 * 2.请求日志时候，往redis里面写入一个 Set<Long> , 同时在添加一个Key
 * 3.定时worker 去扫描Set<Long> , 然后拿到Key, 读获取第二步中key的时间
 */

public class RunningSqlOperationLogCheckWorker {


    final static Logger log = LoggerFactory.getLogger(RunningSqlOperationLogCheckWorker.class);

    /**
     * running set set<Long>
     */
    private static final String REDIS_KEY_RUNNITN_SET = "bdp.datadev.running.set";

    /**
     * running detail  id_timestemp
     */
    private static final String REDIS_KEY_RUNNITN_DETAIL = "bdp.datadev.running.detail.%s";

    @Autowired
    private LockUtil lockUtil;


    private static final String RUNNING_SQL_OPERATION_LOCK = "bdp.datadev.running.lock";


    public static final String KEY_OF_REDIS_NO_HEART_TIME_LIMIT = "bdp.datadev.noheart.time.limit"; //无心跳，持续运行多长时间

    /**
     * worker的时间间隔 , 这里需要去看看worker的间隔时间
     */
    // private static final Long WOKER_INTERVAL = 70 * 1000L;

    private static final Long _1MIN = 60 * 1000L; //1分钟

    @Autowired
    private DataDevScriptRunDetailService dataDevScriptRunDetailService;
    @Autowired
    private DataDevScriptService scriptService;

    @Autowired
    private Cluster jimClient;


    /**
     * 前端页面在获取日志的时候调用。每次往${REDIS_KEY_RUNNITN_SET} 放一个值，同时放${REDIS_KEY_RUNNITN_DETAIL}
     * <p>
     * 如果前端一直在发送请求，说明用户没有关闭浏览器
     */
    public void setJobRunning(DataDevScriptRunDetail dataDevScriptRunDetail) {
        Long runId = dataDevScriptRunDetail.getId();
        if (dataDevScriptRunDetail.getStatus().equals(DataDevScriptRunStatusEnum.Running.toCode())) {
            jimClient.sAdd(REDIS_KEY_RUNNITN_SET, String.valueOf(runId));
            String runDetailKey = String.format(REDIS_KEY_RUNNITN_DETAIL, runId);
            String runDetailValue = runId + "_" + System.currentTimeMillis();
            jimClient.setEx(runDetailKey, runDetailValue, 1, TimeUnit.DAYS);
        } else {
            removeRunningJob(runId);
            //jimClient.sRem(REDIS_KEY_RUNNITN_SET, String.valueOf(runId));
        }
    }


    /**
     * 获取running
     *
     * @return
     */
    public Set<String> getRunningJob() {
        return jimClient.sMembers(REDIS_KEY_RUNNITN_SET);
    }

    public void removeRunningJob(Long runId) {
        jimClient.sRem(REDIS_KEY_RUNNITN_SET, String.valueOf(runId));
        jimClient.expire(String.format(REDIS_KEY_RUNNITN_DETAIL, runId), 10, TimeUnit.SECONDS);
    }

    private Long getTimeLimit() {
        try {
            String timeLimit = jimClient.get(KEY_OF_REDIS_NO_HEART_TIME_LIMIT);
            return Integer.parseInt(timeLimit) * _1MIN;
        } catch (Exception e) {

        }

        return 60 * 5 * _1MIN; // 5 小时
    }

    /**
     * 一个定时任务，不去刷新数据库，REDIS_KEY_RUNNITN_SET
     * <p>
     * 1.work 定义的时间是1分钟运行一次
     */
    public void execute() {


        String refreshSingleKey = String.format(RUNNING_SQL_OPERATION_LOCK, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"));
        boolean result = false;
        String requestId = String.valueOf(System.currentTimeMillis());
        try {
            result = lockUtil.tryNotWaitLock(refreshSingleKey, requestId, 5 * 60);
            if (result) {

                Long WOKER_INTERVAL = getTimeLimit();
                log.info("======WOKER_INTERVAL==" + WOKER_INTERVAL);
                Set<String> runningSet = getRunningJob();
                if (runningSet != null) {
                    for (String idStr : runningSet) {
                        try {
                            String runDetailKey = String.format(REDIS_KEY_RUNNITN_DETAIL, idStr);
                            String runDetailValue = jimClient.get(runDetailKey);
                            if (StringUtils.isEmpty(runDetailValue)) {
                                removeRunningJob(Long.parseLong(idStr));
                                continue;
                            }
                            Long runId = Long.parseLong(runDetailValue.split("_")[0]);
                            Long lastTime = Long.parseLong(runDetailValue.split("_")[1]);

                            if (System.currentTimeMillis() - lastTime > WOKER_INTERVAL) {

                                DataDevScriptRunDetail runDetail = dataDevScriptRunDetailService.findById(Long.parseLong(idStr));
                                if (runDetail.getStatus().equals(DataDevScriptRunStatusEnum.Running.toCode())) {
                                    runDetail.setStopErp(AppGroupUtil.BDP_SYS);
                                    runDetail.setOperator(AppGroupUtil.BDP_SYS);
                                    scriptService.stopScript(runDetail);
                                }

                                removeRunningJob(Long.parseLong(idStr));
                            }
                        } catch (Exception e) {
                            log.error("doCheckRunningSqlOperation", e);
                        }

                    }
                }

            } else {
                log.error("当前结点不执行killrunningjob=");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (result == true) {
                lockUtil.unLock(refreshSingleKey, requestId);
            }
        }


    }
}
