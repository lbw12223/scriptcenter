package com.jd.bdp.datadev.component;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/3/5.
 */
public class SplitFileUtils {

    public static final Integer EACH_BUCK_SIZE = 1024 * 1024;

    public static final Integer BIG_FILE_SIZE = 10 * 1024 * 1024;

/*    public static void main(String[] args) throws Exception {
        File file = new File("d:/cd3.txt");
        List<byte[]> bytess = spitFile(file);
        covertByteToFile(bytess, new File("d:/cd3out.txt"));
    }*/

    /**
     * 转化byte到文件
     *
     * @param bytes
     * @param outFile
     * @throws Exception
     */
    public static void covertByteToFile(List<byte[]> bytes, File outFile) throws Exception {
        for (byte[] temp : bytes) {
            FileUtils.writeByteArrayToFile(outFile, temp, true);
        }
    }

    /**
     * 分隔文件为List<byte[]>
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static List<byte[]> spitFile(File file) throws Exception {
        return splitBytes(FileUtils.readFileToByteArray(file));
    }

    public static List<byte[]> splitContent(String content) throws Exception {
        return splitBytes(content.getBytes());
    }

    /**
     * 分隔bytes
     *
     * @param totalBytes
     * @return
     * @throws Exception
     */
    public static List<byte[]> splitBytes(byte[] totalBytes) throws Exception {
        List<byte[]> result = new ArrayList<byte[]>();
        Integer length = totalBytes.length;

        int totalBucks = length % EACH_BUCK_SIZE == 0 ? length / EACH_BUCK_SIZE : length / EACH_BUCK_SIZE + 1;
        for (int index = 0; index < totalBucks; index++) {
            byte[] temp = null;
            if (length - index * EACH_BUCK_SIZE > EACH_BUCK_SIZE) {
                temp = new byte[EACH_BUCK_SIZE];
            } else {
                temp = new byte[length - index * EACH_BUCK_SIZE];
            }
            System.arraycopy(totalBytes, index * EACH_BUCK_SIZE, temp, 0, temp.length);
            result.add(temp);
        }
        return result;
    }

    public static int isBigFile(Long size) {
        return size != null && size >= BIG_FILE_SIZE ? 1 : 0;
    }

    /**
     * 转换size
     * 123.23K
     * 2.2MB
     *
     * @param size
     * @return
     */
    public static String covertFileSize(Long size) {
        if (size != null && size > 0) {
            Long mb = 1024 * 1024L;
            Long k = 1024L;
            if (size > mb) {
                return getTwoDecimal(size / (mb * 1.0)) + " MB";
            }
            if (size > k) {
                return getTwoDecimal(size / (k * 1.0)) + " K";
            }
            return size + " Bytes";
        }
        return "";
    }

    /**
     * 将数据保留两位小数
     */
    private static double getTwoDecimal(double num) {
        DecimalFormat dFormat = new DecimalFormat("#.00");
        String yearString = dFormat.format(num);
        Double temp = Double.valueOf(yearString);
        return temp;
    }
}
