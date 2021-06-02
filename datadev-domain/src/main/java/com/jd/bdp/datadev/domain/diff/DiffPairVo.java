package com.jd.bdp.datadev.domain.diff;


/**
 *   提交信息对比
 */
public class DiffPairVo {
    /**
     * 待比较信息
     */
    private DiffInfoVo currentInfo;
    /**
     * 待比较信息
     */
    private DiffInfoVo remoteInfo;

    public DiffInfoVo getCurrentInfo() {
        return currentInfo;
    }

    public void setCurrentInfo(DiffInfoVo currentInfo) {
        this.currentInfo = currentInfo;
    }

    public DiffInfoVo getRemoteInfo() {
        return remoteInfo;
    }

    public void setRemoteInfo(DiffInfoVo remoteInfo) {
        this.remoteInfo = remoteInfo;
    }
}
