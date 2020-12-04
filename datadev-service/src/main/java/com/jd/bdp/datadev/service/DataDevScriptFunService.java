package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevFunDetail;
import com.jd.bdp.datadev.domain.ZtreeNode;

import java.util.List;

public interface DataDevScriptFunService {

    List<ZtreeNode> getFunsByDirId(Long dirId) throws Exception;

    DataDevFunDetail findById(Long id) throws Exception;

    /**
     * 查询所有
     * @param keyword
     * @return
     * @throws Exception
     */
    List<ZtreeNode> getFunsByKeyword(String keyword) throws Exception;


    String getFunTipsByScriptType(String scriptType)throws Exception;
}
