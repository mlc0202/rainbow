package com.icitic.binding.json;

import javax.management.MBeanServer;

import com.icitic.core.bundle.BundleException;
import com.icitic.core.service.DefaultBundleActivator;
import com.icitic.core.service.channel.Binding;

public class Activator extends DefaultBundleActivator {

	@Override
	public void start(MBeanServer mBeanServer) throws BundleException {
		super.start(mBeanServer);
		// 注册 Binding扩展
		registerExtension(Binding.class, new JsonBinding());
	}

}
