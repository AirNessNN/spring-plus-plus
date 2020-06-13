package com.an.springplusplus.core.servlet.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.an.springplusplus.core.annotation.mapping.RequestBody;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Set;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/12 10:32 上午
 * @description 用于支持RestRequest的请求处理器
 */
@Slf4j
public class RestHttpServletProcessor extends AbstractHttpServletProcessor{


    @Override
    public void doService(ServletRequest req, ServletResponse resp, Method method, Class<?> tc,Object controller, String contentType, String charset, boolean returnPath) throws IOException {
        HttpServletRequest httpServletRequest= (HttpServletRequest) req;
        ParamFinder finder=new ParamFinder((HttpServletRequest) req, method);
        Object[] args=finder.doIt((index, annotationMap,paramType, parameter, requestParam) -> {
            if (paramType.isAssignableFrom(ServletRequest.class)){
                //填充Servlet参数
                return req;
            } else if (paramType.isAssignableFrom(ServletResponse.class)) {
                //填充Servlet参数
                return resp;
            }else if (annotationMap.get(RequestBody.class)!=null&&httpServletRequest.getMethod().equals("POST")){
                //填充其他参数
                String body=readBody(httpServletRequest);
                log.debug("body={}",body);
                try {
                    return JSON.parseObject(body,paramType);
                }catch (JSONException e){
                    log.error("转换Body时发生错误",e);
                    throw e;
                }
            }else {
                //基础参数转换
                if (!parameter.isNamePresent()) {
                    return null;
                }
                String name=parameter.getName();
                if (!requestParam.isPresent()){
                    //不存在这个参数
                    //可能是复杂类型对象，尝试注入该对象
                    try {
                        Constructor<?> c=paramType.getConstructor();
                        Object o=c.newInstance();
                        JSONObject paramJson=JSON.parseObject(JSON.toJSONString(o, SerializerFeature.WriteMapNullValue));
                        Set<String> paramNames=paramJson.keySet();
                        for (String s:paramNames){
                            String param=req.getParameter(s);
                            if (param!=null){
                                paramJson.put(s,param);
                            }
                        }
                        o=paramJson.toJavaObject(paramType);
                        return o;
                    } catch (NoSuchMethodException | JSONException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.debug("尝试将参数注入到Bean失败，跳过尝试");
                        return null;
                    }
                }
                //仅支持 String Char Boolean BigDecimal Long Double Float Integer
                String attr= req.getParameter(name);
                if (paramType.equals(String.class)){
                    return attr;
                } else if (paramType.equals(Character.class)) {
                    return attr.toCharArray()[0];
                } else if (paramType.equals(Boolean.class)) {
                    return Boolean.parseBoolean(attr);
                } else if (paramType.equals(BigDecimal.class)) {
                    return new BigDecimal(attr);
                } else if (paramType.equals(Long.class)) {
                    return Long.parseLong(attr);
                } else if (paramType.equals(Double.class)) {
                    return Double.parseDouble(attr);
                }else if (paramType.equals(Short.class)) {
                    return Short.parseShort(attr);
                }else if (paramType.equals(Integer.class)) {
                    return Integer.parseInt(attr);
                }else {
                    return null;
                }
            }
        });

        //开始填充参数到方法
        try {
            log.debug("调用执行方法：{}",method.getName());
            Object res=method.invoke(controller,args);
            if (returnPath){
                httpServletRequest.getRequestDispatcher(res.toString()).forward(req,resp);
                return;
            }
            log.debug("方法执行返回:{}",JSON.toJSONString(res,true));
            doFinish(res,resp,contentType,charset);
        } catch (Exception e) {
            log.error("执行请求方法时发生异常",e);
            throw new RuntimeException(e.getMessage(),e);
        }
    }


    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb=new StringBuilder();
        try (InputStreamReader isr=new InputStreamReader(req.getInputStream());
             BufferedReader reader=new BufferedReader(isr)){
            do {
                int i=reader.read();
                if (i==-1){
                    break;
                }
                sb.append((char) i);
            }while (true);
        }
        return sb.toString();
    }





}
