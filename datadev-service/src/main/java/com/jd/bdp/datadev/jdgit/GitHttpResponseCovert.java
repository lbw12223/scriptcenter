package com.jd.bdp.datadev.jdgit;

/**
 * Created by zhangrui25 on 2018/5/18.
 */
public interface GitHttpResponseCovert<T> {

    T covertHttpResponse(GitHttpResponse gitHttpResponse);
}
