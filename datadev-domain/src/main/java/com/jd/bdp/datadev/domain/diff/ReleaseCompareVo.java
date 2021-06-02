package com.jd.bdp.datadev.domain.diff;

import com.alibaba.fastjson.JSONArray;

/**
 *   提交信息对比
 */
public class ReleaseCompareVo {
    /**
     * taskflow，一个任务，0个/多个脚本 ，比较任务里面的环节
     *
     * tasksingle 一个任务，0/1个脚本
     *
     * script 只有一个脚本
     */
    private String compareType;
    /**
     * 任务对比对
     */
    private DiffPairVo taskPair;
    /**
     * 脚本对别对
     */
    private DiffPairVo scriptPair;
    /**
     * 脚本对别对
     */
    private DiffPairVo actionPair;
    /**
     * 工作流任务时候，返回环境列表
     */
    private JSONArray actionList;


    public String getCompareType() {
        return compareType;
    }

    public void setCompareType(String compareType) {
        this.compareType = compareType;
    }

    public DiffPairVo getTaskPair() {
        return taskPair;
    }

    public void setTaskPair(DiffPairVo taskPair) {
        this.taskPair = taskPair;
    }

    public DiffPairVo getScriptPair() {
        return scriptPair;
    }

    public void setScriptPair(DiffPairVo scriptPair) {
        this.scriptPair = scriptPair;
    }

    public JSONArray getActionList() {
        return actionList;
    }

    public void setActionList(JSONArray actionList) {
        this.actionList = actionList;
    }

    public DiffPairVo getActionPair() {
        return actionPair;
    }

    public void setActionPair(DiffPairVo actionPair) {
        this.actionPair = actionPair;
    }
}
