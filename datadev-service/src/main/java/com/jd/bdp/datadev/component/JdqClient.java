package com.jd.bdp.datadev.component;


import com.jd.bdp.datadev.model.ScriptJdqMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.JDQ_ENV;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by zhangrui25 on 2018/3/12.
 * 任务状态jdq
 */
@Component
public class JdqClient implements InitializingBean {

    private static Log logger = LogFactory.getLog(JdqClient.class);

    @Value("${jdq.jdqUserName}")
    private String jdqUserName;

    @Value("${jdq.jdqPassword}")
    private String jdqPassword;

    @Value("${jdq.jdqBrokerList}")
    private String jdqBrokerList;

    @Value("${jdq.clientId}")
    private String clientId;

    @Value("${jdq.topic}")
    private String topic;

    @Value("${client.env}")
    private String env;

    private KafkaProducer<byte[], byte[]> producer;


    /**
     * 启动时候初始化
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            if (true) {
                return;
            }
            Properties props = getProperties(jdqUserName, jdqPassword, jdqBrokerList, clientId);
            producer = new KafkaProducer<byte[], byte[]>(props);
            logger.info("JDQ client init success");
        } catch (Exception e) {
            logger.error("JDQ client init failed...", e);
        }
    }

    public boolean sendDataDevJdqMessage(ScriptJdqMessage scriptJdqMessage) {
        try {
            if (true) {
                return true;
            }
            byte[] key = ("key").getBytes("UTF-8");
            byte[] val = scriptJdqMessage.getValue().getBytes("UTF-8");
            logger.error("========================scriptJdqMessage" + scriptJdqMessage.getValue());
            ProducerRecord<byte[], byte[]> record = new ProducerRecord<byte[], byte[]>(topic, key, val);
            producer.send(record);
            return true;
        } catch (Exception e) {
            logger.error("send message error", e);
        }
        return false;
    }

    private Properties getProperties(String username, String password, String brokerlist, String clientId) {
        Properties props = new Properties();
        if (env.equalsIgnoreCase("dev")) {
            JDQ_ENV.authClinetNV("NULL", 80);
            System.setProperty("java.security.krb5.kdc", "192.168.144.109");
        }
        //泰国和印尼
        if(env.equals("th") || env.equals("yn")){

            /**
             * 注意: 这里必须设置认证的协议方式
             * 不认证集群需要显示指定PLAINTEXT,默认为认证SASL_PLAINTEXT
             */
            props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");

            /**
             * kafka配置列表，可参考（版本0.10.1.0）http://kafka.apache.org/0101/documentation.html#producerconfigs
             */
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerlist);
            props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        }else{

            props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            props.setProperty(ProducerConfig.JDQ_USE_KEYTAB_CONFIG, "false");
            props.setProperty(ProducerConfig.JDQ_PRINCIPAL_NAME_CONFIG, username);
            props.setProperty(ProducerConfig.JDQ_PASSWORD_CONFIG, password);
            props.setProperty(ProducerConfig.JDQ_ENABLE_DEBUG_CONFIG, "false");
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerlist);
            props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        }


        return props;
    }


}
