package com.wy.enums;

import com.google.common.collect.Lists;

/**
 * @author wangyong
 * @Classname IdempotentStatusEnum
 * @Description IdempotentStatusEnum
 * @Date 2021/5/12 17:18
 */
public enum IdempotentStatusEnum {

    /**
     * 进行中
     */
    ING(0),

    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 失败
     */
    FAIL(2),
    ;

    private Integer code;

    IdempotentStatusEnum(Integer code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static IdempotentStatusEnum getByCode(Integer code){
        return Lists.newArrayList(IdempotentStatusEnum.values())
                .stream()
                .filter(l -> l.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
