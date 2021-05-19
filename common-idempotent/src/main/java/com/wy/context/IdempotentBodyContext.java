package com.wy.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wangyong
 * @Classname IdempoemtContext
 * @Description 幂等上下文
 * @Date 2021/5/12 17:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdempotentBodyContext implements Serializable {


    /**
     * 主键
     */
    private Long id;

    /**
     * 幂等主键
     *
     * @return
     */
    private Long idempotentId;


    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 请求body体
     */
    private String responseBody;

    /**
     * 创建时间
     */
    private Long createTime;
}
