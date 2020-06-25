package com.an.springplusplus.core.config;

import com.an.springplusplus.core.annotation.configuration.AutoConfiguration;
import com.an.springplusplus.core.annotation.mapping.*;
import com.an.springplusplus.core.bean.Autowired;
import com.an.springplusplus.core.bean.Component;
import com.an.springplusplus.core.pkg.ReflectContext;
import com.an.springplusplus.core.servlet.mapping.ServletMapping;
import com.an.springplusplus.core.tool.BeanUtils;
import com.an.springplusplus.core.tool.ReflectUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/25 1:15 上午
 * @description
 */
@Slf4j
@AutoConfiguration
public class BeanConfiguration implements ClassScanConfiguration{


    private Set<Class<?>> beanList;

    private Map<String,Object> proxyBean;

    private Map<Class<?>,Object> components;


    private Set<String> patterns;

    private ApplicationProperties properties;

    private final Set<String> servletMappingNameSet=new HashSet<>();

    @Override
    public void init(ApplicationProperties properties, ServletContext context, ReflectContext reflectContext) {
        patterns=new HashSet<>();
        this.properties=properties;
        log.debug("开始扫描Component组件");
        Set<Class<?>> components=reflectContext.findClassByAnnotation(Component.class);
        components.forEach(e->log.info("扫描到组件{}",e.getName()));

        //扫描Controller
        Set<Class<?>> controllerSet = filterAnnotation(components,Controller.class);
        //扫描RestController
        Set<Class<?>> restControllerSet = filterAnnotation(components,RestController.class);

        this.components=newInstanceBatch(components);
        autowiredComponent(this.components);
        BeanUtils.initBeanUtils(this.components);

        controllerSet.forEach(e -> {
            log.info("扫描到 Controller : {}", e.getName());
            searchMapping(this.components.get(e), context);
        });
        restControllerSet.forEach(e -> {
            log.info("扫描到 RestController : {}", e.getName());
            searchRestMapping(this.components.get(e), context);
        });

        log.debug("结束扫描Component组件");

        try {
            initFilter(context);
        } catch (ServletException e) {
            e.printStackTrace();
        }

    }


    /**
     * 过滤类型
     * @param classes
     * @param annotation
     * @return
     */
    private Set<Class<?>>  filterAnnotation(Set<Class<?>> classes,Class<? extends Annotation> annotation){
        return classes.stream().filter(e-> Arrays.stream(e.getAnnotations()).anyMatch(e1->e1.annotationType()
                .equals(annotation))).collect(Collectors.toSet());
    }

    private Map<Class<?>,Object> newInstanceBatch(Set<Class<?>> classes){
        Map<Class<?>,Object> map=new HashMap<>();
        for (Class<?> tc:classes){
            Constructor<?> constructor;
            try {
                constructor = tc.getConstructor();
                Object instance=constructor.newInstance();
                map.put(tc,instance);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.debug("无法实例化组件:{}",e.toString());
            }
        }
        return map;
    }

    private void autowiredComponent(Map<Class<?>,Object> components){
        if (components==null){
            return;
        }
        components.forEach((k,v)->{
            //获取父类成员
            Set<Field> supportedFields=new HashSet<>(Arrays.asList(k.getSuperclass().getDeclaredFields()));
            //获取当前类成员
            Set<Field> fields = new HashSet<>(Arrays.asList(k.getDeclaredFields()));
            //加入父类成员一起遍历
            fields.addAll(supportedFields);
            fields.forEach(e->{
                //查找@Autowired注解
                Autowired autowired=e.getAnnotation(Autowired.class);
                if (autowired==null){
                    return;
                }
                //找到注解
                Class<?> com=e.getType();
                if (com.isInterface()){
                    //如果成员是接口，则查找有实现的实现类
                    Class<?> finalCom = com;
                    com= components.keySet().stream().filter(_k-> Arrays.asList(_k.getInterfaces()).contains(finalCom)).findFirst().orElse(null);
                }
                //检查是否是泛型
                Type type=e.getGenericType();
                if (type instanceof TypeVariable){
                    //是一个类型泛型 T field
                    //判断是否是父类成员
                    if (supportedFields.contains(e)){
                        //是父类成员
                        Type supportType=k.getGenericSuperclass();
                        if (supportType instanceof ParameterizedType){
                            ParameterizedType wildcardType= (ParameterizedType) supportType;
                            Type[] t=wildcardType.getActualTypeArguments();
                            try {
                                com=Class.forName(t[0].getTypeName());
                            } catch (ClassNotFoundException classNotFoundException) {
                                classNotFoundException.printStackTrace();
                            }
                        }
                    }
                }
                Object comInstance=components.get(com);
                if (comInstance==null){
                    return;
                }
                //存在这个组件
                e.setAccessible(true);
                try {
                    e.set(v,comInstance);
                    log.info("注入组件{} 到 {}",comInstance.getClass().getName(),v.getClass().getName());
                } catch (IllegalAccessException illegalAccessException) {
                    log.error("注入组件失败:{}",illegalAccessException.getMessage());
                    throw new RuntimeException(illegalAccessException);
                }
            });
        });
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
     * @param instance 扫描的Class
     */
    private void searchRestMapping(Object instance, ServletContext context) {
        ServletMapping servletMapping=instanceController(instance,context);
        ReflectUtils.MappingFinderBuilder.build(instance.getClass()).addOne(GetMapping.class,(annotation, method)->{
            GetMapping getMapping = (GetMapping) annotation;
            String urlPattern=getUrlPattern(getMapping.value());
            assertUrlNotExist(urlPattern, instance.getClass());
            if (getMapping.returnPath()){
                servletMapping.addPathPattern(getMapping.value(), ServletMapping.METHOD.GET,getMapping.contentType(),getMapping.charset(),method);
            }else {
                servletMapping.addPattern(getMapping.value(), ServletMapping.METHOD.GET,getMapping.contentType(),getMapping.charset(),method);
            }
            log.info("注册 GetHttpServlet [{}] {}", getMapping.value(), method.getName());
        }).addOne(PostMapping.class,(annotation, method)->{
            PostMapping postMapping = (PostMapping) annotation;
            String urlPattern=getUrlPattern(postMapping.value());
            assertUrlNotExist(urlPattern, instance.getClass());
            servletMapping.addPattern(postMapping.value(), ServletMapping.METHOD.POST,postMapping.contentType(),postMapping.charset(),method);
            log.info("注册 PostHttpServlet [{}] {}", postMapping.value(), method.getName());
        }).doIt();
    }

    /**
     * 动态创建HttpServlet
     * @param instance
     * @param context
     */
    private void searchMapping(Object instance, ServletContext context){
        ServletMapping servletMapping=instanceController(instance,context);

        ReflectUtils.MappingFinderBuilder.build(instance.getClass()).addOne(Mapping.class,(annotation, method)->{
            Mapping getMapping = (Mapping) annotation;
            String urlPattern=getUrlPattern(getMapping.value());
            assertUrlNotExist(urlPattern, instance.getClass());
            servletMapping.addPattern(getMapping.value(), ServletMapping.METHOD.GET,getMapping.contentType(),getMapping.charset(),method);
            log.info("注册 GetHttpServlet [{}] {}", getMapping.value(), method.getName());
        }).doIt();
    }


    /**
     * 实例化Controller
     * @param instance
     * @param context
     * @return
     */
    private ServletMapping instanceController(Object instance,ServletContext context){
        //实例化Controller
        Class<?> tClass=instance.getClass();
        String[] tmp=tClass.getName().split("\\.");
        String className=tmp[tmp.length-1];
        if (!servletMappingNameSet.add(className)){
            throw new RuntimeException("Controller "+className+" 名称存在冲突");
        }
        log.debug("Controller Name {}",className);
        ServletMapping servletMapping=new ServletMapping(className, tClass, context);
        servletMapping.initServlet();
        servletMapping.setController(instance);
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
