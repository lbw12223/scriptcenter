package com.jd.bdp.datadev.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DataDevScriptDirServiceTest {


    @Test
    public void testListSetArray(){
        Set<String> set = new HashSet<String>();
        set.add("aaaa");
        String[] array=new String[]{"aaaa","bbbb"};
        set.addAll(Arrays.asList(array));
        System.out.println(set.toString());
    }
}
