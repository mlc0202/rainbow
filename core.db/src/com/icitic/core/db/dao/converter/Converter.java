package com.icitic.core.db.dao.converter;

import com.icitic.core.db.dao.NeoBean;

/**
 * 当属性与数据库设计不匹配的时候，需要转换
 * 
 * @author lijinghui
 * 
 */
public interface Converter {

	/**
	 * 从对象转换到数据库
	 * 
	 * @param obj
	 *            对象
	 * @param property
	 *            属性
	 * @param value
	 *            对象属性值
	 * @return
	 */
	Object toNeoBean(Object obj, Object value);

	/**
	 * 从数据库转换到对象
	 * 
	 * @param neo
	 *            neobean
	 * @param property
	 *            属性
	 * @param value
	 *            neo属性值
	 * @return
	 */
	Object fromNeoBean(NeoBean neo, Object value);

}
