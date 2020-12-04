package com.jd.bdp.datadev.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionMessageAnnotation {

    String errorMessage() ;                       //提示给页面信息（eg.保存文件发生异常 , 执行的时候发送异常）

}
