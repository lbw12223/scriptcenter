package com.jd.bdp.datadev.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class MD5Util {


    private static Log logger = LogFactory.getLog(MD5Util.class);

    /**
     * MD5 加密
     */
    public static String getMD5Str(String str) {
        try {
            str = str == null ? "" : str;
            byte[] bytes = str.getBytes("UTF-8");
            return getMD5(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * MD5 加密
     */
    public static String getMD5(byte[] bytes) {
        try {
            if (bytes == null) {
                return getMD5Str(null);
            }

            MessageDigest messageDigest = null;

            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(bytes);

            byte[] byteArray = messageDigest.digest();

            StringBuffer md5StrBuff = new StringBuffer();

            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                else
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }

            return md5StrBuff.toString();
        } catch (Exception e) {
            logger.error("getMD5", e);
        }
        return "";

    }


    public static void main(String[] args) {
        int i = 0;
        if (i == 0) {
            System.out.println(Integer.toBinaryString(2));
        }
        long current = System.currentTimeMillis();
//        long current = 1374565100599L;
        System.out.println(current);
//        String s = getMD5Str("institute=10&distributionCenter=10&warehouseId=11&predictDate=2013-06-24&days=3&timeBucket=08:00-12:00,13:00-18:00,18:00-23:00&requestTimeDSP=" + current + "&secretKey=esdF0pE3");
//        String s = getMD5Str("apptoken=9dcbf642c78137f656ba7c24381ac25b&institute=10&distributionCenter=10&warehouse=1&firstWorkPeriodBeginTime=08:00&firstWorkPeriodEndTime=12:00&secondWorkPeriodBeginTime=09:17&secondWorkPeriodEndTime=11:18&totalOrder=5000&nowtime=07:55&timeSection=11:00;08:00-12:00,10:30;08:00-11:30&totalOrderPoint=11:00;3000,10:30;2000&undoneOrder=3000&undoneOrderPoint=11:00;2000,10:30;1000&finishedOrder=2000&finishedOrderPoint=11:00;1500,10:30;500&requestTimeDSP=" + current + "&secretKey=esdF0pE3");
//        String s = getMD5Str("kpiId=10001&dateType=2&startDate=2002-01&endDate=2003-31&requestTimeDSP=" + current + "&secretKey=Iq5ZajO5");
//        String s = getMD5Str("s_o_memberid=  w an gwei123&requestTimeDSP=" + current + "&secretKey=efbHU9a2");
        String s = getMD5Str("");
        System.out.println(s);
    }


}
