package com.an.springplusplus.core.config;

import com.an.springplusplus.core.annotation.configuration.AutoConfiguration;
import com.an.springplusplus.core.pkg.ReflectContext;
import com.an.springplusplus.core.tool.ReflectUtils;
import com.an.springplusplus.core.tool.ServletContextUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/9 12:28 上午
 * @description
 */
@Slf4j
public class ServletContextAutoConfiguration {


    private final Set<Configuration> configurations;


    public ServletContextAutoConfiguration(ApplicationProperties properties,ServletContext  context){
        log.info("初始化ServletContext");
        ServletContextUtil.init(context);
        //自动注册Configuration类

        Set<Class<?>> scanClass= ReflectUtils.findClassByAnnotation(AutoConfiguration.class);
        scanClass.forEach(e->log.info("扫描到配置：{}",e.getName()));

        //获取项目全部的Class
        Set<Class<?>> basePackageClass=ReflectUtils.findClass(properties.getBaseControllerScanClassPath(),Object.class);
        //初始化ReflectContext
        final ReflectContext reflectContext=new ReflectContext() {
            private final String baseClasspath=properties.getBaseControllerScanClassPath();

            @Override
            public Set<Class<?>> getScannedClass() {
                return basePackageClass;
            }

            @Override
            public Set<Class<?>> findClassByAnnotation(Class<? extends Annotation> annotation) {
                return ReflectUtils.findClassByAnnotation(baseClasspath,annotation);
            }

            @Override
            public <T> Set<Class<? extends T>> findClass(Class<T> tClass) {
                return ReflectUtils.findClass(baseClasspath,tClass);
            }
        };

        configurations=new HashSet<>();
        //实例化加载
        for (Class<?> tc:scanClass){
            try {
                //获取接口
                List<Class<?>> interfaces=Arrays.asList(tc.getInterfaces());
                if (interfaces.contains(Configuration.class)){
                    Constructor<?> constructor=tc.getConstructor();
                    Object o=constructor.newInstance();
                    Configuration configuration= (Configuration) o;
                    configuration.init(properties,context);
                    log.info("初始化 {} Configuration",tc.getName());
                    configurations.add(configuration);
                }else if (interfaces.contains(ClassScanConfiguration.class)){
                    Constructor<?> constructor=tc.getConstructor();
                    Object o=constructor.newInstance();
                    ClassScanConfiguration configuration= (ClassScanConfiguration) o;
                    configuration.init(properties,context,reflectContext);
                    log.info("初始化 {} ClassScanConfiguration",tc.getName());
                }else {
                    log.warn("未扫描到 {} 实现任何 Configuration 接口",tc.getName());
                }
            }catch (Exception e){
                e.printStackTrace();
                log.warn("实例化 {} 时发生错误 : {}",tc.getName(),e.getMessage());
            }
        }
    }

    public synchronized void destroy(){
        log.info("销毁Configuration");
        configurations.forEach(Configuration::destroy);
    }


}
