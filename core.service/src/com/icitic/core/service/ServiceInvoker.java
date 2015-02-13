package com.icitic.core.service;

import java.lang.reflect.Method;

public interface ServiceInvoker {

	Object getServiceImpl(String serviceId);

	Method getMethod(String serviceId, String methodName);

	Response invoke(Request request);
}
