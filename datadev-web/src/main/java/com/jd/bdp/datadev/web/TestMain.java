package com.jd.bdp.datadev.web;

import com.jd.bdp.planing.api.ProjectInterface;
import com.jd.bdp.planing.api.model.ApiResult;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;

import javax.xml.bind.JAXBException;

public class TestMain {

    public static void main(String[] args) {


        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex("i.jsf.jd.com");

        ConsumerConfig<ProjectInterface> consumerConfig = new ConsumerConfig<ProjectInterface>();
        consumerConfig.setInterfaceId("com.jd.bdp.planing.api.ProjectInterface");
        consumerConfig.setAlias("bdp-api-dev-new");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setRegistry(jsfRegistry);

        ProjectInterface service = consumerConfig.refer();
        String appId = "jupyter.jd.com";
        String token = "60c6ed1882e77ee7f962970060936fc3";
        Long time = System.currentTimeMillis();
        ProjectBO projectBO= new ProjectBO();
        projectBO.setErp("liushuming3");
        ApiResult result = service.getGrantAuthorityProject(appId, token, time, projectBO);
        System.out.println("");
    }
}
