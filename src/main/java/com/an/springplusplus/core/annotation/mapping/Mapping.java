package com.an.springplusplus.core.annotation.mapping;

import java.lang.annotation.*;


/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 1:22 下午
 * @description 普通映射注解
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
    String value();

    String contentType() default "";

    String charset() default "utf-8";
}