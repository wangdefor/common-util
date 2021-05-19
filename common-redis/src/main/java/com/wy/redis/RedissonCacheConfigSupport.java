package com.wy.redis;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @Classname RedissonCacheConfigSupport
 * @Description 基础模板类
 * @Date 2020/10/13 10:17
 * @Created wangyong
 */
@ConditionalOnClass(value = {Config.class, RedissonConnectionFactory.class})
public abstract class RedissonCacheConfigSupport extends RedisCacheConfigSupport {


    /**
     * Redisson 配置
     *
     * @return 配置
     */
    protected abstract RedissonClient getRedissonClient();

    @Override
    public RedisConnectionFactory getRedisConnectionFactory() {
        return new RedissonConnectionFactory(this.getRedissonClient());
    }

}
