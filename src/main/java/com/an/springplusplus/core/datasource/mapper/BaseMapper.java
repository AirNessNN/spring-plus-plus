package com.an.springplusplus.core.datasource.mapper;

import com.an.springplusplus.core.datasource.wrapper.EntityWrapper;
import com.an.springplusplus.core.datasource.wrapper.QueryWrapper;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/23 10:33 下午
 * @description
 */
public class BaseMapper<E> implements Mapper<E>{


    @Override
    public E getById(Serializable id) {
        return new QueryWrapper<>(getGenericSuperclass()).getById(id);
    }

    @Override
    public List<E> getBatchById(List<Serializable> ids) {
        return new QueryWrapper<>(getGenericSuperclass()).getBatchById(ids);
    }

    @Override
    public int updateById(E entity) {
        return new EntityWrapper<>(getGenericSuperclass()).updateById(entity);
    }

    @Override
    public boolean deleteById(Serializable id) {
        return new EntityWrapper<>(getGenericSuperclass()).deleteById(id);
    }

    @Override
    public int deleteBatchById(List<Serializable> id) {
        return new EntityWrapper<>(getGenericSuperclass()).deleteBatchById(id);
    }

    @Override
    public E insert(E entity) {
        return new EntityWrapper<>(getGenericSuperclass()).insert(entity);
    }

    @Override
    public List<E> insertBatch(List<E> entities) {
        return new EntityWrapper<>(getGenericSuperclass()).insertBatch(entities);
    }


    /**
     * 获取父类
     * @return
     */
    private Class<E> getGenericSuperclass(){
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        return  (Class) (pt.getActualTypeArguments()[0]);
    }
}
