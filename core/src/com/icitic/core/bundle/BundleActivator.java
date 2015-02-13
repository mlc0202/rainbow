package com.icitic.core.bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.icitic.core.extension.Extension;
import com.icitic.core.extension.ExtensionRegistry;
import com.icitic.core.platform.Platform;
import com.icitic.core.util.Utils;
import com.icitic.core.util.ioc.Bean;
import com.icitic.core.util.ioc.BundleContext;
import com.icitic.core.util.ioc.Context;

/**
 * Bundle启动器
 * 
 * @author lijinghui
 * 
 */
public abstract class BundleActivator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected MBeanServer mBeanServer;

	private String bundleId;

	protected Context context;

	private List<Class<?>> points;
	private List<Extension> extensions;
	private List<ObjectName> mBeanNames;

	public BundleClassLoader getClassLoader() {
		return (BundleClassLoader) getClass().getClassLoader();
	}

	public String getBundleId() {
		return bundleId;
	}

	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}

	public Context getContext() {
		return context;
	}

	/**
	 * Bundle启动
	 * 
	 * @param mBeanServer
	 * @param bundleId
	 * @throws BundleException
	 */
	public void start(MBeanServer mBeanServer) throws BundleException {
		this.mBeanServer = mBeanServer;
		if (context != null && isContextAutoload())
			context.loadAll();
	}

	/**
	 * 返回是否自动加载context中的所有单例bean
	 * 
	 * @return
	 */
	protected boolean isContextAutoload() {
		return true;
	}

	/**
	 * 创建context
	 * 
	 * @param contextConfig
	 * @param parent
	 */
	protected void createContext(Map<String, Bean> contextConfig, Context[] parent) {
		context = new BundleContext(this, contextConfig, parent);
	}

	/**
	 * 对于自己的context，返回需要的上级Context对应的bundleId
	 * 
	 * @return
	 */
	public List<String> getParentContextId() {
		return Collections.emptyList();
	}

	/**
	 * 返回Context配置
	 * 
	 * @return
	 * @throws BundleException
	 */
	public Map<String, Bean> getContextConfig() throws BundleException {
		final ImmutableMap.Builder<String, Bean> builder = ImmutableMap.builder();
		getClassLoader().procResource(new ResourceProcessor() {
			@Override
			public void processResource(BundleClassLoader classLoader, Resource resource) throws BundleException {
				if (resource.getName().endsWith(".class")) {
					String className = Utils.substringBefore(resource.getName(), ".class").replace('/', '.');
					try {
						Class<?> clazz = classLoader.loadClass(className);
						com.icitic.core.bundle.Bean beandef = clazz.getAnnotation(com.icitic.core.bundle.Bean.class);
						if (beandef != null) {
							String beanName = beandef.name();
							if (beanName.isEmpty()) {
								beanName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName());
								if (beanName.endsWith("Impl"))
									beanName = Utils.substringBefore(beanName, "Impl");
							}
							if (beandef.singleton())
								builder.put(beanName, Bean.singleton(clazz));
							else
								builder.put(beanName, Bean.prototype(clazz));

						}
					} catch (ClassNotFoundException e) {
						throw new BundleException(e.toString());
					}
				}
			}
		});
		Map<String, Bean> map = builder.build();
		return map.isEmpty() ? null : map;
	}

	/**
	 * Bundle停止
	 */
	public void stop() {
		if (points != null)
			for (Class<?> point : points)
				ExtensionRegistry.unregisterExtensionPoint(point);
		if (extensions != null)
			for (Extension extension : extensions)
				ExtensionRegistry.unregisterExtension(extension);
		if (mBeanNames != null)
			unregisterMBean();
		if (context != null)
			context.close();
	}

	/**
	 * 打开bundle中的一个资源
	 * 
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	protected InputStream getResource(String resource) throws IOException {
		Resource r = getClassLoader().getLocalResource(resource);
		return r.getInputStream();
	}

	/**
	 * 返回Bundle的配置目录
	 * 
	 * @return
	 */
	protected File getConfigurePath() {
		return new File(Platform.home, "conf/" + bundleId);
	}

	/**
	 * 返回Bundle的配置目录下指定名字的配置文件对象
	 * 
	 * 在开发阶段，每个项目的配置目录在project/xx/conf目录下，有些文件需要共用统一的配置（如rdm），统一的配置文件在conf/目录下。
	 * 
	 * 这时候可以在文件名后面加上.link后缀,这样就取统一的配置文件去了。
	 * 
	 * @param fileName
	 * @return
	 */
	public File getConfigureFile(String fileName) {
		File file = new File(getConfigurePath(), fileName);
		if (file.exists())
			return file;
		File linkfile = new File(getConfigurePath(), fileName + ".link");
		if (linkfile.exists())
			return new File(Platform.home, "../conf/" + bundleId + "/" + fileName);
		else
			return file;
	}

	/**
	 * 返回Bundle的配置目录下指定后缀的所有文件
	 * 
	 * @param suffix
	 * @return
	 */
	public List<File> getConfigureFiles(final String suffix) {
		final String linksuffix = suffix + ".link";
		File path = getConfigurePath();
		File linkPath = new File(Platform.home, "../conf/" + bundleId);

		String ss[] = path.list();
		if (ss == null)
			return ImmutableList.of();

		Builder<File> builder = ImmutableList.builder();
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].endsWith(suffix))
				builder.add(new File(path, ss[i]));
			else if (ss[i].endsWith(linksuffix))
				builder.add(new File(linkPath, ss[i].substring(0, ss[i].length() - 5)));
		}
		return builder.build();
	}

	/**
	 * 注册扩展点
	 * 
	 * @param clazz
	 * @throws BundleException
	 */
	protected final void registerExtensionPoint(Class<?> clazz) throws BundleException {
		ExtensionRegistry.registerExtensionPoint(bundleId, clazz);
		if (points == null)
			points = new LinkedList<Class<?>>();
		points.add(clazz);
	}

	/**
	 * 注册一个扩展
	 * 
	 * @param clazz
	 *            扩展点
	 * @param object
	 *            扩展对象
	 * @throws BundleException
	 */
	protected final void registerExtension(Class<?> clazz, Object object) throws BundleException {
		Extension extension = ExtensionRegistry.registerExtension(bundleId, clazz, object);
		if (extensions == null)
			extensions = new LinkedList<Extension>();
		extensions.add(extension);
	}

	/**
	 * 注册一个扩展
	 * 
	 * @param clazz
	 *            扩展点
	 * @param beanName
	 *            在context中的扩展对象名
	 * @throws BundleException
	 */
	protected final <T> void registerExtension(Class<T> clazz, String beanName) throws BundleException {
		registerExtension(clazz, getBean(beanName));
	}

	protected final void registerMBean(Object mbean, String name) {
		try {
			ObjectName objName = new ObjectName("com.icitic.rainbow:name=" + name);
			mBeanServer.registerMBean(mbean, objName);
			if (mBeanNames == null)
				mBeanNames = new LinkedList<ObjectName>();
			mBeanNames.add(objName);
		} catch (Exception e) {
			logger.error("registerMBean {} failed", name, e);
		}
	}

	protected final void unregisterMBean() {
		if (mBeanNames == null)
			return;
		for (ObjectName objName : mBeanNames)
			try {
				mBeanServer.unregisterMBean(objName);
			} catch (Exception e) {
				logger.error("unregisterMBean {} failed", objName.getCanonicalName(), e);
			}
	}

	public final <T> T getBean(String name, Class<T> clazz) {
		if (context == null)
			return null;
		return context.getBean(name, clazz);
	}

	public final Object getBean(String name) {
		if (context == null)
			return null;
		return context.getBean(name);
	}
}
