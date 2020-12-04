package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;

/**
 * 两个参数 (时间，格式)
 * Created by zhangrui25 on 2018/7/2.
 */
public class FmtTimeFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");


    public static final String definition = "fmt";

    public FmtTimeFunction(String[] args) {
        super(args);
    }

    public FmtTimeFunction() {
        super();
    }

    /**
     * @return
     */
    @Override
    String execute() {
        if (args == null || args.length != 2) {
            throw new RuntimeException("参数格式错误");
        }
        try {
            String baseTime = args[0].replace("\"", "").replace("'", "");
            String fmt = args[1].replace("\"", "").replace("'", "");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fmt);
            return simpleDateFormat.format(YYYYMMDDHHMMSS.parse(baseTime));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
