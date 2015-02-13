package com.icitic.core.extension;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.icitic.core.model.exception.AppException;
import com.icitic.core.util.Utils;

/**
 * 扩展点及扩展注册表
 * 
 * @author lijinghui
 * 
 */
public abstract class ExtensionRegistry {

	private static Logger logger = LoggerFactory.getLogger(ExtensionRegistry.class);

	private static Map<Class<?>, ExtensionPoint> pointMap = new MapMaker().concurrencyLevel(1).makeMap();

	/**
	 * 注册一个扩展点
	 * 
	 * @param bundle
	 * @param clazz
	 */
	public static void registerExtensionPoint(String bundle, Class<?> clazz) {
		checkState(!pointMap.containsKey(clazz), "duplicated extension point %s ", clazz.getName());
		logger.info("register extension point [{}]", clazz.getSimpleName());
		ExtensionPoint point = new ExtensionPoint(bundle, clazz);
		pointMap.put(clazz, point);
	}

	/**
	 * 注销一个扩展点
	 * 
	 * @param clazz
	 */
	public static void unregisterExtensionPoint(Class<?> clazz) {
		logger.info("try to unregister extension point [{}]", clazz.getName());
		if (pointMap.remove(clazz) == null)
			logger.warn("extension point [{}] not exist", clazz.getName());
		else
			logger.info("extension point [{}] unregistered", clazz.getName());
	}

	/**
	 * 注册一个扩展
	 * 
	 * @param bundle
	 * @param clazz
	 * @param object
	 * @return
	 */
	public static Extension registerExtension(String bundle, Class<?> clazz, Object object) {
		ExtensionPoint point = getExtensionPoint(clazz);
		Extension extension = point.addExtension(bundle, object);
		logger.info("register extension [{}]:  [{}]", clazz.getSimpleName(), extension.getName());
		return extension;
	}

	/**
	 * 注销一个扩展
	 * 
	 * @param extension
	 */
	public static void unregisterExtension(Extension extension) {
		ExtensionPoint point = extension.getExtensionPoint();
		point.removeExtension(extension);
		logger.info("unregister extension object {}", extension.getName());
	}

	/**
	 * 获取一个扩展点
	 * 
	 * @param clazz
	 *            扩展点接口
	 * @return
	 * @throws NullPointerException
	 *             不存在该接口的扩展点时
	 */
	public static ExtensionPoint getExtensionPoint(Class<?> clazz) throws AppException {
		ExtensionPoint point = pointMap.get(clazz);
		checkNotNull(point, "Extension Point [%s] not registered", clazz.getName());
		return point;
	}

	/**
	 * 获得一个扩展点的所有扩展
	 * 
	 * @param clazz
	 * @return
	 */
	public static Collection<Extension> getExtensions(Class<?> clazz) {
		ExtensionPoint point = getExtensionPoint(clazz);
		return point.getExtensions();
	}

	/**
	 * 获得一个扩展点的所有扩展名
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<String> getExtensionNames(Class<?> clazz) {
		ExtensionPoint point = getExtensionPoint(clazz);
		return Utils.transform(point.getExtensions(), Utils.toNameFunction);
	}

	/**
	 * 获得一个扩展点的所有扩展对象
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getExtensionObjects(Class<T> clazz) {
		ExtensionPoint point = getExtensionPoint(clazz);
		Collection<Extension> extensions = point.getExtensions();
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (Extension extension : extensions)
			builder.add((T) extension.getObject());
		return builder.build();
	}

	/**
	 * 获取一个扩展点的指定名字的扩展实例对象
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 * @throws AppException
	 *             扩展点不存在或者扩展不存在时抛出AppException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getExtensionObject(Class<T> clazz, String name) throws AppException {
		ExtensionPoint point = getExtensionPoint(clazz);
		T result = (T) point.getExtensionObject(name);
		return checkNotNull(result, "extension [%s] not registed on point [%s]", name, clazz.getSimpleName());
	}

	/**
	 * 获取一个扩展点的指定名字的扩展实例对象, 不抛出任何异常
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static <T> T safeGetExtensionObject(Class<T> clazz, String name) {
		try {
			return getExtensionObject(clazz, name);
		} catch (Throwable e) {
			logger.warn(e.getMessage());
			return null;
		}
	}
}
