package com.jd.bdp.datadev.json;

import net.sf.json.JsonConfig;

import java.sql.Timestamp;

/**
 * Created by zhangrui25 on 2017/2/28.
 */
public class WebJsonConfig extends JsonConfig{
    public static WebJsonConfig getInstance() {
        return instance;
    }

    private static WebJsonConfig instance = new WebJsonConfig();

    private WebJsonConfig() {
        this.registerJsonValueProcessor(java.util.Date.class, new MyDateJsonValueProcessor());
        this.registerJsonValueProcessor(java.sql.Date.class, new MyDateJsonValueProcessor());
        this.registerJsonValueProcessor(Timestamp.class, new MyDateJsonValueProcessor());
        IgnoreFieldPropertyFilterImpl filter = new IgnoreFieldPropertyFilterImpl(new String[]{"contentFile"});
        this.setJsonPropertyFilter(filter);
    }
}
