package com.jd.bdp.datadev.enums;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhanglei68 on 2017/10/31.
 */
public enum DataDevScriptTypeEnum {
    SQL(1, "Sql", ".sql", true, true),
    Shell(2, "Shell", ".sh", true, true),
    Python2(3, "Python", ".py", true, true),
    Zip(4, "Zip", ".zip", false, true),
    Java(6, "Java", ".java", true, false),
    Txt(7, "Txt", ".txt", true, false,1*1024*1024L*2),
    Xml(8, "Xml", ".xml", true, false,1*1024*1024L*2),
    Html(9, "Html", ".html", true, false),
    Csv(10, "Csv", ".csv", true, false,1*1024*1024L*2),
    Jar(11, "Jar", ".jar", false, false),
    Groovy(12, "Groovy", ".groovy", true, false),
    Javscript(13, "Javascript", ".js", true, false),
    Css(14, "Css", ".css", true, false),
    Prop(15, "Prop", ".properties", true, false,1*1024*1024L*2),
    CONF(16, "Conf", ".conf", true, false,1*1024*1024L*2),
    INI(17, "Ini", ".ini", true, false,1*1024*1024L*2),
    YAML(18, "YAML", ".yaml", true, false,1*1024*1024L*2),
    CFG(19, "Cfg", ".cfg", true, false,1*1024*1024L*2),
    Json(20, "Json", ".json", true, false,1*1024*1024L*2),
    Other(5, "Other", "", false, false);
    private Integer code;
    private String name;
    private String suffix;
    private Boolean canEdit;
    private Boolean canRun;
    private Long limitSize; //B
    DataDevScriptTypeEnum(Integer code, String name, String suffix, Boolean canEdit, Boolean canRun) {
        this(code,name,suffix,canEdit,canRun,null);
    }
    DataDevScriptTypeEnum(Integer code, String name, String suffix, Boolean canEdit, Boolean canRun,Long limitSize) {
        this.code = code;
        this.name = name;
        this.suffix = suffix;
        this.canEdit = canEdit;
        this.canRun = canRun;
        this.limitSize = limitSize;
    }

    /**
     * 获取枚举类型数值编码
     */
    public Integer toCode() {
        return this.code == null ? this.ordinal() : this.code;
    }

    /**
     * 获取枚举类型英文编码名称
     */
    public String toName() {
        return this.name == null ? this.name() : this.name;
    }

    /**
     * 返回后缀名
     *
     * @return
     */
    public String toSuffix() {
        return this.suffix;
    }

    public static DataDevScriptTypeEnum enumValueOf(Integer code) {
        DataDevScriptTypeEnum[] enums = DataDevScriptTypeEnum.values();
        for (DataDevScriptTypeEnum constantEnum : enums) {
            if (constantEnum.toCode().equals(code)) {
                return constantEnum;
            }
        }
        return null;
    }

    public static String suffixOf(Integer code) {
        DataDevScriptTypeEnum[] scriptTypeEnums = DataDevScriptTypeEnum.values();
        for (DataDevScriptTypeEnum dataDevScriptTypeEnum : scriptTypeEnums) {
            if (dataDevScriptTypeEnum.toCode() == code) {
                return dataDevScriptTypeEnum.toSuffix();
            }
        }
        return null;
    }
    public Long toLimitSize(){
        return this.limitSize;
    }

    /**
     * 根据fileName获取类型
     *
     * @param fileName
     * @return
     */
    public static DataDevScriptTypeEnum getFileNameScriptType(String fileName) {
        String pattern = "(\\.\\w+)$";
        Pattern suffixPa = Pattern.compile(pattern);
        Matcher m = suffixPa.matcher(fileName);
        if (m.find() && StringUtils.isNotBlank(m.group(1))) {
            String suffix = m.group(1);
            for (DataDevScriptTypeEnum typeEnum : DataDevScriptTypeEnum.values()) {
                if (typeEnum.toSuffix().equals(suffix)) {
                    return typeEnum;
                }
            }
        }
        return DataDevScriptTypeEnum.Other;
    }

    public static String getFullName(String name, Integer typeCode) {
        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(typeCode);
        if (StringUtils.isBlank(name) || typeEnum == null || typeEnum.toCode().equals(DataDevScriptTypeEnum.Other.toCode())) {
            return name;
        }
        String suffix = typeEnum.toSuffix();
        name = name.endsWith(suffix) ? name : name + suffix;
        return name;
    }

    public static boolean canEdit(int type) {
        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(type);
        return typeEnum.canEdit;
    }

    public static boolean canRun(int type) {
        DataDevScriptTypeEnum typeEnum = DataDevScriptTypeEnum.enumValueOf(type);
        return typeEnum.canRun;
    }

    public static String getNoSuffixName(String fileName) throws Exception {
        String pattern = "(\\.\\w*)$";
        Pattern suffixPa = Pattern.compile(pattern);
        Matcher m = suffixPa.matcher(fileName);
        if (m.find() && StringUtils.isNotBlank(m.group(1))) {
            int start = m.start();
            return fileName.substring(0, start);
        }
        return fileName;
    }

    public static Map<Integer, String> toCodeNameMap() {
        Map<Integer, String> result = new HashMap<Integer, String>();
        DataDevScriptTypeEnum[] scriptTypeEnums = DataDevScriptTypeEnum.values();
        for (DataDevScriptTypeEnum dataDevScriptTypeEnum : scriptTypeEnums) {
            result.put(dataDevScriptTypeEnum.code, dataDevScriptTypeEnum.name);
        }
        return result;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }



}
