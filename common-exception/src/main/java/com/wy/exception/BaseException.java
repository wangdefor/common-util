package com.wy.exception;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 异常基础类
 */
public class BaseException extends RuntimeException implements Supplier<BaseException>, Serializable {
    /**
     * 异常错误类型
     */

    private IErrorType errorType;

    public BaseException(IErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public BaseException(IErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public BaseException(IErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public IErrorType getErrorType() {
        return errorType;
    }

    @Override
    public BaseException get() {
        return this;
    }

    public static BaseException definedException(Integer code, String message) {
        return new BaseException(new IErrorType() {
            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Integer getCode() {
                return code;
            }

        });
    }

    public static BaseException builder(@NotNull IErrorType errorType) {
        return definedException(errorType.getCode(),errorType.getMessage());
    }


}
