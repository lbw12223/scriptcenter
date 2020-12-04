package com.jd.bdp.datadev.argsParse;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/7/2.
 */
public class ParseArgsUtil {

    private static final Log logger = LogFactory.getLog(ParseArgsUtil.class);

    public static final Map<String, Class<? extends Function>> registor = new HashMap<String, Class<? extends Function>>();

    //注册
    static {
        registor.put(FmtTimeFunction.definition, FmtTimeFunction.class);

        //N
        registor.put(NowDayFunction.definition, NowDayFunction.class);
        registor.put(NowHourFunction.definition, NowHourFunction.class);
        registor.put(NowTimeFunction.definition, NowTimeFunction.class);

        //C
        registor.put(CDayFunction.definition, CDayFunction.class);
        registor.put(CTimeFunction.definition, CTimeFunction.class);
        registor.put(CHourFunction.definition, CHourFunction.class);

        //y
        registor.put(YTimeFunction.definition, YTimeFunction.class);
        registor.put(YDayFunction.definition, YDayFunction.class);
        registor.put(YHourFunction.definition, YHourFunction.class);

        //add
        registor.put(AddFunction.definition, AddFunction.class);
        registor.put(FmtTimeFunction.definition, FmtTimeFunction.class);

    }

    private static final Pattern FUNCTION_PATTER = Pattern.compile("(.+?)\\((.*?)\\)");
    private static final Pattern EXPORESS_PATTER = Pattern.compile("^\\$\\{(.*)\\}$");

    public static void main(String[] args) throws Exception {

        for (int index = 1; index < 3; index++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(new Random().nextInt(10000));
                        System.out.println(Thread.currentThread().getName() + " set " + runFunction("${YTIME()}"));
                        Thread.sleep(new Random().nextInt(10000));
                        System.out.println(Thread.currentThread().getName() + " set " + runFunction("${YTIME()}"));
                    } catch (Exception e) {

                    }

                }
            }).start();
        }


        System.out.println(runFunction("${NTIME()}"));
        Thread.sleep(1000 * 5);
        System.out.println(runFunction("${NTIME()}"));
        System.out.println(runFunction("${NDAY()}"));
        System.out.println(runFunction("${NHOUR()}"));
        System.out.println(runFunction("${ytime()}"));
        System.out.println(runFunction("${yday()}"));
        System.out.println(runFunction("${yhour()}"));
        System.out.println(runFunction("${add(CTIME(),-7,'day')}"));
        System.out.println(runFunction("${fmt(add(CTIME(),1,'day'),'yyyyMMdd')}"));
        String s = "fmt(add(CTIME(),-1,'day'),'yyyyMMdd')";


        LinkedList<String> params = findParams(s);
        System.out.println(Arrays.toString(params.toArray()));
    }

    /**
     * 查找参数列表
     *
     * @param s
     * @return
     */
    private static LinkedList<String> findParams(String s) {
        LinkedList<String> params = new LinkedList<String>();
        String[] splitParams = s.split(",");
        for (int index = 0; index < splitParams.length; ) {
            String temp = splitParams[index];
            if (countChar(temp, '(') == 0) {
                params.add(temp);
                index++;
            } else {
                index = findNextParmas(splitParams, index, params);
                index++;
            }
        }
        return params;
    }

    /**
     * 计算字符串 相同char个数
     *
     * @param value
     * @param chars
     * @return
     */
    private static int countChar(String value, char chars) {
        int count = 0;
        for (int index = 0; index < value.length(); index++) {
            if (chars == value.charAt(index)) {
                count++;
            }
        }
        return count;
    }

    private static Integer findNextParmas(String[] splitParams, int index, LinkedList<String> params) {
        String temp = splitParams[index];
        for (; index < splitParams.length; ) {
            if (countChar(temp, '(') == countChar(temp, ')')) {
                params.add(temp);
                return index;
            } else {
                index++;
                temp += "," + splitParams[index];
            }
        }
        throw new IllegalArgumentException("参数错误!" + Arrays.toString(splitParams));
    }

    /**
     * 解析表达式
     *
     * @param functions
     * @return
     */
    public static String runFunction(String functions) {
        try {
            Matcher exporessMathcher = EXPORESS_PATTER.matcher(functions);
            if (exporessMathcher.matches()) {
                String temp = (exporessMathcher.group(1)).trim();
                if (isFunction(temp)) {
                    String result = covertToFuncion(temp).run();
                    Function.removeThreadData();
                    return result;

                }
            }
        } catch (Exception e) {
            logger.error("",e);
        }
        Function.removeThreadData();
        return functions;
    }

    /**
     * 用指定时间，解析表达式
     *
     * @param functions
     * @param targetDate
     * @return
     */
    public static String runFunction(String functions, Date targetDate) {
        Function.setDefaultData(targetDate);
        return runFunction(functions);
    }

    /**
     * 是否是函数
     *
     * @return
     */
    public static boolean isFunction(String str) {
        if (StringUtils.isNotBlank(str)) {
            str = str.trim();
            Matcher matcher = FUNCTION_PATTER.matcher(str);
            if (matcher.matches()) {
                String functionName = matcher.group(1);
                functionName = functionName.toLowerCase();
                if (registor.containsKey(functionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 目前有个问题，ctime() , 同一次请求有可能不一样
     * 比如同时执行1000个ctime(); 已经解决
     *
     * @param str
     * @return
     */
    public static Function covertToFuncion(String str) {
        try {
            Matcher matcher = FUNCTION_PATTER.matcher(str);
            if (matcher.matches()) {
                String functionName = matcher.group(1);
                functionName = functionName.toLowerCase();
                if (registor.containsKey(functionName)) {
                    String arg = matcher.group(2);
                    Function function = (Function) registor.get(functionName).newInstance();
                    if (StringUtils.isNotBlank(arg)) {
                        function.setArgs(findParams(arg).toArray(new String[]{}));
                    }
                    return function;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
