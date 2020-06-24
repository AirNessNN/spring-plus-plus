package com.an.springplusplus.core.datasource.wrapper;

import cn.hutool.core.collection.CollectionUtil;
import com.an.springplusplus.core.datasource.mapper.TableIdEnum;
import com.an.springplusplus.core.tool.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/24 1:23 上午
 * @description
 */
@Slf4j
public class EntityWrapper<E> extends SqlWrapper<E> implements InsertWrapper<E>, UpdateWrapper<E>, DeleteWrapper<E> {


    public EntityWrapper(Class<E> tClass) {
        super(tClass);
    }

    public EntityWrapper(Class<E> tClass, boolean isUnderscoreToCamelCase) {
        super(tClass, isUnderscoreToCamelCase);
    }


    private String initUpdate(){
        return UPDATE + getTableName() + SET;
    }



    public boolean deleteById(Serializable id) {
        if (id==null){
            return false;
        }
        initSql();
        //DELETE FROM table WHERE
        sql.append(DELETE).append(FROM).append(tableName).append(WHERE)
                .append(StringUtils.humpToUnderline(tableId))
                .append(EQUAL).append(PARAM);
        paramValues.add(id);
        try (Connection con = getConnection()){
            PreparedStatement statement=exec(con, Statement.NO_GENERATED_KEYS);
            return statement.executeUpdate()>0;
        } catch (SQLException e) {
            log.error("执行删除命令时发生错误",e);
        }
        return false;
    }


    public int deleteBatchById(List<Serializable> id) {
        if (id==null){
            return 0;
        }
        initSql();
        //初始化SQL
        sql.append(DELETE).append(FROM).append(tableName).append(WHERE)
                .append(StringUtils.humpToUnderline(tableId)).append(IN);
        sql.append(appendParam(LEFT_BRACKETS,RIGHT_BRACKETS,COMMA,id,PARAM));
        paramValues.addAll(id);
        try(Connection con=getConnection()){
            PreparedStatement statement=exec(con,Statement.NO_GENERATED_KEYS);
            return statement.executeUpdate();
        } catch (SQLException e) {
            log.error("删除数据集发生错误",e);
            throw new RuntimeException("删除数据集发生错误",e);
        }
    }


    public E insert(E entity) {
        if (entity==null){
            return null;
        }
        initSql();
        sql.append(INSERT + INTO).append(getTableName());
        //初始化
        Map<String,Serializable> valueMapper=convertEntityToSqlMap(true,entity);
        //拼接参数
        sql.append(appendParam(LEFT_BRACKETS,RIGHT_BRACKETS,COMMA,valueMapper.keySet()));
        sql.append(VALUES);
        //拼接Values
        sql.append(SPACE).append(appendParam(LEFT_BRACKETS,RIGHT_BRACKETS,COMMA,valueMapper.keySet(),PARAM));
        valueMapper.forEach((k,v)-> paramValues.add(v));
        try (Connection con=getConnection()){
            PreparedStatement statement=insert(con);
            int count=statement.executeUpdate();
            if (count==0){
                return null;
            }
            if (getTableIdEnum().equals(TableIdEnum.PRIMARY_KEY)){
                ResultSet resultSet=statement.getGeneratedKeys();
                if (resultSet.next()){
                    Object o=resultSet.getObject(1);
                    putIdToEntity(o,entity);
                    return entity;
                }
            }else{
                return entity;
            }
        } catch (SQLException e) {
            log.error("查询数据集发生错误",e);
            throw new RuntimeException("查询数据集发生错误",e);
        }
        return null;
    }


    public List<E> insertBatch(List<E> entities) {
        if (CollectionUtil.isEmpty(entities)){
            return entities;
        }
        initSql();
        //初始化
        sql.append(INSERT + INTO).append(getTableName());
        //拼接参数
        Set<String> columnSet=new HashSet<>();
        fieldNameToColumnNameMap.forEach((k,v)-> columnSet.add(v));
        sql.append(appendParam(LEFT_BRACKETS,RIGHT_BRACKETS,COMMA,columnSet)).append(VALUES);
        log.debug("拼接参数之前：{}",sql.toString());
        //开始循环拼接值
        List<String> values=new ArrayList<>();
        for (E entity:entities){
            Map<String,Serializable> map=convertEntityToSqlMap(false,entity);
            map.forEach((k,v)->paramValues.add(v));
            String param=appendParam(LEFT_BRACKETS,RIGHT_BRACKETS,COMMA,map.keySet(),PARAM);
            values.add(param);
        }
        sql.append(appendParam(SPACE,SPACE,COMMA,values));
        try (Connection con=getConnection()){
            PreparedStatement statement=insert(con);
            int count=statement.executeUpdate();
            if (count==0){
                return null;
            }
            if (getTableIdEnum().equals(TableIdEnum.PRIMARY_KEY)) {
                ResultSet resultSet = statement.getGeneratedKeys();
                int index = 0;
                while (resultSet.next()) {
                    try {
                        if (index >= entities.size()) {
                            break;
                        }
                        Object o = resultSet.getObject(1);
                        putIdToEntity(o, entities.get(index));
                        index++;
                    } catch (Exception e) {
                        index++;
                        log.warn("注入ID失败,index={}", index);
                    }
                }
            }
            return entities;
        }catch (SQLException e){
            log.error("执行SQL查询发生错误",e);
            throw new RuntimeException(e);
        }
    }


    public int updateById(E entity,boolean ignoreNull){
        initSql();
        //UPDATE xxxx SET
        sql.append(initUpdate());

        List<String> sqlValues=new ArrayList<>();
        Map<String,Serializable> values;
        //拼接参数,忽略Null
        if (ignoreNull){
            //获取全部属性
            values=convertEntityToSqlMap(true,entity);
        }else {
            values=convertEntityToSqlMap(false,entity);
        }
        //移除TableID
        final String columnId=StringUtils.humpToUnderline(tableId);
        final Serializable id=values.get(columnId);
        values.remove(columnId);
        values.forEach((k,v)->{
            sqlValues.add(k.concat(EQUAL).concat(PARAM));
            paramValues.add(v);
        });
        paramValues.add(id);
        sql.append(appendParam(SPACE,SPACE,COMMA,sqlValues));
        //WHERE xxxid = ?
        sql.append(WHERE).append(columnId).append(EQUAL).append(PARAM);
        try (Connection con = getConnection()){
            PreparedStatement statement=update(con);
            return statement.executeUpdate();
        }catch (SQLException e){
            log.error("获取SQL连接失败",e);
        }
        return 0;
    }

    /**
     * 更新数据
     * @param entity
     * @return
     */
    public int updateById(E entity) {
        return updateById(entity,true);
    }


}
