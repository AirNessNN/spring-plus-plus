package com.an.springplusplus.core.config;

import com.an.springplusplus.core.exception.BaseException;
import com.an.springplusplus.core.tool.ServletContextUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 1:42 下午
 * @description
 */
@Getter
public class ApplicationProperties{

    private static final String PROPERTIES_NAME="application.properties";

    /**
     * 默认包扫描路径
     */
    private String baseControllerScanClassPath;
    private String acceptContentType;
    private String acceptCharset;
    private Properties properties;



    /**
     * 应用程序配置
     */
    public ApplicationProperties(){
        try {
            initProfile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration. Please check the application.properties  configuration file");
        }
    }


    /**
     * 加载配置
     * @throws IOException IO异常
     */
    private void initProfile() throws IOException {
        InputStream in= ServletContextUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_NAME);
        PropertiesLoader propertiesLoader=new PropertiesLoader(in);
        propertiesLoader.init();
        //填充值
        baseControllerScanClassPath=propertiesLoader.getBaseControllerScanClassPath();
        acceptCharset=propertiesLoader.getAcceptCharset();
        acceptContentType=propertiesLoader.getAcceptContentType();
        properties=propertiesLoader.properties;
    }

    /**
     * Properties处理内部类
     */
    @Slf4j
    private static class PropertiesLoader{
        private static final String BASE_PACKAGE_SCAN="spring-plus-plus.base-controller-scan-classpath";
        private static final String ACCEPT_CONTENT_TYPE="spring-plus-plus.accept-content-type";
        private static final String ACCEPT_CHARSET="spring-plus-plus.accept-charset";

        @Getter
        private String baseControllerScanClassPath;
        @Getter
        private String acceptContentType;
        @Getter
        private String acceptCharset;

        private final Properties properties;

        public PropertiesLoader(InputStream in) throws IOException {
            if (in==null||in.available()==-1){
                log.error("Could not found application.properties, please create the file on your resource directory");
                throw new RuntimeException("Invalid application.properties");
            }
            Properties properties=new Properties();
            this.properties=properties;
            properties.load(in);
        }

        public synchronized void init(){
            initPackagePath(properties);
            //加载其他
            acceptContentType=properties.getProperty(ACCEPT_CONTENT_TYPE,"application/json");
            acceptCharset=properties.getProperty(ACCEPT_CHARSET,"utf-8");


            //加载其他配置
        }

        private void initPackagePath(Properties properties){
            String value=properties.getProperty(BASE_PACKAGE_SCAN).replace("\"","");
            if (StringUtils.isEmpty(value)){
                log.error("Invalid base-package-scan, please check the application.properties");
                throw new BaseException("Invalid base-package-scan, please check the application.properties");
            }
            baseControllerScanClassPath=value;
            log.info("基础包扫描路径：{}",value);
        }
    }

}

