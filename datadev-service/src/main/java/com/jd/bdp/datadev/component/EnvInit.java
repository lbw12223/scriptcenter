package com.jd.bdp.datadev.component;

import com.jd.bdp.datadev.util.ApiEnv;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvInit implements InitializingBean {
    @Value("${datadev.env}")
    private String env;

    @Override
    public void afterPropertiesSet() throws Exception {
        ApiEnv.setEnv(env);
    }

    /**
     * 获取Hook地址
     * @return
     */
    public String getGitProjectHookUrl() {
        String uri = "datadev/hooks/post";

        if (env.equalsIgnoreCase("online")) {
            return "http://bdp.jd.com/" + uri;
        }
        if (env.equalsIgnoreCase("test")) {
            return "http://test.bdp.jd.com/" + uri;
        }
        return "http://192.168.144.103:8900/" + uri;

    }

    public String getEnv() {
        return env;
    }
}
