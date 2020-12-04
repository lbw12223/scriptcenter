package com.jd.bdp.datadev.web.interceptor;

import com.jd.bdp.datadev.component.ProjectSpaceRightComponent;
import com.jd.bdp.planing.domain.bo.ProjectBO;
import com.jd.bdp.urm.sso.UrmUserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 项目空间
 */
public class ProjectSpaceInterceptor implements HandlerInterceptor {

    public static final Logger log = LoggerFactory.getLogger(ProjectSpaceInterceptor.class);

    public static final String COOKIE_KEY = "P_S_I_D";
    @Value("${datadev.appId}")
    private String appId;
    @Value("${datadev.token}")
    private String token;

    @Autowired
    private ProjectSpaceRightComponent projectSpaceRightComponent;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        boolean isHtml = request.getRequestURL().toString().toLowerCase().indexOf(".html") > -1;
        boolean isAjax = request.getRequestURL().toString().toLowerCase().indexOf(".ajax") > -1;
        if (isHtml || isAjax) {
            Long defaultProjectSpaceId = -1L;
            String defaultProjectSpaceName = "项目空间";

            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIE_KEY)) {
                    String value = cookie.getValue();
                    log.error("====ProjectSpaceInterceptor=cookie=value===== {} " , value);
                    defaultProjectSpaceId = Long.parseLong(value);
                    break;
                }
            }

            if (isHtml) {
                try {
                    UrmUserHolder userHolder = (UrmUserHolder) request.getAttribute(UrmUserHolder.REQUEST_KEY_HOLDER);
                    List<ProjectBO> projectSpaces = projectSpaceRightComponent.getProjectSpaces(userHolder.getErp());
                    request.setAttribute("projectBos", projectSpaces);

                    if (projectSpaces != null && projectSpaces.size() > 0) {
                        ProjectBO defaultProjectSpace = null;

                        for(ProjectBO temp : projectSpaces){
                            if(temp.getId().equals(defaultProjectSpaceId)){
                                defaultProjectSpace = temp ;
                                break;
                            }
                        }
                        defaultProjectSpace = defaultProjectSpace == null ? projectSpaces.get(0) : defaultProjectSpace;
                        defaultProjectSpaceName = defaultProjectSpace.getName();
                        defaultProjectSpaceId = defaultProjectSpace.getId();

                    }

                } catch (Exception e) {
                    log.error(" ===ProjectSpaceInterceptor======{} ", e);
                }

                //用于界面显示
                request.setAttribute("defaultProjectSpaceId", defaultProjectSpaceId);
                request.setAttribute("defaultProjectSpaceName", defaultProjectSpaceName);
                log.error("====ProjectSpaceInterceptor=default=value===== {} " , defaultProjectSpaceId);
            }
            //用于Controller 里面计算
            request.setAttribute(COOKIE_KEY, defaultProjectSpaceId);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
