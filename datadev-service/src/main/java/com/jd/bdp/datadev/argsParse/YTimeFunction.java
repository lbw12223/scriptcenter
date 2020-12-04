package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 前一天时间
 * Created by zhangrui25 on 2018/7/2.
 */
public class YTimeFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String definition = "ytime";

    public YTimeFunction(String[] args) {
        super(args);
    }

    public YTimeFunction() {
        super();
    }

    /**
     * @return
     */
    @Override
    String execute() {
        Calendar calendar = getCurrentCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (args == null || args.length < 1) {
            return YYYYMMDDHHMMSS.format(calendar.getTime());
        }
        throw new RuntimeException("YTime函数 不需要参数");
    }


}
