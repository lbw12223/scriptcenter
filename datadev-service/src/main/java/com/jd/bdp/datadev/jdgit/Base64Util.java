package com.jd.bdp.datadev.jdgit;

import com.jd.bdp.datadev.domain.HoldDoubleValue;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/5/31.
 */
public class Base64Util {
    private static Pattern pattern = Pattern.compile("^[ /]*(.*?)[ /]*$");

    /**
     * base 64 to String
     *
     * @param content
     * @return
     * @throws Exception
     */
    public static String base64ToString(String content) throws Exception {
        Base64 b64 = new Base64();
        byte[] buffer = b64.decode(content);
        return new String(buffer, "utf-8");
    }

    /**
     * String to base64
     *
     * @param buffer
     * @return
     * @throws Exception
     */
    public static String StringToBase64(byte[] buffer) throws Exception {
        Base64 b64 = new Base64();
        return b64.encodeToString(buffer);
    }

    /**
     * 分隔filePath
     * app/foo/a.java
     * <p>
     * return app/foo a.java
     *
     * @param filePath
     * @return
     */
    public static HoldDoubleValue<String, String> splitFilePath(String filePath) {
        int lastIndexOfDir = filePath.lastIndexOf("/");
        if (lastIndexOfDir == -1) {
            return new HoldDoubleValue<String, String>("", filePath);
        } else {
            return new HoldDoubleValue<String, String>(filePath.substring(0, lastIndexOfDir), filePath.substring(lastIndexOfDir + 1));
        }
    }

    public static String appendFileName(String fileName, String append) {
        if (fileName.indexOf(".") != -1) {
            String su = fileName.substring(fileName.indexOf("."));
            return fileName.substring(0, fileName.indexOf(".")) + append + su;
        }
        return fileName + append;
    }

    /**
     * 去掉前后的斜杠空格
     *
     * @param name
     * @return
     */
    public static String formatName(String name) {
        if (StringUtils.isNotBlank(name)) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return name;
    }

    public static String getFullPath(String dirPath, String name) {
        String newDirPath = Base64Util.formatName(dirPath);
        String newName = Base64Util.formatName(name);
        return StringUtils.isNotBlank(newDirPath) ? (newDirPath + "/" + newName) : newName;
    }

    /**
     * @param dirPath  从哪一级目录开始
     * @param filePath
     * @return
     */
    public static List<String> getDirsOfFile(String dirPath, String filePath) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotBlank(filePath)) {
            Integer index = StringUtils.isNotBlank(dirPath) ? dirPath.length() : 0;
            Integer splitIndex = filePath.indexOf("/", index);
            while (splitIndex != -1) {
                String tmpPath = filePath.substring(0, splitIndex);
                list.add(tmpPath);
                splitIndex = filePath.indexOf("/", tmpPath.length() + 1);
            }
        }
        return list;
    }

    public static List<String> getSelectDirs(String dirPath, String[] selectFilePath, String selectDirPath) {
        if (StringUtils.isNotBlank(dirPath)) {
            dirPath = formatName(dirPath) + "/";
        }
        List<String> list = new ArrayList<String>();
        for (String tmp : selectFilePath) {
            if (StringUtils.isNotBlank(tmp) && (StringUtils.isBlank(dirPath) || tmp.startsWith(dirPath))) {
                Integer index = StringUtils.isNotBlank(dirPath) ? dirPath.length() : 0;
                Integer splitIndex = tmp.indexOf("/", index);
                while (splitIndex != -1) {
                    String tmpPath = tmp.substring(0, splitIndex);
                    if (!list.contains(tmpPath)) {
                        list.add(tmpPath);
                    }
                    splitIndex = tmp.indexOf("/", tmpPath.length() + 1);
                }
            }
        }

        if (StringUtils.isNotBlank(selectDirPath) && (StringUtils.isBlank(dirPath) || selectDirPath.startsWith(dirPath))) {
            Integer index = StringUtils.isNotBlank(dirPath) ? dirPath.length() : 0;
            Integer splitIndex = selectDirPath.indexOf("/", index);
            while (splitIndex != -1) {
                String tmpPath = selectDirPath.substring(0, splitIndex);
                if (!list.contains(tmpPath)) {
                    list.add(tmpPath);
                }
                splitIndex = selectDirPath.indexOf("/", tmpPath.length() + 1);
            }
            if (!list.contains(selectDirPath)) {
                list.add(selectDirPath);
            }
        }
        return list;
    }

    /**
     * 分隔DirPath
     * parent,path
     * app/foo/data0
     * <p>
     * return app/foo data0
     *
     * @param dirPath
     * @return
     */
    public static HoldDoubleValue<String, String> splitDirPath(String dirPath) {
        int lastIndex = dirPath.lastIndexOf("/");
        if (lastIndex > 0) {
            return new HoldDoubleValue<String, String>(dirPath.substring(0, lastIndex), dirPath.substring(lastIndex + 1));
        } else {
            return new HoldDoubleValue<String, String>("", dirPath);
        }
    }

    public static void main(String[] args) {
        Matcher matcher = pattern.matcher("    ");
        if (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }
}

