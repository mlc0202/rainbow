package com.icitic.core.db;

import java.util.Map;

import javax.management.MBeanServer;

import com.google.common.collect.ImmutableMap;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.db.dao.object.CodeGenerator;
import com.icitic.core.db.internal.DaoManagerImpl;
import com.icitic.core.db.internal.ObjectManagerImpl;
import com.icitic.core.db.object.ObjectType;
import com.icitic.core.service.DefaultBundleActivator;
import com.icitic.core.service.InjectProvider;
import com.icitic.core.util.ioc.Bean;

public class Activator extends DefaultBundleActivator {

	private static Activator plugin;

	@Override
	public Map<String, Bean> getContextConfig() {
		return ImmutableMap.of( //
				"daoManager", Bean.singleton(DaoManagerImpl.class), //
				"objectManager", Bean.singleton(ObjectManagerImpl.class));
	}

	@Override
	public void start(MBeanServer mBeanServer) throws BundleException {
		super.start(mBeanServer);
		plugin = this;
		// 注册扩展点
		registerExtensionPoint(ObjectType.class);
		registerExtensionPoint(CodeGenerator.class);

		// 注册扩展
		registerExtension(InjectProvider.class, "daoManager");
		registerExtension(InjectProvider.class, "objectManager");
	}

	@Override
	public void stop() {
		context.close();
		super.stop();
		plugin = null;
	}

	public static DaoManager getDaoManager() {
		return plugin.getContext().getBean("daoManager", DaoManagerImpl.class);
	}
}
