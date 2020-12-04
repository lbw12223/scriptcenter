package com.jd.bdp.datadev.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.enums.ScriptJdqMessageEnum;
import com.jd.bdp.datadev.model.ScriptJdqMessage;
import com.jd.bdp.datadev.model.ScriptRunDetail;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/3/20.
 */
public class ScriptJdqMessageUtil {

    private static Logger log = Logger.getLogger(ScriptJdqMessageUtil.class);


    public static final Pattern pattern = Pattern.compile("^(\\d+_\\d+_\\d+_\\d+:).*", Pattern.DOTALL);

    public static final String SPLIT_KEY_VALUE = ":";

    public static final String SPECIL_SPLIT = "##";

    public static void main(String[] args) {

  /*      String keyValue = ":212121055471562\t1055471562 1020639817\t经济法教程(第二版)\t经济法教程(第二版)\t3\t-1\t1\t1\t3000\t3001\t11048\t0\t图书\t0\tPOP1055471562\t1055471562\t2013-11-12 15:01:19.0 2013-11-12\t2013-11-12 15:01:19.0\t2013-12-12 03:54:50.0\tNULL\t0.0\t5000.0\t3000.0\t100.0\t1.5E9\t522000.0\t0\t0\tNULL\t0\t31605\t47813\t北京阳光晋熙图书有限公司\t46023\t阳光书店\t8\t已启用\tbjqhyu\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\tNULL\t2013-12-17";

        ScriptRunDetail scriptRunDetail = new ScriptRunDetail();
        scriptRunDetail.setType(1);
        scriptRunDetail.setId(1l);

        String value = (createStatus2JdqMessage(scriptRunDetail, "1", 1)).getValue();
        System.out.println(value);
        System.out.println(covertStringJdqMessage(value));*/



        ScriptJdqMessage scriptJdqMessage = covertStatusExecCode("1_329410_2_0001:0##35817##jijiayue_20190517102207.sql");
        System.out.println(scriptJdqMessage);
    }


    /**
     * covert data
     *
     * @param scriptRunDetail
     * @param value
     * @param index
     * @return
     */
    public static ScriptJdqMessage createData2JdqMessage(ScriptRunDetail scriptRunDetail, String value, Integer index) {
        ScriptJdqMessageEnum messageEnum = ScriptJdqMessageEnum.DATA;
        String hbaseKey = String.format(messageEnum.toFormat(), scriptRunDetail.getType(), scriptRunDetail.getId(), messageEnum.toCode(), index);

        ScriptJdqMessage result = new ScriptJdqMessage();
        result.setValue(hbaseKey + SPLIT_KEY_VALUE + value);
        result.setMessageIndex(String.valueOf(index));
        result.setType(scriptRunDetail.getType());
        result.setScriptRunDetailId(scriptRunDetail.getId());
        result.setMessageType(messageEnum.toCode());
        result.setHbaseKey(hbaseKey);
        scriptRunDetail.setCurrentDataIndex(index);
        return result;
    }

    /**
     * exeCode
     *
     * @param scriptRunDetail
     * @param value
     * @param index
     * @return
     */
    public static ScriptJdqMessage createResponseCode2JdqMessage(ScriptRunDetail scriptRunDetail, String value, Integer index) {
        ScriptJdqMessageEnum messageEnum = ScriptJdqMessageEnum.EXECODE;
        String hbaseKey = String.format(messageEnum.toFormat(), scriptRunDetail.getType(), scriptRunDetail.getId(), messageEnum.toCode(), index);
        value = value + SPECIL_SPLIT + scriptRunDetail.getGitProjectId() + SPECIL_SPLIT + scriptRunDetail.getGitProjectFilePath();


        ScriptJdqMessage result = new ScriptJdqMessage();
        result.setValue(hbaseKey + SPLIT_KEY_VALUE + value);
        result.setMessageIndex(String.valueOf(index));
        result.setType(scriptRunDetail.getType());
        result.setScriptRunDetailId(scriptRunDetail.getId());
        result.setMessageType(messageEnum.toCode());
        result.setHbaseKey(hbaseKey);
        result.setGitProjectFilePath(scriptRunDetail.getGitProjectFilePath());
        result.setGitProjectId(scriptRunDetail.getGitProjectId());
        scriptRunDetail.setCurrentDataIndex(index);
        return result;
    }

    /**
     * covert statis
     *
     * @param scriptRunDetail
     * @param value
     * @param index
     * @return
     */
    public static ScriptJdqMessage createStatus2JdqMessage(ScriptRunDetail scriptRunDetail, String value, Integer index) {
        ScriptJdqMessageEnum messageEnum = ScriptJdqMessageEnum.STATUS;
        String hbaseKey = String.format(messageEnum.toFormat(), scriptRunDetail.getType(), scriptRunDetail.getId(), messageEnum.toCode(), index);

        value = value + SPECIL_SPLIT + scriptRunDetail.getGitProjectId() + SPECIL_SPLIT + scriptRunDetail.getGitProjectFilePath();

        ScriptJdqMessage result = new ScriptJdqMessage();
        result.setValue(hbaseKey + SPLIT_KEY_VALUE + value);
        result.setGitProjectId(scriptRunDetail.getGitProjectId());
        result.setGitProjectFilePath(scriptRunDetail.getGitProjectFilePath());
        result.setMessageIndex(String.valueOf(index));
        result.setType(scriptRunDetail.getType());
        result.setScriptRunDetailId(scriptRunDetail.getId());
        result.setMessageType(messageEnum.toCode());
        result.setHbaseKey(hbaseKey);
        scriptRunDetail.setCurrentDataIndex(index);
        return result;
    }


    /**
     * covert log
     *
     * @param scriptRunDetail
     * @param value
     * @param index
     * @return
     */
    public static ScriptJdqMessage createLog2JdqMessage(ScriptRunDetail scriptRunDetail, String value, Integer index) {
        ScriptJdqMessageEnum messageEnum = ScriptJdqMessageEnum.LOG;
        String hbaseKey = String.format(messageEnum.toFormat(), scriptRunDetail.getType(), scriptRunDetail.getId(), messageEnum.toCode(), index);
        ScriptJdqMessage result = new ScriptJdqMessage();
        result.setValue(hbaseKey + SPLIT_KEY_VALUE + value);
        result.setMessageIndex(String.valueOf(index));
        result.setType(scriptRunDetail.getType());
        result.setScriptRunDetailId(scriptRunDetail.getId());
        result.setMessageType(messageEnum.toCode());
        result.setHbaseKey(hbaseKey);
        scriptRunDetail.setCurrentLogIndex(index);
        return result;
    }

    /**
     * storm 里面消费的时候调用
     *
     * @param value
     * @return
     */
    public static ScriptJdqMessage covertStringJdqMessage(String value) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            String matchValues = matcher.group(1);
            matchValues = matchValues.substring(0, matchValues.length() - 1);
            String[] keys = matchValues.split("_");

            int type = Integer.parseInt(keys[0]);
            Long scriptRunDetailId = Long.parseLong(keys[1]);
            int messageType = Integer.parseInt(keys[2]);
            String messageIndex = keys[3];
            String messageValue = value.substring(matchValues.length() + SPLIT_KEY_VALUE.length());
            ScriptJdqMessage dataDevJdqMessage = new ScriptJdqMessage();
            dataDevJdqMessage.setValue(messageValue);
            dataDevJdqMessage.setHbaseKey(matchValues);
            dataDevJdqMessage.setMessageType(messageType);
            dataDevJdqMessage.setScriptRunDetailId(scriptRunDetailId);
            dataDevJdqMessage.setMessageIndex(messageIndex);
            dataDevJdqMessage.setType(type);
            if (messageType == ScriptJdqMessageEnum.EXECODE.toCode()
                    || messageType == ScriptJdqMessageEnum.STATUS.toCode()) {

                String arrays[] = messageValue.split(SPECIL_SPLIT);
                if (arrays.length == 3) {
                    dataDevJdqMessage.setGitProjectId(Long.parseLong(arrays[1]));
                    dataDevJdqMessage.setGitProjectFilePath(arrays[2]);
                    dataDevJdqMessage.setValue(arrays[0]);
                }
            }

            return dataDevJdqMessage;
        }
        return null;
    }

    //3_280418_3_0001:0-||-59159-||-py_test_20190507_1.py
    public static ScriptJdqMessage covertStatusExecCode(String value) {
        try {
            ScriptJdqMessage scriptJdqMessage = new ScriptJdqMessage();
            String[] keyValue = value.split(SPLIT_KEY_VALUE);
            if (keyValue != null && keyValue.length == 2) {
                String key = keyValue[0];
                scriptJdqMessage.setType(Integer.parseInt(key.split("_")[0]));
                scriptJdqMessage.setScriptRunDetailId(Long.parseLong(key.split("_")[1]));
                scriptJdqMessage.setMessageType(Integer.parseInt(key.split("_")[2]));

                String values = keyValue[1];
                String[] valueArrays = values.split(SPECIL_SPLIT);
                scriptJdqMessage.setValue(valueArrays[0]);
                scriptJdqMessage.setGitProjectId(Long.parseLong(valueArrays[1]));
                scriptJdqMessage.setGitProjectFilePath(valueArrays[2]);
                return scriptJdqMessage;
            }
        } catch (Exception e) {
            log.error("解析失败:" + value, e);
        }
        return null;
    }
}
