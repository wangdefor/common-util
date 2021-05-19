package com.wy.aop;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.wy.IdempotentService;
import com.wy.context.AbstractIdRequest;
import com.wy.context.IdempotentBodyContext;
import com.wy.context.IdempotentContext;
import com.wy.enums.IdempotentStatusEnum;
import com.wy.exception.AssertEx;
import com.wy.exception.BaseException;
import com.wy.json.JsonUtil;
import com.wy.redis.lock.RedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.wy.exception.RedisLockException.REQUEST_IS_PROCESS;
import static com.wy.exception.RedisLockException.REQUEST_IS_REPEAT;


/**
 * @author wangyong
 * @Classname IdempoemtAspect
 * @Description 切面
 * @Date 2021/5/8 17:22
 */
@Slf4j
@Configuration
@ComponentScan(value = "org.redisson")
@ConditionalOnClass(value = {ConfigureRedisAction.class, Redisson.class})
@Aspect
public class IdempotentAspect {

    /**
     * spring el表达式开始字符
     */
    private static final String EL_START_CHAR = "#";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private IdempotentService idempotentService;

    /**
     * AOP切入点
     */
    @Pointcut("@annotation(com.dmall.demeter.remote.common.idempotent.aop.IdempoentKey)")
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
        Object[] args = point.getArgs();
        IdempoentKey idempoentKey = method.getAnnotation(IdempoentKey.class);
        RLock rLock = null;
        //开始解析 相关值
        String[] parameterNames = methodSignature.getParameterNames();
        EvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        AssertEx.isTrue(args.length - 1 == 0, BaseException.definedException(1008,"幂等请求参数为多个，请将参数放进继承AbstractIdRequest的POJO中")) ;
        AssertEx.isTrue(args[0] instanceof AbstractIdRequest,BaseException.definedException(1008,"参数请继承AbstractIdRequest抽象类")) ;
        if (idempotentService == null) {
            log.warn("method [{}] 没有实现 IdempotentService 无法执行幂等操作", method.getName());
            return point.proceed();
        }
        String requestId = this.getElValue(point, idempoentKey.requestId(), parser, context);
        if (StringUtils.isBlank(requestId)) {
            log.warn("method [{}] requestId is null,无法执行幂等操作", method.getName());
            return point.proceed();
        }
        String appCode = this.getElValue(point, idempoentKey.appCode(), parser, context);
        if (StringUtils.isBlank(appCode)) {
            log.warn("method [{}] appCode is null,无法执行幂等操作", method.getName());
            return point.proceed();
        }
        Hessian2Output output = null;
        IdempotentContext idempotentContext = null;
        String key = requestId + ":" + appCode;
        //开始加锁
        rLock = redisLockUtil.tryLock(key, idempoentKey.timeout(), idempoentKey.keeps(), TimeUnit.SECONDS, REQUEST_IS_REPEAT);
        try {
            //序列化所用
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            output = new Hessian2Output(os);
            //只有处理正确的结果才会进行redis的存储
            if(redisTemplate.hasKey(key)){
                byte[] bytes = (byte[]) redisTemplate.opsForValue().get(key);
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                Hessian2Input input = new Hessian2Input(bis);
                return input.readObject();
            }
            idempotentContext = idempotentService.queryByReqId(requestId, appCode);
            AssertEx.isTrue(idempotentContext == null || idempotentContext.getId() != null,BaseException.definedException(1098,"幂等主键未返回"));
            //状态为空或者失败则执行方法 然后存储结果5分钟
            if(idempotentContext == null || idempotentContext.getStatus().equals(IdempotentStatusEnum.FAIL)){
                Object proceed = point.proceed();
                //写入object对象中
                output.writeObject(proceed);
                output.close();
                //写入序列化后的数组
                byte[] bytes = os.toByteArray();
                redisTemplate.opsForValue().set(key,bytes, Duration.ofMinutes(60L));
                Long id = null;
                //开始插入表 插入两张表皮
                if(idempotentContext != null){
                    idempotentContext.setStatus(IdempotentStatusEnum.SUCCESS);
                    //更新状态
                    idempotentService.updateInfo(idempotentContext);
                    id = idempotentContext.getId();
                }else{
                    id = this.insertIde(point,idempoentKey,parser,context,IdempotentStatusEnum.SUCCESS);
                }
                //插入参数记录
                this.insertBody(point,bytes,id);
                return proceed;
            }
            //剩下的只有处理成功或者处理中的了
            IdempotentStatusEnum status = idempotentContext.getStatus();
            //如果正在处理中
            if(status.equals(IdempotentStatusEnum.ING)){
                throw REQUEST_IS_PROCESS.get();
            }
            //开始处理结果，如果是成功的状态则将结果直接返回，而不用重新请求
            IdempotentBodyContext contexts = idempotentService.getLatestRecordByIdeKey(idempotentContext.getId());
            if(contexts == null){
                log.warn("查询返回结果为空,requestId [{}],appCode [{}]",requestId,appCode);
                return null;
            }
            //取最新的一条
            String responseBody = contexts.getResponseBody();
            if(StringUtils.isBlank(responseBody)){
                log.warn("查询返回结果为空,requestId [{}],appCode [{}],context [{}]",requestId,appCode, JsonUtil.toJson(context));
                return null;
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(contexts.getResponseBody().getBytes(CharEncoding.ISO_8859_1));
            Hessian2Input input = new Hessian2Input(bis);
            return input.readObject();
        } catch (Exception e) {
            //发生异常记录失败
            //开始插入表 插入两张表皮
            Hessian2Output outputE = null;
            try {
                //序列化所用
                ByteArrayOutputStream ose = new ByteArrayOutputStream();
                outputE = new Hessian2Output(ose);
                //写入object对象中
                outputE.writeObject(e);
                outputE.close();
                Long id = null;
                if(idempotentContext == null){
                    idempotentContext = idempotentService.queryByReqId(requestId,appCode);
                    if(idempotentContext == null){
                        //试图插入
                        id = this.insertIde(point,idempoentKey,parser,context,IdempotentStatusEnum.FAIL);
                    }
                }else{
                    id = idempotentContext.getId();
                }
                if(id == null){
                    log.error("幂等主键未返回，无法插入参数表");
                }else{
                    //插入参数记录表
                    this.insertBody(point,ose.toByteArray(),id);
                }
            }catch (Exception e1){
                log.error("插入失败数据异常",e1);
            }finally {
                try {
                    outputE.close();
                }catch (Exception ioE){
                    log.error("关闭流异常",ioE);
                }
            }
            throw e;
        }finally {
            redisLockUtil.tryUnLock(rLock);
            if(output != null){
                try {
                    output.close();
                }catch (Exception e){
                    log.error("关闭流异常",e);
                }
            }
        }
    }

    private void insertBody(ProceedingJoinPoint point, byte[] bytes,Long id) {
        try {
            idempotentService.insertBody(IdempotentBodyContext.builder()
                    .responseBody(new String(bytes,CharEncoding.ISO_8859_1))
                    .requestParam(JsonUtil.toJson(point.getArgs()))
                    .idempotentId(id)
                    .build());
        } catch (IOException ioException) {
            log.error("序列化失败无法插入",ioException);
        }
    }

    private Long insertIde(ProceedingJoinPoint point, IdempoentKey idempoentKey, ExpressionParser parser, EvaluationContext context,IdempotentStatusEnum statusEnum) {
        return idempotentService.insert(IdempotentContext.builder()
                .appCode(getElValue(point,idempoentKey.appCode(),parser,context))
                .businessDesc(getElValue(point,idempoentKey.businessDesc(),parser,context))
                .businessNo(getElValue(point,idempoentKey.businessNo(),parser,context))
                .businessType(getElValue(point,idempoentKey.businessType(),parser,context))
                .requestDesc(getElValue(point,idempoentKey.requestDesc(),parser,context))
                .requestId(getElValue(point,idempoentKey.requestId(),parser,context))
                .status(statusEnum)
                .build());
    }


    private String getElValue(ProceedingJoinPoint point, String value, ExpressionParser parser, EvaluationContext context) {
        if (!value.startsWith(EL_START_CHAR)) {
            return value;
        }
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            Expression expression = parser.parseExpression(value);
            String s = expression.getValue(context, String.class);
            return s;
        }
        return "";
    }

    public static void main(String[] args) {

    }

}
