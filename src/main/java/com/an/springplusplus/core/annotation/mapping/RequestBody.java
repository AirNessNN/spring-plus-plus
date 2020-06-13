package com.an.springplusplus.core.annotation.mapping;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 1:20 下午
 * @description POST Body标记
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
}
