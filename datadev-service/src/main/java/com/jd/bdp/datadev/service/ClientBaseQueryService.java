package com.jd.bdp.datadev.service;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ClientBaseQueryService {

    public List<DataDevClientBase> queryClientBase();

    public List<DataDevClientBase> queryClientInfo(String ip);

    public void modifyStatus(Long  id,Integer status);

    PageResultDTO list4page(Pageable pageable) throws Exception;
}
