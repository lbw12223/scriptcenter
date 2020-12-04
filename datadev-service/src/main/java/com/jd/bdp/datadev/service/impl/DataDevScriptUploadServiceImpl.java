package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.dao.DataDevScriptFileDao;
import com.jd.bdp.datadev.dao.DataDevScriptUpLoadDao;
import com.jd.bdp.datadev.domain.DataDevScriptUpLoad;
import com.jd.bdp.datadev.enums.DataDevScriptUploadStatusEnum;
import com.jd.bdp.datadev.service.DataDevScriptUploadService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataDevScriptUploadServiceImpl implements DataDevScriptUploadService {

    @Autowired
    DataDevScriptUpLoadDao dataDevScriptUpLoadDao;

    @Autowired
    private DataDevScriptFileDao dataDevScriptFileDao;

    @Override
    public void addDataDevScriptUpLoad(DataDevScriptUpLoad dataDevScriptUpLoad) {
        dataDevScriptUpLoadDao.insertScriptUpLoad(dataDevScriptUpLoad);
    }

    @Override
    public Page<DataDevScriptUpLoad> findByPage(DataDevScriptUpLoad dataDevScriptUpLoad, String startTime, String endTime, Pageable pageable) {
        Long gitProjectId = dataDevScriptUpLoad.getGitProjectId();
        String creator = dataDevScriptUpLoad.getCreator();

        Long total = dataDevScriptUpLoadDao.findAllCount(gitProjectId);
        if(StringUtils.isBlank(startTime)){
            startTime=null;
        }
        if(StringUtils.isBlank(endTime)){
            endTime=null;
        }
        List<DataDevScriptUpLoad> datas = dataDevScriptUpLoadDao.findByPage(gitProjectId, startTime, endTime, creator, pageable.getOffset(), pageable.getPageSize());
        for (DataDevScriptUpLoad dataDevScript : datas) {
            Long id = dataDevScript.getId();//根据id获取一次上传的个数
            int fileCount = dataDevScriptFileDao.findCount(id);
            dataDevScript.setFileCount(fileCount);
        }
        return new PageImpl<>(datas, pageable, total);
    }
}
