package com.icitic.core.service;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import com.icitic.core.service.util.ApplicationContext;

/**
 * 服务实现Bundle的Activator，提供了服务实现的注册函数
 * 
 * @author lijinghui
 * 
 */
public abstract class ServiceImplActivator extends DefaultBundleActivator {

	private List<String> services = null;

	/**
	 * 注册一个服务实现类，平台会创建该实现的实例并做依赖注入
	 * 
	 * @param serviceClass
	 */
	protected final void registerServiceImpl(Class<?> serviceClass) {
		String serviceId = Activator.getServiceRegistry().registerServiceImpl(serviceClass);
		addServiceId(serviceId);
	}

	/**
	 * 注册一个服务实现对象，该对象的依赖应该已经完全被注入了。
	 * <P>
	 * 提供这个方法的主要目的，是因为除了平台级的依赖注入外，还可能需要本地ioc容器进行依赖注入，
	 * 这时候服务对象就应该用本地的IOC容器创建并完成所有的依赖注入，包括平台级的依赖
	 * 
	 * @param service
	 * @see ApplicationContext
	 */
	protected final void registerServiceImpl(Object service) {
		String serviceId = Activator.getServiceRegistry().registerServiceImpl(service);
		addServiceId(serviceId);
	}

	/**
	 * 注册一个服务实现对象，该对象在Bundle的Context中
	 * 
	 * @param name
	 */
	protected final void registerServiceImpl(String name) {
		registerServiceImpl(getBean(name));
	}

	/**
	 * 注册一个服务实现对象， 指定服务id
	 * 
	 * @param serviceId
	 * @param service
	 */
	protected final void registerServiceImpl(String serviceId, Object service) {
		Activator.getServiceRegistry().registerServiceImpl(serviceId, service);
		addServiceId(serviceId);
	}

	/**
	 * 注册一个服务实现对象， 指定服务id
	 * 
	 * @param serviceId
	 * @param name
	 */
	protected final void registerServiceImpl(String serviceId, String name) {
		registerServiceImpl(serviceId, getBean(name));
	}

	/**
	 * 为一个对象做平台级的依赖注入
	 * 
	 * @param object
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	protected final void platformInject(Object object) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Activator.getServiceRegistry().platformInject(object);
	}

	private void addServiceId(String serviceId) {
		if (services == null)
			services = new LinkedList<String>();
		services.add(serviceId);
	}

	@Override
	public void stop() {
		if (services != null)
			for (String serviceId : services) {
				Activator.getServiceRegistry().unregisterServiceImpl(serviceId);
			}
		super.stop();
	}

}
