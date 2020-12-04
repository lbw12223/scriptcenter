package com.jd.bdp.datadev.service;

import com.jd.bdp.datadev.agent.api.AgentScriptInterface;

public interface DynamicJsfService {

    void addScriptInterfaceJsfClient(String ip)throws Exception;
    AgentScriptInterface getScriptInterfaceJsfClient(String ip)throws Exception;

}
