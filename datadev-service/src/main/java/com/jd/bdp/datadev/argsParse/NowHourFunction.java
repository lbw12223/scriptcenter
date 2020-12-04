package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;

/**
 * 两个参数
 * Created by zhangrui25 on 2018/7/2.
 */
public class NowHourFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHH = new SimpleDateFormat("yyyyMMddHH");

    public static final String definition = "nhour";

    public NowHourFunction(String[] args) {
        super(args);
    }

    public NowHourFunction() {
        super();
    }

    /**
     * @return
     */
    @Override
    String execute() {
        if (args == null || args.length < 1) {
            return YYYYMMDDHH.format(getCurrentDate());
        }
        throw new RuntimeException("Nhour函数 不需要参数");
    }

}
