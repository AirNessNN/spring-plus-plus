package com.an.springplusplus.core.config;

import com.an.springplusplus.core.pkg.ReflectContext;

import javax.servlet.ServletContext;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 3:36 下午
 * @description 带类扫描器的配置器
 */
public interface ClassScanConfiguration {


    /**
     * 初始化方法
     * @param properties 配置
     * @param context Servlet上下文
     * @param reflectContext 反射支持上下文
     */
    void init(ApplicationProperties properties, ServletContext context, ReflectContext reflectContext);
}
