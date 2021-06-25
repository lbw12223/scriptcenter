package com.jd.bdp.datadev.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.api.ScriptBuffaloInterface;
import com.jd.bdp.datadev.dao.DataDevScriptConfigDao;
import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.dao.DataDevScriptPublishDao;
import com.jd.bdp.datadev.dao.DataDevScriptRunDetailDao;
import com.jd.bdp.datadev.domain.DataDevScriptConfig;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptFilePublish;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.enums.DataDevScriptTypeEnum;
import com.jd.bdp.datadev.model.BuffaloJobInfo;
import com.jd.bdp.datadev.model.Script;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ScriptBuffaloServiceImpl implements ScriptBuffaloInterface {

    private static final Logger logger = Logger.getLogger(ScriptBuffaloServiceImpl.class);
    @Autowired
    private DataDevScriptFileDao fileDao;
    @Autowired
    private DataDevScriptRunDetailDao runDetailDao;
    @Autowired
    private DataDevScriptConfigDao configDao;

    @Override
    public BuffaloJobInfo getScriptInfo(Script script) throws Exception {
        BuffaloJobInfo buffaloJobInfo = new BuffaloJobInfo();
        logger.error("===========================getScriptInfo:" + JSONObject.toJSONString(script));

        if (script.getGitProjectId() != null && StringUtils.isNotBlank(script.getGitProjectFilePath())) {
            DataDevScriptFile file = fileDao.getSingleScriptFile(script.getGitProjectId(), script.getGitProjectFilePath());
            if (file == null) {
                return buffaloJobInfo;
            }
            DataDevScriptRunDetail runDetail = runDetailDao.findLastRunDetail(file.getId(), script.getOwner());
            String fileName = file.getName();
            Integer dotIndex = fileName.indexOf(".");
            if (dotIndex != -1) {
                fileName = fileName.substring(0, dotIndex);
            }
            buffaloJobInfo.setJobName(fileName);
            if (file != null) {
                if (file.getType() == DataDevScriptTypeEnum.Zip.toCode()) {
                    buffaloJobInfo.setStartShellPath((runDetail != null && StringUtils.isNotBlank(runDetail.getStartShellPath())) ? runDetail.getStartShellPath() : file.getStartShellPath());
                } else {
                    buffaloJobInfo.setStartShellPath(file.getName());
                }
                if (StringUtils.isNotBlank(file.getArgs())) {
                    JSONObject jsonObject = JSONObject.parseObject(file.getArgs());
                    String args = "";
                    Integer index = 1;
                    while (jsonObject.get(index.toString()) != null) {
                        args += jsonObject.get(index.toString()).toString() + ";";
                        index++;
                    }
                    if (args.length() > 0) {
                        args = args.substring(0, args.length() - 1);
                    }
                    buffaloJobInfo.setArgs(args);
                }
            }
            if (runDetail != null && runDetail.getScriptConfigId() != null) {
                DataDevScriptConfig config = configDao.findById(Long.parseLong(runDetail.getScriptConfigId()));
                if (config != null) {
                    buffaloJobInfo.setMarketId(config.getMarketId());
                    buffaloJobInfo.setAccountId(config.getAccountId());
                    buffaloJobInfo.setQueueId(config.getQueueId());
                }
            }
        }
        logger.error("===========================getScriptInfo:" + JSONObject.toJSONString(buffaloJobInfo));
        return buffaloJobInfo;
    }

    public static void main(String[] args) {
        String s = "2";
        String sss = s.substring(0, 0);
        System.out.println(s.substring(0, 0));
        JSONObject jsonObject = JSONObject.parseObject("{\"1\":\"11111\",\"2\":\"22222\"}");
        String argss = "";
        Integer index = 1;
        while (jsonObject.get(index.toString()) != null) {
            argss += jsonObject.get(index.toString()).toString() + ";";
            index++;
        }
        if (argss.length() > 0) {
            argss = argss.substring(0, argss.length() - 1);
        }


    }
}
