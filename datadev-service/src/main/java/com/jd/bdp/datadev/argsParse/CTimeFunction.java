package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;

/**
 * 两个参数 (时间，格式)
 * Created by zhangrui25 on 2018/7/2.
 */
public class CTimeFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String definition = "ctime";

    public CTimeFunction(String[] args) {
        super(args);
    }

    public CTimeFunction() {
        super();
    }

    /**
     * @return
     */
    @Override
    String execute() {
        if (args == null || args.length < 1) {
            return YYYYMMDDHHMMSS.format(getCurrentDate());
        }
        throw new RuntimeException("CTime函数 不需要参数");
    }

}
