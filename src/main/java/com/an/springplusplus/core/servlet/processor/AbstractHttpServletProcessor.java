package com.an.springplusplus.core.servlet.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 6:00 下午
 * @description HTTP Servlet处理器
 */
@Slf4j
public abstract class AbstractHttpServletProcessor implements ServletProcessor {

    public AbstractHttpServletProcessor(){}

    /**
     * 完成请求
     * @param res
     * @param resp
     * @param contentType
     * @param charset
     */
    protected void doFinish(Object res,ServletResponse resp, String contentType, String charset){
        if (resp.isCommitted()){
            log.debug("回应已经提前提交");
            return;
        }
        resp.setContentType(contentType);
        resp.setCharacterEncoding(charset);
        try(BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()))) {
            writer.write(JSON.toJSONString(res));
            writer.flush();
            log.debug("完成请求");
        }catch (JSONException e){
            log.error("请求返回时解析JSON失败",e);
            throw new RuntimeException(e.getMessage(),e);
        } catch (IOException e) {
            log.error("无法写入输出流",e);
            throw new RuntimeException(e.getMessage(),e);
        }
    }


    /**
     * 方法反射查找器
     */
    protected static class ParamFinder{

        private final Method method;

        private final HttpServletRequest req;

        public ParamFinder(HttpServletRequest req, Method method){
            this.req=req;
            this.method=method;
        }

        private Set<String> getParamName(HttpServletRequest req){
            Set<String> attrs=new HashSet<>();
            Enumeration<String> enumeration=req.getParameterNames();
            while (enumeration.hasMoreElements()){
                String attr=enumeration.nextElement();
                attrs.add(attr);
            }
            return attrs;
        }

        public Object[] doIt(ParamFinderExecutor executor) throws IOException {
            //本次请求的方法
            Class<?>[] paramTypes=method.getParameterTypes();
            Parameter[] parameters=method.getParameters();
            Set<String> reqParamNames= getParamName(req);
            log.debug("paramTypes={}",JSON.toJSONString(paramTypes));
            log.debug("parameters={}",JSON.toJSONString(Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new)));
            log.debug("attrNames={}",JSON.toJSONString(reqParamNames));
            //url参数转换
            Object[] args=new Object[paramTypes.length];
            Annotation[][] annotations=method.getParameterAnnotations();

            for (int i=0;i<paramTypes.length;i++){
                Annotation[] ans=annotations[i];
                Map<Class<? extends Annotation>,Annotation> map=new HashMap<>();
                Arrays.stream(ans).forEach(e->map.put(e.annotationType(),e));

                String name=parameters[i].getName();
                Optional<String> opt=Optional.ofNullable(req.getParameter(name));
                args[i]=executor.exec(i,map,paramTypes[i],parameters[i],opt);
            }
            return args;
        }

    }

    /**
     * 处理接口
     */
    protected interface ParamFinderExecutor{

        /**
         * 执行
         * @param paramType
         * @param parameter
         * @param requestParam
         * @return
         */
        Object exec(int index, Map<Class<? extends Annotation>, Annotation> annotationMap, Class<?> paramType, Parameter parameter, Optional<String> requestParam) throws IOException;
    }
}
