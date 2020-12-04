package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevGitHis;
import com.jd.bdp.datadev.domain.DataDevGitProjectMember;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.enums.DataDevRunTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptEngineTypeEnum;
import com.jd.bdp.datadev.enums.DataDevScriptRunStatusEnum;
import com.jd.bdp.datadev.service.DataDevGitHisService;
import com.jd.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataDevGitHisServiceImpl implements DataDevGitHisService {


   /* @Override
    public void insertDataDevGitHis(List<DataDevGitHis> dataDevGitHis) {

    }

    @Override
    public void deleteDataDevGitHis(Long gitProjedtId) {

    }

    @Override
    public Page<DataDevGitProjectMember> queryDataDevGitHis(DataDevGitHis dataDevGitHis, Pageable pageable) {
        return null;
    }*/

}
