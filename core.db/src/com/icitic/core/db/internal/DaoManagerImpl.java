package com.icitic.core.db.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.icitic.core.db.DaoManager;
import com.icitic.core.db.config.Config;
import com.icitic.core.db.config.Logic;
import com.icitic.core.db.config.Physic;
import com.icitic.core.db.config.Property;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.DaoImpl;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.Model;
import com.icitic.core.model.exception.AppException;
import com.icitic.core.service.InjectProvider;
import com.icitic.core.util.Utils;
import com.icitic.core.util.XmlBinder;
import com.icitic.core.util.ioc.ActivatorAwareObject;
import com.icitic.core.util.ioc.DisposableBean;
import com.icitic.core.util.ioc.InitializingBean;

public class DaoManagerImpl extends ActivatorAwareObject implements DaoManager, InjectProvider, InitializingBean,
		DisposableBean {

	private static Logger logger = LoggerFactory.getLogger(DaoManagerImpl.class);

	private Map<String, DruidDataSource> physicMap;

	private Map<String, Dao> logicMap;

	private String defaultLogic;

	private Context ctx;

	@Override
	public boolean provide(Class<?> injectType) {
		return (Dao.class == injectType || DaoManager.class == injectType);
	}

	@Override
	public Object getInjectObject(Class<?> injectType, String name) {
		if (Dao.class == injectType)
			return getDao(name);
		else
			return this;
	}

	public Dao getDao(String name) {
		if (name == null || name.isEmpty())
			name = defaultLogic;
		return logicMap.get(name);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		File file = activator.getConfigureFile("database.xml");
		checkState(file.exists(), "database config file not found");
		Config config = Config.getXmlBinder().unmarshal(file);
		physicMap = readPhysicConfig(config);
		try {
			logicMap = readLogicConfig(config, procRDMs());
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException ex) {
					logger.debug("Could not close JNDI InitialContext", ex);
				}
			}
		}
	}

	/**
	 * 读取物理数据源配置
	 * 
	 * @param config
	 * @return
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private Map<String, DruidDataSource> readPhysicConfig(Config config) throws IntrospectionException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		ImmutableMap.Builder<String, DruidDataSource> builder = ImmutableMap.builder();

		BeanInfo beanInfo = Introspector.getBeanInfo(DruidDataSource.class, Object.class);
		Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
		Set<String> general = ImmutableSet.of("url", "name", "username", "password", "driverClassName", "driver",
				"driverClassLoader");
		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			String name = pd.getName();
			if (general.contains(name))
				continue;
			map.put(pd.getName(), pd);
		}

		for (Physic physic : config.getPhysics()) {
			DruidDataSource dataSource = new DruidDataSource();
			dataSource.setName(physic.getId());
			dataSource.setDriverClassName(physic.getDriverClass());
			dataSource.setUrl(physic.getJdbcUrl());
			dataSource.setUsername(physic.getUsername());
			dataSource.setPassword(physic.getPassword());

			if (!Utils.isNullOrEmpty(physic.getProperty())) {
				for (Property property : physic.getProperty()) {
					PropertyDescriptor pd = map.get(property.getName());
					if (pd == null) {
						logger.warn("unsupported datasource property [{}]", property.getName());
					} else {
						Class<?> ptype = pd.getWriteMethod().getParameterTypes()[0];
						if (ptype == String.class) {
							pd.getWriteMethod().invoke(dataSource, property.getValue());
						} else if (ptype == Integer.TYPE || ptype == Integer.class) {
							int value = Integer.parseInt(property.getValue());
							pd.getWriteMethod().invoke(dataSource, value);
						} else if (ptype == Long.TYPE || ptype == Long.class) {
							long value = Long.parseLong(property.getValue());
							pd.getWriteMethod().invoke(dataSource, value);
						} else if (ptype == Boolean.TYPE || ptype == Boolean.class) {
							boolean value = property.getValue().equalsIgnoreCase("true");
							pd.getWriteMethod().invoke(dataSource, value);
						} else
							throw new AppException("错误的数据库参数配置[%s]", property.toString());
					}
					logger.info("read datasource [{}] property: {}", physic.getId(), property.toString());
				}
			}
			builder.put(physic.getId(), dataSource);
			logger.debug("register physic datasource {}", physic.toString());
		}
		return builder.build();
	}

	/**
	 * 处理所有的rmd文件，转化为一个Map（key是模型名，value是实体翻译的函数）
	 * 
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	private Map<String, Map<String, Entity>> procRDMs() throws JAXBException, IOException {
		List<File> rdmFiles = activator.getConfigureFiles(".rdm");
		if (Utils.isNullOrEmpty(rdmFiles))
			return ImmutableMap.of();
		Map<String, Map<String, Entity>> entityMaps = Maps.newHashMap();
		XmlBinder<Model> binder = Model.getXmlBinder();
		for (File modelFile : rdmFiles) {
			Model model = binder.unmarshal(modelFile);
			String modelName = model.getName();
			Map<String, Entity> map = entityMaps.get(modelName);
			if (map == null) {
				map = Maps.newHashMap();
				entityMaps.put(modelName, map);
			}
			for (Entity entity : model.getEntities()) {
				entity.afterLoad();
				Entity old = map.put(entity.getName(), entity);
				if (old != null)
					logger.warn("Entity {} is duplicated in model file {}", old.getName(), modelFile.getName());
			}
		}
		return entityMaps;
	}

	/**
	 * 读取逻辑数据源配置
	 * 
	 * @param config
	 *            配置对象
	 * @param entityMaps
	 *            数据模型Map
	 * @return
	 */
	private Map<String, Dao> readLogicConfig(Config config, Map<String, Map<String, Entity>> entityMaps) {
		defaultLogic = null;
		ImmutableMap.Builder<String, Dao> logicBuilder = ImmutableMap.builder();
		for (Logic logic : config.getLogics()) {
			if (defaultLogic == null)
				defaultLogic = logic.getId();
			String model = logic.getModel();
			if (model == null)
				model = logic.getId();
			Map<String, Entity> entityMap = entityMaps.get(model);
			DataSource dataSource = getDataSource(logic.getPhysic());
			checkNotNull(dataSource, "physic datasource[%s] of logic source[%s] not defined", logic.getPhysic(),
					logic.getId());
			DaoImpl dao = new DaoImpl(dataSource, entityMap);
			dao.setName(logic.getId());
			dao.setTableSpace(logic.getTableSpace());
			dao.setIndexSpace(logic.getIndexSpace());
			dao.setSchema(logic.getSchema());
			if (Strings.isNullOrEmpty(logic.getPageSize()))
				dao.setPageSize(16);
			else
				dao.setPageSize(Integer.valueOf(logic.getPageSize()));
			logicBuilder.put(logic.getId(), dao);
			if (entityMap == null || entityMap.isEmpty())
				logger.warn("register logic datasource [{}] with no model", logic.toString());
			else
				logger.info("register logic datasource [{}] with model [{}]", logic.toString(), model);
		}
		return logicBuilder.build();
	}

	@Override
	public void destroy() throws Exception {
		if (logicMap != null)
			logicMap = null;
		if (physicMap != null) {
			for (DruidDataSource ds : physicMap.values()) {
				ds.close();
			}
			physicMap = null;
		}
	}

	public Collection<String> getLogicSources() {
		return logicMap.keySet();
	}

	private DataSource getDataSource(String name) {
		DataSource dataSource = physicMap.get(name);
		if (dataSource == null) {
			logger.debug("Looking up JNDI dataSource object with name {}", name);
			try {
				if (ctx == null)
					ctx = new InitialContext();
				dataSource = (DataSource) ctx.lookup(name);
			} catch (Exception e) {
				logger.error("lookup JNDI datasource[{}] object failed", name, e);
			}
		}
		return dataSource;
	}

}
