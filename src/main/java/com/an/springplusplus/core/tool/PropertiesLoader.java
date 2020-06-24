package com.an.springplusplus.core.tool;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/14 1:49 上午
 * @description
 */
@Slf4j
public class PropertiesLoader {

    private static final String PROPERTIES_NAME="application.properties";

    private String propertiesName;

    private Properties properties;

    public PropertiesLoader(){

    }

    public PropertiesLoader(String propertiesName){
        this.propertiesName=propertiesName;
    }


    private void init(String propertiesName) throws IOException {
        InputStream in=PropertiesLoader.class.getResourceAsStream(propertiesName);
        if (in==null){
            log.error("Could not found {}",propertiesName);
            throw new RuntimeException("Could not found "+propertiesName);
        }
        Properties properties=new Properties();
        properties.load(in);
    }



}
