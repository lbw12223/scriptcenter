package com.jd.bdp.datadev.web.interceptor;

import com.jd.bdp.urm.sso.UrmUserHolderArgumentResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class ProjectSpaceArgumentResolver implements HandlerMethodArgumentResolver {
    private static final Log logger = LogFactory.getLog(UrmUserHolderArgumentResolver.class);

    public ProjectSpaceArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ProjectSpaceIdParam.class);
    }

    /**
     * 之前是Cookie，现在改成通过params里面获取，前端页面提交的数据统一，需要添加这个参数
     * @param parameter
     * @param mavContainer
     * @param webRequest
     * @param binderFactory
     * @return
     * @throws Exception
     */
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Long spaceProjetId = 0L ;
        if(request.getParameter("projectSpaceId") != null){
            try{
                return Long.parseLong(request.getParameter("projectSpaceId"));
            }catch (Exception e){
            }
        }
        return spaceProjetId ;
    }
}

