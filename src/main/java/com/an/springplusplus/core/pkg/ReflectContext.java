package com.an.springplusplus.core.pkg;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 3:39 下午
 * @description
 */
public interface ReflectContext {

    Set<Class<?>> getScannedClass();

    Set<Class<?>> findClassByAnnotation(Class<? extends Annotation> annotation);

    <T> Set<Class<? extends T> > findClass(Class<T> tClass);
}
