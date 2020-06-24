package com.an.springplusplus.core.config;

import com.an.springplusplus.core.annotation.configuration.AutoConfiguration;
import com.an.springplusplus.core.datasource.SpringPlusPlusDatasourcePool;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/14 1:59 上午
 * @description
 */
@Slf4j
@AutoConfiguration
public class DataSourceConfiguration implements Configuration{

    private static SpringPlusPlusDatasourcePool dataSource;


    @Override
    public void init(ApplicationProperties properties, ServletContext context) {
        SpringPlusPlusDatasourcePool.DataSourceProperties dataSourceProperties=new SpringPlusPlusDatasourcePool.DataSourceProperties();
        dataSourceProperties.setConnectionUrl(properties.getProperties().getProperty("spring-plus-plus.sql.sql-connection-url",""));
        dataSourceProperties.setDriverName(properties.getProperties().getProperty("spring-plus-plus.sql.class-name"));
        dataSourceProperties.setMaxConnectionSize(Integer.valueOf(properties.getProperties().getProperty("spring-plus-plus.sql.max-size",String.valueOf(10))));
        dataSourceProperties.setUsername(properties.getProperties().getProperty("spring-plus-plus.sql.username"));
        dataSourceProperties.setPassword(properties.getProperties().getProperty("spring-plus-plus.sql.password"));
        dataSource=new SpringPlusPlusDatasourcePool(dataSourceProperties);
    }


    /**
     * 销毁数据连接
     */
    public void destroy(){
        dataSource.destroy();
    }


    /**
     * 获取数据连接池
     * @return
     */
    public static DataSource getDataSource(){
        return dataSource;
    }
}
