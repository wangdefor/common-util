package com.wy.context;

import java.io.Serializable;

/**
 * @author wangyong
 * @Classname AbstractIdRequest
 * @Description 抽象的幂等请求参数模型
 * @Date 2021/5/13 14:33
 */
public abstract class AbstractIdRequest implements Serializable {


    /**
     * 请求编号
     */
    private String requestId;

    /**
     * appCode 编码
     */
    private String appCode;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
