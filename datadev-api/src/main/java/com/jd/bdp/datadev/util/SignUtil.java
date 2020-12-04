package com.jd.bdp.datadev.util;


import com.jd.bdp.datadev.json.WebJsonConfig;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by zhangrui25 on 2017/3/1.
 */
public class SignUtil {

    /**
     * 获取Sign
     * @param token
     * @param time
     * @param object
     * @return
     */
    public static String getSign(String token , Long time, Object object){
        String md5Str = token + JSONObject.fromObject(object, WebJsonConfig.getInstance()).toString();
        String md5 =  MD5Util.getMD5Str(md5Str);
        return  md5;
    }
    /**
     * 获取Sign
     * @param apptoken
     * @param userToken
     * @param time
     * @return
     */
    public static String getSign(String apptoken , String userToken , Long time){
        String md5Str = apptoken + userToken+time.toString();
        String md5 =  MD5Util.getMD5Str(md5Str);
        return  md5;
    }

    public static void main(String[] args) {
        String s="哈啊哈哈";
        System.out.println(s);

        System.out.println(s);
        String ss=StringEscapeUtils.escapeJava(s);
        ss="\""+ss+"\"";
        System.out.println(ss);
        System.out.println("\u54C8\u554A\u54C8\u54C8");

    }
}