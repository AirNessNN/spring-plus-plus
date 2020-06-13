package com.an.spring.demo.test;

import com.an.springplusplus.core.annotation.mapping.GetMapping;
import com.an.springplusplus.core.annotation.mapping.PostMapping;
import com.an.springplusplus.core.annotation.mapping.RequestBody;
import com.an.springplusplus.core.annotation.mapping.RestController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/11 11:46 下午
 * @description
 */
@Slf4j
@RestController
public class TestController {

    /**
     * 自动注入ServletRequest和ServletResponse
     * 参数自动识别自动注入
     * @param test url 参数
     * @param request Servlet请求
     * @param response Servlet回应
     * @return 需要返回的数据
     */
    @GetMapping("/api/v1/test/automatic-injection")
    public String hello(String test, ServletRequest request, ServletResponse response) {
        return test;
    }

    /**
     * POST Body 自动注入
     * @param user JavaBean
     * @return 需要返回的数据
     */
    @PostMapping("/api/v1/test/post")
    public Object testPost(@RequestBody User user){
        if (user!=null){
            return "你好"+user.getName();
        }
        return "不存在User";
    }

    /**
     * JSP快速跳转
     * @param request Servlet请求
     * @return 需要返回的数据
     */
    @GetMapping(value = "api/v1/test/test-path",returnPath = true)
    public String testPath(ServletRequest request){
        request.setAttribute("name","张三");
        return "/index.jsp";
    }
}
