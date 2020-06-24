package com.an.springplusplus.core.datasource.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/21 8:51 上午
 * @description 表主键策略枚举
 */
@AllArgsConstructor
@Getter
public enum TableIdEnum {
    UUID("UUID"),
    PRIMARY_KEY("PRIMARYKEY");
    private final String value;
}
