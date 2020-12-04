package com.jd.bdp.datadev.argsParse;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.方法的定义
 * 2.方法的执行
 * <p>
 * <p>
 * Created by zhangrui25 on 2018/7/2.
 */
public abstract class Function {

    public static final String CURRENT_DATE = "CURRENT_DATE";
    public static final String CURRENT_CALENDAR = "CURRENT_CALENDAR";

    protected static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            HashMap<String, Object> params = new HashMap<String, Object>();
            Date insertDate = new Date();
            params.put(CURRENT_DATE, insertDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(insertDate);
            params.put(CURRENT_CALENDAR, calendar);
            return params;
        }
    };

    static void removeThreadData() {
        context.remove();
    }

    static void setDefaultData(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        context.get().put(CURRENT_DATE,date);
        context.get().put(CURRENT_CALENDAR,calendar);
    }

    static Date getCurrentDate() {
        return (Date) context.get().get(CURRENT_DATE);
    }

    static Calendar getCurrentCalendar() {
        return (Calendar) context.get().get(CURRENT_CALENDAR);
    }

    protected String args[];

    public Function() {
    }

    public Function(String[] args) {
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    abstract String execute();

    public String run() {
        if (args != null && args.length > 0) {
            for (int index = 0; index < args.length; index++) {
                args[index] = args[index].trim();
                if (ParseArgsUtil.isFunction(args[index])) {
                    Function function = ParseArgsUtil.covertToFuncion(args[index]);
                    args[index] = function.run();
                }
            }
        }
        return execute();
    }
}
