package com.icitic.core.db.internal;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.icitic.core.db.dao.PageData;
import com.icitic.core.db.object.Name;
import com.icitic.core.db.object.ObjectManager;
import com.icitic.core.db.object.ObjectType;
import com.icitic.core.extension.ExtensionRegistry;
import com.icitic.core.service.InjectProvider;
import com.icitic.core.util.Utils;

public class ObjectManagerImpl implements ObjectManager, InjectProvider {

	private static final Logger logger = LoggerFactory.getLogger(ObjectManagerImpl.class);

	@Override
	public String translate(String typeName, Object key) {
		ObjectType objectType = ExtensionRegistry.safeGetExtensionObject(ObjectType.class, typeName);
		if (objectType == null) {
			logger.warn("objectType [{}] not available", typeName);
			return key.toString();
		} else
			return objectType.getObjectName(key);
	}

	@Override
	public <T> List<T> listSetName(List<T> source, List<ObjectNameRule> rules) {
		if (!Utils.isNullOrEmpty(source)) {
			for (ObjectNameRule rule : rules) {
				ObjectType objectType = ExtensionRegistry.safeGetExtensionObject(ObjectType.class, rule.getObjType());
				if (objectType == null)
					logger.warn("objectType [{}] not available", rule.getObjType());
				else {
					for (Object obj : source) {
						try {
							Object key = rule.getGetKeyMethod().invoke(obj);
							String name = objectType.hasSubType() ? objectType.getObjectName(rule.getSubType(), key)
									: objectType.getObjectName(key);
							rule.getSetNameMethod().invoke(obj, name);
						} catch (Exception e) {
						}
					}
				}
			}
		}
		return source;
	}

	@Override
	@Deprecated
	public <T> PageData<T> listSetName(PageData<T> source, List<ObjectNameRule> rules) {
		listSetName(source.getData(), rules);
		return source;
	}

	@Override
	public <T> T setName(T obj, List<ObjectNameRule> rules) {
		for (ObjectNameRule rule : rules) {
			ObjectType objectType = ExtensionRegistry.safeGetExtensionObject(ObjectType.class, rule.getObjType());
			if (objectType == null)
				logger.warn("objectType [{}] not available", rule.getObjType());
			else {
				try {
					Object key = rule.getGetKeyMethod().invoke(obj);
					String name = objectType.hasSubType() ? objectType.getObjectName(rule.getSubType(), key)
							: objectType.getObjectName(key);
					rule.getSetNameMethod().invoke(obj, name);
				} catch (Exception e) {
				}
			}
		}
		return obj;
	}

	public List<ObjectNameRule> getObjectNameRule(Class<?> clazz) {
		ImmutableList.Builder<ObjectNameRule> builder = ImmutableList.builder();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("set") && method.getParameterTypes().length == 1
					&& method.getParameterTypes()[0] == String.class) {
				Name name = method.getAnnotation(Name.class);
				if (name != null) {
					ObjectNameRule rule = new ObjectNameRule();
					rule.setSetNameMethod(method);
					rule.setObjType(name.type());
					rule.setSubType(name.subType());

					String m = name.src();
					if (m.isEmpty()) {
						m = "get" + Utils.substringBetween(methodName, "set", "Name");
					} else {
						m = "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, m);
					}
					try {
						rule.setGetKeyMethod(clazz.getMethod(m));
					} catch (Exception e) {
						logger.error("invalide @Name definition of class [{}] @ method [{}]", clazz.getName(),
								methodName, e);
						Throwables.propagate(e);
					}
					builder.add(rule);
				}
			}
		}
		return builder.build();
	}

	@Override
	public boolean provide(Class<?> injectType) {
		return ObjectManager.class == injectType;
	}

	@Override
	public Object getInjectObject(Class<?> injectType, String name) {
		return this;
	}

}
