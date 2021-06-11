package com.jd.bdp.datadev.web.interceptor;

import com.jd.bdp.urm.sso.UrmUserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by zhangrui25 on 2017/4/25.
 */
public class CreateNewUserHolderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UrmUserHolder hostHolder = new UrmUserHolder();
        hostHolder.setErp("wangchengcheng17");//jiaboyu5
        hostHolder.setLogin(true);
        hostHolder.setName("wangchengcheng17");
        request.setAttribute(UrmUserHolder.REQUEST_KEY_HOLDER, hostHolder);
        request.setAttribute("bdpDomain", "http://t.bdp.jd.com");


//        request.setAttribute("staticVersion",new Date().getTime());
        request.setAttribute("staticVersion",new Date().getTime());
        request.setAttribute("_staticVersion",new Date().getTime());
//       request.setAttribute("_staticVersion",new Date().getTime());
//       request.setAttribute("staticVersion",System.currentTimeMillis());
         return true ;
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
