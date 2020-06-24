package com.an.springplusplus.core.tool;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 12:45 上午
 * @description 反射支持
 */
public class ReflectUtils {

    private final static String GETTER_PATTERN="^get\\w+?$";
    private final static String SETTER_PATTERN="^set\\w+?$";

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
     * 获取类的属性 GetSet
     * @param tClass
     * @return
     */
    public static Map<String,Field> findPropertiesGetSet(Class<?> tClass){
        Map<String,Field> fieldNames=new HashMap<>();
        Field[] fields=tClass.getDeclaredFields();

        //GetSet属性必须满足以下几个条件
        //必须是公共方法public 必须get set齐全，get返回类型和set参数必须一致且set参数表只能存在一个

        for (Field field:fields){
            String fieldName=field.getName();
            //获取Getter
            try {
                Method getter=tClass.getMethod("get"+StringUtils.toUpperCaseFirst(fieldName));
                Method setter=tClass.getMethod("set"+StringUtils.toUpperCaseFirst(fieldName),field.getType());
                Parameter[] parameters=setter.getParameters();
                if (parameters.length>1){
                    //参数表参数大于1
                    continue;
                }
                if (!getter.getReturnType().equals(field.getType())||!parameters[0].getType().equals(field.getType())){
                    //返回类型不匹配或参数类型不匹配
                    continue;
                }
                if (!Modifier.isPublic(getter.getModifiers())||!Modifier.isPublic(setter.getModifiers())){
                    //非Public方法
                    continue;
                }
                fieldNames.put(fieldName,field);
            } catch (NoSuchMethodException e) {
                //没有Getter 或者Setter
            }
        }
        return fieldNames;
    }

    /**
     * 根据注解找到字段
     * @param target
     * @param annotationClass
     * @return
     */
    public static Map<Field,Annotation> findFieldByAnnotation(Class<?> target,Class<? extends Annotation> annotationClass){
        Field[] fields=target.getDeclaredFields();
        Map<Field,Annotation> map=new HashMap<>();
        for (Field f:fields){
            Annotation annotation=f.getAnnotation(annotationClass);
            if (annotation==null){
                continue;
            }
            map.put(f,annotation);
        }
        return map;
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


    public  static Class<?> getSuperClassGenericType(Class<?> clazz, int index)
            throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) params[index];
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

