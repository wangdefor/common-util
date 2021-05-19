package com.wy.exception;

import java.io.Serializable;


public interface IErrorType extends Serializable {
    /**
     * 返回错误类型码
     *
     * @return
     */
    Integer getCode();

    /**
     * 返回错误类型描述信息
     *
     * @return
     */
    String getMessage();

    /**
     * 默认的设置message。 如果使用这个请在自己的方法中重写，现在写在这里是为了下面convertMessageByParams的message覆盖
     *
     * @return
     */
    default void setMessage(String message) {
    }

    /**
     * 方便使用Supplier中的get方法调用
     *
     * @return BaseException
     */
    default BaseException get() {
        return new BaseException(this);
    }
}
