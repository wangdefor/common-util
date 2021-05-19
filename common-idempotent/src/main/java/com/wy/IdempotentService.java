package com.wy;


import com.wy.context.IdempotentBodyContext;
import com.wy.context.IdempotentContext;

/**
 * @author wangyong
 * @Classname IServiceIdempoemt
 * @Description 幂等服务表
 * @Date 2021/5/12 17:02
 */
public interface IdempotentService {

    /**
     * 插入数据
     *
     * @param model 实体类
     * @return id
     */
    Long insert(IdempotentContext model);

    /**
     * 插入出入参
     *
     * @param model 实体类
     */
    void insertBody(IdempotentBodyContext model);

    /**
     * 更新幂等状态
     *
     * @param model 实体类
     */
    void updateInfo(IdempotentContext model);


    /**
     * 根据请求id和appCode查询
     *
     * @param requestId 请求id
     * @param appCode appCode
     * @return {@link IdempotentContext}
     */
    IdempotentContext queryByReqId(String requestId, String appCode);


    /**
     * 根据请求id和appCode查询最新的一条记录
     *
     * @param idempotentId 幂等主键
     * @return {@link IdempotentContext}
     */
    IdempotentBodyContext getLatestRecordByIdeKey(Long idempotentId);
}
