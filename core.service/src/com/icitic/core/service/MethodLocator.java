package com.icitic.core.service;

import java.lang.reflect.Method;

public interface MethodLocator {

    Method locate(String serviceId, String methodName);

}
