package com.jd.bdp.datadev.web.interceptor;

import com.jd.bdp.amp.sdk.ApiInvokeInterrupt;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhangrui25 on 2018/4/10.
 * <p>
 * 服务治理
 */

public class WrapApiInvokeInterrupt extends ApiInvokeInterrupt {

    @Value("${datadev.env}")
    private String env;

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (env.equalsIgnoreCase("online")) {
            return super.preHandle(httpServletRequest, httpServletResponse, o);
        }
        return true;
//        return super.preHandle(httpServletRequest, httpServletResponse, o);

    }
}