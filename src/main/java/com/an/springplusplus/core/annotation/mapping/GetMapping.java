package com.an.springplusplus.core.annotation.mapping;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 1:23 下午
 * @description Get Url映射注解
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GetMapping {
    String value();

    boolean returnPath() default false;

    String contentType() default "application/json";

    String charset() default "utf-8";
}
