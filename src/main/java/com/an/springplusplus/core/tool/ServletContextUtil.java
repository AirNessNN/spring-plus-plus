package com.an.springplusplus.core.tool;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/9 12:32 上午
 * @description
 */
@Slf4j
public class ServletContextUtil {

    private static ServletContext CONTEXT;

    public static synchronized void init(ServletContext context) {
        CONTEXT=context;
        log.info("Context 已加载");
    }



    public static  ServletContext getServletContext(){
        return CONTEXT;
    }
}
