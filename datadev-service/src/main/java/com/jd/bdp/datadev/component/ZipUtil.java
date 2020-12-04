package com.jd.bdp.datadev.component;

import com.jd.bdp.datadev.domain.HoldDoubleValue;
import com.jd.bdp.datadev.jdgit.Base64Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static Log log = LogFactory.getLog(ZipUtil.class);

    /**
     * @param bytes
     * @param root
     * @return HoldDoubleValue.a：所有的目录路径   HoldDoubleValue.b:脚本路径对应的脚本内容
     */
    public static HoldDoubleValue<Set<String>, Map<String, byte[]>> unCompress(byte[] bytes, String root) {
        Map<String, byte[]> mapZip = new HashMap<>();//key：路径  value：文件
        Set<String> dirSet = new HashSet<>();
        HoldDoubleValue<Set<String>, Map<String, byte[]>> holdDoubleValue = new HoldDoubleValue<>(dirSet, mapZip);
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes), Charset.forName("GBK"));
            ZipEntry ze = zis.getNextEntry();
            byte[] b = new byte[1024];
            int n;
            while (ze != null) {
                String fileName = ze.getName();
                String filePath = Base64Util.getFullPath(root, fileName);
                if (ze.isDirectory()) {
                    dirSet.add(filePath);
                } else {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    while ((n = zis.read(b)) != -1) {
                        outputStream.write(b, 0, n);
                    }
                    byte[] fileBytes = outputStream.toByteArray();
                    System.out.println(fileBytes.length);
                    mapZip.put(filePath, fileBytes);
                    outputStream.close();
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Done");
        } catch (Exception e) {
            log.error("======================解压文件夹失败:" + e.getMessage());
        }
        return holdDoubleValue;
    }

    public static  byte[] compress(Set<String> dirSet, Map<String, byte[]> mapZip,Integer length) throws Exception {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length!=null?length:1024);
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        for (String dir : dirSet) {
            zipOutputStream.putNextEntry(new ZipEntry(dir.endsWith("/")?dir:(dir+"/")));
            zipOutputStream.closeEntry();
        }
        for (Map.Entry<String, byte[]> entry : mapZip.entrySet()) {
            String path = entry.getKey();
            byte[] bytes = entry.getValue();
            zipOutputStream.putNextEntry(new ZipEntry(path));
            zipOutputStream.write(bytes, 0, bytes!=null?bytes.length:0);
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();
        byte[] result = byteArrayOutputStream.toByteArray();
        return result;
    }

    public static byte[] File2byte(File file) throws Exception {
        byte[] buffer = null;
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = fis.read(b)) != -1) {
            bos.write(b, 0, n);
        }
        fis.close();
        bos.close();
        buffer = bos.toByteArray();
        file.setLastModified(new Date().getTime());
        return buffer;
    }

    public static void byte2File(byte[] buf, File file) throws Exception {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        fos = new FileOutputStream(file);
        bos = new BufferedOutputStream(fos);
        bos.write(buf);
        bos.close();
        fos.close();
    }

    public static void renameFile(String file, String toFile) {
        try {
            File toBeRenamed = new File(file);
            //检查要重命名的文件是否存在，是否是文件
            if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
                log.error("修改文件名称原文件：" + file + "不存在");
                return;
            }

            File newFile = new File(toFile);

            //修改文件名
            if (toBeRenamed.renameTo(newFile)) {
                log.error("文件名称修改成功：" + file + "->" + toFile);
            } else {
                log.error("文件名称修改失败：" + file + "->" + toFile);
            }

        } catch (Exception e) {
            log.error("=============修改文件报错：" + e.getMessage());
        }
    }

}
