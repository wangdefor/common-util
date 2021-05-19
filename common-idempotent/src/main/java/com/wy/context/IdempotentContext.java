package com.wy.context;

import com.wy.enums.IdempotentStatusEnum;
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
public class IdempotentContext implements Serializable {


    /**
     * 主键
     */
    private Long id;

    /**
     * 请求编号id
     *
     * @return
     */
    private String requestId;

    /**
     * 请求类型编码
     *
     * @return
     */
    private String appCode;

    /**
     * 请求类型描述
     * 主要用来告诉我是做啥的
     * 非必填
     * @return
     */
    private String requestDesc ;

    /**
     * 需要储存的业务字段
     * 非必填
     * @return
     */
    private String businessNo ;

    /**
     * 需要储存的业务字段类型
     * 非必填
     * @return
     */
    private String businessType ;

    /**
     * 需要储存的业务字段类型描述
     * 非必填
     * @return
     */
    private String businessDesc;

    /**
     * 状态
     */
    private IdempotentStatusEnum status;
}
