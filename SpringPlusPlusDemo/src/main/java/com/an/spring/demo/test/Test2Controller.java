package com.an.spring.demo.test;

import com.alibaba.fastjson.JSON;
import com.an.springplusplus.core.annotation.mapping.GetMapping;
import com.an.springplusplus.core.annotation.mapping.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/13 2:49 上午
 * @description
 */
@Slf4j
@RestController
public class Test2Controller {

    /**
     * 全自动包装类拼装
     * @param v1
     * @param v2
     * @param v3
     * @param v5
     * @param v4
     * @return
     */
    @GetMapping("/api/v1/test/param-test")
    public Map<String,Object> testParam(Integer v1, Long v2, String v3, User v5,Boolean v4){
        log.debug("v1={} v2={} v3={} v4={} v5={}",v1,v2,v3,v4, JSON.toJSONString(v5));
        Map<String,Object> map=new HashMap<>();
        map.put("v1",v1);
        map.put("v2",v2);
        map.put("v3",v3);
        map.put("v4",v4);
        map.put("v5",v5);
        return map;
    }

    public String v(){
        return "";
    }
}
