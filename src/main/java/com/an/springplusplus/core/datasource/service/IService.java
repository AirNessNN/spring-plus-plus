package com.an.springplusplus.core.datasource.service;

import com.an.springplusplus.core.datasource.page.Page;
import com.an.springplusplus.core.datasource.wrapper.SelectWrapper;

import java.io.Serializable;
import java.util.List;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 8:04 下午
 * @description
 */
public interface IService<E> {

    E getById(Serializable id);

    Page<E> getPage(Page<E> page, SelectWrapper<E> wrapper);

    List<E> getBatchById(List<Serializable> ids);

    int updateById(E entity);

    boolean deleteById(Serializable id);

    int deleteBatchById(List<Serializable> id);

    E insert(E entity);

    List<E> insertBatch(List<E> entities);
}
