package com.wy.exception;

/**
 * @author wangyong
 * @Classname JsonErrorException
 * @Description json错误相关枚举类
 * @Date 2021/5/19 11:37
 */
public enum JsonErrorException implements IErrorType{

    CONVERT_JSON_ERROR(1001,"转换json错误"),

    CONVERT_OBJECT_ERROR(1002,"转换对象失败"),

    CONVERT_OBJECT_ARRAY_ERROR(1003,"转换对象数组失败"),
    ;

    /**
     * 错误类型
     **/
    private Integer code;

    /**
     * 错误描述
     **/
    private String message;

    JsonErrorException(Integer code, String message) {
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
