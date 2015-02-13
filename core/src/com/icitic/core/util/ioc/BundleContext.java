package com.icitic.core.util.ioc;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.icitic.core.bundle.BundleActivator;

public class BundleContext extends Context {

	private BundleActivator activator;

	public BundleContext(BundleActivator activator, Map<String, Bean> beans, Context... parents) {
		super(beans, parents);
		this.activator = activator;
	}

	@Override
	public void dependInject(Object object, Bean bean) throws IllegalArgumentException, NoSuchBeanDefinitionException,
			IllegalAccessException, InvocationTargetException {
		super.dependInject(object, bean);
		if (object instanceof ActivatorAware)
			((ActivatorAware) object).setActivator(activator);
	}

}
