package com.wy.redis.lock;

import com.wy.exception.BaseException;
import com.wy.exception.IErrorType;
import com.wy.exception.AssertEx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisLockUtil
 * @Description redisLock工具类
 * @Date 2020/12/25 16:41
 * @Created wangyong
 */
@Slf4j
@Service
public class RedisLockUtil {


    @Autowired
    private RedissonClient redisson;

    /**
     *
     * 根据自定义key进行加锁 并返回当前锁对象
     *
     * @param lockKey 加锁的key值
     * @param waitTime 未获得锁对象等待的最大时间 建议设置为 0
     * @param leaseTime 持有锁的时间 根据代码执行的时间来设置。 未设置看门狗 不会自动续期
     * @param unit 单位
     * @param errorType 加锁失败返回的枚举值
     * @return {@link RLock#tryLock(long, long, TimeUnit)} 
     */
    public RLock tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, IErrorType errorType) {
        AssertEx.isTrue(StringUtils.isNotEmpty(lockKey), BaseException.definedException(7877,"加锁的key不能为空"));
        log.info("start try lock and lock key {},waitTime {},leaseTime {}",lockKey,waitTime,leaseTime);
        RLock lock = null;
        try {
            lock = redisson.getLock(lockKey);
            boolean result = lock.tryLock(waitTime, leaseTime, unit);
            AssertEx.isTrue(result,errorType.get());
        }catch (Exception e){
            //抛出自定义异常
            log.error("lock fail ",e);
            throw errorType.get();
        }
        return lock;
    }

    /**
     * 释放锁
     *
     * @param rLock 锁对象
     */
    public void tryUnLock(RLock rLock){
        if(rLock != null){
            try {
                rLock.unlock();
            }catch (Exception e){
                String message = String.format("un lock fail and lock key %s . error detail message  ", rLock.getName());
                log.error(message,e);
            }
        }
    }
}
