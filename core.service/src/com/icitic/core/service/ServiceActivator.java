package com.icitic.core.service;

import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.icitic.core.bundle.BundleClassLoader;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.bundle.Resource;
import com.icitic.core.bundle.ResourceProcessor;
import com.icitic.core.service.internal.Service;
import com.icitic.core.service.util.ApplicationContext;
import com.icitic.core.util.Utils;
import com.icitic.core.util.ioc.NoSuchBeanDefinitionException;

public abstract class ServiceActivator extends DefaultBundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(ServiceActivator.class);

	private List<String> services = null;
	private List<String> serviceImpls = null;

	/**
	 * Bundle启动
	 * 
	 * @param mBeanServer
	 * @param bundleId
	 * @throws BundleException
	 */
	@Override
	public void start(MBeanServer mBeanServer) throws BundleException {
		super.start(mBeanServer);
		services = new LinkedList<String>();
		getClassLoader().procResource(new ResourceProcessor() {
			@Override
			public void processResource(BundleClassLoader classLoader, Resource resource) throws BundleException {
				if (resource.getName().endsWith("Service.class")) {
					String className = resource.getName().replace('/', '.').replace(".class", Utils.NULL_STR);
					try {
						Class<?> serviceClass = classLoader.loadClass(className);
						String serviceId = Activator.getServiceRegistry().registerService(serviceClass);
						services.add(serviceId);
					} catch (ClassNotFoundException e) {
						throw new BundleException(e.toString());
					}
				}
			}
		});
		for (String serviceId : services) {
			Service service = Activator.getServiceRegistry().getService(serviceId);
			String bean = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, service.getServiceClass().getSimpleName());
			try {
				registerServiceImpl(bean);
			} catch (NoSuchBeanDefinitionException e) {
				logger.error("service implement bean 【{}】not found", bean);
			}
		}
	}

	/**
	 * @param service
	 * @see ApplicationContext
	 */
	protected final void registerServiceImpl(Object service) {
		Activator.getServiceRegistry().registerServiceImpl(service);
	}

	/**
	 * 注册一个服务实现对象，该对象在Bundle的Context中
	 * 
	 * @param name
	 */
	protected final void registerServiceImpl(String name) {
		registerServiceImpl(getBean(name));
	}

	private void addServiceImpl(String serviceId) {
		if (serviceImpls == null)
			serviceImpls = new LinkedList<String>();
		serviceImpls.add(serviceId);
	}

	/**
	 * 注册一个服务实现对象， 指定服务id
	 * 
	 * @param serviceId
	 * @param service
	 */
	protected final void registerServiceImpl(String serviceId, Object service) {
		Activator.getServiceRegistry().registerServiceImpl(serviceId, service);
		addServiceImpl(serviceId);
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

	@Override
	public void stop() {
		if (services != null) {
			for (String serviceId : services) {
				Activator.getServiceRegistry().unregisterService(serviceId);
			}
		}
		if (serviceImpls != null) {
			for (String serviceId : serviceImpls) {
				Activator.getServiceRegistry().unregisterServiceImpl(serviceId);
			}
		}
		super.stop();
	}

}
