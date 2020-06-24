package com.an.springplusplus.core.datasource.mapper;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/21 8:48 上午
 * @description 数据表 信息注解
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * 表名
     * @return
     */
    String tableName() default "";
}
