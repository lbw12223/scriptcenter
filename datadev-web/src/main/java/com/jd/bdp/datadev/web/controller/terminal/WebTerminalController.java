package com.jd.bdp.datadev.web.controller.terminal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by zhangrui25 on 2018/10/27.
 */

//@Controller
public class WebTerminalController {

    @RequestMapping("/scriptcenter/terminal.html")
    public String queryBase(Model model) throws Exception{
        return "/scriptcenter/terminal/terminal";
    }
}
