package com.jd.bdp.datadev.enums;

import org.apache.commons.lang.StringUtils;
import sun.rmi.runtime.Log;

/**
 * @Author wangxiaoyu12
 * @Date 2018/8/21 14:46
 * @Description
 **/

public enum DataDevScriptEngineTypeEnum {

    Hive(Integer.valueOf(1), "jd-hive", "Hive-引擎", "ENGINE_HIVE"),
    Presto(Integer.valueOf(2), "presto", "Presto-高速引擎", "ENGINE_PRESTO"),
    Spark(Integer.valueOf(3), "spark", "Spark-分析引擎", "ENGINE_SPARK"),
    Impala(Integer.valueOf(4), "impala", "Impala-引擎", "ENGINE_IMPALA");

    private Integer code;
    private String value;
    private String name;
    private String name2;
    private DataDevScriptEngineTypeEnum(Integer code, String value, String name, String name2) {
        this.code = code;
        this.name = name;
        this.value = value;
        this.name2 = name2;
    }

    public static DataDevScriptEngineTypeEnum enumValueOf(String value) {
        if (StringUtils.isNotBlank(value)) {
            DataDevScriptEngineTypeEnum[] engineTypeEnums = DataDevScriptEngineTypeEnum.values();
            for (DataDevScriptEngineTypeEnum engineTypeEnum : engineTypeEnums) {
                if (engineTypeEnum.getValue().equals(value)) {
                    return engineTypeEnum;
                }
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }
}