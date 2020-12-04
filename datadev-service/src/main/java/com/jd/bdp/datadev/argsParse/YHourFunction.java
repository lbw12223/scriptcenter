package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 前一天时间
 * Created by zhangrui25 on 2018/7/2.
 */
public class YHourFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHH = new SimpleDateFormat("yyyyMMddHH");

    public static final String definition = "yhour";

    public YHourFunction(String[] args) {
        super(args);
    }

    public YHourFunction() {
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
            return YYYYMMDDHH.format(calendar.getTime());
        }
        throw new RuntimeException("yhour函数 不需要参数");
    }


}
