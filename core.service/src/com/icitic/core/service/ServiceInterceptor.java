package com.icitic.core.service;

import java.lang.reflect.Method;

/**
 * 服务拦截器接口
 * 
 * @author lijinghui
 * 
 */
public interface ServiceInterceptor {

    void beforeService(Class<?> service, Method method, Request request);

    void afterService(Class<?> service, Method method, Request request, Response response);
}
