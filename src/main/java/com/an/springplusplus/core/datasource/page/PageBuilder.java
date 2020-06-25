package com.an.springplusplus.core.datasource.page;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/24 11:40 下午
 * @description
 */
public class PageBuilder {

    /**
     * 构建分页
     * @param pageParam
     * @param <T>
     * @return
     */
    public static <T> Page<T> buildPage(PageParam pageParam){
        Page<T> page=new Page<>();
        page.setIndex(pageParam.getIndex());
        page.setSize(pageParam.getSize());
        return page;
    }
}
