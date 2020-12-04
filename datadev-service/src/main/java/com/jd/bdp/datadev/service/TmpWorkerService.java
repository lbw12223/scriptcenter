package com.jd.bdp.datadev.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class TmpWorkerService {
    private static final Logger logger = Logger.getLogger(TmpWorkerService.class);

    @Autowired
    private DataDevScriptFileService fileService;

    public void execute() {
        try {
            fileService.deleteTmpFile();
        } catch (Exception e) {
            logger.error("===============worker error:"+e.getMessage());
        }
    }
}
