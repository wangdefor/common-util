package com.wy.aop;

import java.lang.annotation.*;

/**
 * @author wangyong
 * @Classname IdempoentKey
 * @Description 幂等相关key
 * @Date 2021/5/8 17:12
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IdempoentKey {

    /**
     * 请求编号id 支持el表达式
     *
     * @return
     */
    String requestId();

    /**
     * 请求类型编码 支持el表达式
     *
     * @return
     */
    String appCode();

    /**
     * 请求类型描述 支持el表达式
     * 主要用来告诉我是做啥的
     * 非必填
     * @return
     */
    String requestDesc() default "";

    /**
     * 需要储存的业务字段 持el表达式
     * 非必填
     * @return
     */
    String businessNo() default "";

    /**
     * 需要储存的业务字段类型 持el表达式
     * 非必填
     * @return
     */
    String businessType() default "";

    /**
     * 需要储存的业务字段类型描述 持el表达式
     * 非必填
     * @return
     */
    String businessDesc() default "";

    /**
     * 持锁时间
     * 单位毫秒，默认 5 * 1000 秒
     */
    long keeps() default 5 * 1000;

    /**
     * 超时时间（没有获得锁时的等待时间）
     * 单位毫秒，默认 15 秒，不等待
     */
    long timeout() default 15 * 1000;
}
