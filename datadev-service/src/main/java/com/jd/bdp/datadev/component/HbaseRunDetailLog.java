package com.jd.bdp.datadev.component;

import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.dao.DataDevScriptRunDetailDao;
import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.enums.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangrui25 on 2018/3/2.
 */
@Component
public class HbaseRunDetailLog {


    private Log logger = LogFactory.getLog(HbaseRunDetailLog.class);
    @Autowired
    private DataDevScriptRunDetailDao runDetailDao;

    @Autowired
    private HbaseService hbaseService;

    @Value("${bdp.hbase.table.hbaseDatadevLog}")
    private String hbaseTableName ; // = "bdp:dataDevLog";

    private byte[] familySysNameBytes = Bytes.toBytes("log");

    private byte[] qualifierBytes = Bytes.toBytes("log");

    public static String getKey(DataDevScriptRunDetail dataDevScriptRunDetail, int index) {
        ScriptJdqMessageEnum messageEnum = ScriptJdqMessageEnum.LOG;
        String key = String.format(messageEnum.toFormat(), dataDevScriptRunDetail.getType(), dataDevScriptRunDetail.getId(), messageEnum.toCode(), index);
        return key;
    }


    public PageResultDTO getRunDetailLog(DataDevScriptRunDetail dataDevScriptRunDetail, Integer page, Integer rows) throws Exception {
        PageResultDTO resultDTO = new PageResultDTO();
        resultDTO.setPage(page);
        resultDTO.setLimit(rows);
        Pageable pageable = new PageRequest(page - 1, rows);
        List<String> result = new ArrayList<String>();
        if (dataDevScriptRunDetail.getLogCount() > 0) {
            int start = pageable.getOffset() + 1;
            int end = start + pageable.getPageSize();
            String queryStartKey = getKey(dataDevScriptRunDetail, start);
            String queryEndKey = getKey(dataDevScriptRunDetail, end);
            if (dataDevScriptRunDetail.getLogCount() > 0) {
                result.addAll(query(queryStartKey, queryEndKey).b);
            }
        }
        resultDTO.setRows(result);
        resultDTO.setRecords((long) dataDevScriptRunDetail.getLogCount());
        resultDTO.setSuccess(true);
        return resultDTO;
    }

    /**
     * 查询日志<isFinsih,List<String>>
     *
     * @param queryStartKey
     * @param queryEndKey
     * @return
     * @throws Exception
     */
    private HoldDoubleValue<HoldDoubleValue<Boolean,Boolean>, List<String>> queryRuntime(String queryStartKey, String queryEndKey, int start) throws Exception {
        ArrayList<String> resultArrayList = new ArrayList<String>();
        boolean isLastLog = false;
        boolean isErrorLog = false;
        Integer loopIndex = start;

        try {
            Table table = hbaseService.getConnection().getTable(TableName.valueOf(hbaseTableName));
            Scan scan = new Scan();
            scan.setStartRow(Bytes.toBytes(queryStartKey));
            scan.setStopRow(Bytes.toBytes(queryEndKey));
            ResultScanner resultScanner = table.getScanner(scan);
            Result result = resultScanner.next();

            //取连续的log，不连续则中断
            while (result != null) {
                String rowKey = Bytes.toString(result.getRow());
                Integer index = Integer.parseInt(rowKey.substring(rowKey.lastIndexOf("_") + 1));

                if(!index.equals(loopIndex)) {
                    break;
                }

                byte[] datas = result.getValue(familySysNameBytes, qualifierBytes);
                if (datas != null) {
                    String strData = new String(datas, "utf-8");
                    if (strData.indexOf("datadev--请选择或配置账号队列。--datadev") != -1) {
                        strData=strData.replaceAll("datadev--|--datadev", "");
                        isErrorLog = true;
                    }
                    resultArrayList.add(strData);
                }
                result = resultScanner.next();
                loopIndex++;
            }
        } catch (Exception e) {
            throw e;
        }
        return new HoldDoubleValue<HoldDoubleValue<Boolean, Boolean>, List<String>>(new HoldDoubleValue<Boolean, Boolean>(isLastLog,isErrorLog), resultArrayList);
    }

    /**
     * 查询日志<isFinsih,List<String>>
     *
     * @param queryStartKey
     * @param queryEndKey
     * @return
     * @throws Exception
     */
    public HoldDoubleValue<HoldDoubleValue<Boolean,Boolean>, List<String>> query(String queryStartKey, String queryEndKey) throws Exception {
        ArrayList<String> resultArrayList = new ArrayList<String>();
        boolean isLastLog = false;
        boolean isErrorLog = false;
        try {
            Table table = hbaseService.getConnection().getTable(TableName.valueOf(hbaseTableName));
            Scan scan = new Scan();
            scan.setStartRow(Bytes.toBytes(queryStartKey));
            scan.setStopRow(Bytes.toBytes(queryEndKey));
            ResultScanner resultScanner = table.getScanner(scan);
            Result result = resultScanner.next();
            while (result != null) {
                byte[] datas = result.getValue(familySysNameBytes, qualifierBytes);
                if (datas != null) {
                    String strData = new String(datas, "utf-8");
                    if (strData.indexOf("datadev--请选择或配置账号队列。--datadev") != -1) {
                        strData=strData.replaceAll("datadev--|--datadev", "");
                        isErrorLog = true;
                    }
                    resultArrayList.add(strData);
                }
                result = resultScanner.next();
            }
        } catch (Exception e) {
            throw e;
        }
        return new HoldDoubleValue<HoldDoubleValue<Boolean, Boolean>, List<String>>(new HoldDoubleValue<Boolean, Boolean>(isLastLog,isErrorLog), resultArrayList);
    }

    /**
     * 查询Log日志
     *
     * @param dataDevScriptRunDetail
     * @return
     */
    public HoldDoubleValue<HoldDoubleValue<Boolean,Boolean>, List<String>> getRunTimeRunDetailLog(DataDevScriptRunDetail dataDevScriptRunDetail) {
        boolean isLastLog = false;
        int logPageSize = 200;
        if (dataDevScriptRunDetail.getCurrentLogIndex() == null || dataDevScriptRunDetail.getCurrentLogIndex() < 1) {
            dataDevScriptRunDetail.setCurrentLogIndex(1);
        }
        DataDevScriptRunDetail tmp = runDetailDao.findById(dataDevScriptRunDetail.getId());
        if(DataDevRunTypeEnum.DependencyRun.tocode().equals(tmp.getRunType())){
            dataDevScriptRunDetail.setType(DataDevScriptTypeEnum.Zip.toCode());
        }
        String hbaseStartKey = getKey(dataDevScriptRunDetail, dataDevScriptRunDetail.getCurrentLogIndex());
        String hbaseEndKey = getKey(dataDevScriptRunDetail, dataDevScriptRunDetail.getCurrentLogIndex() + logPageSize);
        ArrayList<String> resultArrayList = new ArrayList<String>();
        HoldDoubleValue<HoldDoubleValue<Boolean,Boolean>, List<String>> data = new HoldDoubleValue<HoldDoubleValue<Boolean, Boolean>, List<String>>(new HoldDoubleValue<Boolean, Boolean>(false,false),new ArrayList<String>()) ;
        try {
            data = queryRuntime(hbaseStartKey, hbaseEndKey, dataDevScriptRunDetail.getCurrentLogIndex());
            resultArrayList.addAll(data.b);
        } catch (Exception e) {
            logger.error("读取HbaseBase报错", e);
        }
        /*有LogCount说明脚本已经执行完成，状态已经修改*/
        if (dataDevScriptRunDetail.getLogCount() != null &&
                dataDevScriptRunDetail.getLogCount() <= resultArrayList.size() + dataDevScriptRunDetail.getCurrentLogIndex() - 1) {
            isLastLog = true;
        }
        return new HoldDoubleValue<HoldDoubleValue<Boolean,Boolean>, List<String>>(new HoldDoubleValue<Boolean, Boolean>(isLastLog,data.a.b), resultArrayList);
    }
}
