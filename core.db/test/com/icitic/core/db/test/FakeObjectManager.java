package com.icitic.core.db.test;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.icitic.core.db.dao.PageData;
import com.icitic.core.db.internal.ObjectNameRule;
import com.icitic.core.db.object.ObjectManager;

public class FakeObjectManager implements ObjectManager {

	@Override
	public String translate(String typeName, Object key) {
		return key.toString();
	}

	@Override
	public <T> List<T> listSetName(List<T> source, List<ObjectNameRule> rules) {
		return source;
	}

	@Override
	public <T> PageData<T> listSetName(PageData<T> source, List<ObjectNameRule> rules) {
		return source;
	}

	@Override
	public <T> T setName(T obj, List<ObjectNameRule> rules) {
		return obj;
	}

	public List<ObjectNameRule> getObjectNameRule(Class<?> clazz) {
		return ImmutableList.of();
	}

}
