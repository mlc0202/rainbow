package com.icitic.core.db.dao;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.base.Throwables;
import com.icitic.core.db.dao.converter.Converter;
import com.icitic.core.db.jdbc.RowMapper;
import com.icitic.core.db.model.Entity;

public class NeoBeanObjectMapper<T> implements RowMapper<T> {

	private Class<T> clazz;

	private BeanInfo beanInfo;

	private Map<String, Converter> converters;
	
	private NeoBeanMapper mapper;
	
	public void setConverters(Map<String, Converter> converters) {
		this.converters = converters;
	}

	public NeoBeanObjectMapper(Entity entity, Class<T> clazz) {
		this.clazz = clazz;
		try {
			beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		} catch (IntrospectionException e) {
			throw Throwables.propagate(e);
		}
		mapper = new NeoBeanMapper(entity);
	}

	public NeoBeanObjectMapper(Entity entity, Class<T> clazz, Map<String, Converter> converters) {
		this(entity, clazz);
		this.converters = converters;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		NeoBean neo = mapper.mapRow(rs, rowNum);
		T object = null;
		try {
			object = clazz.newInstance();
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		return neo.transfer(object, beanInfo, converters);
	}

}
