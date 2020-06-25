package com.an.springplusplus.core;

import com.an.springplusplus.core.config.ApplicationProperties;
import com.an.springplusplus.core.config.ServletContextAutoConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 1:47 下午
 * @description
 */
@Slf4j
@Getter
public class ServletApplication {
    private static volatile boolean running =false;
    private static ServletApplication application;

    private ApplicationProperties applicationProperties;
    private ServletContextAutoConfiguration servletContextAutoConfiguration;


    /**
     * 启动类
     */
    private ServletApplication(){
        show();
        log.info("初始化 ServletApplication");
    }

    private void show(){
        String img="                                                                          \n" +
                "                                                   $$\\             $$\\    \n" +
                "                                                   $$ |            $$ |   \n" +
                "                                                $$$$$$$$\\       $$$$$$$$\\ \n" +
                "                                                \\__$$  __|      \\__$$  __|\n" +
                "                                                   $$ |            $$ |   \n" +
                "                                                   \\__|            \\__|   \n" +
                " $$$$$$\\                      $$\\                                         \n" +
                "$$  __$$\\                     \\__|                                        \n" +
                "$$ /  \\__| $$$$$$\\   $$$$$$\\  $$\\ $$$$$$$\\   $$$$$$\\                      \n" +
                "\\$$$$$$\\  $$  __$$\\ $$  __$$\\ $$ |$$  __$$\\ $$  __$$\\                     \n" +
                " \\____$$\\ $$ /  $$ |$$ |  \\__|$$ |$$ |  $$ |$$ /  $$ |                    \n" +
                "$$\\   $$ |$$ |  $$ |$$ |      $$ |$$ |  $$ |$$ |  $$ |                    \n" +
                "\\$$$$$$  |$$$$$$$  |$$ |      $$ |$$ |  $$ |\\$$$$$$$ |                    \n" +
                " \\______/ $$  ____/ \\__|      \\__|\\__|  \\__| \\____$$ |                    \n" +
                "          $$ |                              $$\\   $$ |                    \n" +
                "          $$ |                              \\$$$$$$  |                    \n" +
                "          \\__|                               \\______/                     ";
        System.out.println(img);
    }


    /**
     * 启动程序
     */
    public static synchronized ServletApplication run(ServletContext context){
        long time=System.currentTimeMillis();
        if (running){
            throw new RuntimeException("程序已经有实例在运行");
        }
        ServletApplication application=new ServletApplication();
        application.applicationProperties=new ApplicationProperties();
        application.servletContextAutoConfiguration=new ServletContextAutoConfiguration(application.applicationProperties,context);
        running =true;
        ServletApplication.application=application;
        log.info("Spring-plus-plus初始化完成。   耗时：{}ms",(System.currentTimeMillis()-time));
        return application;
    }

    public synchronized void destroy(){
        log.info("销毁ServletApplication");
        application.servletContextAutoConfiguration.destroy();
    }
}
