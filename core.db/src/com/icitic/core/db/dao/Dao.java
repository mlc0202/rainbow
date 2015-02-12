package com.icitic.core.db.dao;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.icitic.core.db.database.Dialect;
import com.icitic.core.db.jdbc.Atom;
import com.icitic.core.db.jdbc.JdbcTemplate;
import com.icitic.core.db.jdbc.ResultSetExtractor;
import com.icitic.core.db.jdbc.RowCallbackHandler;
import com.icitic.core.db.jdbc.RowMapper;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.Entity;
import com.icitic.core.model.object.INameObject;
import com.icitic.core.model.object.NameObject;

/**
 * 类说明 ：DB访问器接口，封装了对一个DataSource的所有访问。一个DataSource只能有一个Dao实例。
 * 
 * @author lijinghui
 * 
 */
public interface Dao extends INameObject {

	/**
	 * 返回屏蔽数据库差异的数据库方言对象
	 * 
	 * @return
	 */
	Dialect getDatabaseDialect();

	/**
	 * 返回更底层的数据库访问对象
	 * 
	 * @return
	 */
	JdbcTemplate getJdbcTemplate();

	/**
	 * 返回一个字符串代表的实体对象
	 * 
	 * @param entityName
	 *            实体名
	 * @return 实体对象
	 */
	Entity getEntity(String entityName);

	/**
	 * 根据一个实体名创建一个NeoBean
	 * 
	 * @param entityName
	 *            实体名
	 * @return 新创建的NeoBean
	 */
	NeoBean newNeoBean(String entityName);

	/**
	 * 根据一个对象创建一个NeoBean并传输属性到NeoBean中，对象类名为数据模型的实体名
	 * 
	 * @param obj
	 * @return
	 */
	NeoBean makeNeoBean(Object obj);

	/**
	 * 根据一个实体名创建一个NeoBean，并设置属性值
	 * 
	 * @param entityName
	 *            实体对象名
	 * @param obj
	 *            传值对象，该对象属性名若与实体属性名一致，则copy该属性值到新创建的NeoBean中
	 * @return 新创建的NeoBean
	 */
	NeoBean makeNeoBean(String entityName, Object obj);

	/**
	 * 包裹一个事务
	 * 
	 * @param level
	 *            one of the following <code>Connection</code> constants:
	 *            <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>,
	 *            <code>Connection.TRANSACTION_READ_COMMITTED</code>,
	 *            <code>Connection.TRANSACTION_REPEATABLE_READ</code>, or
	 *            <code>Connection.TRANSACTION_SERIALIZABLE</code>. (Note that
	 *            <code>Connection.TRANSACTION_NONE</code> cannot be used
	 *            because it specifies that transactions are not supported.)
	 * @param atom
	 */
	public void transaction(int level, Atom atom);

	/**
	 * 包裹一个事务
	 * 
	 * @param level
	 * @param atom
	 */
	public void transaction(Atom atom);

	/**
	 * 检查指定实体对象是否在数据库中存在数据表
	 * 
	 * @param entityName
	 *            数据实体名
	 * @return
	 */
	boolean exists(String entityName);

	/** 检查数据库中是否存在指定数据表 */
	boolean existsOfTable(String tableName);

	/** 判断数据库中是否有某条记录 */
	boolean exist(String entityName, Object... keyValues);

	/**
	 * 返回指定实体的主键条件
	 * @param entity
	 * @param keyValues
	 * @return
	 */
	Condition keyCnd(String entityName, Object... keyValues);

	/**
	 * 插入一个对象。如果该对象不是NeoBean，则其类名为数据模型的实体名
	 * 
	 * @param obj
	 */
	void insert(Object obj);

	/**
	 * 清空一个实体在数据库中的数据
	 * 
	 * @param entityName
	 */
	void clear(String entityName);

	/**
	 * 根据主键，删除一个对象
	 * 
	 * @param entityName
	 *            实体名称
	 * @param keyValues
	 *            主键值
	 */
	public void delete(String entityName, Object... keyValues);

	/**
	 * 更新一个对象，如果该对象不是NeoBean，则其类名为数据模型的实体名
	 * 
	 * @param obj
	 * @return
	 */
	int update(Object obj);

	/**
	 * 更新一个对象
	 * 
	 * @param entityName
	 * @param obj
	 * @return
	 */
	int update(String entityName, Object obj);

	/**
	 * 根据主键，获得一个NeoBean对象
	 * 
	 * @param entityName
	 *            实体名称
	 * @param keys
	 *            主键值
	 * @return
	 */
	NeoBean fetch(String entityName, Object... keyValues);

	/**
	 * 根据主键，获得一个实体对象，并转化为一个类的实例
	 * 
	 * @param entityName
	 *            实体名称
	 * @param clazz
	 *            转化类型
	 * @param keys
	 *            主键值
	 * @return
	 */
	<T> T fetch(String entityName, Class<T> clazz, Object... keys);

	/**
	 * 根据主键，获得一个类实例对象，该对象的类名与数据模型的实体名一致
	 * 
	 * @param entityName
	 *            实体名称
	 * @param clazz
	 *            转化类型
	 * @param keys
	 *            主键值
	 * @return
	 */
	<T> T fetch(Class<T> clazz, Object... keys);

	/**
	 * 查询一个实体的NeoBean实例
	 */
	NeoBean fetch(SqlBuilder sb);

	/**
	 * 查询一个实体的实例，并转化为一个类的对象实例
	 */
	<T> T fetch(SqlBuilder sb, Class<T> clazz);

	/**
	 * 查询实体的NeoBean实例列表
	 */
	List<NeoBean> query(SqlBuilder sb);

	/**
	 * 分页查询实体的NeoBean实例列表
	 */
	List<NeoBean> query(SqlBuilder sb, Pager pager);

	/**
	 * 查询实体对象列表并转化为指定类型
	 */
	<T> List<T> query(SqlBuilder sb, Class<T> clazz);

	/**
	 * 分页查询实体对象列表并转化为指定类型
	 */
	<T> List<T> query(SqlBuilder sb, Class<T> clazz, Pager pager);

	/** 根据条件求一个实体分页对象数据 */
	<T> PageData<T> pageQuery(SqlBuilder sb, Class<T> clazz, Pager pager);

	/** 查询一个值 */
	<T> T queryForObject(SqlBuilder sb, Class<T> requiredType);

	/** 查询一个对象 */
	<T> T queryForObject(SqlBuilder sb, RowMapper<T> mapper);

	/** 根据条件求一个字段的值的列表 */
	<T> List<T> queryForList(SqlBuilder sb, Class<T> requiredType);

	/** 根据条件求一个字段的值的分页列表 */
	<T> List<T> queryForList(SqlBuilder sb, Class<T> requiredType, Pager pager);

	/** 查询一个对象列表 */
	<T> List<T> queryForList(SqlBuilder sb, RowMapper<T> mapper);

	/** 根据条件求一个实体分页对象列表 */
	<T> List<T> queryForList(SqlBuilder sb, RowMapper<T> mapper, Pager pager);

	/** 根据条件求一个实体分页对象数据 */
	<T> PageData<T> pageQuery(SqlBuilder sb, RowMapper<T> mapper, Pager pager);

	/** 求一个整数值 */
	int queryForInt(SqlBuilder sb);

	/** 根据条件，计算某个对象在数据库中有多少条记录 */
	int count(String entityName, Condition cnd);

	/** 计算某个对象在数据库中有多少条记录 */
	int count(String entityName);

	/** 做一个查询，具体每行的查询结果的处理由callback来做 */
	void doQuery(SqlBuilder sb, RowCallbackHandler callback);

	/** 做一个查询，具体结果集的处理由callback来做 */
	<T> T doQuery(SqlBuilder sb, ResultSetExtractor<T> rse);

	/** 以下查询返回一个值 *********************************************************/

	/** 查询一个值 */
	<T> T queryForObject(String sql, Class<T> requiredType, Object... args);

	/** 查询一个对象 */
	<T> T queryForObject(String sql, RowMapper<T> mapper, Object... args);

	/** 查询一条数据，返回一个Map对象 */
	Map<String, Object> queryForMap(String sql, Object... args);

	int queryForInt(String sql, Object... args);

	/** 以下查询返回一组值 *********************************************************/

	/** 根据条件求一个字段的值的列表 */
	<T> List<T> queryForList(String sql, Class<T> requiredType, Object... args);

	<T> List<T> queryForList(String sql, Class<T> requiredType, Pager pager, Object... args);

	/** 根据条件求一个对象分页列表 */
	<T> List<T> queryForList(String sql, RowMapper<T> mapper, Pager pager, Object... args);

	/** 返回结果为Map列表的分页查询 */
	List<Map<String, Object>> queryForMapList(String sql, Object... args);

	/** 做一个查询，具体每行的查询结果的处理由callback来做 */
	void doQuery(String sql, RowCallbackHandler callback, Object... args);

	/** 做一个查询，具体结果集的处理由ResultSetExtractor来做 */
	<T> T doQuery(String sql, ResultSetExtractor<T> rse, Object... args);

	/**
	 * 直接调用执行SQL的语句
	 * 
	 * @param sb
	 * @return
	 */
	int execSql(SqlBuilder sb);

	/**
	 * 直接调用执行SQL的语句
	 * 
	 * @param sql
	 * @return
	 */
	int execSql(Sql sql);

	/**
	 * 直接调用执行SQL的语句
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	int execSql(String sql, Object... args);

	/**
	 * 在指定的表空间上从sql创建表
	 * 
	 * @param tableName
	 * @param sql
	 * @param table_space
	 * @param index_space
	 */
	void createTableAs(String tableName, Sql sql, String table_space, String index_space, boolean distribute,
			String distributeKey);

	/**
	 * 在制定的表空间上创建一个表
	 * 
	 * @param tableName
	 * @param columns
	 * @param table_space
	 * @param index_space
	 */
	void createTable(String tableName, List<Column> columns, String table_space, String index_space,
			boolean distribute, String distributeKey);

	/**
	 * 在dao配置的表空间上从sql创建表
	 * 
	 * @param tableName
	 * @param sql
	 */
	void createTableAs(String tableName, Sql sql, boolean distribute, String distributeKey);

	/**
	 * 在dao配置的表空间上创建一个表
	 * 
	 * @param tableName
	 * @param columns
	 */
	void createTable(String tableName, List<Column> columns, boolean distribute, String distributeKey);

	/**
	 * 创建一个视图
	 * 
	 * @param viewName
	 * @param sql
	 */
	void createView(String viewName, String sql);

	/**
	 * 把一个函数翻译为sql放到StringBuilder中
	 * 
	 * @param sb
	 * @param funcName
	 * @param params
	 */
	void function(StringBuilder sb, String funcName, Object... params);

	/**
	 * 计算一个数据表所占用的存储空间大小
	 * 
	 * @param table
	 * @return
	 */
	long calcTableSize(String table);

	/**
	 * 返回数据库中的数据表列表
	 * 
	 * @param nameFilter
	 *            名字过滤器，符合过滤器条件的将被筛掉
	 * @return
	 */
	List<NameObject<String>> getTableList(Predicate<String> nameFilter);
	
	List<Column> getColumn(String table);
	
	public String getSchema();
	
}
