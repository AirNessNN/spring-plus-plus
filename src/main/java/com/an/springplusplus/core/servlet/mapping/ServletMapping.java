package com.an.springplusplus.core.servlet.mapping;

import com.an.springplusplus.core.servlet.processor.RestHttpServletProcessor;
import com.an.springplusplus.core.servlet.processor.ServletProcessor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 4:31 下午
 * @description
 */
@Slf4j
public class ServletMapping {

    public enum METHOD {
        GET, POST, PUT
    }

    @Setter
    private Object controller;

    private final Class<?> target;
    private final ServletProcessor processor;
    private final ServletRegistration.Dynamic dynamic;
    private final Map<String, ApiInfo> patternApiMap;







    /**
     * 初始化一个Mapping包装
     *
     * @param tClass
     */
    public ServletMapping(String name, Class<?> tClass, ServletContext context) {
        target = tClass;
        this.patternApiMap = new HashMap<>();
        //内建Servlet
        Servlet servlet = initServlet();
        this.dynamic = context.addServlet(name, servlet);
        processor = new RestHttpServletProcessor();
    }


    /**
     * 添加映射
     * @param urlPattern
     * @param method
     * @param contentType
     * @param charset
     * @param api
     */
    public void addPattern(String urlPattern, METHOD method, String contentType, String charset, Method api) {
        ApiInfo info = new ApiInfo();
        info.setUrlPattern(urlPattern);
        info.setApi(api);
        info.setCharset(charset);
        info.setMethod(method);
        info.setContentType(contentType);
        info.setReturnPath(false);
        //检查url
        patternApiMap.put(addPrefix(urlPattern), info);
        dynamic.addMapping(addPrefix(urlPattern));
        log.debug("{} 映射到 URL Mapping", urlPattern);
    }

    /**
     * 添加映射
     * @param urlPattern
     * @param method
     * @param contentType
     * @param charset
     * @param api
     */
    public void addPathPattern(String urlPattern, METHOD method, String contentType, String charset, Method api){
        if (!api.getReturnType().equals(String.class)){
            addPattern(urlPattern,method,contentType,charset,api);
            return;
        }
        ApiInfo info = new ApiInfo();
        info.setUrlPattern(urlPattern);
        info.setApi(api);
        info.setCharset(charset);
        info.setMethod(method);
        info.setContentType(contentType);
        info.setReturnPath(true);
        //检查url
        patternApiMap.put(addPrefix(urlPattern), info);
        dynamic.addMapping(addPrefix(urlPattern));
        log.debug("{} 映射到 URL Mapping", urlPattern);
    }

    private String addPrefix(String urlPattern){
        if (urlPattern.indexOf("/")>0){
            return "/"+urlPattern;
        }
        return urlPattern;
    }



    /**
     * 初始化Servlet
     *
     * @return
     */
    public Servlet initServlet() {
        return new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                String method = req.getMethod();
                String urlPath = req.getServletPath();
                String contextPath = req.getContextPath();
                String host = req.getRemoteHost();
                log.debug("method={} urlPath={} contextPath={} host={}", method, urlPath, contextPath, host);
                ApiInfo info = match(urlPath);
                if (info == null) {
                    log.warn("匹配不到路径");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                log.debug("请求通过");
                try {
                    processor.doService(req, resp, info.api, target, controller, info.contentType, info.charset, info.returnPath);
                } catch (ServletException e) {
                    log.error("处理请求发生错误");
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        };
    }


    private ApiInfo match(String urlPath) {
        Optional<String> opt = patternApiMap.keySet().stream().filter(e -> isMatch(urlPath, e)).findFirst();
        return opt.map(patternApiMap::get).orElse(null);
    }

    /**
     * 是否匹配路径
     *
     * @param urlPath
     * @param urlPattern
     * @return
     */
    private boolean isMatch(String urlPath, String urlPattern) {
        String regx = getRegPath(urlPattern);
        boolean b=Pattern.matches(regx,urlPath);
        log.debug("Path={} Pattern={} Regx={} 是否匹配={}", urlPath, urlPattern, regx,b);
        return b;
    }

    /**
     * 将Pattern转换为为Regx
     *
     * @param path
     * @return
     */
    private String getRegPath(String path) {
        char[] chars = path.toCharArray();
        int len = chars.length;
        StringBuilder sb = new StringBuilder();
        boolean preX = false;
        for (int i = 0; i < len; i++) {
            if (chars[i] == '*') {//遇到*字符
                if (preX) {//如果是第二次遇到*，则将**替换成.*
                    sb.append(".*");
                    preX = false;
                } else if (i + 1 == len) {//如果是遇到单星，且单星是最后一个字符，则直接将*转成[^/]*
                    sb.append("[^/]*");
                } else {//否则单星后面还有字符，则不做任何动作，下一把再做动作
                    preX = true;
                }
            } else {//遇到非*字符
                if (preX) {//如果上一把是*，则先把上一把的*对应的[^/]*添进来
                    sb.append("[^/]*");
                    preX = false;
                }
                if (chars[i] == '?') {//接着判断当前字符是不是?，是的话替换成.
                    sb.append('.');
                } else {//不是?的话，则就是普通字符，直接添进来
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }


    /**
     * ApiInfo
     */
    @Data
    private static class ApiInfo {
        private String urlPattern;
        private String contentType;
        private String charset;
        private METHOD method;
        private Method api;
        private Boolean returnPath;
    }


}
