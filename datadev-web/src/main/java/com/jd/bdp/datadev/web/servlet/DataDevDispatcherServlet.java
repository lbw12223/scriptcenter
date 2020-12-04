package com.jd.bdp.datadev.web.servlet;

import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhangrui25 on 2017/10/19.
 */
public class DataDevDispatcherServlet extends DispatcherServlet {
    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();


    @Override
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (pageNotFoundLogger.isWarnEnabled()) {
            String requestUri = urlPathHelper.getRequestUri(request);
            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + requestUri +
                    "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        response.sendRedirect(request.getContextPath() + "/scriptcenter/error/404.html");
    }
}
