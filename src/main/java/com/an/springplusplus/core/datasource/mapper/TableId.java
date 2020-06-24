package com.an.springplusplus.core.datasource.mapper;

import java.lang.annotation.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/23 10:34 下午
 * @description
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
    TableIdEnum idType() default TableIdEnum.PRIMARY_KEY;
}
