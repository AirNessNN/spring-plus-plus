package com.an.springplusplus.core.datasource.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/15 8:07 下午
 * @description
 */
@Slf4j
public class QueryWrapper<E> extends SqlWrapper<E> implements SelectWrapper<E> {





    public QueryWrapper(Class<E> targetClass) {
        super(targetClass);
        init();
    }


    /**
     * 准备SQL
     */
    private void init() {
        sql.append(SELECT);
        boolean f = true;
        for (String key : getFieldNameToColumnNameMap().keySet()) {
            if (!f) {
                sql.append(COMMA);
            }
            sql.append(getFieldNameToColumnNameMap().get(key));
            f = false;
        }
        sql.append(FROM).append(getTableName()).append(SPACE);
        isPrepared = true;
    }








    public QueryWrapper<E> eq(String column, Serializable value) {
        return eq(true, column, value);
    }

    public QueryWrapper<E> eq(boolean condition, String column, Serializable value) {
        appendWhere();
        appendCondition(AND);
        if (condition && StringUtils.isNoneBlank(column)) {
            sql.append(column).append(EQUAL).append(PARAM);
            paramValues.add(value);
        }
        return this;
    }

    public QueryWrapper<E> ne(String column, Serializable value) {
        return ne(true, column, value);
    }

    public QueryWrapper<E> ne(boolean condition, String column, Serializable value) {
        appendWhere();
        appendCondition(AND);
        if (condition && StringUtils.isNoneBlank(column)) {
            sql.append(NOT).append(column).append(EQUAL).append(PARAM);
            paramValues.add(value);
        }
        return this;
    }

    public QueryWrapper<E> like(String column, Serializable value) {
        return like(true, column, value);
    }

    public QueryWrapper<E> like(boolean condition, String column, Serializable value) {
        appendWhere();
        appendCondition(AND);
        if (condition && StringUtils.isNoneBlank(column)) {
            sql.append(column).append(LIKE).append("CONCAT('%',").append(PARAM).append(",'%')");
            paramValues.add(value);
        }
        return this;
    }

    public QueryWrapper<E> in(String column, Serializable... values) {
        return in(true, column, values);
    }

    public QueryWrapper<E> in(boolean condition, String column, Serializable... values) {
        appendWhere();
        appendCondition(AND);
        if (condition && StringUtils.isNoneBlank(column)) {
            sql.append(column).append(IN).append(LEFT_BRACKETS);
            boolean flag = false;
            for (Serializable value : values) {
                if (flag) {
                    sql.append(COMMA);
                }
                sql.append(PARAM);
                paramValues.add(value);
                flag = true;
            }
            sql.append(RIGHT_BRACKETS);
        }
        return this;
    }

    public QueryWrapper<E> in(String column, Collection<Serializable> values) {
        return in(true, column, values);
    }

    public QueryWrapper<E> in(boolean condition, String column, Collection<Serializable> values) {
        return in(condition, column, values.toArray(new Serializable[0]));
    }

    public QueryWrapper<E> groupBy(boolean condition, String column) {
        if (condition && StringUtils.isNoneBlank(column)) {
            sql.append(GROUP_BY).append(column);
        }
        return this;
    }

    public QueryWrapper<E> groupBy(String column) {
        return groupBy(true,column);
    }

    public QueryWrapper<E> orderBy(String column) {
        return orderBy(true,column);
    }

    public QueryWrapper<E> orderBy(boolean condition, String column) {
        return orderBy(true,column,false);
    }

    public QueryWrapper<E> orderBy(boolean condition, String column, boolean isDesc) {
        if (condition && StringUtils.isNoneBlank(column)) {
            sql.append(ORDER_BY).append(column);
            if (isDesc){
                sql.append(DESC);
            }else {
                sql.append(ASC);
            }
        }
        return this;
    }

    public QueryWrapper<E> last(String sql) {
        return last(true,sql);
    }

    public QueryWrapper<E> last(boolean condition, String sql) {
        if (!condition)return this;
        this.sql.append(SPACE).append(sql).append(SPACE);
        return this;
    }


    @Override
    public E getById(Serializable id) {
        return eq(com.an.springplusplus.core.tool.StringUtils.humpToUnderline(getTableId()),id).oneOrNull();
    }

    public List<E> getBatchById(List<Serializable> ids){
        return in(com.an.springplusplus.core.tool.StringUtils.humpToUnderline(getTableId()),ids).list();
    }

    @Override
    public List<E> list() {
        if (!isPrepared) throw new RuntimeException("未初始化的SQL");
        try (Connection connection=getConnection()){
            return resultSetToEntity(select(connection));
        } catch (SQLException e) {
            log.error("无法查询数据",e);
            throw new RuntimeException(e);
        }finally {
            initSql();
        }
    }

    @Override
    public E one() {
        Optional<E> optionalE = optionalOne();
        if (!optionalE.isPresent()) {
            throw new RuntimeException("当前数据集为空");
        }
        return optionalE.get();
    }

    @Override
    public Optional<E> optionalOne() {
        return Optional.ofNullable(oneOrNull());
    }

    @Override
    public E oneOrNull() {
        if (!isPrepared) throw new RuntimeException("未初始化的SQL");
        List<E> list = list();
        if (list.size() > 1) {
            throw new RuntimeException("当前数据集不唯一");
        }
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }


    @Override
    public String toString() {
        return sql.toString();
    }
}
