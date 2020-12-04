package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.component.HbaseRunDetailData;
import com.jd.bdp.datadev.datapreview.api.DataPreviewJsfInterface;
import com.jd.bdp.datadev.datapreview.domain.DataDevDataPreview;
import com.jd.bdp.datadev.datapreview.domain.DataPreViewApiResult;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.service.DataDevDataPreviewService;
import com.jd.bdp.datadev.service.DataDevScriptRunDetailService;
import com.jd.bdp.domain.urm.right.ApiResultDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ide  表数据预览 暴露接口
 * @author lifangli
 * @date 2019-08-01
 */
@Service
public class DataPreviewJsfInterfaceImpl implements DataPreviewJsfInterface {
    private static final Logger logger = Logger.getLogger(DataPreviewJsfInterfaceImpl.class);
    @Autowired
    private DataDevDataPreviewService dataPreviewService;
    @Autowired
    private DataDevScriptRunDetailService dataDevScriptRunDetailService;
    @Autowired
    private HbaseRunDetailData hbaseRunDetailData;

    @Override
    public DataPreViewApiResult doDataPreview(String clusterCode, String marketCode, String dbName, String tableName , String erp) {
        DataPreViewApiResult apiResultDTO = new DataPreViewApiResult();
        try{
            DataDevDataPreview preview = dataPreviewService.doDataPreview(clusterCode, marketCode, dbName, tableName,erp);
            apiResultDTO.setSuccess(true);
            apiResultDTO.setCode(0);
            apiResultDTO.setObj(preview);
        }catch (Exception e){
            apiResultDTO.setSuccess(false);
            apiResultDTO.setCode(1);
            logger.error("DataPreviewJsfInterfaceImpl doDataPreview 异常！",e);
        }
        return apiResultDTO;
    }

    @Override
    public DataPreViewApiResult validData(Long runDetailId) {
        DataPreViewApiResult apiResultDTO = new DataPreViewApiResult();
        try{
            HoldDoubleValue<Boolean,Integer> holdDoubleValue = dataPreviewService.validData(runDetailId);
            apiResultDTO.setSuccess(true);
            apiResultDTO.setCode(0);
            apiResultDTO.setObj(holdDoubleValue);
        }catch (Exception e){
            apiResultDTO.setSuccess(false);
            apiResultDTO.setCode(1);
            logger.error("DataPreviewJsfInterfaceImpl validData 异常！",e);
        }
        return apiResultDTO;
    }

    @Override
    public DataPreViewApiResult getDataTitle(Long runDetailId) {
        DataPreViewApiResult apiResultDTO = new DataPreViewApiResult();
        try{
            DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
            Map<String, String> map = hbaseRunDetailData.getRunDetailDataTableTitle(dataDevScriptRunDetail);
            apiResultDTO.setSuccess(true);
            apiResultDTO.setCode(0);
            apiResultDTO.setObj(map);
        }catch (Exception e){
            apiResultDTO.setSuccess(false);
            apiResultDTO.setCode(1);
            logger.error("DataPreviewJsfInterfaceImpl getDataTitle 异常！",e);
        }
        return apiResultDTO;
    }

    @Override
    public DataPreViewApiResult runLogData(Integer page, Integer rows, Long runDetailId) {
        DataPreViewApiResult apiResultDTO = new DataPreViewApiResult();
        try{
            DataDevScriptRunDetail dataDevScriptRunDetail = dataDevScriptRunDetailService.findById(runDetailId);
            PageResultDTO pageResultDTO = hbaseRunDetailData.getRunDetailData(dataDevScriptRunDetail, page, rows);
            apiResultDTO.setSuccess(true);
            apiResultDTO.setCode(0);
            apiResultDTO.setObj(pageResultDTO);
        }catch (Exception e){
            apiResultDTO.setSuccess(false);
            apiResultDTO.setCode(1);
            logger.error("DataPreviewJsfInterfaceImpl getDataTitle 异常！",e);
        }
        return apiResultDTO;
    }
}
