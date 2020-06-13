package com.an.springplusplus.core.servlet.processor;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 5:32 下午
 * @description
 */
public interface ServletProcessor {

    void doService(ServletRequest req, ServletResponse resp, Method method, Class<?> tc, Object controller, String contentType, String charset, boolean returnPath) throws ServletException, IOException;
}
