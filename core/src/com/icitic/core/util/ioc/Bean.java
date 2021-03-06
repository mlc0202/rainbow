package com.icitic.core.util.ioc;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 这个对象封装了一个IOC Bean 的配置定义
 * 
 * @author lijinghui
 * @see Context
 */
public final class Bean {

	private Class<?> clazz;

	private Map<String, Object> params;

	private Object object;

	boolean prototype;

	private Bean() {
	}

	/**
	 * 返回bean的类型
	 * 
	 * @return bean的类型
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * 返回bean的实例
	 * 
	 * @return 如果是原型bean或者实例没有创建则返回null
	 */
	public Object getObject() {
		return object;
	}

	void setObject(Object object) {
		this.object = object;
	}

	/**
	 * 返回bean是否是原型bean，原型bean在每次获取时都创建一个新的实例
	 * 
	 * @return 是否是原型bean
	 */
	public boolean isPrototype() {
		return prototype;
	}

	/**
	 * 设定一个属性值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Bean set(String key, Object value) {
		if (params == null)
			params = Maps.newHashMap();
		params.put(key, value);
		return this;
	}

	/**
	 * 返回指定名字的属性值
	 * 
	 * @param key
	 * @return
	 */
	public Object getParam(String key) {
		return params == null ? null : params.get(key);
	}

	/**
	 * 创建一个原型bean定义
	 * 
	 * @param clazz
	 *            bean类型
	 * @param depends
	 *            依赖列表
	 * @return 创建的原型Bean定义
	 */
	public static Bean prototype(Class<?> clazz) {
		Bean bean = new Bean();
		bean.clazz = clazz;
		bean.prototype = true;
		return bean;
	}

	/**
	 * 创建一个单例bean定义
	 * 
	 * @param clazz
	 * @param depends
	 * @return
	 */
	public static Bean singleton(Class<?> clazz) {
		Bean bean = new Bean();
		bean.clazz = clazz;
		bean.prototype = false;
		return bean;
	}

	/**
	 * 创建一个单例Bean
	 * 
	 * @param object
	 *            对象
	 * @param clazz
	 *            超类
	 * @return
	 */
	public static <T> Bean singleton(T object, Class<T> clazz) {
		Bean bean = new Bean();
		bean.clazz = clazz;
		bean.prototype = false;
		bean.object = object;
		return bean;
	}

	/**
	 * 创建一个单例Bean
	 * 
	 * @param object
	 *            对象
	 * @return
	 */
	public static <T> Bean singleton(T object) {
		Bean bean = new Bean();
		bean.clazz = object.getClass();
		bean.prototype = false;
		bean.object = object;
		return bean;
	}
}
