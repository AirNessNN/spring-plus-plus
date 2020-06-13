package com.an.springplusplus.core.exception;

/**
 * @author Airness
 * @email huhaowei16@hotmail.com
 * @date 2020/6/10 1:39 上午
 * @description 基础异常类
 */
public class BaseException extends RuntimeException{


    public BaseException(){
        super();
    }

    public BaseException(String msg){
        super(msg);
    }
}
