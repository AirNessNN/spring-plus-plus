package com.an.springplusplus.core.annotation.mapping;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 1:25 下午
 * @description 映射扫描入口
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
}
