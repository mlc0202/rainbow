package com.icitic.core.service;

import java.util.Map;

import com.icitic.core.bundle.BundleActivator;
import com.icitic.core.service.util.ApplicationContext;
import com.icitic.core.util.ioc.Bean;
import com.icitic.core.util.ioc.Context;

/**
 * 平台缺省的BundleActivator，使用ApplicationContext
 * 
 * @author lijinghui
 * 
 */
public abstract class DefaultBundleActivator extends BundleActivator {

	@Override
	protected void createContext(Map<String, Bean> contextConfig, Context[] parent) {
		context = new ApplicationContext(this, contextConfig, parent);
	}

}
