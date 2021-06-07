package com.jd.bdp.datadev.service.impl;

import com.jd.bdp.datadev.agent.api.AgentScriptInterface;
import com.jd.bdp.datadev.domain.DataDevClientBase;
import com.jd.bdp.datadev.service.DataDevClientBaseService;
import com.jd.bdp.datadev.service.DynamicJsfService;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DynamicJsfServiceImpl implements DynamicJsfService,InitializingBean,ApplicationContextAware {

    private static final Logger logger = Logger.getLogger(DynamicJsfServiceImpl.class);
    private Map<String,AgentScriptInterface> stringScriptInterfaceMap=new HashMap<String, AgentScriptInterface>();


    @Value("${bdp.jsf.alias}")
    private String jsfAlias;
    @Value("${jsf.register}")
    private String jsfRegister ;
    @Autowired
    private DataDevClientBaseService clientBaseService;




    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;




    @Override
    public void addScriptInterfaceJsfClient(String ip) throws Exception{
        if(StringUtils.isBlank(ip)){
            throw new RuntimeException("注册ip不能为空");
        }
        String beanName = AgentScriptInterface.class.getName();
        String beanId   = beanName+"-center-"+ip;
        String alias    = jsfAlias+"-center-"+ip;
        registerJsf(new ConsumerConfig<AgentScriptInterface>(),AgentScriptInterface.class,beanId,alias);
    }

    /**
     * 如果map里面拿不到就注册一个并put进去
     * @param ip
     * @throws Exception
     */
    @Override
    public AgentScriptInterface getScriptInterfaceJsfClient(String ip) throws Exception {
        String beanId=AgentScriptInterface.class.getName()+"-center-"+ip;
        AgentScriptInterface scriptInterface=stringScriptInterfaceMap.get(beanId);
        if(scriptInterface==null){
            synchronized(this){
                scriptInterface=stringScriptInterfaceMap.get(beanId);
                if(scriptInterface==null){
                    addScriptInterfaceJsfClient(ip);
                    scriptInterface=configurableApplicationContext.getBean(beanId,AgentScriptInterface.class);
                    if(scriptInterface!=null){
                        logger.error("=====================注册bean id  "+","+beanId);
                    }
                    stringScriptInterfaceMap.put(beanId,scriptInterface);
                }
            }
        }
        return scriptInterface;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<DataDevClientBase> list=clientBaseService.findAliveClient();
        for(DataDevClientBase clientBase:list){
            String ip=clientBase.getIp();
            getScriptInterfaceJsfClient(ip);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.configurableApplicationContext= (ConfigurableApplicationContext) applicationContext;
    }
    /**
     * 注册 Bean 到 JSF
     * 注册 Bean 到 Spring
     * @param interfaceClass
     * @param beanId
     * @param alias
     */
    private void registerJsf(ConsumerConfig<?> consumerConfig , Class<?> interfaceClass , String beanId, String alias){

        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex(jsfRegister);
        jsfRegistry.setProtocol("jsfRegistry");
        consumerConfig.setInterfaceId(interfaceClass.getName());
        consumerConfig.setAlias(alias);
        consumerConfig.setProtocol("jsf");
        consumerConfig.setTimeout(600 * 1000);
        consumerConfig.setRetries(0);
        consumerConfig.setRegistry(jsfRegistry);
        DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) (configurableApplicationContext.getBeanFactory());
        if (registry.containsSingleton(beanId)) {
            registry.destroySingleton(beanId);
        }
        registry.registerSingleton(beanId, consumerConfig.refer());
    }

}
