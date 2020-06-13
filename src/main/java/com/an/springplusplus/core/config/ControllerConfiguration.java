package com.an.springplusplus.core.config;

import com.an.springplusplus.core.annotation.configuration.AutoConfiguration;
import com.an.springplusplus.core.annotation.mapping.*;
import com.an.springplusplus.core.pkg.ReflectContext;
import com.an.springplusplus.core.servlet.mapping.ServletMapping;
import com.an.springplusplus.core.tool.ReflectUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 2:03 下午
 * @description
 */
@Slf4j
@AutoConfiguration
public class ControllerConfiguration implements ClassScanConfiguration {


    private Set<String> patterns;

    private ApplicationProperties properties;

    private final Set<String> servletMappingNameSet=new HashSet<>();

    @Override
    public void init(ApplicationProperties properties, ServletContext context, ReflectContext reflectContext) {
        this.properties=properties;
        patterns=new HashSet<>();
        //扫描Controller
        Set<Class<?>> controllerSet = reflectContext.findClassByAnnotation(Controller.class);
        controllerSet.forEach(e -> {
            log.info("扫描到 Controller : {}", e.getName());
            searchMapping(e, context);
        });

        //扫描RestController
        Set<Class<?>> restControllerSet = reflectContext.findClassByAnnotation(RestController.class);
        restControllerSet.forEach(e -> {
            log.info("扫描到 RestController : {}", e.getName());
            searchRestMapping(e, context);
        });

        //初始化Filter
        try {
            initFilter(context);
        } catch (ServletException e) {
            log.error("无法初始化RouteFilter",e);
            throw new RuntimeException(e.getMessage(),e);
        }
    }


    /**
     * 初始化内建路由Filter
     * @param context
     */
    private void initFilter(ServletContext context) throws ServletException {
        RouteFilter filter=context.createFilter(RouteFilter.class);
        filter.properties=properties;
        FilterRegistration.Dynamic dynamic=context.addFilter("RouteFilter",filter);
        dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),true,"/*");
    }


    /**
     * 动态创建HttpServlet
     *
     * @param tClass 扫描的Class
     */
    private void searchRestMapping(Class<?> tClass, ServletContext context) {
        ServletMapping servletMapping=instanceController(tClass,context);
        ReflectUtils.MappingFinderBuilder.build(tClass).addOne(GetMapping.class,(annotation, method)->{
            GetMapping getMapping = (GetMapping) annotation;
            String urlPattern=getUrlPattern(getMapping.value());
            assertUrlNotExist(urlPattern, tClass);
            if (getMapping.returnPath()){
                servletMapping.addPathPattern(getMapping.value(), ServletMapping.METHOD.GET,getMapping.contentType(),getMapping.charset(),method);
            }else {
                servletMapping.addPattern(getMapping.value(), ServletMapping.METHOD.GET,getMapping.contentType(),getMapping.charset(),method);
            }
            log.info("注册 GetHttpServlet [{}] {}", getMapping.value(), method.getName());
        }).addOne(PostMapping.class,(annotation,method)->{
            PostMapping postMapping = (PostMapping) annotation;
            String urlPattern=getUrlPattern(postMapping.value());
            assertUrlNotExist(urlPattern, tClass);
            servletMapping.addPattern(postMapping.value(), ServletMapping.METHOD.POST,postMapping.contentType(),postMapping.charset(),method);
            log.info("注册 PostHttpServlet [{}] {}", postMapping.value(), method.getName());
        }).doIt();
    }

    /**
     * 动态创建HttpServlet
     * @param tClass
     * @param context
     */
    private void searchMapping(Class<?> tClass, ServletContext context){
        ServletMapping servletMapping=instanceController(tClass,context);

        ReflectUtils.MappingFinderBuilder.build(tClass).addOne(Mapping.class,(annotation, method)->{
            Mapping getMapping = (Mapping) annotation;
            String urlPattern=getUrlPattern(getMapping.value());
            assertUrlNotExist(urlPattern, tClass);
            servletMapping.addPattern(getMapping.value(), ServletMapping.METHOD.GET,getMapping.contentType(),getMapping.charset(),method);
            log.info("注册 GetHttpServlet [{}] {}", getMapping.value(), method.getName());
        }).doIt();
    }


    /**
     * 实例化Controller
     * @param tClass
     * @param context
     * @return
     */
    private ServletMapping instanceController(Class<?> tClass,ServletContext context){
        //实例化Controller
        String[] tmp=tClass.getName().split("\\.");
        String className=tmp[tmp.length-1];
        if (!servletMappingNameSet.add(className)){
            throw new RuntimeException("Controller "+className+" 名称存在冲突");
        }
        log.debug("Controller Name {}",className);
        Object controller;
        try {
            controller = ReflectUtils.newInstance(tClass);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("无法实例化目标控制器 {}，控制器可能没有默认构造，或者是私有构造", tClass.getName());
            throw new RuntimeException(e.getMessage(),e);
        }
        ServletMapping servletMapping=new ServletMapping(className, tClass, context);
        servletMapping.initServlet();
        servletMapping.setController(controller);
        return servletMapping;
    }

    private String getUrlPattern(String url){
        return url.indexOf("/")>0?"/"+url:url;
    }

    private void assertUrlNotExist(String urlPattern, Class<?> tc){
        if (!patterns.add(urlPattern)){
            throw new RuntimeException(String.format("Controller: %s URL %s 匹配存在冲突",tc.getName(),urlPattern));
        }
    }


    /**
     * 内置路由分发过滤器
     */
    public static class RouteFilter implements Filter {

        private ApplicationProperties properties;


        @Override
        public void init(FilterConfig filterConfig) { }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            log.debug("通过过滤器");
            HttpServletRequest req= (HttpServletRequest) request;
            if (!req.getMethod().equals("POST")||!req.getMethod().equals("PUT")||!req.getMethod().equals("DELETE")){
                log.debug("非POST、PUT、DELETE请求，跳过检查Content-Type");
                chain.doFilter(request,response);
                return;
            }
            req.setCharacterEncoding(StandardCharsets.UTF_8.name());
            if (!validContentType(req,response,properties.getAcceptContentType())){
                log.error("不支持的Content-Type {}",req.getContentType());
            }
        }


        /**
         * 校验ContentType
         * @param req
         * @param resp
         * @param contentType
         * @return
         * @throws IOException
         */
        private boolean validContentType(ServletRequest req,ServletResponse resp,String contentType) throws IOException {
            String reqContentType=req.getContentType();
            if (!contentType.equals(reqContentType)){
                ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                return false;
            }
            return true;
        }
    }
}
