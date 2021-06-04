package com.jd.bdp.datadev.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.jd.bdp.datadev.domain.diff.ReleaseCompareVo;
import com.jd.jbdp.release.model.po.ReleaseObjInfo;
import com.jd.jbdp.release.model.vo.SubmitObj;

public interface DataDevScriptDiffService {

    ReleaseCompareVo compareInfo(Long projectSpaceId, Long scriptId, String scriptName) throws Exception;

    JSONObject getTaskList(Long projectSpaceId, String scriptName, String operator) throws Exception ;

    /**
     * 校验
     *
     * @param projectSpaceId
     * @param devContent
     * @param releaseCompareVo
     * @return  100 -- 校验通过
     *          101 -- 强校验不通过
     *          102 -- 弱校验不通过
     */
    Integer check(Long projectSpaceId, String devContent, ReleaseCompareVo releaseCompareVo) throws Exception;

    /**
     * 提交到发布中心
     * @param projectSpaceId
     * @param desc
     * @param operator
     * @param submitObj
     * @return
     */
    boolean submit2RC(Long projectSpaceId, String desc, String operator, SubmitObj submitObj) throws Exception;

    /**
     * 发布上线历史
     *
     * @param projectSpaceId
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    PageInfo<ReleaseObjInfo> releaseRecord(Long projectSpaceId, String scriptName, Integer page, Integer size) throws Exception;
}
