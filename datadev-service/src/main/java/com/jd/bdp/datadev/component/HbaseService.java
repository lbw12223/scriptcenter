package com.jd.bdp.datadev.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * hbaseService
 */
@Component
public class HbaseService implements InitializingBean {

    private Log logger = LogFactory.getLog(HbaseService.class);

    private Connection connection;

    @Value("${datadev.env}")
    private String env;

    @Value("${bdp.hbase.erp}")
    private String erp;

    @Value("${bdp.hbase.instance.name}")
    private String instance;

    @Value("${bdp.hbase.accesskey}")
    private String accesskey;

    @Value("${bdp.hbase.domain}")
    private String domain;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            Configuration configuration = new Configuration();
            configuration.set("bdp.hbase.erp", erp);//你的erp
            configuration.set("bdp.hbase.instance.name", instance);//申请的实例名称
            configuration.set("bdp.hbase.accesskey", accesskey);//实例对应的accesskey，请妥善保管你的AccessKey
            if ("dev".equals(env) || "test".equals(env)) {
                configuration.set("hbase.policyserver.domain", domain);
            }
           // connection = ConnectionFactory.createConnection(configuration);//保持单例
        } catch (Exception e) {
            logger.error("初始化失败", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
