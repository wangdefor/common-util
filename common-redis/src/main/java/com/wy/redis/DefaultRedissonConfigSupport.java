package com.wy.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @Classname DefaultRedissonConfigSupport
 * @Description 默认配置
 * @Date 2020/10/13 11:00
 * @Created wangyong
 */
@Component
public class DefaultRedissonConfigSupport extends CommonRedissonConfigSupport {

    /**
     * Bean name of default CacheManager
     */
    public static final String DEFAULT_REDIS_CACHE_MANAGER = "cache.manager.default";

    /**
     * Bean name of RedisTemplate
     */
    public static final String DEFAULT_REDIS_TEMPLATE = "redis.template.default";

    @Primary
    @Bean(name = DEFAULT_REDIS_CACHE_MANAGER)
    @Override
    public CacheManager cacheManager() {
        return super.cacheManager();
    }

    @Bean(name = DEFAULT_REDIS_TEMPLATE)
    @Override
    public RedisTemplate<?, ?> redisTemplate() {
        return super.redisTemplate();
    }

    @Override
    protected RedisSerializationContext.SerializationPair<String> serializeKeysWith() {
        return RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string());
    }

    @Override
    protected RedisSerializationContext.SerializationPair<?> serializeValuesWith() {
        //enericJackson2JsonRedisSerializer serializer = (GenericJackson2JsonRedisSerializer) RedisSerializer.json();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        return RedisSerializationContext.fromSerializer(serializer)
                .getValueSerializationPair();
    }
}
