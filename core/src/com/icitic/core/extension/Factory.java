package com.icitic.core.extension;

import com.google.common.base.Throwables;
import com.icitic.core.model.object.INameObject;

public class Factory implements INameObject {

	protected Class<?> clazz;

	public Factory(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String getName() {
		return clazz.getSimpleName();
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Object createInstance() {
		try {
			return clazz.newInstance();
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
}

