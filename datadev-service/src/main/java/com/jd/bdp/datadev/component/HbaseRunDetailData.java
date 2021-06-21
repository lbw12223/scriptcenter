package com.jd.bdp.datadev.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.PageResultDTO;
import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;
import com.jd.bdp.datadev.enums.DataDevScriptRunDetailTypeEnum;
import com.jd.bdp.datadev.enums.ScriptJdqMessageEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by zhangrui25 on 2018/3/2.
 */
@Component
public class HbaseRunDetailData {


    private static final Logger logger = Logger.getLogger(HbaseRunDetailData.class);


    @Autowired
    private HbaseServiceLogData hbaseService;

    @Value("${bdp.hbase.table.hbaseDatadevData}")
    private String hbaseTableName; //= "bdp:dataDevData";

    private byte[] familySysNameBytes = Bytes.toBytes("data");

    private byte[] qualifierBytes = Bytes.toBytes("data");

    private static String getKey(DataDevScriptRunDetail dataDevScriptRunDetail, int index) {
        ScriptJdqMessageEnum messageEnum = ScriptJdqMessageEnum.DATA;
        String key = String.format(messageEnum.toFormat(), dataDevScriptRunDetail.getType(), dataDevScriptRunDetail.getId(), messageEnum.toCode(), index);
        return key;
    }

    /**
     * {name: '0' , label:'ces'}
     * {name: '1' , label:'ces1'}
     *
     * @param dataDevScriptRunDetail
     * @return
     * @throws Exception
     */
    public Map<String, String> getRunDetailDataTableTitle(DataDevScriptRunDetail dataDevScriptRunDetail) {
        Map<String, String> title = new HashMap<String, String>();
        try {
            if (dataDevScriptRunDetail.getType().equals(DataDevScriptRunDetailTypeEnum.SQL.toCode())) {
                /*Sql 按照 \t 分隔*/
                String titleKey = getKey(dataDevScriptRunDetail, 1);
                List<String> titleArrays = query(titleKey, titleKey);
                if (titleArrays.size() > 0) {
                    logger.error("==========================title==========" + titleArrays.get(0));
                    String[] titleA = titleArrays.get(0).split("(\\t)");
                    for (int index = 0; index < titleA.length; index++) {
                        title.put(String.valueOf(index), titleA[index]);
                    }
                    return title;
                }
            }
            title.put("0", "执行结果");
        } catch (Exception e) {
            logger.error(e);
        }
        return title;
    }

    /**
     * 查询数据
     *
     * @param dataDevScriptRunDetail
     * @param page
     * @param rows
     * @return
     * @throws Exception
     */
    public PageResultDTO getRunDetailData(DataDevScriptRunDetail dataDevScriptRunDetail, Integer page, Integer rows) throws Exception {
        PageResultDTO resultDTO = new PageResultDTO();
        resultDTO.setPage(page);
        resultDTO.setLimit(rows);
        Pageable pageable = new PageRequest(page - 1, rows);
        List<String> dataRows = new ArrayList<String>();
        if (dataDevScriptRunDetail.getDataCount() > 0) {
            int start = pageable.getOffset() + 1;
            int end = start + pageable.getPageSize();
            //sql 的查询多了title在hbase里面
            String queryStartKey = getKey(dataDevScriptRunDetail, start + 1);
            String queryEndKey = getKey(dataDevScriptRunDetail, end + 1);
            long dataCount = (long) dataDevScriptRunDetail.getDataCount();
            if (dataCount > 0) {
                if (dataDevScriptRunDetail.getType().equals(DataDevScriptRunDetailTypeEnum.SQL.toCode())) {
                    dataRows.addAll(query(queryStartKey, queryEndKey));
                    dataCount -= 1;
                } else {
                    dataRows.addAll(query(queryStartKey, queryEndKey));
                }
                resultDTO.setRows(fixRunDetailData(dataRows, dataDevScriptRunDetail));
                resultDTO.setRecords(dataCount);
                resultDTO.setSuccess(true);
            }
        }
        return resultDTO;
    }

    public PageResultDTO getRunDetailTitle(DataDevScriptRunDetail dataDevScriptRunDetail) throws Exception {
        PageResultDTO resultDTO = new PageResultDTO();
        List<String> dataRows = new ArrayList<String>();
        //只有SQL才有运行结果
        String queryStartKey = getKey(dataDevScriptRunDetail, 1);
        String queryEndKey = getKey(dataDevScriptRunDetail, 1);
        if (dataDevScriptRunDetail.getType().equals(DataDevScriptRunDetailTypeEnum.SQL.toCode())) {
            dataRows.addAll(query(queryStartKey, queryEndKey));
            resultDTO.setRecords(1L);
        } else {
            resultDTO.setRecords(0L);
        }
        if (dataRows.size() > 0) {
            resultDTO.setRows(fixRunDetailData(dataRows, dataDevScriptRunDetail));
        }
        resultDTO.setSuccess(true);

        logger.error("getRunDetailTitle" + JSONObject.toJSONString(resultDTO));
        return resultDTO;
    }

    /**
     * 目前只有sql的才有结果
     *
     * @param dataDevScriptRunDetail
     * @throws Exception
     */
    public List<JSONObject> getAllDataDevScriptRunDetailData(DataDevScriptRunDetail dataDevScriptRunDetail) throws Exception {
        List<String> dataRows = new ArrayList<String>();
        String queryEndKey = getKey(dataDevScriptRunDetail, dataDevScriptRunDetail.getDataCount()) + 1;
        if (dataDevScriptRunDetail.getType().equals(DataDevScriptRunDetailTypeEnum.SQL.toCode())) {
            String queryStartKey = getKey(dataDevScriptRunDetail, 2);
            dataRows.addAll(query(queryStartKey, queryEndKey));
        } else {
            String queryStartKey = getKey(dataDevScriptRunDetail, 1);
            dataRows.addAll(query(queryStartKey, queryEndKey));
        }
        return fixRunDetailData(dataRows, dataDevScriptRunDetail);
    }

    /**
     * 查看当前任务的结果是否已经保存在Hbase中
     *
     * @param dataDevScriptRunDetail
     * @return
     */
    public boolean hasDataDevScriptRunDetailDataFinish(DataDevScriptRunDetail dataDevScriptRunDetail) {
        try {
            String queryStartKey = getKey(dataDevScriptRunDetail, dataDevScriptRunDetail.getDataCount()-1);
            String queryEndKey = getKey(dataDevScriptRunDetail, dataDevScriptRunDetail.getDataCount()-1);
            List<String> list = query(queryStartKey, queryEndKey);
            logger.info("============hasDataDevScriptRunDetailDataFinish=====" + queryStartKey);
            logger.info("============hasDataDevScriptRunDetailDataFinishsize====" + list.size() + "=====");

            return list != null && list.size() > 0;
        } catch (Exception e) {

        }
        return false;
    }

    public List<List<String>> getDataList(DataDevScriptRunDetail dataDevScriptRunDetail) throws Exception {
        List<List<String>> dataList = new ArrayList<List<String>>();
        if (dataDevScriptRunDetail.getDataCount() > 0) {
            int start = 2;
            int end = dataDevScriptRunDetail.getDataCount() + 1;
            String queryStartKey = getKey(dataDevScriptRunDetail, start);
            String queryEndKey = getKey(dataDevScriptRunDetail, end);
            List<String> data = query(queryStartKey, queryEndKey);
            for (String str : data) {
                String[] dataArr = str.split("(\\t)");
                List<String> dataRow = Arrays.asList(dataArr);
                dataList.add(dataRow);
            }
        }
        return dataList;
    }

    /**
     * 页面Jqgrid显示
     * {0:"value",1:"value1}
     *
     * @param rows
     * @param dataDevScriptRunDetail
     * @return
     */
    private List<JSONObject> fixRunDetailData(List<String> rows, DataDevScriptRunDetail dataDevScriptRunDetail) {
        List<JSONObject> rowArrays = new ArrayList<JSONObject>();
        for (String row : rows) {
            JSONObject temp = new JSONObject();
            try {
                if (dataDevScriptRunDetail.getType().equals(DataDevScriptRunDetailTypeEnum.SQL.toCode())) {
                    String[] sqlData = row.split("(\\t)");
                    for (int index = 0; index < sqlData.length; index++) {
                        temp.put("" + index, sqlData[index]);
                    }
                } else {
                    temp.put("0", row);
                }
            } catch (Exception e) {
                logger.error(e);
            }
            rowArrays.add(temp);
        }
        return rowArrays;
    }

    private List<String> query(String startKey, String endKey) throws Exception {
        ArrayList<String> resultArrayList = new ArrayList<String>();

        Table table = hbaseService.getConnection().getTable(TableName.valueOf(hbaseTableName));
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startKey));
        scan.setStopRow(Bytes.toBytes(endKey));
        ResultScanner resultScanner = table.getScanner(scan);
        Result result = resultScanner.next();
        while (result != null) {
            byte[] datas = result.getValue(familySysNameBytes, qualifierBytes);
            if (datas != null) {
                String strData = new String(datas, "utf-8");
                resultArrayList.add(strData);
            }
            result = resultScanner.next();
        }
        return resultArrayList;
    }

    public static void main(String[] args) {
        String[] arr = new String[]{"dfsf", "fddff", "dfsde"};
        System.out.println(Arrays.asList(arr).toString());
    }

}
