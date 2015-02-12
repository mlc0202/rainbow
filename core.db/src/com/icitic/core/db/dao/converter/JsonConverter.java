package com.icitic.core.db.dao.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.util.Utils;

public class JsonConverter implements Converter {

	private Object type;

	private JsonConverter(Object type) {
		this.type = type;
	}

	@Override
	public Object toNeoBean(Object obj, Object value) {
		return value == null ? null : Utils.toJson(value);
	}

	@Override
	public Object fromNeoBean(NeoBean neo, Object value) {
		if (type instanceof TypeReference<?>)
			return JSON.parseObject((String) value, (TypeReference<?>) type);
		else
			return JSON.parseObject((String) value, (Class<?>) type);
	}

	public static JsonConverter make(Class<?> clazz) {
		return new JsonConverter(clazz);
	}

	public static JsonConverter make(TypeReference<?> type) {
		return new JsonConverter(type);
	}

}
