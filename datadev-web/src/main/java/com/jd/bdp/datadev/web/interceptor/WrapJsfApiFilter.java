package com.jd.bdp.datadev.web.interceptor;

import com.jd.bdp.amp.sdk.filter.JsfApiFilter;
import com.jd.jsf.gd.msg.RequestMessage;
import com.jd.jsf.gd.msg.ResponseMessage;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by zhangrui25 on 2018/4/10.
 * <p>
 * 服务治理
 */
public class WrapJsfApiFilter extends JsfApiFilter {

    @Value("${datadev.env}")
    private String env;

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        if (env.equalsIgnoreCase("online")) {
            return super.invoke(request);
        }
        return getNext().invoke(request);
    }


}
