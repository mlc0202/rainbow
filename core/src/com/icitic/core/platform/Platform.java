package com.icitic.core.platform;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.icitic.core.bundle.Bundle;
import com.icitic.core.bundle.BundleListener;
import com.icitic.core.bundle.BundleManager;
import com.icitic.core.bundle.BundleManagerImpl;
import com.icitic.core.bundle.BundleState;
import com.icitic.core.console.CommandProvider;
import com.icitic.core.console.Console;
import com.icitic.core.extension.ExtensionRegistry;
import com.icitic.core.util.JMXServiceURLBuilder;
import com.icitic.core.util.ioc.Bean;
import com.icitic.core.util.ioc.Context;

/**
 * Rainbow 系统平台
 * 
 * @author lijinghui
 * 
 */
public final class Platform {

	private Platform() {
	}

	private final static Logger logger = LoggerFactory.getLogger(Platform.class);

	private static Platform platform;

	private static Config config;

	public static File home;

	public static PlatformState state = PlatformState.READY;

	/**
	 * 返回平台的id
	 * 
	 * @return
	 */
	public static int getId() {
		return config.getId();
	}

	public static String getProject() {
		return config.getProject();
	}

	/**
	 * Rainbow 平台的启动入口
	 */
	public static void startup() {
		startup(true);
	}

	/**
	 * 启动Rainbow平台
	 */
	public static void startup(boolean startLocalJmxServer) {
		if (state != PlatformState.READY)
			return;

		String homeStr = System.getProperty("RAINBOW_HOME");
		checkNotNull(homeStr, "RAINBOW_HOME must be set");
		home = new File(homeStr);
		logger.info("RAINBOW_HOME = {}", home.getAbsolutePath());

		logger.info("loading config param from core.json...");
		File configFile = new File(home, "conf/core.json");
		try {
			config = JSON.parseObject(Files.toString(configFile, Charsets.UTF_8), Config.class);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		logger.info("done! Rainbow ID = {} Project = {} ", getId(), getProject());

		state = PlatformState.STARTING;
		try {
			platform = new Platform();
			platform.doStart(startLocalJmxServer);
			state = PlatformState.STARTED;
		} catch (Throwable e) {
			platform = null;
			state = PlatformState.READY;
			logger.error("start rainbow failed", e);
			Throwables.propagate(e);
		}
	}

	/**
	 * 关闭Rainbow平台
	 */
	public static void shutdown() {
		if (state != PlatformState.STARTED)
			return;
		state = PlatformState.STOPPING;
		platform.doShutdown();
		state = PlatformState.READY;
	}

	/**
	 * 用来与外部世界联系的口，轻易不要使用这个函数。
	 * 
	 * @param bundleId
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createBundleObject(String bundleId, String className, Class<T> requiredType)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		BundleManager bundleManager = platform.context.getBean(BundleManager.class);
		Bundle bundle = bundleManager.get(bundleId);
		checkNotNull(bundle, "bundle [%s] not found", bundleId);
		checkState(bundle.getState() == BundleState.ACTIVE, "bundle [%s] is not active", bundleId);
		Class<?> clazz = bundle.getClassLoader().loadClass(className);
		checkArgument(requiredType.isAssignableFrom(clazz), "[%s] does not match requiredType [%s]", className,
				requiredType.getName());
		return (T) clazz.newInstance();
	}

	/**
	 * 用来与外部世界联系的口，轻易不要使用这个函数。 返回指定bundle的context中的一个bean
	 * 
	 * @param bundleId
	 * @param name
	 * @param requiredType
	 * @return
	 */
	public static <T> T getBundleObject(String bundleId, String name, Class<T> requiredType) {
		BundleManager bundleManager = platform.context.getBean(BundleManager.class);
		Bundle bundle = bundleManager.get(bundleId);
		checkNotNull(bundle, "bundle [%s] not found", bundleId);
		checkState(bundle.getState() == BundleState.ACTIVE, "bundle [%s] is not active", bundleId);
		return bundle.getActivator().getBean(name, requiredType);
	}

	private JMXConnectorServer cs;

	private Context context = new Context(ImmutableMap.of( //
			"mBeanServer", Bean.singleton(MBeanServerFactory.createMBeanServer(), MBeanServer.class), //
			"bundleLoader", Bean.singleton(BundleLoader.class), //
			"bundleManager", Bean.singleton(BundleManagerImpl.class), //
			"bundleCommandProvider", Bean.singleton(BundleCommandProvider.class) //
			));

	/**
	 * 启动平台
	 * 
	 * @param startLocalJmxServer
	 *            是否启动本地的JMX Server
	 */
	private void doStart(boolean startLocalJmxServer) throws Throwable {
		setBundleLoader();
		if (startLocalJmxServer)
			startLocalJmxServer();

		// 注册扩展点
		ExtensionRegistry.registerExtensionPoint(null, BundleListener.class);

		// 控制台
		boolean enableConsole = "true".equals(System.getProperty("console", "false"));
		if (enableConsole) {
			ExtensionRegistry.registerExtensionPoint(null, CommandProvider.class);
			ExtensionRegistry.registerExtension(null, CommandProvider.class,
					context.getBean(BundleCommandProvider.class));
		}

		BundleManager bundleManager = context.getBean(BundleManager.class);
		bundleManager.refresh();
		bundleManager.initStart(config.getOn(), config.getOff());

		if (enableConsole) {
			Console console = new Console();
			Thread t = new Thread(console, "Rainbow Console");
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * 设定BundleLoader
	 * 
	 * @throws Exception
	 */
	private void setBundleLoader() throws Exception {
		BundleLoader bundleLoader = null;
		try {
			// 开发环境
			Class<?> clazz = Class.forName("com.icitic.core.platform.ProjectBundleLoader");
			bundleLoader = (BundleLoader) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			bundleLoader = new JarBundleLoader(); // 生产环境
		}
		context.setBean("bundleLoader", bundleLoader);
	}

	private void startLocalJmxServer() {
		// MBeanServer
		MBeanServer mBeanServer = context.getBean("mBeanServer", MBeanServer.class);
		try {
			JMXServiceURL url = new JMXServiceURLBuilder(config.getJmxPort(), "rainbow").getJMXServiceURL();
			cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);
			cs.start();
		} catch (IOException e) {
			logger.error("start jmx server on {} error", config.getJmxPort(), e);
			Throwables.propagate(e);
		}
		try {
			mBeanServer.registerMBean(new PlatformManager(), PlatformManager.getName());
		} catch (Exception e) {
			logger.error("register PlatformManager failed", config.getJmxPort(), e);
			Throwables.propagate(e);
		}
	}

	/**
	 * 关闭Rainbow平台
	 */
	public void doShutdown() {
		context.close();
		if (cs != null)
			try {
				cs.stop();
			} catch (IOException e) {
				logger.error("Stop JMX connect server failed", e);
			}
		logger.info("Rainbow platform is shutted down!");
	}
}
