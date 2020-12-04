package com.jd.bdp.datadev.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by zhangrui25 on 2017/10/18.
 */

@Controller
public class ExceptionPageController {

    @RequestMapping("/scriptcenter/404.html")
    public String html404() {
        return "scriptcenter/error/404";
    }

    /**
     * 500 异常
     * @return
     */
    @RequestMapping("/scriptcenter/error/500.html")
    public String html500() {
        return "scriptcenter/error/500";
    }

    /**
     * 无权限页面
     * @return
     */
    @RequestMapping("/scriptcenter/error/noAuthorize.html")
    public String noAuthorize() {
        return "scriptcenter/error/noAuthorize";
    }



}
