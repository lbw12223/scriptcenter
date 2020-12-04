package com.jd.bdp.datadev.util;

/**
 * Created by zhangrui25 on 2018/3/12.
 */
public class ApiEnv {

    public static String LOCAL = "local";
    public static String TEST = "test";
    public static String DEV = "dev";
    public static String ONLINE = "online";
    private static String currentENV = "online";
    private static String domain;

    private ApiEnv() {
    }

    public static void setEnv(String env) {
        currentENV = env;
    }

    public static String getDomain() {
        if (currentENV.equals(ONLINE)) {
            domain = "http://dev.bdp.jd.com/";
        } else if (currentENV.equals(TEST)) {
            domain = "http://test.bdp.jd.com/";
        } else if (currentENV.equals(DEV)) {
            domain = "http://t.bdp.jd.com/";
        }else if(currentENV.equals(LOCAL)){
            domain = "http://d.bdp.jd.com/";
        }
        return domain;
    }


}
