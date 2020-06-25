package com.an.springplusplus.core.datasource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/14 2:01 上午
 * @description 数据连接实例
 */
@Slf4j(topic = "数据库连接池")
public class SpringPlusPlusDatasourcePool implements DataSource {

    /**
     * 连接管理器
     */
    private final Vector<Connection> connections;
    private final DataSourceProperties properties;
    private boolean destroyFlag=false;


    @Data
    public static class DataSourceProperties {
        private String connectionUrl;
        private String username;
        private String password;
        private Integer maxConnectionSize=10;
        private String driverName="com.mysql.jdbc.Driver";
    }


    private void initConnection() {
        try {
            Class.forName(properties.driverName);
//            for (int i = 0; i < properties.maxConnectionSize; i++) {
//                Connection connection= DriverManager.getConnection(properties.connectionUrl,properties.username,properties.password);
//                connections.addElement(connection);
//                log.debug("完成连接初始化 【connection {}】",i);
//            }
        } catch (ClassNotFoundException e) {
            log.error("无法载入数据库或连接",e);
            throw new RuntimeException(e.getMessage(),e);
        }

    }

    public SpringPlusPlusDatasourcePool(DataSourceProperties properties) {
        connections = new Vector<>(properties.maxConnectionSize);
        this.properties = properties;
        initConnection();
    }


    @Override
    public Connection getConnection() {
//        if (connections.size()>0){
//            Connection connection=connections.firstElement();
//            connections.removeElementAt(0);
//            log.debug("申请了连接池 池子内还剩{}个连接",connections.size());
//            return (Connection) Proxy.newProxyInstance(SpringPlusPlusDatasourcePool.class.getClassLoader(), connection.getClass().getInterfaces(), (proxy, method, args) -> {
//                log.debug(method.getName());
//                if (!method.getName().equals("close")){
//                    return method.invoke(args);
//                }else {
//                    if (!destroyFlag){
//                        connections.addElement(connection);
//                        log.debug("连接已被回收");
//                    }else {
//                        log.debug("销毁连接池");
//                        return method.invoke(args);
//
//                    }
//                    return null;
//                }
//            });
//        }else {
//            log.error("连接池枯竭");
//            throw new RuntimeException("连接池枯竭");
//        }
        try {
            return DriverManager.getConnection(properties.connectionUrl,properties.username,properties.password);
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败",e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new RuntimeException("不支持此方法获取连接");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public void destroy(){
        destroyFlag=true;
        for (Connection c:connections){
            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
