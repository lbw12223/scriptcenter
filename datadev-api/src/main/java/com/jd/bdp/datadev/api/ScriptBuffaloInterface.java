package com.jd.bdp.datadev.api;

import com.jd.bdp.datadev.model.BuffaloJobInfo;
import com.jd.bdp.datadev.model.Script;

public interface ScriptBuffaloInterface {
    BuffaloJobInfo getScriptInfo(Script script) throws Exception;
}
