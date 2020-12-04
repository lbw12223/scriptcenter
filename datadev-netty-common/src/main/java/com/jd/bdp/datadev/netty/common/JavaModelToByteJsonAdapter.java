package com.jd.bdp.datadev.netty.common;


import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhangrui25 on 2018/11/12.
 */
public abstract class JavaModelToByteJsonAdapter implements JavaModelToByte {

    public byte[] covertBytes()throws Exception {
        return JSONObject.toJSONString(this).getBytes("utf-8");
    }
}
