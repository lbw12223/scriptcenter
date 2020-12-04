package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.dao.DataDevClientBaseDao;
import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.service.ClientBaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientBaseQueryImpl implements ClientBaseQueryService {

    @Autowired
    DataDevClientBaseDao dataDevClientBaseDao;

    public List<DataDevClientBase> queryClientBase() {
        List<DataDevClientBase> dataDevClientBase = dataDevClientBaseDao.findClientBase();
        return dataDevClientBase;
    }

    public List<DataDevClientBase> queryClientInfo(String ip) {
        List<DataDevClientBase> dataDevClientinfo = dataDevClientBaseDao.findClientInfo(ip);
        return dataDevClientinfo;
    }

    @Override
    public void modifyStatus(Long id, Integer status) {
        dataDevClientBaseDao.modifyStatus(id,status);
    }

    @Override
    public PageResultDTO list4page(Pageable pageable) throws Exception {
        PageResultDTO pageResultDTO = new PageResultDTO();
        Long count = 0L;
        List<DataDevClientBase> list = new ArrayList<DataDevClientBase>();
        count = dataDevClientBaseDao.count();
        if (count > 0) {
            list = dataDevClientBaseDao.list(pageable.getOffset(),pageable.getPageSize());
        }
        pageResultDTO.setRecords(count);
        pageResultDTO.setSuccess(true);
        pageResultDTO.setRows(list);
        return pageResultDTO;
    }
}
