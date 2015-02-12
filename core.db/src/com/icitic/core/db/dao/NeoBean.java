package com.icitic.core.db.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.icitic.core.db.dao.converter.Converter;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;
import com.icitic.core.util.Utils;

public class NeoBean {

	private final static Logger logger = LoggerFactory.getLogger(NeoBean.class);

	private Entity entity;

	private Map<Column, Object> valueMap = Maps.newHashMap();

	public Entity getEntity() {
		return entity;
	}

	public NeoBean(Entity entity) {
		checkNotNull(entity);
		this.entity = entity;
	}

	public NeoBean(Entity entity, Object object) {
		this(entity);
		init(object, null);
	}

	/**
	 * 从一个map初始化
	 * 
	 * @param map
	 */
	public void init(Map<String, ?> map) {
		for (Entry<String, ?> entry : map.entrySet()) {
			String property = entry.getKey();
			Column column = entity.getColumn(property);
			if (column != null) {
				setObject(column, entry.getValue());
			}
		}
	}

	public void init(Object obj, Map<String, Converter> converters) {
		if (obj == null) {
			valueMap.clear();
			return;
		}
		BeanInfo info = getBeanInfo(obj);
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			Column column = entity.getColumn(pd.getName());
			if (column != null) {
				Object value = null;
				try {
					value = pd.getReadMethod().invoke(obj);
				} catch (Exception e) {
				}
				if (value != null) {
					Converter converter = Utils.safeGet(converters, pd.getName());
					if (converter != null)
						value = converter.toNeoBean(obj, value);
					setObject(column, value);
				}
			}
		}
	}

	/**
	 * 从一个属性获取指定类型的值
	 * 
	 * @param column
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private <T> T getObject(Column column, Class<T> clazz) {
		if (column.getJavaType().getJavaClass().isAssignableFrom(clazz))
			return (T) valueMap.get(column);
		throw new IllegalArgumentException(String.format("column %s(%s) is not %s", column.getName(),
				column.getJavaType(), clazz));
	}

	private <T> T getObject(String property, Class<T> clazz) {
		Column column = entity.getColumn(property);
		checkNotNull(column);
		return getObject(column, clazz);
	}

	Object getObject(Column column) {
		return valueMap.get(column);
	}

	public Object getObject(String property) {
		Column column = entity.getColumn(property);
		checkNotNull(column);
		return valueMap.get(column);
	}

	public Integer getInt(String property) {
		return getObject(property, Integer.class);
	}

	public Boolean getBoolean(String property) {
		return getObject(property, Boolean.class);
	}

	public <E extends Enum<E>> E getEnum(String property, Class<E> enumType) {
		Column column = entity.getColumn(property);
		checkNotNull(column);
		return getEnum(column, enumType);
	}

	private <E extends Enum<E>> E getEnum(Column column, Class<E> enumType) {
		Object value = getObject(column);
		try {
			return Utils.toEnum(enumType, value);
		} catch (Throwable e) {
			throw new IllegalArgumentException(String.format("column [%s] is not an enum", column.getName()));
		}
	}

	public Long getLong(String property) {
		return getObject(property, Long.class);
	}

	public Double getDouble(String property) {
		return getObject(property, Double.class);
	}

	public BigDecimal getBigDecimal(String property) {
		return getObject(property, BigDecimal.class);
	}

	public String getString(String property) {
		return getObject(property, String.class);
	}

	public Date getDate(String property) {
		return getObject(property, Date.class);
	}

	public byte[] getByteArray(String property) {
		return getObject(property, byte[].class);
	}

	/**
	 * 向一个属性设置值，这个函数仅供内部使用，如果类型不匹配，会抛出IllegalArgumentException
	 * 
	 * @param column
	 * @param value
	 */
	@SuppressWarnings("rawtypes")
	void setObject(Column column, Object value) {
		if (value == null) {
			valueMap.remove(column);
			return;
		}
		if (value instanceof Number) {
			Number number = (Number) value;
			switch (column.getJavaType()) {
			case INT:
				valueMap.put(column, number.intValue());
				return;
			case LONG:
				valueMap.put(column, number.longValue());
				return;
			case DOUBLE:
				valueMap.put(column, number.doubleValue());
				return;
			case BIGDECIMAL:
				if (value instanceof BigDecimal)
					valueMap.put(column, value);
				else
					valueMap.put(column, new BigDecimal(number.toString()));
				return;
			case BOOL:
				valueMap.put(column, number.intValue() == 1);
				return;
			case DATE:
				checkArgument(value.getClass() == Long.class);
				valueMap.put(column, new Date(number.longValue()));
				return;
			default:
				throw new IllegalArgumentException(String.format("column [%s] can not save a number", column.getName()));
			}
		}
		if (value instanceof Enum) {
			switch (column.getJavaType()) {
			case INT:
				valueMap.put(column, ((Enum) value).ordinal());
				return;
			case STRING:
				valueMap.put(column, ((Enum) value).name());
				return;
			default:
				throw new IllegalArgumentException(String.format("column [%s] can not save an enum", column.getName()));
			}
		}

		if (column.getJavaType().getJavaClass().isAssignableFrom(value.getClass()))
			valueMap.put(column, value);
		else if (column.getType() == ColumnType.BLOB && value instanceof InputStream)
			valueMap.put(column, value);
		else
			throw new IllegalArgumentException(String.format("invalid colume [%s] value [%s]", column.getName(), value));
	}

	public void setObject(String property, Object value) {
		Column column = entity.getColumn(property);
		checkArgument(column != null, "property [%s] not defined", property);
		setObject(column, value);
	}

	public NeoBean setInt(String property, Integer value) {
		setObject(property, value);
		return this;
	}

	public NeoBean setBoolean(String property, Boolean value) {
		setObject(property, value);
		return this;
	}

	public <E extends Enum<E>> NeoBean setEnum(String property, E value) {
		setObject(property, value);
		return this;
	}

	public NeoBean setLong(String property, Number value) {
		setObject(property, value.longValue());
		return this;
	}

	public NeoBean setDouble(String property, Number value) {
		setObject(property, value.doubleValue());
		return this;
	}

	public NeoBean setBigDecimal(String property, BigDecimal value) {
		setObject(property, value);
		return this;
	}

	public NeoBean setBigDecimal(String property, Number value) {
		setObject(property, value);
		return this;
	}

	public NeoBean setString(String property, String value) {
		setObject(property, value);
		return this;
	}

	public NeoBean setDate(String property, Date value) {
		setObject(property, value);
		return this;
	}

	public NeoBean setDate(String property, long value) {
		setDate(property, new Date(value));
		return this;
	}

	public NeoBean setByteArray(String property, byte[] value) {
		setObject(property, value);
		return this;
	}

	/**
	 * 具有Blob字段对象，如果存放的是比较大的数据，不适合用byteArray时，才用这个函数。
	 * 因为InputStream是单向读的，因此这个函数只是为了在insert的时候准备参数用的。
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public NeoBean setBlob(String property, InputStream value) {
		setObject(property, value);
		return this;
	}

	public void addWhereKey(StringBuilder sb, List<Object> params) {
		checkArgument(entity.getKeyCount() > 0, "entity[%s] has no key defined", entity.getName());
		sb.append(" where ");
		boolean first = true;
		for (Column column : entity.getKeys()) {
			if (first)
				first = false;
			else
				sb.append(" and ");
			sb.append(column.getDbName()).append("=?");
			params.add(column.toDb(getObject(column)));
		}
	}

	/**
	 * 变身为某一个类实例
	 * 
	 * @param clazz
	 * @param strict
	 *            对于能匹配名称的属性传递失败的处理，true则抛异常， false则忽略
	 * @return
	 */
	public <T> T bianShen(Class<T> clazz, Map<String, Converter> converters) {
		T target = null;
		try {
			target = clazz.newInstance();
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		return transfer(target, getBeanInfo(target), converters);
	}

	/**
	 * NeoBean向一个对象传递匹配的属性值
	 * 
	 * @param object
	 *            传递的对象
	 * @param strict
	 *            对于能匹配名称的属性传递失败的处理，true则抛异常， false则忽略
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T transfer(T object, BeanInfo beanInfo, Map<String, Converter> converters) {
		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			Column column = entity.getColumn(pd.getName());
			if (column != null) {
				try {
					Object value = getObject(column);
					if (value != null) {
						Converter converter = Utils.safeGet(converters, pd.getName());
						if (converter != null)
							value = converter.fromNeoBean(this, value);
						else {
							if (value != null && Enum.class.isAssignableFrom(pd.getPropertyType()))
								value = Utils.toEnum((Class<Enum>) pd.getPropertyType(), value);
						}
					}
					pd.getWriteMethod().invoke(object, value);
				} catch (Throwable e) {
					logger.error("transfer column[{}] data error", column.getName(), e);
					Throwables.propagate(e);
				}
			}
		}
		return object;
	}

	private BeanInfo getBeanInfo(Object obj) {
		checkNotNull(obj);
		try {
			return Introspector.getBeanInfo(obj.getClass(), Object.class);
		} catch (IntrospectionException e) {
			throw Throwables.propagate(e);
		}
	}

}
