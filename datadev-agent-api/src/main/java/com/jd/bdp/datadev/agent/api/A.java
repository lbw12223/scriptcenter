package com.jd.bdp.datadev.agent.api;

import com.jd.bdp.datadev.enums.ScriptJdqMessageEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/3/6.
 */
public class A {
    public  static  void main(String[] args){

        Map<String,String> ss = new HashMap<String, String>();
        ss.put("key","value");
        System.out.println(ss.remove("key1"));

    }
}
