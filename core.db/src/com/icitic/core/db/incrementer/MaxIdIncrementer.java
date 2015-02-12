package com.icitic.core.db.incrementer;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Strings;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.SqlBuilder;
import com.icitic.core.util.ioc.InitializingBean;
import com.icitic.core.util.ioc.Inject;

public class MaxIdIncrementer extends AbstractIncrementer implements InitializingBean {

	private Dao dao;

	private String tblName;

	private String entityName;

	public MaxIdIncrementer() {
	}
	
	public MaxIdIncrementer(Dao dao, String entityName) {
		this.dao = dao;
		this.entityName = entityName;
	}

	@Inject
	public void setDao(Dao dao) {
		this.dao = dao;
	}

	public void setTblName(String tblName) {
		this.tblName = tblName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		checkNotNull(dao, "Property 'dao' is required");
		checkArgument(!Strings.isNullOrEmpty(tblName) || !Strings.isNullOrEmpty(entityName),
				"either tblName or entityName is required");
	}

	@Override
	protected long getNextKey() {
		if (Strings.isNullOrEmpty(tblName)) {
			Long id = dao.queryForObject(SqlBuilder.select("max(id)").from(entityName), Long.class);
			return id == null ? 1 : id.longValue() + 1;
		} else {
			Long id = dao.queryForObject("SELECT max(ID) FROM " + tblName, Long.class);
			return id == null ? 1 : id.longValue() + 1;
		}
	}
}
