package com.icitic.core.db.database;

import com.google.common.collect.ImmutableList;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.Sql;
import com.icitic.core.db.model.ColumnType;

public class Postgres extends AbstractDialect {

	@Override
	public String getTimeSql() {
		return "select now()";
	}

	@Override
	public String wrapLimitSql(String sql, int limit) {
		return String.format("%s limit %d offset 0", sql, limit);
	}

	@Override
	public String wrapPagedSql(String sql, Pager pager) {
		return String.format("%s limit %d offset %d", sql, pager.getLimit(), pager.getFrom() - 1);
	}

	@Override
	public String wrapPagedSql(String sql, String select, Pager pager) {
		return String.format("select %s from (%s) sybhwcm limit %d offset %d", select, sql, pager.getLimit(),
				pager.getFrom() - 1);
	}

	@Override
	public String wrapDirtyRead(String sql) {
		return sql;
	}

	@Override
	public String toDateSql(String field, ColumnType type) {
		StringBuilder sb = new StringBuilder("to_date(");
		sb.append(field).append(",");
		switch (type) {
		case DATE:
			sb.append("'YYYY-MM-DD'");
			break;
		case TIME:
			sb.append("'HH24:MI:SS'");
			break;
		case TIMESTAMP:
			sb.append("'YYYY-MM-DD HH24:MI:SS'");
			break;
		default:
			throw new IllegalArgumentException();
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String toPhysicType(ColumnType type, int length, int precision) {
		switch (type) {
		case SMALLINT:
		case INT:
			return type.name();
		case LONG:
			return "BIGINT";
		case DOUBLE:
			return "NUMERIC";
		case NUMERIC:
			if (length == 0)
				return "NUMERIC";
			else
				return String.format("NUMERIC(%d,%d)", length, precision);
		case DATE:
		case TIME:
		case TIMESTAMP:
			return type.name();
		case CHAR:
		case NCHAR:
			return String.format("CHAR(%d)", length);
		case VARCHAR:
		case NVARCHAR:
			return String.format("VARCHAR(%d)", length);
		case CLOB:
		case NCLOB:
			return "TEXT";
		case BLOB:
			return "BYTEA";
		default:
			return type.name();
		}
	}

	@Override
	public Sql findEntityNameSql(String schema, String entityName) {
		Sql result = new Sql(
				"Select tablename,'table' AS entityType from pg_tables where tablename like ? and schemaname=? UNION ALL "
						+ "Select viewname,'view' AS entityType pg_views where viewname like ? AND schemaname=?");
		entityName = entityName.toUpperCase();
		result.setParams(ImmutableList.<Object> of(entityName, schema, entityName, schema));
		return result;
	}

}