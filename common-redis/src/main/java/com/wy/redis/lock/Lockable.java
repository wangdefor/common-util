package com.wy.redis.lock;

import java.lang.annotation.*;

/**
 * @Classname Lockable
 * @Description 锁的枚举类，用于切面
 * @Date 2020/10/13 11:27
 * @author  wangyong
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Lockable {

    /**
     * 锁的名称
     * 完整名称拼写规则为 values_@LockKeys
     */
    String[] value();

    /**
     * 持锁时间
     * 单位毫秒，默认 5 * 1000 秒
     */
    long keeps() default 5 * 1000;

    /**
     * 超时时间（没有获得锁时的等待时间）
     * 单位毫秒，默认 0 秒，不等待
     */
    long timeout() default 0;

    /**
     * 获取锁失败的提示
     */
    String message() default "Lock failed.";

}
