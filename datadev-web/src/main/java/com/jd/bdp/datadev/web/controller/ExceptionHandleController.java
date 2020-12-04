package com.jd.bdp.datadev.web.controller;

import IceInternal.Ex;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.common.utils.AjaxUtil;
import com.jd.bdp.datadev.component.JSONObjectUtil;
import com.jd.bdp.datadev.web.annotations.ExceptionMessageAnnotation;
import com.jd.bdp.datadev.web.exception.*;
import com.jd.fastjson.JSONPath;
import com.jd.jsf.gd.error.RpcException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangrui25 on 2017/10/18.
 */
@ControllerAdvice
public class ExceptionHandleController {

    private static final Log logger = LogFactory.getLog(ExceptionHandleController.class);

    private static final SimpleDateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 404
     *
     * @param re
     * @param request
     * @param response
     * @throws Exception
     */
    @ExceptionHandler(NoSuchRequestHandlingMethodException.class)
    public void handle404(NoSuchRequestHandlingMethodException re, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect("/scriptcenter/404.html");
    }

    /**
     * 500
     *
     * @param re
     * @param request
     * @param response
     * @throws Exception
     */
    @ExceptionHandler({ScriptException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void scriptException(Exception re, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //返回Ajax数据
        if (re.getStackTrace() != null) {
            for (StackTraceElement element : re.getStackTrace()) {
                logger.error(element.toString());
            }
        }
        JSONObject resultJson = JSONObjectUtil.getFailResult(re.getMessage(), null);
        response.setHeader("Content-type", "text/json;charset=UTF-8");
        response.setStatus(500);
        response.getOutputStream().write(resultJson.toString().getBytes("utf-8"));
        response.getOutputStream().close();

    }

    /**
     * 500
     *
     * @param re
     * @param request
     * @param response
     * @throws Exception
     */
    @ExceptionHandler({TypeMismatchException.class, ParamsException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void paramException(Exception re, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.error("paramException", re);
        String uri = request.getRequestURI();
        if (uri.toUpperCase().endsWith(".AJAX")) {
            //返回Ajax数据
            String message = "参数错误";
            message = getAnnotationErrorMessage(message, re);
            JSONObject resultJson = JSONObjectUtil.getFailResult(message, null);
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.getWriter().print(resultJson.toString());
            response.getWriter().flush();
            response.getWriter().close();
        } else {
            //调转到页面
            request.setAttribute("exception", new ParamsException("参数错误!"));
            request.getRequestDispatcher("/scriptcenter/error/500.html").forward(request, response);
        }
    }

    /**
     * 500
     *
     * @param re
     * @param request
     * @param response
     * @throws Exception
     */
    @ExceptionHandler({Exception.class, SystemException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void page500(Exception re, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();
        if (uri.toUpperCase().endsWith(".AJAX")) {
            //返回Ajax数据
            String message = "";
            if (re instanceof RpcException) {
                logger.error("请求jsf出错");
                message = "请求出错";
            } else {
                message = request.getAttribute("errorMsg") != null ? (String) request.getAttribute("errorMsg") : (re.getMessage() != null ? re.getMessage() : "系统错误!");
            }
            message = getAnnotationErrorMessage(message, re);
            logger.error(message, re);
            JSONObject resultJson = JSONObjectUtil.getFailResult(message, null);
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.getWriter().print(resultJson.toString());
            response.getWriter().flush();
            response.getWriter().close();
        } else {
            //调转到页面
            request.setAttribute("exception", re);
            logger.error("500",re);

//            request.getRequestDispatcher("/scriptcenter/error/500.html").forward(request, response);
        }
    }


    /**
     * 获取 ExceptionMessageAnnotation 定义的错误信息
     *
     * @param message
     * @param re
     * @return
     */
    private String getAnnotationErrorMessage(String message, Exception re) {
        try {
            StackTraceElement controllerStack = null;
            for (StackTraceElement element : re.getStackTrace()) {
                Class classes = Class.forName(element.getClassName());
                if (classes.getAnnotation(Controller.class) != null || classes.getAnnotation(RestController.class) != null) {
                    controllerStack = element;
                }
            }
            String classDefined = controllerStack.getClassName();
            if (StringUtils.isNotBlank(classDefined)) {
                Class classs = Class.forName(classDefined);
                Method[] methods = classs.getDeclaredMethods();
                Method excutedMethod = null;
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase(controllerStack.getMethodName())) {
                        excutedMethod = method;
                    }
                }
                if (excutedMethod != null) {
                    ExceptionMessageAnnotation exceptionMessageAnnotation = excutedMethod.getAnnotation(ExceptionMessageAnnotation.class);
                    if (exceptionMessageAnnotation != null) {
                        message = "[" + YYYYMMDDHHMMSS.format(new Date()) + "] " + exceptionMessageAnnotation.errorMessage() + "发生异常:" + message;
                        return message;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getAnnotationErrorMessage", e);
        }

        return message;
    }

    /**
     * 无权限
     *
     * @param re
     * @param request
     * @param response annotation     * @throws Exception
     */
    @ExceptionHandler(NoAuthorizeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void pageAuthorize(Exception re, HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        String uri = request.getRequestURI();
        if (uri.toUpperCase().endsWith(".AJAX")) {
            //返回Ajax数据
            JSONObject resultJson = JSONObjectUtil.getFailResult(re.getMessage(), null);
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.getWriter().print(resultJson.toString());
            response.getWriter().flush();
            response.getWriter().close();
        } else {
            //调转到页面
            request.getRequestDispatcher("/scriptcenter/error/noAuthorize.html").forward(request, response);
        }
    }


}
