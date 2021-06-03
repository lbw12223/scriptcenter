package com.jd.bdp.datadev.service;

import com.jd.bdp.rc.api.ApiResult;
import com.jd.bdp.rc.api.ReleaseInterface;
import com.jd.bdp.rc.api.domains.ReleaseInfoFromDevDto;
import com.jd.bdp.rc.domain.bo.ReleaseRecordBo;
import com.jd.bdp.rc.domain.common.PageResult;
import org.springframework.stereotype.Service;

//@Service
public class ReleaseInterfaceImpl implements ReleaseInterface {
    @Override
    public ApiResult<ReleaseRecordBo> submitRelease(String s, String s1, Long aLong, ReleaseInfoFromDevDto releaseInfoFromDevDto) {
        return null;
    }

    @Override
    public ApiResult<ReleaseRecordBo> requestApprove(String s, String s1, Long aLong, ReleaseRecordBo releaseRecordBo) {
        return null;
    }

    @Override
    public ApiResult<ReleaseRecordBo> approveResult(String s, String s1, Long aLong, ReleaseRecordBo releaseRecordBo) {
        return null;
    }

    @Override
    public ApiResult<ReleaseRecordBo> releaseOnline(String s, String s1, Long aLong, ReleaseRecordBo releaseRecordBo) {
        return null;
    }

    @Override
    public ApiResult<PageResult<ReleaseRecordBo>> releaseRecord(String s, String s1, Long aLong, ReleaseRecordBo releaseRecordBo) {
        return null;
    }

    @Override
    public ApiResult<Boolean> isHaveReleaseing(String s, String s1, Long aLong, ReleaseInfoFromDevDto releaseInfoFromDevDto) {
        return null;
    }
}
