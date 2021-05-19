package com.wy.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * @Classname RedisCacheConfigSupport
 * @Description redisCache支持类
 * @Date 2020/10/13 10:18
 * @Created wangyong
 */
@EnableCaching
@ConditionalOnClass(value = {CacheManager.class, RedisCacheManager.class})
public abstract class RedisCacheConfigSupport {

    public static final String CACHE_BEAN_NAME = "com.dmall.redis.cache";

    /**
     * 获取指定过期时间的 Redis 缓存配置
     *
     * @param duration 过期时间
     * @return 缓存配置
     */
    protected RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Duration duration) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(duration)
                .serializeKeysWith(this.serializeKeysWith())
                .serializeValuesWith(this.serializeValuesWith());
    }

    /**
     * 生成缓存 map 用于初始化缓存的命令空间的各种属性
     *
     * @return {@link Map<String,Duration>}
     * @see RedisCacheConfiguration
     * {@link RedisCacheConfigSupport#getInitialCacheConfigurationMap()}
     */
    @Autowired(required = false)
    @Qualifier(CACHE_BEAN_NAME)
    private Map<String, Duration> cacheMap;

    /**
     * 缓存键序列化模板
     *
     * @return {@link RedisSerializationContext.SerializationPair)
     */
    protected abstract RedisSerializationContext.SerializationPair<String> serializeKeysWith();

    /**
     * 缓存键序列化模板
     *
     * @return {@link RedisSerializationContext.SerializationPair)
     */
    protected abstract RedisSerializationContext.SerializationPair<?> serializeValuesWith();

    /**
     * 提供两种方式 1 申明一个Map<String,Duration> 集合对象bean。 如果成功将会在这里对该对象的cache进行配置过期时间
     * 2 子类配置通过覆写此方法配置各个缓存命名空间的过期时间
     *
     * @return 缓存名:缓存配置映射
     * {@link RedisCacheConfigSupport#getRedisCacheConfigurationWithTtl(Duration)}
     * {@link RedisCacheConfigSupport#cacheMap}
     */
    protected Map<String, RedisCacheConfiguration> getInitialCacheConfigurationMap() {
        Map<String, RedisCacheConfiguration> configurationMap = Maps.newHashMap();
        //对cacheMap与RedisCacheConfiguration进行绑定
        Optional.ofNullable(cacheMap)
                .ifPresent(map -> map.forEach((key, value) -> configurationMap.put(key, getRedisCacheConfigurationWithTtl(value))));
        return configurationMap;
    }

    /**
     * 需要在实现类配置中加入 @Bean 注解并指定 beanName
     *
     * @return CacheManager
     */
    public CacheManager cacheManager() {
        RedisCacheManager build = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(this.getRedisConnectionFactory())
                .cacheDefaults(this.getRedisCacheConfigurationWithTtl(Duration.ZERO))
                .withInitialCacheConfigurations(this.getInitialCacheConfigurationMap())
                .build();
        return build;
    }

    /**
     * 需要在实现类配置中加入 @Bean 注解并指定 beanName
     * 使用jackson形式的序列化 本质任然是string类型的
     *
     * @return Redis Template
     */
    public RedisTemplate<?, ?> redisTemplate() {

        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(getRedisConnectionFactory());
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        objectMapper.registerModule(javaTimeModule);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setDefaultSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Redis 连接工厂
     *
     * @return Redis 连接
     */
    public abstract RedisConnectionFactory getRedisConnectionFactory();
}
