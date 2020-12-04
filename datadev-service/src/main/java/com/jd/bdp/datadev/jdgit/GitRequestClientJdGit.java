package com.jd.bdp.datadev.jdgit;


import com.jd.bdp.datadev.component.SpringPropertiesUtils;

public class GitRequestClientJdGit extends GitRequestClient {

    private static String PRIVATE_TOKEN = null; //"j9e-suFgk6p7di-rgKFg";
    private static String PRIVETE_TOKEN_USER = null;//"zhangrui156";
    private static final String REQUEST_URI_PRE = "http://git.jd.com/api/v4/";

    @Override
    String getPrivetToken() {
        if (PRIVATE_TOKEN == null) {
            PRIVATE_TOKEN = SpringPropertiesUtils.getPropertiesValue("${git.private.token}");
        }
        return PRIVATE_TOKEN;
    }

    @Override
    String getRequestUriPre() {
        return REQUEST_URI_PRE;
    }


}
