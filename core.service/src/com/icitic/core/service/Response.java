package com.icitic.core.service;

import com.icitic.core.model.exception.AppException;
import com.icitic.core.util.Utils;

/**
 * 服务调用结果对象
 * 
 * @author lijinghui
 * 
 */
public class Response {

    private Object value;

    private Throwable exception;

    public Response(Object value) {
        this.value = value;
    }

    public Response(Throwable exception) {
        this.exception = exception;
    }

    public Object getValue() {
        return value;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean hasException() {
        return (this.exception != null);
    }

    public boolean isAppException() {
        return exception instanceof AppException;
    }

    public String getExceptionMessage() {
        if (hasException()) {
            String result = exception.getMessage();
            return (result == null) ? exception.toString() : result;
        }
        return Utils.NULL_STR;
    }
}
