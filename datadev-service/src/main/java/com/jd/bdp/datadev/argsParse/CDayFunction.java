package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;

/**
 * 两个参数 (时间，格式)
 * Created by zhangrui25 on 2018/7/2.
 */
public class CDayFunction extends Function {

    private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    public static final String definition = "cday";

    public CDayFunction(String[] args) {
        super(args);
    }

    public CDayFunction() {
        super();
    }

    /**
     * @return
     */
    @Override
    String execute() {
        if (args == null || args.length < 1) {
            return YYYYMMDD.format(getCurrentDate());
        }
        throw new RuntimeException("CDay函数 不需要参数");
    }

}
