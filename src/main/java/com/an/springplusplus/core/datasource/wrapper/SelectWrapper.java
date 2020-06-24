package com.an.springplusplus.core.datasource.wrapper;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 8:06 下午
 * @description
 */
public interface SelectWrapper<E> extends Wrapper {

    E getById(Serializable id);

    List<E> getBatchById(List<Serializable> ids);

    List<E> list();

    E one();

    Optional<E> optionalOne();

    E oneOrNull();
}
