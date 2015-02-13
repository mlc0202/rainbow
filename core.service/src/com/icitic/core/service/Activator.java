package com.icitic.core.service;

import java.util.Map;

import javax.management.MBeanServer;

import com.google.common.collect.ImmutableMap;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.service.channel.Binding;
import com.icitic.core.service.channel.Transport;
import com.icitic.core.service.internal.ServiceRegistry;
import com.icitic.core.util.ioc.Bean;

public class Activator extends DefaultBundleActivator {

	private static Activator activator;

	@Override
	public Map<String, Bean> getContextConfig() {
		return ImmutableMap.of( //
				"serviceInvoker", Bean.singleton(ServiceRegistry.class) //
				);
	}

	public void start(MBeanServer mBeanServer) throws BundleException {
		super.start(mBeanServer);
		activator = this;

		// 注册扩展点
		registerExtensionPoint(Binding.class);
		registerExtensionPoint(Transport.class);
		registerExtensionPoint(InjectProvider.class);
		registerExtensionPoint(ServiceInterceptor.class);
	}

	@Override
	public void stop() {
		activator = null;
		context.close();
		super.stop();
	}

	public static Activator getDefault() {
		return activator;
	}

	public static ServiceRegistry getServiceRegistry() {
		return activator.context.getBean(ServiceRegistry.class);
	}
}
