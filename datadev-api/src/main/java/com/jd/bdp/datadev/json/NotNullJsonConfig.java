package com.jd.bdp.datadev.json;

import net.sf.json.JsonConfig;

import java.sql.Timestamp;

public class NotNullJsonConfig extends JsonConfig {
    public static NotNullJsonConfig getInstance() {
        return instance;
    }

    private static NotNullJsonConfig instance = new NotNullJsonConfig();

    private NotNullJsonConfig() {
        this.registerJsonValueProcessor(java.util.Date.class, new MyDateJsonValueProcessor());
        this.registerJsonValueProcessor(java.sql.Date.class, new MyDateJsonValueProcessor());
        this.registerJsonValueProcessor(Timestamp.class, new MyDateJsonValueProcessor());
        IgnoreFieldPropertyFilterImpl filter = new IgnoreFieldPropertyFilterImpl(new String[]{});
        this.setJsonPropertyFilter(filter);
    }

}
