package com.wy.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static io.vavr.API.*;

/**
 * @Classname CommonRedissonConfigSupport
 * @Description 通用的 Redisson 配置
 * @Date 2020/10/13 10:16
 * @Created wangyong
 */
@ConditionalOnBean(value = RedisProperties.class)
@EnableConfigurationProperties(RedisProperties.class)
public abstract class CommonRedissonConfigSupport extends RedissonCacheConfigSupport {

    /**
     * 客户端
     */
    public static final String CLIENT = "redisson.client.default";

    /**
     * Config
     */
    @Autowired
    private RedisProperties redisProperties;

    @Bean(CLIENT)
    @Override
    @Primary
    protected RedissonClient getRedissonClient() {
        Config config = new Config();
        Match(this.redisProperties).of(
                // 集群模式
                Case($(properties -> null != properties.getCluster()), properties -> config.useSingleServer()
                        .setAddress(properties.getHost() + ":" + properties.getPort())
                        .setDatabase(properties.getDatabase())
                        .setPassword(properties.getPassword())
                        .setConnectTimeout(Optional.of(properties)
                                .map(RedisProperties::getTimeout)
                                .map(duration -> duration.get(ChronoUnit.NANOS) / 1000_000L)
                                .map(Number::intValue)
                                .orElse(10000))),
                // 哨兵模式
                Case($(properties -> null != properties.getSentinel()), properties -> {
                    RedisProperties.Sentinel sentinel = properties.getSentinel();
                    SentinelServersConfig sentinelServersConfig = config.useSentinelServers()
                            .setMasterName(sentinel.getMaster());
                    sentinel.getNodes().forEach(sentinelServersConfig::addSentinelAddress);
                    sentinelServersConfig.setDatabase(properties.getDatabase())
                            .setPassword(properties.getPassword())
                            .setConnectTimeout(Optional.of(properties)
                                    .map(RedisProperties::getTimeout)
                                    .map(duration -> duration.get(ChronoUnit.NANOS) / 1000_000L)
                                    .map(Number::intValue)
                                    .orElse(10000));
                    return sentinelServersConfig;
                }),
                // 单机模式
                Case($(), () -> config.useSingleServer()
                        .setAddress("redis://" + this.redisProperties.getHost() + ":" + this.redisProperties.getPort())
                        .setDatabase(this.redisProperties.getDatabase())
                        .setPassword(this.redisProperties.getPassword())
                        .setConnectTimeout(Optional.of(this.redisProperties)
                                .map(RedisProperties::getTimeout)
                                .map(duration -> duration.get(ChronoUnit.NANOS) / 1000_000L)
                                .map(Number::intValue)
                                .orElse(10000)))
        );
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
