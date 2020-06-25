package com.an.springplusplus.core.datasource.mapper;

import com.an.springplusplus.core.datasource.page.Page;
import com.an.springplusplus.core.datasource.wrapper.EntityWrapper;
import com.an.springplusplus.core.datasource.wrapper.QueryWrapper;
import com.an.springplusplus.core.datasource.wrapper.SelectWrapper;

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
public abstract class BaseMapper<E>{


    public E getById(Serializable id) {
        return new QueryWrapper<>(getGenericSuperclass()).getById(id);
    }

    public Page<E> getPage(Page<E> page, SelectWrapper<E> wrapper){
        //直接获取条件
        List<E> records=wrapper.list();
        int totals=records.size()/page.getSize();
        totals=records.size()%page.getSize()>0?totals+1:totals;
        page.setTotals(totals);
        int endIndex=((page.getIndex()-1)*page.getSize())+page.getSize();
        endIndex= Math.min(endIndex, records.size());
        page.setRecords(records.subList((page.getIndex()-1)*page.getSize(),endIndex));
        return page;
    }

    public List<E> getBatchById(List<Serializable> ids) {
        return new QueryWrapper<>(getGenericSuperclass()).getBatchById(ids);
    }

    public int updateById(E entity) {
        return new EntityWrapper<>(getGenericSuperclass()).updateById(entity);
    }

    public boolean deleteById(Serializable id) {
        return new EntityWrapper<>(getGenericSuperclass()).deleteById(id);
    }

    public int deleteBatchById(List<Serializable> id) {
        return new EntityWrapper<>(getGenericSuperclass()).deleteBatchById(id);
    }

    public E insert(E entity) {
        return new EntityWrapper<>(getGenericSuperclass()).insert(entity);
    }

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
