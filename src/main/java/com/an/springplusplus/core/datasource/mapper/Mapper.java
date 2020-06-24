package com.an.springplusplus.core.datasource.mapper;

import java.io.Serializable;
import java.util.List;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 8:05 下午
 * @description 对象映射抽象类 存放实体解析与数据库能力
 */
public interface Mapper<E> {

    E getById(Serializable id);

    List<E> getBatchById(List<Serializable> ids);

    int updateById(E entity);

    boolean deleteById(Serializable id);

    int deleteBatchById(List<Serializable> id);

    E insert(E entity);

    List<E> insertBatch(List<E> entities);
}
