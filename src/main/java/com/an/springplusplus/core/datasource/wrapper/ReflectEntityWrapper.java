package com.an.springplusplus.core.datasource.wrapper;

import com.an.springplusplus.core.datasource.mapper.Table;
import com.an.springplusplus.core.datasource.mapper.TableId;
import com.an.springplusplus.core.datasource.mapper.TableIdEnum;
import com.an.springplusplus.core.tool.ReflectUtils;
import com.an.springplusplus.core.tool.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 9:59 下午
 * @description
 */
@Slf4j
@Getter
public class ReflectEntityWrapper<E> {

    protected Class<E> targetClass;

    //key是columnName value是fieldName
    protected Map<String,String> fieldNameToColumnNameMap;
    protected Map<String, Field> fieldMap;

    protected String tableName;
    protected String tableId;
    protected TableIdEnum tableIdEnum;

    private boolean isUnderscoreToCamelCase;



    private void init(boolean isUnderscoreToCamelCase,Class<E> tClass){
        targetClass=tClass;
        this.isUnderscoreToCamelCase=isUnderscoreToCamelCase;
        this.fieldNameToColumnNameMap =initMapperFields(targetClass);
        this.tableName=initTableName(targetClass);
        initTableId(targetClass);
    }

    public ReflectEntityWrapper(Class<E> tClass){
        init(true,tClass);
    }

    public ReflectEntityWrapper(Class<E> tClass,boolean isUnderscoreToCamelCase){
        init(isUnderscoreToCamelCase,tClass);
    }

    /**
     * 初始化数据库映射
     * @param targetClass
     * @return
     */
    private Map<String,String> initMapperFields(Class<E> targetClass) {
        Map<String,Field> fieldNames=ReflectUtils.findPropertiesGetSet(targetClass);
        Map<String,String> paramNameToColumnNameMap=new HashMap<>();
        Map<String,String> fieldNameToParamNameMap=new HashMap<>();
        //方法名循环   转换出列名
        for (String paramName:fieldNames.keySet()){
            if (isUnderscoreToCamelCase){
                //驼峰转下划线
                //getName
                paramNameToColumnNameMap.put(paramName, StringUtils.humpToUnderline(paramName));
                fieldNameToParamNameMap.put(StringUtils.humpToUnderline(paramName),paramName);
            }else {
                //原始方式
                paramNameToColumnNameMap.put(paramName,paramName);
            }
        }
        this.fieldMap=fieldNames;
        return paramNameToColumnNameMap;
    }

    /**
     * 初始化表名
     * @param targetClass
     * @return
     */
    private String initTableName(Class<E> targetClass){
        Table table=targetClass.getAnnotation(Table.class);
        if (table==null){
            throw new RuntimeException("找不到TableName");
        }
        return table.tableName();
    }

    /**
     * 初始化表主键
     * @param targetClass
     */
    private void initTableId(Class<E> targetClass){
        Map<Field, Annotation> annotationMap=ReflectUtils.findFieldByAnnotation(targetClass, TableId.class);
        if (annotationMap.size()>1){
            throw new RuntimeException("Id主键冲突：存在多个TableId注解");
        }
        if (annotationMap.size()==0){
            return;
        }
        Field field=annotationMap.keySet().toArray(new Field[0])[0];
        TableId annotation= (TableId) annotationMap.get(field);
        this.tableId=field.getName();
        this.tableIdEnum=annotation.idType();
    }


    /**
     * 实体对象转SQL Map
     * @param ignoreNull
     * @return String [表字段名] Serializable[值]
     */
    protected Map<String, Serializable> convertEntityToSqlMap(boolean ignoreNull,E entity){
        //遍历Entity的属性，获取到数据库列名
        Map<String, Serializable> map=new HashMap<>();
        for (String fieldName: fieldNameToColumnNameMap.keySet()){
            try {
                Method getter=targetClass.getMethod(
                        "get"+StringUtils.toUpperCaseFirst(fieldName));
                Serializable value= (Serializable) getter.invoke(entity);
                if (value==null&&ignoreNull){
                    //忽略空值
                    continue;
                }
                map.put(fieldNameToColumnNameMap.get(fieldName),value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("注入{}失败",fieldName,e);
                throw new RuntimeException("注入实体参数发生错误",e);
            }
        }
        return map;
    }


    /**
     * 填充ID
     * @param id
     * @param entity
     * @return
     */
    protected void putIdToEntity(Object id,E entity){
        try {
            Method setter=targetClass.getMethod(
                    "set"+StringUtils.toUpperCaseFirst(tableId),fieldMap.get(tableId).getType());
            if (fieldMap.get(tableId).getType().equals(Integer.class)) {
                //将Long 转换成Int
                id=Long.valueOf(id.toString()).intValue();
            }
            setter.invoke(entity,id);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("无法填充ID",e);
        }
    }

    /**
     * 拼接参数
     * @param open
     * @param close
     * @param separator
     * @param collection
     * @return
     */
    protected String appendParam(String open,String close,String separator,Collection<?> collection){
        StringBuilder sb=new StringBuilder(open);
        boolean b=false;
        for (Object s:collection){
            if (b)sb.append(separator).append(s.toString()); else sb.append(s.toString());
            b=true;
        }
        sb.append(close);
        return sb.toString();
    }

    protected String appendParam(String open,String close,String separator,Collection<?> collection,String replace){
        StringBuilder sb=new StringBuilder(open);
        boolean b=false;
        for (int i=0;i<collection.size();i++){
            if (b)sb.append(separator).append(replace); else sb.append(replace);
            b=true;
        }
        sb.append(close);
        return sb.toString();
    }


    /**
     * 将Set数据填充至List
     * @param resultSet
     * @return
     */
    protected List<E> resultSetToEntity(ResultSet resultSet){
        if (resultSet != null) {
            List<E> list=new ArrayList<>();
            while (true){
                //实例化Entity
                E entity;
                try {
                    entity=targetClass.getConstructor().newInstance();
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    log.error("无法实例化Entity",e);
                    throw new RuntimeException("无法实例化Entity",e);
                }
                //遍历set
                try {
                    if (!resultSet.next()) break;
                } catch (SQLException e) {
                    log.debug("ResultSet next 失败");
                    return null;
                }
                //遍历Entity的属性，获取到数据库列名
                for (String fieldName: fieldNameToColumnNameMap.keySet()){
                    try {
                        Method setter=targetClass.getMethod(
                                "set"+StringUtils.toUpperCaseFirst(fieldName),
                                fieldMap.get(fieldName).getType());
                        setter.invoke(entity,resultSet.getObject(fieldNameToColumnNameMap.get(fieldName)));
                    } catch (NoSuchMethodException | SQLException | IllegalAccessException | InvocationTargetException e) {
                        log.error("注入{}失败",fieldName,e);
                        throw new RuntimeException("注入实体参数发生错误",e);
                    }
                }
                list.add(entity);
            }
            return list;
        }
        return null;
    }



}
