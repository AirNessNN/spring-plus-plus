package com.an.springplusplus.core.datasource.page;

import lombok.Data;

import java.util.Collection;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/24 11:40 下午
 * @description
 */
@Data
public class Page<T> {

    private int index=1;

    private int size=10;

    private int totals=0;

    private Collection<T> records;
}
