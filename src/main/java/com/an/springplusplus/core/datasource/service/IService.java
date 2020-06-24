package com.an.springplusplus.core.datasource.service;

import com.an.springplusplus.core.datasource.wrapper.SelectWrapper;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 8:04 下午
 * @description
 */
public interface IService<E> {


    SelectWrapper query();
}
