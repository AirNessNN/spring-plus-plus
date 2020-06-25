package com.an.springplusplus.core.tool;

import java.util.Map;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/25 2:02 下午
 * @description
 */
public class BeanUtils {



    private static BeanUtils beanUtils;

    private Map<Class<?>,Object> beans;

    private BeanUtils(Map<Class<?>,Object> beans){
        this.beans=beans;
    }

    public static void initBeanUtils(Map<Class<?>,Object> beans){
        if (beanUtils==null){
            beanUtils=new BeanUtils(beans);
        }
    }


    public static Object getBean(Class<?> tc){
        return beanUtils.beans.get(tc);
    }
}
