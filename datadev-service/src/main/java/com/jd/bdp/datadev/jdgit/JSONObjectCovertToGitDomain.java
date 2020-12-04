package com.jd.bdp.datadev.jdgit;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhangrui25 on 2018/5/18.
 */
public interface JSONObjectCovertToGitDomain<T> {

    T covertGitDomain(JSONObject jsonObject);
}
