package com.an.springplusplus.core.config;

import javax.servlet.ServletContext;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 1:43 下午
 * @description
 */
public interface Configuration {

    void init(ApplicationProperties properties, ServletContext context);
}
