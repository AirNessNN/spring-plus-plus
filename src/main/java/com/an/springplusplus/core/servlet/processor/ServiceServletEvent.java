package com.an.springplusplus.core.servlet.processor;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 5:46 下午
 * @description
 */
public interface ServiceServletEvent {

    /**
     * 执行服务
     * @param req
     * @param resp
     */
    Object doService(ServletRequest req, ServletResponse resp) throws ServletException, IOException;
}
