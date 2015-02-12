package com.icitic.core.db.database;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.Sql;
import com.icitic.core.db.dao.SqlPart;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.model.exception.AppException;

/**
 * 数据库方言接口
 * 
 * @author lijinghui
 * 
 */
public abstract class AbstractDialect implements Dialect {

	/**
	 * 获取去系统表查询表和视图名的sql
	 * 
	 * @param schema
	 * @param entityName
	 *            like表名的参数
	 * @return
	 */
	@Override
	public Sql findEntityNameSql(String schema, String entityName) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取指定scheme下的所有存储过程
	 * 
	 * @param schema
	 * @return
	 */
	@Override
	public String getProcedureSql(String schema) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createTableAs(String tableName, Sql sql, String table_space, String index_space, boolean distribute,
			String distributeKey, Dao dao) {
		dao.execSql(String.format("create table %s as %s ", tableName, sql.getSql()), sql.getParamArray());
	}

	@Override
	public StringBuilder createTableSql(String tableName, List<Column> columns, String table_space, String index_space,
			boolean distribute, String distributeKey, Dao dao) {
		StringBuilder sb = new StringBuilder("create table ").append(tableName).append('(');
		int i = 0;
		List<String> keys = Lists.newArrayList();
		for (Column column : columns) {
			if (i++ > 0)
				sb.append(',');
			sb.append(column.getDbName()).append(' ')
					.append(toPhysicType(column.getType(), column.getLength(), column.getPrecision()));
			if (column.isKey()) {
				sb.append(" not null");
				keys.add(column.getDbName());
			}
		}
		if (!keys.isEmpty()) {
			sb.append(",constraint PK_").append(tableName).append(" PRIMARY KEY(");
			if (keys.size() == 1)
				sb.append(keys.get(0));
			else
				sb.append(Joiner.on(',').join(keys));
			sb.append(')');
		}
		sb.append(')');
		return sb;
	}

	/**
	 * 翻译数据库函数
	 * 
	 * @param sb
	 * @param funcName
	 * @param params
	 */
	@Override
	public void function(StringBuilder sb, String funcName, Object[] params) {
		sb.append(funcName);
		addFuncParam(sb, params);
	}

	/**
	 * 向StringBuilder中添加函数的参数
	 * 
	 * @param dao
	 * @param sb
	 * @param params
	 */
	public static void addFuncParam(StringBuilder sb, Object[] params) {
		switch (params.length) {
		case 0:
			sb.append("()");
			break;
		case 1:
			sb.append("(");
			addParam(sb, params[0]);
			sb.append(")");
			break;
		default:
			boolean first = true;
			sb.append("(");
			for (Object param : params) {
				if (first)
					first = false;
				else
					sb.append(',');
				addParam(sb, param);
			}
			sb.append(")");
		}
	}

	protected static void addParam(StringBuilder sb, Object param) {
		if (param instanceof SqlPart) {
			((SqlPart) param).toSql(sb);
		} else
			sb.append(param.toString());
	}

	@Override
	public String getProcedureContent(String procName, Dao dao) {
		throw new AppException("%s not implement getProcedureContent", getClass().getSimpleName());
	}

	@Override
	public String getColumnInfoSql() {
		throw new AppException("%s not implement getColumnInfoSql", getClass().getSimpleName());
	}

	@Override
	public String getCalcTableSizeSql(String schema, String table_name, int pageSize) {
		throw new AppException("%s not implement getCalcTableSizeSql", getClass().getSimpleName());
	}

	@Override
	public String toPhysicType(ColumnType type, int length, int precision) {
		throw new AppException("%s not implement toPhysicType", getClass().getSimpleName());
	}

	@Override
	public String getTableListSql() {
		throw new AppException("%s not implement getTableListSql", getClass().getSimpleName());
	}

	@Override
	public List<Column> getColumn(String table, Dao dao) {
		throw new AppException("%s not implement getColumnSql", getClass().getSimpleName());
	}
}