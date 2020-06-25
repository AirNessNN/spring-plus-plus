package com.an.springplusplus.core.datasource.service;

import com.an.springplusplus.core.bean.Autowired;
import com.an.springplusplus.core.datasource.mapper.BaseMapper;
import com.an.springplusplus.core.datasource.page.Page;
import com.an.springplusplus.core.datasource.wrapper.EntityWrapper;
import com.an.springplusplus.core.datasource.wrapper.QueryWrapper;
import com.an.springplusplus.core.datasource.wrapper.SelectWrapper;

import java.io.Serializable;
import java.util.List;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 8:05 下午
 * @description
 */
public class ServiceImpl<M extends BaseMapper<E>,E> implements IService<E>{

    @Autowired
    private M baseMapper;

    public E getById(Serializable id) {
        return baseMapper.getById(id);
    }

    public Page<E> getPage(Page<E> page, SelectWrapper<E> wrapper) {
        //直接获取条件
        return baseMapper.getPage(page,wrapper);
    }

    public List<E> getBatchById(List<Serializable> ids) {
        return baseMapper.getBatchById(ids);
    }

    public int updateById(E entity) {
        return baseMapper.updateById(entity);
    }

    public boolean deleteById(Serializable id) {
        return baseMapper.deleteById(id);
    }

    public int deleteBatchById(List<Serializable> id) {
        return baseMapper.deleteBatchById(id);
    }

    public E insert(E entity) {
        return baseMapper.insert(entity);
    }

    public List<E> insertBatch(List<E> entities) {
        return baseMapper.insertBatch(entities);
    }
}
