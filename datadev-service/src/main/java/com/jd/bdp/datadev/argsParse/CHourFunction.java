package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;

/**
 * 两个参数
 * Created by zhangrui25 on 2018/7/2.
 */
public class CHourFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHH = new SimpleDateFormat("yyyyMMddHH");

    public static final String definition = "chour";

    public CHourFunction(String[] args) {
        super(args);
    }

    public CHourFunction() {
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
        throw new RuntimeException("Chour函数 不需要参数");
    }

}
