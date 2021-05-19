package com.wy.exception;

import org.springframework.util.Assert;


/**
 * @Classname AssertEx
 * @Description 简单重写Assert
 * @Date 2020/10/16 11:06
 * @Created wangyong
 */
public class AssertEx extends Assert {

    /**
     * 如果不符合表达式，则抛出errorType
     *
     * @param expression 表达式为真则不抛出异常
     * @param errorType
     */
    public static void isTrue(boolean expression, IErrorType errorType) {
        if (!expression) {
            throw new BaseException(errorType);
        }
    }


    /**
     * 如果不符合表达式，则抛出BaseException
     *
     * @param expression 表达式为真则不抛出异常
     * @param exception
     */
    public static void isTrue(boolean expression, BaseException exception) {
        if (!expression) {
            throw exception;
        }
    }

}
