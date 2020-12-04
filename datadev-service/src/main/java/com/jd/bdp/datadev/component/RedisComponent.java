package com.jd.bdp.datadev.component;

import com.jd.jim.cli.Cluster;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisComponent {



    public static final String KEY_OF_REDIS_DO_SQL_TOOL_CHECK = "bdp.datadev.dosqltool.check" ;

    public static final String KEY_OF_REDIS_DO_SQL_LIMIT_CHECK = "bdp.datadev.dosqllimit.check";

    @Autowired
    private Cluster jimClient;


    public boolean doLimitCheck(){
       String value = jimClient.get(KEY_OF_REDIS_DO_SQL_LIMIT_CHECK);
       if(StringUtils.isEmpty(value)){
           value = Boolean.TRUE.toString();
           jimClient.set(KEY_OF_REDIS_DO_SQL_LIMIT_CHECK,value);
       }
       return value.equalsIgnoreCase(Boolean.TRUE.toString()) ;
    }

    public void setValue(String key , String value){
        jimClient.set(key,value);
    }

    public String getValue(String key ){
       return jimClient.get(key);
    }
    public String getValue(String key , String setDefaultValue ){
        String value = jimClient.get(KEY_OF_REDIS_DO_SQL_TOOL_CHECK);
        if(StringUtils.isEmpty(value)){
            value = setDefaultValue ;
            jimClient.set(KEY_OF_REDIS_DO_SQL_TOOL_CHECK,value);
        }
        return value ;
    }

    public boolean doCheck(){
        String value = jimClient.get(KEY_OF_REDIS_DO_SQL_TOOL_CHECK);
        if(StringUtils.isEmpty(value)){
            value = Boolean.TRUE.toString();
            jimClient.set(KEY_OF_REDIS_DO_SQL_TOOL_CHECK,value);
        }
        return value.equalsIgnoreCase(Boolean.TRUE.toString()) ;
    }
}
