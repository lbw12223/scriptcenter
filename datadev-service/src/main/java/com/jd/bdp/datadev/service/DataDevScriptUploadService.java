package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.domain.DataDevScriptUpLoad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataDevScriptUploadService {


    void addDataDevScriptUpLoad(DataDevScriptUpLoad dataDevScriptUpLoad );

    Page<DataDevScriptUpLoad> findByPage(DataDevScriptUpLoad dataDevScriptUpLoad, String startTime, String endTime, Pageable pageable);
}
