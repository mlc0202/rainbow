package com.icitic.core.service.util;

import java.util.Map;

import com.icitic.core.bundle.BundleActivator;
import com.icitic.core.service.Activator;
import com.icitic.core.util.ioc.Bean;
import com.icitic.core.util.ioc.BundleContext;
import com.icitic.core.util.ioc.Context;
import com.icitic.core.util.ioc.Inject;

/**
 * 支持平台注入的Context，注入的顺序是容器优先
 * 
 * 容器中没找到并且有Inject注解的，去平台注入。注入名为Inject注解的value值
 * 
 * @author lijinghui
 * 
 */
public class ApplicationContext extends BundleContext {

	public ApplicationContext(BundleActivator activator, Map<String, Bean> beans, Context... parents) {
		super(activator, beans, parents);
	}

	@Override
	protected Object getInjectBean(Inject inject, String injectName, Class<?> injectType) {
		Object injectBean = super.getInjectBean(inject, injectName, injectType);
		if (injectBean == null && inject != null)
			injectBean = Activator.getServiceRegistry().getInjectTarget(inject.value(), injectType);
		return injectBean;
	}
}
