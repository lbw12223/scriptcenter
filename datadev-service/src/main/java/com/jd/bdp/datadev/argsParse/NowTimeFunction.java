package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;

/**
 * 两个参数 (时间，格式)
 * Created by zhangrui25 on 2018/7/2.
 */
public class NowTimeFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String definition = "ntime";

    public NowTimeFunction(String[] args) {
        super(args);
    }

    public NowTimeFunction() {
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
        throw new RuntimeException("NowTime函数 不需要参数");
    }


}
