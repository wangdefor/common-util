package com.wy.redis.util;

import com.wy.exception.BaseException;
import com.wy.redis.DefaultRedissonConfigSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisGenerateCodeUtil
 * @Description redisCode生成器
 * @Date 2020/11/4 17:33
 * @Created wangyong
 */
@Service
public class RedisGenerateCodeUtil {


    /**
     * key值前缀
     */
    public static final String REDIS_KEY_PREFIX = "com:dmall:generate:code:";
    @Autowired
    @Qualifier(DefaultRedissonConfigSupport.DEFAULT_REDIS_TEMPLATE)
    private RedisTemplate redisTemplate;

    /**
     * 补位自增值的长度
     *
     * @param value
     * @param length
     * @return
     */
    private static String codeCovering(Long value, Integer length) {
        String strLength = String.valueOf(value);
        if (strLength.length() > length) {
            return strLength.substring(strLength.length() - length);
        }
        int sub = length - strLength.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sub; i++) {
            sb.append("0");
        }
        sb.append(value);
        return sb.toString();
    }

    public static void main(String[] args) {
        String s = codeCovering(5012L, 3);
        System.out.println(s);
    }

    /**
     * 产生code 该值将不会过期
     *
     * @param length       长度
     * @param change       变化量
     * @param businessType 业务类型
     * @return length长度code码值
     */
    @SuppressWarnings("unchecked")
    public String shortGenerator(Integer length, Integer change, String businessType) {
        String key = REDIS_KEY_PREFIX + length + ":" + businessType;
        Long increment = redisTemplate.opsForValue().increment(key, change);
        return codeCovering(increment, length);
    }

    /**
     * 创建一个在某个date之后过期的自增key
     *
     * @param length       长度
     * @param change       变化量
     * @param businessType 业务类型
     * @param date         日期
     * @return length长度code码值
     */
    @SuppressWarnings("unchecked")
    public String fixDateGenerator(Integer length, Integer change, String businessType, Date date) {
        String key = REDIS_KEY_PREFIX + length + ":" + businessType;
        Long increment = redisTemplate.opsForValue().increment(key, change);
        redisTemplate.expireAt(key, date);
        return codeCovering(increment, length);
    }

    /**
     * 设置有过期时间的自增key 默认两种策略
     * 1. timeStrategy = {@link TimeStrategy.ALWAYS} 此时每次调用将会重置过期时间
     * 2. timeStrategy = {@link TimeStrategy.ONLY_INIT} 仅仅只在第一次的时候才会有过期时间，每次更新不会延长过期时间
     *
     * @param length       自增码的长度
     * @param change       变化量
     * @param businessType 业务类型
     * @param unit         单位
     * @param time         时间
     * @param timeStrategy 策略
     * @return length长度code码值
     */
    @SuppressWarnings({"unchecked","unboxing"})
    public String generatorByTimeStrategy(Integer length, Integer change, String businessType, TimeUnit unit, Long time, TimeStrategy timeStrategy) {
        String key = REDIS_KEY_PREFIX + length + ":" + businessType;
        Long increment;
        switch (timeStrategy) {
            case ALWAYS:
                increment = redisTemplate.opsForValue().increment(key, change);
                redisTemplate.expire(key, time, unit);
                break;
            case ONLY_INIT:
                if (!redisTemplate.hasKey(key)) {
                    increment = redisTemplate.opsForValue().increment(key, change);
                    redisTemplate.expire(key, time, unit);
                    break;
                }
                increment = redisTemplate.opsForValue().increment(key, change);
                break;
            default:
                throw BaseException.definedException(8099,"未知的时间策略类型");

        }
        return codeCovering(increment, length);
    }

    public enum TimeStrategy {

        /**
         * 总是，每次产生code值将会刷新过期时间
         */
        ALWAYS,

        /**
         * 仅仅只在第一次将会设置过期时间
         */
        ONLY_INIT,
        ;
    }


}
