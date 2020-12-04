package com.jd.bdp.datadev.component;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

/**
 * Created by zhangrui25 on 2018/3/13.
 */
@Component
public class SpringPropertiesUtils implements EmbeddedValueResolverAware {

    private static StringValueResolver stringValueResolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        stringValueResolver = resolver;
    }

    public static String getPropertiesValue(String name) {
        return stringValueResolver.resolveStringValue(name);
    }
}
