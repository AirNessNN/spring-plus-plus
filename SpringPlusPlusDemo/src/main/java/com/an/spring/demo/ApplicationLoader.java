package com.an.spring.demo;

import com.an.springplusplus.core.ServletApplication;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 2:46 下午
 * @description
 */
public class ApplicationLoader implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletApplication application=ServletApplication.run(sce.getServletContext());
    }
}
