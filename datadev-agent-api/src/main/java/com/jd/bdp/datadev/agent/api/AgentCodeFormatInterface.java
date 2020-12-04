package com.jd.bdp.datadev.agent.api;

import com.jd.bdp.datadev.domain.DataDevScriptFile;
import com.jd.bdp.datadev.model.ApiResult;

/**
 * Created by zhangrui25 on 2018/3/6.
 */
public interface AgentCodeFormatInterface {


    /**
     * 格式化代码
     * @param dataDevScriptFile
     * @return
     */
    ApiResult formatCode(DataDevScriptFile dataDevScriptFile);

}
