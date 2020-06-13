package com.an.springplusplus.core.annotation.mapping;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 1:23 下午
 * @description
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {
    String value();

    String contentType() default "application/json";

    String charset() default "utf-8";
}
