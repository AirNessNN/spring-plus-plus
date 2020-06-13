package com.an.springplusplus.core.annotation.configuration;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 1:22 下午
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AutoConfiguration {
    String value() default "";
}
