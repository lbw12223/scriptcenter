package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevClientBase;

import java.util.List;

public interface DataDevClientBaseService {

    List<DataDevClientBase> findAliveClient() throws Exception;
}
