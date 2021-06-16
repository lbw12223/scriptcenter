package com.jd.bdp.datadev.component;

import com.alibaba.fastjson.JSONObject;
import com.jd.jim.cli.Cluster;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用来记录 导入时候 记录导入
 */
@Component
public class ImportScriptManagerRedis {


    private static final String SYNC_APPGROUP_ID_KEY = "%s_bdp_data_dev_sync_appgroup_key_%s";

    @Autowired
    private Cluster jimClient;


   private class ImportBean {
        private Long projectSpaceId;
        private Integer total;
        private Integer processOn;
        private Integer success;
        private Integer failed;

        public Long getProjectSpaceId() {
            return projectSpaceId;
        }

        public void setProjectSpaceId(Long projectSpaceId) {
            this.projectSpaceId = projectSpaceId;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getProcessOn() {
            return processOn;
        }

        public void setProcessOn(Integer processOn) {
            this.processOn = processOn;
        }

        public Integer getSuccess() {
            return success;
        }

        public void setSuccess(Integer success) {
            this.success = success;
        }

        public Integer getFailed() {
            return failed;
        }

        public void setFailed(Integer failed) {
            this.failed = failed;
        }
    }


    public String key(Long projectSpaceId) {
        String syncKey = String.format(SYNC_APPGROUP_ID_KEY, SpringPropertiesUtils.getPropertiesValue("${datadev.env}"), projectSpaceId);
        return syncKey;
    }

    /**
     * 是否可以同步
     *
     * @param projectSpaceId
     * @return
     */
    boolean isCanSyncProjectId(Long projectSpaceId) {
        return StringUtils.isBlank(jimClient.get(key(projectSpaceId)));
    }

    String startImport(Long projectSpaceId, Integer total) {
        ImportBean importBean = new ImportBean();
        importBean.processOn = 1;
        importBean.total = total;
        importBean.success = 0;
        importBean.failed = 0;
        importBean.projectSpaceId = projectSpaceId;

        jimClient.set(key(projectSpaceId), JSONObject.toJSONString(importBean));
        return JSONObject.toJSONString(importBean);

    }

    public void increaseFailed(Long projectSpaceId) {
        increase(projectSpaceId, false);
    }

    public void increaseSuccess(Long projectSpaceId) {
        increase(projectSpaceId, true);
    }

    private void increase(Long projectSpaceId, Boolean isSuccess) {
        JSONObject importBean = JSONObject.parseObject(jimClient.get(key(projectSpaceId)));
        if (isSuccess) {
            Integer success = importBean.getInteger("success");
            importBean.put("success",++success);
        } else {
            Integer failed = importBean.getInteger("failed");
            importBean.put("failed",++failed);

        }
        Integer processOn = importBean.getInteger("processOn");
        importBean.put("processOn",++processOn);
        jimClient.set(key(projectSpaceId), JSONObject.toJSONString(importBean));
    }


    public void endImport(Long projectSpaceId) {
        jimClient.del(key(projectSpaceId));
    }

    public String current(Long projectSpaceId) {
        return JSONObject.parseObject(jimClient.get(key(projectSpaceId))).toJSONString();
    }

}
