package com.icitic.core.db.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.icitic.core.db.database.Dialect;
import com.icitic.core.db.database.DialectManager;
import com.icitic.core.db.jdbc.Atom;
import com.icitic.core.db.jdbc.ColumnMapRowMapper;
import com.icitic.core.db.jdbc.DataAccessException;
import com.icitic.core.db.jdbc.IncorrectResultSizeDataAccessException;
import com.icitic.core.db.jdbc.JdbcTemplate;
import com.icitic.core.db.jdbc.JdbcUtils;
import com.icitic.core.db.jdbc.ResultSetExtractor;
import com.icitic.core.db.jdbc.RowCallbackHandler;
import com.icitic.core.db.jdbc.RowMapper;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.Entity;
import com.icitic.core.model.object.NameObject;
import com.icitic.core.model.object.SimpleNameObject;
import com.icitic.core.util.Utils;

public final class DaoImpl extends SimpleNameObject implements Dao {

	private static Logger logger = LoggerFactory.getLogger(DaoImpl.class);

	private Map<String, Entity> entityMap = ImmutableMap.<String, Entity> of();

	private Dialect dialect;

	private JdbcTemplate jdbcTemplate;

	private String tableSpace;

	private String indexSpace;

	private String schema;

	private int pageSize;

	private LoadingCache<Entity, NeoBeanMapper> mapperCache = CacheBuilder.newBuilder().softValues()
			.build(new CacheLoader<Entity, NeoBeanMapper>() {
				@Override
				public NeoBeanMapper load(Entity key) {
					return new NeoBeanMapper(key);
				}
			});

	public DaoImpl() {
		this(null, null);
	}

	public DaoImpl(DataSource dataSource) {
		this(dataSource, null);
	}

	public DaoImpl(DataSource dataSource, Map<String, Entity> entityMap) {
		setDataSource(dataSource);
		setEntityMap(entityMap);
	}

	public void setDataSource(DataSource dataSource) {
		if (dataSource == null) {
			this.jdbcTemplate = null;
			this.dialect = null;
		} else {
			this.jdbcTemplate = new JdbcTemplate(dataSource);
			this.dialect = initDatabaseDialect(dataSource);
		}
	}

	public void setEntityMap(Map<String, Entity> entityMap) {
		this.entityMap = (entityMap == null) ? ImmutableMap.<String, Entity> of() : entityMap;
		mapperCache.invalidateAll();
	}

	public void setTableSpace(String tableSpace) {
		this.tableSpace = tableSpace;
	}

	public void setIndexSpace(String indexSpace) {
		this.indexSpace = indexSpace;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	private Dialect initDatabaseDialect(DataSource dataSource) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			return DialectManager.getDialect(conn.getMetaData());
		} catch (Throwable e) {
			logger.error("failed to init database dialect", e);
			throw Throwables.propagate(e);
		} finally {
			JdbcUtils.closeConnection(conn);
		}
	}

	@Override
	public Dialect getDatabaseDialect() {
		return dialect;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public Entity getEntity(String entityName) {
		checkNotNull(entityName);
		checkArgument(entityMap.containsKey(entityName), "entity [%s] not defined", entityName);
		return entityMap.get(entityName);
	}

	@Override
	public NeoBean newNeoBean(String entityName) {
		Entity entity = getEntity(entityName);
		return new NeoBean(entity);
	}

	@Override
	public NeoBean makeNeoBean(Object obj) {
		return makeNeoBean(obj.getClass().getSimpleName(), obj);
	}

	@Override
	public NeoBean makeNeoBean(String entityName, Object obj) {
		NeoBean neo = newNeoBean(entityName);
		neo.init(obj, null);
		return neo;
	}

	@Override
	public void transaction(int level, Atom atom) {
		jdbcTemplate.getTransactionManager().transaction(level, atom);
	}

	@Override
	public void transaction(Atom atom) {
		transaction(Connection.TRANSACTION_READ_COMMITTED, atom);
	}

	@Override
	public boolean exists(String entityName) {
		Entity entity = getEntity(entityName);
		return existsOfTable(entity.getDbName());
	}

	@Override
	public boolean existsOfTable(String tableName) {
		String sql = String.format("SELECT COUNT(1) FROM %s where 1!=1", tableName);
		try {
			jdbcTemplate.queryForInt(sql);
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean exist(String entityName, Object... keyValues) {
		return queryForInt(SqlBuilder.select("count(1)").from(entityName).where(keyCnd(entityName, keyValues)))>0;
	}

	@Override
	public Condition keyCnd(String entityName, Object... keyValues) {
		Entity entity = getEntity(entityName);
		checkArgument(entity.getKeyCount() > 0, "entity[%s] has no key", entityName);
		checkArgument(entity.getKeyCount() == keyValues.length,
				"param size(%s) not match key size(%s) of entity [%s]", keyValues.length,entity.getKeyCount(),entityName);
		Condition cnd = Condition.make();
		int i = 0;
		for (Column column: entity.getKeys()) {
			cnd = cnd.and(column.getName(), keyValues[i++]);
		}
		return cnd;
	}

	@Override
	public void insert(Object obj) {
		if (obj instanceof NeoBean)
			insert((NeoBean) obj);
		else
			insert(makeNeoBean(obj));
	}

	private void insert(NeoBean neo) {
		Entity entity = neo.getEntity();
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(entity.getDbName()).append("(");
		List<Object> params = new ArrayList<Object>(entity.getColumns().size());
		for (Column column : entity.getColumns()) {
			Object v = neo.getObject(column);
			if (v == null) {
				checkArgument(!column.isMandatory(), "property %s cannot be null", column.getName());
			} else {
				if (!params.isEmpty())
					sb.append(",");
				sb.append(column.getDbName());
				params.add(column.toDb(v));
			}
		}
		checkArgument(!params.isEmpty(), "all property value is null");
		sb.append(") values (?");
		for (int i = 1; i < params.size(); i++)
			sb.append(",?");
		sb.append(")");
		jdbcTemplate.update(sb.toString(), params.toArray());
	}

	@Override
	public void clear(String entityName) {
		SqlBuilder sb = SqlBuilder.delete().from(entityName);
		execSql(sb);
	}

	@Override
	public void delete(String entityName, Object... keyValues) {
		execSql(SqlBuilder.delete().from(entityName).where(keyCnd(entityName, keyValues)));
	}

	@Override
	public int update(Object obj) {
		if (obj instanceof NeoBean)
			return update((NeoBean) obj);
		else
			return update(makeNeoBean(obj));
	}

	@Override
	public int update(String entityName, Object obj) {
		return update(makeNeoBean(entityName, obj));
	}

	private int update(NeoBean neo) {
		Entity entity = neo.getEntity();
		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(entity.getDbName()).append(" set ");
		boolean first = true;
		List<Object> params = new ArrayList<Object>(entity.getColumns().size());
		for (Column column : entity.getColumns()) {
			if (!column.isKey()) {
				if (first)
					first = false;
				else
					sb.append(",");
				sb.append(column.getDbName());
				Object v = neo.getObject(column);
				if (v == null)
					sb.append("=null");
				else {
					sb.append("=?");
					params.add(column.toDb(v));
				}
			}
		}
		neo.addWhereKey(sb, params);
		return jdbcTemplate.update(sb.toString(), params.toArray());
	}

	@Override
	public NeoBean fetch(String entityName, Object... keyValues) {
		Entity entity = getEntity(entityName);
		SqlBuilder sb = SqlBuilder.select().from(entity).where(keyCnd(entityName, keyValues));
		return queryForObject(sb, mapperCache.getUnchecked(entity));
	}

	@Override
	public <T> T fetch(String entityName, Class<T> clazz, Object... keyValues) {
		Entity entity = getEntity(entityName);
		SqlBuilder sb = SqlBuilder.select().from(entity).where(keyCnd(entityName, keyValues));
		return queryForObject(sb, new NeoBeanObjectMapper<T>(entity, clazz));
	}

	@Override
	public <T> T fetch(Class<T> clazz, Object... keyValues) {
		return fetch(clazz.getSimpleName(), clazz, keyValues);
	}

	@Override
	public NeoBean fetch(SqlBuilder sb) {
		Entity entity = getEntity(sb.getEntityName());
		return queryForObject(sb, mapperCache.getUnchecked(entity));
	}

	@Override
	public <T> T fetch(SqlBuilder sb, Class<T> clazz) {
		Entity entity = getEntity(sb.getEntityName());
		return queryForObject(sb, new NeoBeanObjectMapper<T>(entity, clazz));
	}

	@Override
	public List<NeoBean> query(SqlBuilder sb) {
		return query(sb, (Pager) null);
	}

	@Override
	public List<NeoBean> query(SqlBuilder sb, Pager pager) {
		Entity entity = getEntity(sb.getEntityName());
		Sql sql = sb.build(this);
		return queryForList(sql.getSql(), mapperCache.getUnchecked(entity), pager, sql.getParamArray());
	}

	@Override
	public <T> List<T> query(SqlBuilder sb, Class<T> clazz) {
		return query(sb, clazz, null);
	}

	@Override
	public <T> List<T> query(SqlBuilder sb, Class<T> clazz, Pager pager) {
		Entity entity = getEntity(sb.getEntityName());
		return queryForList(sb, new NeoBeanObjectMapper<T>(entity, clazz), pager);
	}

	@Override
	public <T> PageData<T> pageQuery(SqlBuilder sb, Class<T> clazz, Pager pager) {
		Entity entity = getEntity(sb.getEntityName());
		return pageQuery(sb, new NeoBeanObjectMapper<T>(entity, clazz), pager);
	}

	@Override
	public <T> T queryForObject(SqlBuilder sb, Class<T> requiredType) {
		Sql sql = sb.build(this);
		return queryForObject(sql.getSql(), requiredType, sql.getParamArray());
	}

	@Override
	public <T> T queryForObject(SqlBuilder sb, RowMapper<T> mapper) {
		Sql sql = sb.build(this);
		return queryForObject(sql.getSql(), mapper, sql.getParamArray());
	}

	@Override
	public int queryForInt(SqlBuilder sb) {
		Integer result = queryForObject(sb, Integer.class);
		return result == null ? 0 : result.intValue();
	}

	@Override
	public <T> List<T> queryForList(SqlBuilder sb, Class<T> requiredType) {
		return queryForList(sb, requiredType, null);
	}

	@Override
	public <T> List<T> queryForList(SqlBuilder sb, Class<T> requiredType, Pager pager) {
		Sql sql = sb.build(this);
		return queryForList(sql.getSql(), requiredType, pager, sql.getParamArray());
	}

	@Override
	public <T> List<T> queryForList(SqlBuilder sb, RowMapper<T> mapper) {
		return queryForList(sb, mapper, null);
	}

	@Override
	public <T> List<T> queryForList(SqlBuilder sb, RowMapper<T> mapper, Pager pager) {
		Sql sql = sb.build(this);
		return queryForList(sql.getSql(), mapper, pager, sql.getParamArray());
	}

	@Override
	public <T> PageData<T> pageQuery(SqlBuilder sb, RowMapper<T> mapper, Pager pager) {
		int count = count(sb.getEntityName(), sb.getCondition());
		if (count == 0)
			return new PageData<T>();
		else {
			List<T> list = queryForList(sb, mapper, pager);
			return new PageData<T>(count, list);
		}
	}

	@Override
	public int count(String entityName, Condition cnd) {
		if (cnd != null)
			return queryForInt(SqlBuilder.select("count(1)").from(entityName).where(cnd));
		else
			return queryForInt(SqlBuilder.select("count(1)").from(entityName));
	}

	@Override
	public int count(String entityName) {
		return count(entityName, null);
	}

	@Override
	public void doQuery(SqlBuilder sb, RowCallbackHandler callback) {
		Sql sql = sb.build(this);
		doQuery(sql.getSql(), callback, sql.getParamArray());
	}

	@Override
	public <T> T doQuery(SqlBuilder sb, ResultSetExtractor<T> rse) {
		Sql sql = sb.build(this);
		return doQuery(sql.getSql(), rse, sql.getParamArray());
	}

	@Override
	public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
		try {
			if (args.length == 0)
				return jdbcTemplate.queryForObject(sql, requiredType);
			else
				return jdbcTemplate.queryForObject(sql, args, requiredType);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
		try {
			if (args.length == 0)
				return jdbcTemplate.queryForObject(sql, mapper);
			else
				return jdbcTemplate.queryForObject(sql, args, mapper);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	public Map<String, Object> queryForMap(String sql, Object... args) {
		return queryForObject(sql, ColumnMapRowMapper.instance, args);
	}

	@Override
	public int queryForInt(String sql, Object... args) {
		Integer result = queryForObject(sql, Integer.class, args);
		return result == null ? 0 : result.intValue();
	}

	@Override
	public <T> List<T> queryForList(String sql, Class<T> requiredType, Object... args) {
		return queryForList(sql, requiredType, null, args);
	}

	@Override
	public <T> List<T> queryForList(String sql, Class<T> requiredType, Pager pager, Object... args) {
		if (pager != null) {
			String select = selectPart(sql);
			sql = getDatabaseDialect().wrapPagedSql(sql, select, pager);
		}
		if (args.length == 0)
			return jdbcTemplate.queryForList(sql, requiredType);
		else
			return jdbcTemplate.queryForList(sql, args, requiredType);
	}

	private static String selectPart(String sql) {
		Iterator<String> i = Splitter.on(' ').omitEmptyStrings().split(sql).iterator();
		checkArgument("SELECT".equalsIgnoreCase(i.next()));
		List<String> result = new ArrayList<String>();
		String str = i.next();
		if (!"DISTINCT".equalsIgnoreCase(str))
			result.add(str);
		while (i.hasNext()) {
			str = i.next();
			if ("FROM".equalsIgnoreCase(str))
				return Joiner.on(' ').join(result);
			else
				result.add(str);
		}
		throw new IllegalArgumentException("wrong sql:" + sql);
	}

	@Override
	public <T> List<T> queryForList(String sql, RowMapper<T> mapper, Pager pager, Object... args) {
		if (pager != null)
			sql = getDatabaseDialect().wrapPagedSql(sql, pager);
		if (args.length == 0)
			return jdbcTemplate.query(sql, mapper);
		else
			return jdbcTemplate.query(sql, args, mapper);
	}

	@Override
	public List<Map<String, Object>> queryForMapList(String sql, Object... args) {
		return queryForList(sql, ColumnMapRowMapper.instance, null, args);
	}

	@Override
	public void doQuery(String sql, RowCallbackHandler callback, Object... args) {
		if (args.length == 0)
			jdbcTemplate.query(sql, callback);
		else
			jdbcTemplate.query(sql, args, callback);
	}

	@Override
	public <T> T doQuery(String sql, ResultSetExtractor<T> rse, Object... args) {
		if (args.length == 0)
			return jdbcTemplate.query(sql, rse);
		else
			return jdbcTemplate.query(sql, args, rse);
	}

	@Override
	public int execSql(SqlBuilder sb) {
		Sql sql = sb.build(this);
		return execSql(sql.getSql(), sql.getParamArray());
	}

	@Override
	public int execSql(Sql sql) {
		return execSql(sql.getSql(), sql.getParamArray());
	}

	@Override
	public int execSql(String sql, Object... args) {
		if (args.length == 0)
			return jdbcTemplate.update(sql);
		else
			return jdbcTemplate.update(sql, args);
	}

	@Override
	public void createTableAs(String tableName, Sql sql, String table_space, String index_space, boolean distribute,
			String distributeKey) {
		if (existsOfTable(tableName))
			execSql(String.format("drop table %s", tableName));
		getDatabaseDialect().createTableAs(tableName, sql, table_space, index_space, distribute, distributeKey, this);
	}

	@Override
	public void createTable(String tableName, List<Column> columns, String table_space, String index_space,
			boolean distribute, String distributeKey) {
		if (existsOfTable(tableName))
			execSql(String.format("drop table %s", tableName));
		StringBuilder sb = getDatabaseDialect().createTableSql(tableName, columns, table_space, index_space,
				distribute, distributeKey, this);
		execSql(sb.toString());
	}

	@Override
	public void createTableAs(String tableName, Sql sql, boolean distribute, String distributeKey) {
		createTableAs(tableName, sql, tableSpace, indexSpace, distribute, distributeKey);
	}

	@Override
	public void createTable(String tableName, List<Column> columns, boolean distribute, String distributeKey) {
		createTable(tableName, columns, tableSpace, indexSpace, distribute, distributeKey);
	}

	@Override
	public void function(StringBuilder sb, String funcName, Object... params) {
		getDatabaseDialect().function(sb, funcName, params);
	}

	@Override
	public void createView(String viewName, String sql) {
		if (existsOfTable(viewName))
			execSql(String.format("drop view %s", viewName));
		StringBuilder sb = new StringBuilder("create view ");
		sb.append(viewName).append(" as (").append(sql).append(')');
		execSql(sb.toString());
	}

	@Override
	public long calcTableSize(String table) {
		String sql = getDatabaseDialect().getCalcTableSizeSql(schema, table, pageSize);
		if (Utils.hasContent(sql)) {
			Long result = queryForObject(sql, Long.class);
			return result == null ? 0L : result;
		}
		return 10L;
	}

	@Override
	public List<NameObject<String>> getTableList(final Predicate<String> nameFilter) {
		String sql = getDatabaseDialect().getTableListSql();
		final ImmutableList.Builder<NameObject<String>> builder = ImmutableList.builder();
		doQuery(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String id = rs.getString(1);
				if (nameFilter == null || !nameFilter.apply(id)) {
					String name = rs.getString(2);
					builder.add(new NameObject<String>(id, name));
				}
			}
		});
		return builder.build();
	}

	@Override
	public List<Column> getColumn(String table) {
		return getDatabaseDialect().getColumn(table, this);
	}
}
