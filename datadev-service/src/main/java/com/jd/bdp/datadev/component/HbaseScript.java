package com.jd.bdp.datadev.component;

import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.exception.HbaseUploadException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/3/2.
 */
@Component
public class HbaseScript {

    @Autowired
    private HbaseService hbaseService;

    private Log logger = LogFactory.getLog(HbaseScript.class);


    @Value("${bdp.hbase.table.hbaseHisTableName}")
    private String hbaseHisTableName ; //= "bdp:dataDevScriptHis";

    @Value("${bdp.hbase.table.hbaseDirZipTableName}")
    private String hbaseDirZipTableName ; //= "bdp:dataDevScript";

    private byte[] familySysNameBytes = Bytes.toBytes("script");

    private byte[] qualifierBytes = Bytes.toBytes("script");
    //下文件时key的间隔
    private Integer keynums = 50;
    private static String HBASE_PRE_KEY = "data_script_";
    private static String UPLINE_PRE_KEY = "data_upline_";
    private static String RUN_PRE_KEY = "data_run_";

    private static String His_ROW_KEY_FORMATE = "%s_%s_%04d"; //data_script_scriptId_version_index

    @Value("${datadev.env}")
    private String env;

    /**
     * 上传脚本到Hbase
     *
     * @param scriptFile
     */
    private void upScriptToHbase(DataDevScriptFile scriptFile, String tableName) {
        try {
            if (scriptFile.getId() == null) {
                throw new RuntimeException("脚本id不能为空");
            }
            if (StringUtils.isBlank(scriptFile.getVersion())) {
                throw new RuntimeException("脚本历史记录需要版本号");
            }
            List<byte[]> dataBytes = null;
            if (scriptFile.getBytes() != null && scriptFile.getBytes().length > 0) {
                dataBytes = SplitFileUtils.splitBytes(scriptFile.getBytes());
            } else if (scriptFile.getContentFile() != null && scriptFile.getContentFile().exists()) {
                dataBytes = SplitFileUtils.spitFile(scriptFile.getContentFile());
            } else if (StringUtils.isNotEmpty(scriptFile.getContent())) {
                dataBytes = SplitFileUtils.splitContent(scriptFile.getContent());
            }
            if (dataBytes == null) {
                return;
            }
            Table table = hbaseService.getConnection().getTable(TableName.valueOf(tableName));
            List<Put> putList = new LinkedList<Put>();
            for (int index = 0; index < dataBytes.size(); index++) {
                Put put = new Put(Bytes.toBytes(getKey(scriptFile.getHbasePreKey(), scriptFile.getId(), (long) index, scriptFile.getVersion())));
                put.addColumn(familySysNameBytes, qualifierBytes, dataBytes.get(index));
                putList.add(put);
            }
            table.put(putList);
        } catch (Exception e) {
            logger.error("写入HbaseBase报错", e);
            throw new HbaseUploadException("写入HbaseBase报错", e);
        }
    }

    /**
     * 上传脚本到Hbase
     *
     * @param scriptFile
     */
    public void upScriptToHbase(DataDevScriptFile scriptFile) {
        scriptFile.setHbasePreKey(HBASE_PRE_KEY);
        upScriptToHbase(scriptFile, hbaseHisTableName);
    }

    /**
     * 上传脚本到Hbase
     *
     * @param scriptFile
     */
    private void upDirZipToHbase(DataDevScriptFile scriptFile) {
        scriptFile.setVersion("1000");
        upScriptToHbase(scriptFile, hbaseDirZipTableName);
    }

    /**
     * 上传脚本到Hbase
     *
     * @param scriptFile
     */
    public void upDirZipToRunHbase(DataDevScriptFile scriptFile) {
        scriptFile.setHbasePreKey(RUN_PRE_KEY);
        scriptFile.setId(scriptFile.getRunDetailId());
        upDirZipToHbase(scriptFile);
    }

    /**
     * 上传脚本到Hbase
     *
     * @param scriptFile
     */
    public void upDirZipToUplineHbase(DataDevScriptFile scriptFile) {
        scriptFile.setHbasePreKey(UPLINE_PRE_KEY);
        scriptFile.setId(scriptFile.getPublishId());
        upDirZipToHbase(scriptFile);
    }

    /**
     * getScriptFile
     *
     * @param
     */
    private List<byte[]> getScriptFile(String preKey, String version, Long id) throws Exception {

        try {


//            if (scriptFile.getSize() <= 0) {
//                throw new RuntimeException("scriptFile 大小为0");
//            }
            List<byte[]> bytes = new ArrayList<byte[]>();
            logger.error("==================================================================1");
            if (StringUtils.isBlank(version)) {
                return bytes;
            }
            logger.error("==================================================================2");
            String tableName = hbaseHisTableName;
            Table table = hbaseService.getConnection().getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            Long startIndex = 0L;
            while (true) {
                logger.error("=====================================================startkey:" + getKey(preKey, id, startIndex, version));
                logger.error("=====================================================endkey:" + getKey(preKey, id, startIndex + keynums, version));

                scan.setStartRow(Bytes.toBytes(getKey(preKey, id, startIndex, version)));
                scan.setStopRow(Bytes.toBytes(getKey(preKey, id, startIndex + keynums, version)));
                ResultScanner resultScanner = table.getScanner(scan);
                Result result = resultScanner.next();
                if (result == null) {
                    break;
                }
                while (result != null) {
                    byte[] datas = result.getValue(familySysNameBytes, qualifierBytes);
                    if (datas != null) {
                        bytes.add(datas);
                    }
                    result = resultScanner.next();
                }
                startIndex += keynums;
            }
            return bytes;
        } catch (Exception e) {
            logger.error("Script文件获取失败", e);
            throw new RuntimeException("Script文件获取失败:" + e.getMessage(), e);
        }
    }


    /**
     * 获取可编辑文件内容
     *
     * @param file
     * @return
     * @throws Exception
     */
    public String getScriptContent(DataDevScriptFile file) throws Exception {
        String res = "";
        byte[] result = getScriptBytes(file);
        if (result != null && result.length > 0) {
            res = new String(result, "utf-8");
        }
        return res;
    }

    private byte[] getScriptBytes(String preKey, Long id, String version) throws Exception {
        byte[] result = new byte[0];
        if (StringUtils.isNotBlank(preKey) && StringUtils.isNotBlank(version) && id != null) {
            List<byte[]> list = getScriptFile(preKey, version, id);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            for (byte[] bytes : list) {
                stream.write(bytes);
            }
            result = stream.toByteArray();
        }
        return result;
    }

    public byte[] getScriptBytes(DataDevScriptFile file) throws Exception {
        return getScriptBytes(HBASE_PRE_KEY, file.getId(), file.getVersion());
    }

    private static String getKey(String preKey, Long id, Long index, String version) {
        return String.format(preKey + His_ROW_KEY_FORMATE, id, version, index);
    }

    public static void main(String[] args) {
        DataDevScriptFile dataDevScriptFile = new DataDevScriptFile();
        dataDevScriptFile.setId(13L);
        dataDevScriptFile.setSize(5L);

    }

}
