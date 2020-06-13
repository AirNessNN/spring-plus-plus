package com.an.springplusplus.core.tool;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 12:45 上午
 * @description 反射支持
 */
public class ReflectUtils {

    /**
     * 实例化一个对象
     * @param tc 目标类
     * @return 调用空参构造实例化对象
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    static
    public Object newInstance(Class<?> tc) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor=tc.getConstructor();
        return constructor.newInstance();
    }



    /**
     * 扫描指定包下的Annotation
     * @param classPath 扫描包路径
     * @param annotation 注解类
     * @return 被扫描到的注解
     */
    public static Set<Class<?>> findClassByAnnotation(String classPath,Class<? extends Annotation> annotation){
        Reflections reflections=new Reflections(classPath);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    /**
     * 全包扫描Annotation
     * @param annotation 注解类
     * @return 被扫描到的注解
     */
    public static Set<Class<?>> findClassByAnnotation(Class<? extends Annotation> annotation){
        Reflections reflections=new Reflections();
        return reflections.getTypesAnnotatedWith(annotation);
    }


    /**
     * 扫描指定包下的子类
     * @param classPath 扫描包路径
     * @param tClass 子类
     * @param <T> 子类泛型
     * @return 返回扫描到的子类
     */
    public static<T> Set<Class<? extends T> > findClass(String classPath,Class<T> tClass){
        Reflections reflections=new Reflections(classPath);
        return reflections.getSubTypesOf(tClass);
    }


    /**
     * 映射查找器内部类
     */
    private static class MappingFinder{

        private final Class<?> targetClass;
        private final Map<Class<? extends Annotation>,Finder> finderMap;
        private boolean onlyPublic=true;


        private MappingFinder(Class<?> target){
            this.targetClass=target;
            finderMap=new HashMap<>();
        }

        public void build(Class<? extends Annotation> ac,Finder finder){
            finderMap.put(ac,finder);
        }

        private void process(){
            //获取方法列表
            Method[] methods = targetClass.getMethods();
            for (Method method : methods) {
                if (onlyPublic&&method.getModifiers()!= Modifier.PUBLIC){
                    continue;
                }
                //获取注解
                List<Class<? extends Annotation>> annotations = Arrays.stream(method.getAnnotations()).map(Annotation::annotationType).collect(Collectors.toList());
                for (Class<? extends Annotation> ac:finderMap.keySet()){
                    int index=annotations.indexOf(ac);
                    if (index>=0){
                        finderMap.get(ac).onMappingFound(method.getAnnotations()[index],method);
                    }
                }
            }
        }
    }

    /**
     * 映射查找器构建器
     */
    public static class MappingFinderBuilder{

        private final MappingFinder mappingFinder;

        /**
         * 实例化
         * @param target
         * @return
         */
        public static MappingFinderBuilder build(Class<?> target){
            return new MappingFinderBuilder(new MappingFinder(target));
        }

        private MappingFinderBuilder(MappingFinder finder){
            this.mappingFinder=finder;
        }

        /**
         * 是否只查找公共方法
         * @param b
         * @return
         */
        public MappingFinderBuilder setFindOnlyPublic(boolean b){
            mappingFinder.onlyPublic=b;
            return this;
        }

        /**
         * 添加新的条件
         * @param ac
         * @param finder
         * @return
         */
        public MappingFinderBuilder addOne(Class<? extends Annotation> ac, Finder finder){
            mappingFinder.build(ac,finder);
            return this;
        }

        /**
         * 执行查找
         */
        public void doIt(){
            mappingFinder.process();
        }
    }


    public interface Finder {
        void onMappingFound(Annotation annotation, Method method);
    }
}

