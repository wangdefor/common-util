package com.wy.redis.lock;

import com.wy.exception.BaseException;
import com.wy.exception.RedisLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Classname LockableAspect
 * @Description 切面
 * @Date 2020/10/13 11:28
 * @Created wangyong
 */
@Slf4j
@Configuration
@ComponentScan(value = "org.redisson")
@ConditionalOnClass(value = {ConfigureRedisAction.class, Redisson.class})
@Aspect
public class LockableAspect {

    /**
     * 注解参数分隔符
     */
    private static final String SEPARATOR = ":";

    /**
     * spring el表达式开始字符
     */
    private static final String EL_START_CHAR = "#";

    /**
     * spring el表达式分割字符
     */
    private static final String EL_SPLIT_CHAR = ".";

    private static final Integer COMMON_LOCK_CODE = 5098;

    private static final Integer COMMON_LOCK_TIME_OUT_CODE = 5099;

    @Autowired
    private RedissonClient redisson;

    /**
     * 获取注解类型
     *
     * @param annotationClass
     * @param annotations
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T getAnnotation(final Class<T> annotationClass, final Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (final Annotation annotation : annotations) {
                if (annotationClass.equals(annotation.annotationType())) {
                    return (T) annotation;
                }
            }
        }
        return null;
    }

    @Bean(value = "configureRedisAction")
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    /**
     * AOP切入点
     */
    @Pointcut("@annotation(com.wy.redis.lock.Lockable)")
    public void pointCut() {
    }

    /**
     * 切面方法
     *
     * @param point 切入点
     * @return
     * @throws Throwable
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Lockable lockable = method.getAnnotation(Lockable.class);
        boolean lockStatus = false;
        RLock lock = null;
        try {
            String key = this.getLockKey(point, lockable);
            log.info("####redis lock key:{}", key);
            lock = this.redisson.getLock(key);
            lockStatus = lock.tryLock(lockable.timeout(), lockable.keeps(), TimeUnit.MILLISECONDS);
            if (lockStatus) {
                return point.proceed();
            }
            throw BaseException.definedException(COMMON_LOCK_CODE, lockable.message());
        } catch (BaseException ex) {
            throw ex;
        } catch (RedisException | InterruptedException e) {
            String message = String.format("redis lock fail and method name is %s. error message ", method.getName());
            log.error(message,e);
            throw BaseException.definedException(COMMON_LOCK_TIME_OUT_CODE, lockable.message());
        } finally {
            if (lockStatus == Boolean.TRUE && lock != null) {
                this.unlock(lock);
            }
        }
    }

    private void unlock(RLock lock) {
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException ex) {
            String message = String.format("unlock fail and key %s",lock.getName());
            log.error(message,ex);
        }
    }

    /**
     * 获取包括方法参数上的key
     * redis key的拼写规则为 "DistLock+" + lockKey + @DistLockKey<br/>
     *
     * @param point
     * @param lockable
     * @return
     */
    private String getLockKey(ProceedingJoinPoint point, Lockable lockable) {
        String name = null;
        StringBuffer keyBuffer = new StringBuffer();
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            String[] parameterNames = methodSignature.getParameterNames();
            String[] value = lockable.value();
            EvaluationContext context = new StandardEvaluationContext();
            ExpressionParser parser = new SpelExpressionParser();
            for (String key : value) {
                Optional.ofNullable(key)
                        .filter(StringUtils::isNotEmpty)
                        .orElseThrow(RedisLockException.PLEASE_FILL_IN_VALID_KEY.get());
                key = Optional.of(key)
                        .filter(el -> el.startsWith(EL_START_CHAR))
                        .map(el -> {
                            int index = el.indexOf(EL_SPLIT_CHAR);
                            if (index == -1) {
                                String variableName = el.substring(1);
                                Integer ind = this.matching(parameterNames, variableName);
                                Optional.ofNullable(ind)
                                        .filter(Objects::nonNull)
                                        .orElseThrow(RedisLockException.SPRING_EL_CAN_MATCHING_PARAM.get());
                                Object arg = args[ind];
                                Optional.ofNullable(arg)
                                        .filter(Objects::nonNull)
                                        .orElseThrow(RedisLockException.PARAM_LOCK_VALUE_CAN_NOT_NULL.get());
                                return arg.toString();
                            }
                            String variableName = el.substring(1, index);
                            Integer ind = this.matching(parameterNames, variableName);
                            Optional.ofNullable(ind)
                                    .filter(Objects::nonNull)
                                    .orElseThrow(RedisLockException.SPRING_EL_CAN_MATCHING_PARAM.get());
                            Object arg = args[ind];
                            Optional.ofNullable(arg)
                                    .filter(Objects::nonNull)
                                    .orElseThrow(RedisLockException.PARAM_LOCK_VALUE_CAN_NOT_NULL.get());
                            context.setVariable(variableName, arg);
                            try {
                                log.info("spring el,el:{},arg:{},variableName:{},argIndex:{}", el, arg.toString(), variableName, ind);
                                return parser.parseExpression(el).getValue(context, Object.class);
                            } catch (SpelEvaluationException ex) {
                                ex.printStackTrace();
                                throw BaseException.definedException(COMMON_LOCK_CODE, ex.getMessage());
                            }
                        })
                        .map(Object::toString)
                        .orElse(key);
                keyBuffer.append(key).append(SEPARATOR);
            }
            name = keyBuffer.toString();
        }
        return name;
    }

    /**
     * @param parameterNames
     * @param el
     * @return 参数名与el表达式匹配
     */
    private Integer matching(String[] parameterNames, String el) {
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (parameterName.equals(el)) {
                return i;
            }
        }
        return null;
    }

}
