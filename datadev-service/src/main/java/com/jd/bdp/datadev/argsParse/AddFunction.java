package com.jd.bdp.datadev.argsParse;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * ${add(CTIME(),-7,'day')}
 * year,month,day,hour,minute,second
 * Created by zhangrui25 on 2018/7/2.
 */
public class AddFunction extends Function {

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String definition = "add";

    public AddFunction(String[] args) {
        super(args);
    }

    public AddFunction() {
        super();
    }

    /**
     * @return
     */
    @Override
    String execute() {
        try {
            if (args != null && args.length == 3) {
                Calendar calendar = getCurrentCalendar();
                args[0] = args[0].replace("\"","").replace("'","");
                calendar.setTime(YYYYMMDDHHMMSS.parse(args[0]));
                int append = 0 ;
                try{
                     append = Integer.parseInt(args[1]);
                }catch (Exception e){
                    throw new RuntimeException("调整时间,请输入整数.");
                }
                String filed = args[2].replace("\"", "");
                filed = filed.replace("'", "");
                if (filed.equalsIgnoreCase("year")) {
                    calendar.add(Calendar.YEAR, append);
                }
                if (filed.equalsIgnoreCase("month")) {
                    calendar.add(Calendar.MONTH, append);
                }
                if (filed.equalsIgnoreCase("day")) {
                    calendar.add(Calendar.DAY_OF_MONTH, append);
                }
                if (filed.equalsIgnoreCase("hour")) {
                    calendar.add(Calendar.HOUR_OF_DAY, append);
                }
                if (filed.equalsIgnoreCase("minute")) {
                    calendar.add(Calendar.MINUTE, append);
                }
                if (filed.equalsIgnoreCase("second")) {
                    calendar.add(Calendar.SECOND, append );
                }
                return YYYYMMDDHHMMSS.format(calendar.getTime());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("add函数 需要三参数");

    }


}
