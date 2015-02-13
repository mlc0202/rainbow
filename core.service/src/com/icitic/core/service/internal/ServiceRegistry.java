package com.icitic.core.service.internal;

import static com.icitic.core.util.Preconditions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.icitic.core.extension.ExtensionRegistry;
import com.icitic.core.model.exception.AppException;
import com.icitic.core.service.InjectProvider;
import com.icitic.core.service.Internal;
import com.icitic.core.service.Request;
import com.icitic.core.service.Response;
import com.icitic.core.service.ServiceInterceptor;
import com.icitic.core.service.ServiceInvoker;
import com.icitic.core.util.ioc.Inject;

/**
 * 服务注册表
 * 
 * @author lijinghui
 * 
 */
public final class ServiceRegistry implements ServiceInvoker {

	private Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

	/** 本地服务 */
	private ConcurrentMap<String, Service> services = new ConcurrentHashMap<String, Service>();

	/**
	 * 返回指定名称的本地服务
	 * 
	 * @param service
	 * @return
	 */
	public Service getService(String serviceId) {
		Service service = services.get(serviceId);
		checkNotNull(service, "未注册服务 [%s]", serviceId);
		return service;
	}

	public List<Service> getServices(final String prefix) {
		Predicate<Service> predicate = Predicates.alwaysTrue();
		if (prefix != null && !prefix.isEmpty())
			predicate = new Predicate<Service>() {
				@Override
				public boolean apply(Service input) {
					return input.getServiceId().startsWith(prefix);
				}
			};
		Iterable<Service> result = Collections2.filter(services.values(), predicate);
		return Ordering.usingToString().sortedCopy(result);
	}

	/**
	 * 注册一个服务定义
	 * 
	 * @param serviceId
	 * @param serviceClass
	 */
	public String registerService(Class<?> serviceClass) {
		checkNotNull(serviceClass, "null service def class");
		checkArgument(serviceClass.isInterface(), "[%s] is not an interface", serviceClass.getName());
		String serviceId = findServiceId(serviceClass.getName(), true);
		checkArgument(!services.containsKey(serviceId), "duplicated service id of [%s]", serviceId);
		Service service = new Service(serviceId, serviceClass);
		services.put(serviceId, service);
		logger.info("service [{}] registered", serviceId);
		return serviceId;
	}

	/**
	 * 注册一个服务实现类，创建该类实例并作平台级依赖注入
	 * 
	 * @param serviceId
	 * @param serviceClass
	 */
	public String registerServiceImpl(Class<?> serviceClass) {
		checkNotNull(serviceClass, "null service impl class");
		String serviceId = findServiceId(serviceClass.getName(), false);
		Service service = getService(serviceId);
		checkState(service.getServiceClass().isAssignableFrom(serviceClass), "[%s] should implement [%s]",
				serviceClass.getName(), service.getServiceClass().getName());
		try {
			logger.debug("create service [{}] implement object:[{}]", serviceId, serviceClass.getName());
			Object impl = serviceClass.newInstance();
			platformInject(impl);
			service.setServiceImpl(impl);
			logger.info("service [{}] implement registered", serviceId);
			return serviceId;
		} catch (Exception e) {
			logger.error("create service [{}] implement object failed", serviceId, e);
			throw Throwables.propagate(e);
		}
	}

	/**
	 * 注册一个服务实现对象
	 * 
	 * @param serviceImpl
	 * @return
	 */
	public String registerServiceImpl(Object serviceImpl) {
		checkNotNull(serviceImpl, "null service impl class");
		String serviceId = findServiceId(serviceImpl.getClass().getName(), false);
		return registerServiceImpl(serviceId, serviceImpl);
	}

	/**
	 * 注册一个服务实现对象
	 * 
	 * @param serviceId
	 *            服务id
	 * @param serviceImpl
	 *            实现对象
	 * @return
	 */
	public String registerServiceImpl(String serviceId, Object serviceImpl) {
		checkNotNull(serviceId, "cannot register [%s] to null serviceId", serviceImpl.getClass().getName());
		Service service = getService(serviceId);
		checkState(service.getServiceClass().isAssignableFrom(serviceImpl.getClass()), "[%s] should implement [%s]",
				serviceImpl.getClass().getName(), service.getServiceClass().getName());
		service.setServiceImpl(serviceImpl);
		logger.info("service [{}] implement registered", serviceId);
		return serviceId;
	}

	/**
	 * 为一个对象做平台级的依赖注入
	 * 
	 * @param serviceClass
	 * @param object
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void platformInject(Object object) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class<?> clazz = object.getClass();
		logger.debug("prepare Application Depend Inject for [{}]", clazz.getName());
		for (Method method : clazz.getMethods()) {
			if (method.getName().startsWith("set")) {
				Inject inject = method.getAnnotation(Inject.class);
				if (inject != null) {
					Class<?>[] paramTypes = method.getParameterTypes();
					checkArgument(paramTypes.length == 1, "invalid inject method [%s]", method.getName());
					Class<?> injectType = method.getParameterTypes()[0];
					Object injectTarget = getInjectTarget(inject.value(), injectType);
					checkNotNull(injectTarget, "inject target [%s] not found", inject.value());
					method.invoke(object, injectTarget);
				}
			}
		}
	}

	/**
	 * 返回一个平台管理的可用来注入的对象，如服务，Dao等
	 * 
	 * @param injectName
	 *            对象名，仅用于InjectProvider定位用
	 * @param injectType
	 *            对象类型
	 * @return
	 */
	public Object getInjectTarget(String injectName, Class<?> injectType) {
		logger.debug("prepare to inject [{}({})]", injectType.getSimpleName(), injectName);
		String injectClassName = injectType.getName();
		if (injectClassName.endsWith("Service")) {
			String serviceId = findServiceId(injectClassName, true);
			Service service = getService(serviceId);
			return service.getServiceProxy();
		} else {
			Object injectTarget = null;
			for (InjectProvider ip : ExtensionRegistry.getExtensionObjects(InjectProvider.class)) {
				if (ip.provide(injectType)) {
					injectTarget = ip.getInjectObject(injectType, injectName);
					if (injectTarget != null)
						break;
				}
			}
			checkNotNull(injectTarget, "inject target [%s(%s)] not found", injectClassName, injectName);
			return injectTarget;
		}
	}

	/**
	 * 注销一个服务定义
	 * 
	 * @param serviceId
	 */
	public void unregisterService(String serviceId) {
		services.remove(serviceId);
		logger.info("service [{}] unregistered", serviceId);
	}

	/**
	 * 注销一个服务实现
	 * 
	 * @param serviceId
	 */
	public void unregisterServiceImpl(String serviceId) {
		Service service = services.get(serviceId);
		service.setServiceImpl(null);
		logger.info("service [{}] implement unregistered", serviceId);
	}

	/**
	 * 根据类名获取服务的Id号。
	 * 
	 * <p>
	 * Bundle设计规范要求Bundle内的代码的root
	 * package必须与Bundle名称一致。以api结尾的包是接口定义Bundle，以impl结尾的包是接口实现Bundle。
	 * <p>
	 * 旧的设计方案是：一个Bundle仅允许定义一个服务。 Bundle名扣除com.icitic.和结尾（.api/.impl）即为服务名。如：
	 * <blockquote>
	 * 
	 * <pre>
	 * com.icitic.scheduler.wait.api.WaitService 定义了serviceId 为 scheduler.wait的服务接口，在名为scheduler.wait.api的Bundle中
	 * com.icitic.scheduler.wait.impl.WaitServiceImpl 定义了serviceId 为 scheduler.wait的服务实现，在名为scheduler.wait.impl的Bundle中
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * <p>
	 * 新的设计方案是：一个Bundle允许定义多个服务。如： <blockquote>
	 * 
	 * <pre>
	 * com.icitic.scheduler.api.wait.WaitService 定义了serviceId 为 scheduler.wait的服务接口，在名为scheduler.api的Bundle中
	 * com.icitic.scheduler.impl.wait.WaitServiceImpl 定义了serviceId 为 scheduler.wait的服务实现，在名为scheduler.impl的Bundle中
	 * </pre>
	 * 
	 * </blockquote> 目前采用新的设计方案并兼容旧的方案
	 * 
	 * @param serviceClassName
	 *            服务接口或服务实现类名
	 * @param isDef
	 *            <code>true</code>为接口，<code>false</code>为实现
	 * @return
	 */
	public String findServiceId(String serviceClassName, boolean isDef) {
		String suffix = isDef ? "Service" : "ServiceImpl";
		String flag = isDef ? "api" : "impl";
		try {
			checkArgument(serviceClassName.endsWith(suffix), null);
			Iterator<String> i = Splitter.on('.').split(serviceClassName).iterator();
			checkArgument("com".equals(i.next()), null);
			checkArgument("icitic".equals(i.next()), null);

			LinkedList<String> idParts = Lists.newLinkedList();
			while (i.hasNext()) {
				String s = i.next();
				if (flag.equals(s))
					break;
				idParts.add(s);
			}
			String last = i.next();

			if (i.hasNext()) {
				idParts.add(last);
				last = i.next();
			}

			checkArgument(!i.hasNext(), null);
			last = last.substring(0, last.length() - suffix.length()).toLowerCase();
			checkArgument(idParts.getLast().equals(last), null);
			return Joiner.on('.').join(idParts);
		} catch (AppException e) {
			throw new AppException("[%s] is not a valid %s name", serviceClassName, suffix);
		}
	}

	/**
	 * 处理对本地服务的一个请求
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public Response invoke(Request request) {
		Service service = getService(request.getService());
		checkState(!service.isInternal(), "不能调用内部服务[%s]", request.getService());

		Method method = service.getMethod(request.getMethod());
		checkState(method.getAnnotation(Internal.class) == null, "不能调用服务[%s]的内部函数[%s]", request.getService(),
				request.getMethod());
		Object target = service.getServiceImpl();
		Response response = null;

		List<ServiceInterceptor> interceptors = ExtensionRegistry.getExtensionObjects(ServiceInterceptor.class);
		for (ServiceInterceptor interceptor : interceptors) {
			interceptor.beforeService(service.getServiceClass(), method, request);
		}
		try {
			Object value = method.invoke(target, request.getArgs());
			response = new Response(value);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			response = new Response(t);
		} catch (Throwable e) {
			response = new Response(e);
		}
		if (response.hasException()) {
			if (response.isAppException())
				logger.info("[{}:{}] AppException: {}", service, method, response.getException().getMessage());
			else
				logger.error("invoking [{}:{}] failed", service, method, response.getException());
		}
		for (ServiceInterceptor interceptor : interceptors) {
			interceptor.afterService(service.getServiceClass(), method, request, response);
		}
		return response;
	}

	@Override
	public Method getMethod(String serviceId, String methodName) {
		Service service = getService(serviceId);
		return service.getMethod(methodName);
	}

	@Override
	public Object getServiceImpl(String serviceId) {
		Service service = getService(serviceId);
		return service.getServiceImpl();
	}

}
