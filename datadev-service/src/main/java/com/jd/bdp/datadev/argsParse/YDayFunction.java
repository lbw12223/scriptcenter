package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 前一天时间
 * Created by zhangrui25 on 2018/7/2.
 */
public class YDayFunction extends Function {

    private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    public static final String definition = "yday";

    public YDayFunction(String[] args) {
        super(args);
    }

    public YDayFunction() {
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
            return YYYYMMDD.format(calendar.getTime());
        }
        throw new RuntimeException("Yday函数 不需要参数");
    }


}
