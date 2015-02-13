package com.icitic.core.service.internal;

import static com.icitic.core.util.Preconditions.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.icitic.core.model.exception.AppException;
import com.icitic.core.service.Internal;
import com.icitic.core.service.ServiceNotReadyException;

public class Service implements InvocationHandler {

	/**
	 * 服务ID
	 */
	private String serviceId;

	/**
	 * 服务定义接口类
	 */
	private Class<?> serviceClass;

	/**
	 * 方法对象缓存
	 */
	private Map<String, Method> methods;

	/**
	 * 注入到其他服务的代理对象
	 */
	private Object serviceProxy;

	/**
	 * 服务实现对象
	 */
	private Object serviceImpl;

	/**
	 * 注册时间
	 */
	private long registerTime;

	/**
	 * 内部服务标识
	 */
	private boolean internal;

	public String getServiceId() {
		return serviceId;
	}

	public Class<?> getServiceClass() {
		return serviceClass;
	}

	public Object getServiceImpl() {
		checkNotNull(serviceImpl, "服务[%s]的实现未注册", serviceId);
		return serviceImpl;
	}

	public void setServiceImpl(Object serviceImpl) {
		this.serviceImpl = serviceImpl;
	}

	public Object getServiceProxy() {
		return serviceProxy;
	}

	public long getRegisterTime() {
		return registerTime;
	}

	public Service(String serviceId, Class<?> serviceClass) {
		this.serviceId = serviceId;
		this.serviceClass = serviceClass;
		this.serviceProxy = Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[] { serviceClass }, this);
		internal = serviceClass.getAnnotation(Internal.class) != null;
		methods = cacheMethods(serviceClass);
		registerTime = System.currentTimeMillis();
	}

	public boolean isInternal() {
		return internal;
	}

	public Method getMethod(String methodName) {
		Method method = methods.get(methodName);
		checkNotNull(method, "服务[%s]没有[%s]这个函数", serviceId, methodName);
		return method;
	}

	private static Map<String, Method> cacheMethods(Class<?> serviceClass) {
		Method[] allMethod = serviceClass.getMethods();
		if (allMethod.length == 0)
			return ImmutableMap.of();

		Map<String, Method> map = new HashMap<String, Method>(allMethod.length);
		for (Method method : allMethod) {
			String name = method.getName();
			if (!map.containsKey(name))
				map.put(name, method);
			else if (method.getClass() == serviceClass) { // 不相等表示上级接口中的函数，忽略之
				Method old = map.get(name);
				if (old.getClass() != serviceClass) {
					map.put(name, method);
				} else
					throw new AppException("服务[%s]的函数[%s]重名了", serviceClass.getSimpleName(), method.getName());
			}
		}
		return map;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (serviceImpl == null) {
			// 将来可能会访问远程部署的服务, 那时候就要注入远程registry了
			throw new ServiceNotReadyException(serviceId);
		}
		try {
			return method.invoke(serviceImpl, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	@Override
	public String toString() {
		return "Service:" + getServiceId();
	}

}
