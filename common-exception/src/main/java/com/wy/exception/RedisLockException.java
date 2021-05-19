package com.wy.exception;

/**
 * @Classname RedisLockException
 * @Description redis锁相关异常
 * @Date 2020/10/13 11:42
 * @Created wangyong
 */
public enum RedisLockException implements IErrorType {

    PARAM_LOCK_VALUE_CAN_NOT_NULL(6002, "锁参数值不允许为空，请确认"),
    SPRING_EL_CAN_MATCHING_PARAM(6003, "el表达式无法匹配形参"),
    PLEASE_FILL_IN_VALID_KEY(6004, "请填写有效的key"),
    REQUEST_IS_REPEAT(5015,"请求重复"),
    REQUEST_IS_PROCESS(5016,"请求正在处理中"),
    ;

    /**
     * 错误类型
     **/
    private Integer code;

    /**
     * 错误描述
     **/
    private String message;

    RedisLockException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
