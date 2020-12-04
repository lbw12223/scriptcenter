package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.dao.DataDevClientBaseDao;
import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.service.DataDevClientBaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class DataDevClientBaseServiceImpl implements DataDevClientBaseService {

    private static final Long ACTIVE_TIME_INTERNAL = 1000 * 60 * 2L;//单位ms

    @Autowired
    DataDevClientBaseDao clientBaseDao;

    @Override
    public List<DataDevClientBase> findAliveClient()throws Exception {
        Long nowTime = System.currentTimeMillis();
        Long preTime = nowTime - ACTIVE_TIME_INTERNAL;
        List<DataDevClientBase> baseList = clientBaseDao.findByPreTime(new Date(preTime));
        return baseList;
    }


}
