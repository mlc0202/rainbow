package com.icitic.core.db.database;

import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.model.ColumnType;

public class Mysql extends AbstractDialect {

	public String getTimeSql() {
		return "select current_timestamp";
	}

	public String wrapLimitSql(String sql, int limit) {
		throw new RuntimeException("not impl");
	}

	public String wrapPagedSql(String sql, Pager pager) {
		return String.format("%s LIMIT %d, %d", sql, pager.getFrom(), pager.getLimit());
	}

	public String wrapPagedSql(String sql, String select, Pager pager) {
		return String.format("%s LIMIT %d, %d", sql, pager.getFrom(), pager.getLimit());
	}

	public String wrapDirtyRead(String sql) {
		throw new RuntimeException("not impl");
	}

	@Override
	public String toDateSql(String field, ColumnType type) {
		StringBuilder sb = new StringBuilder("to_date(");
		sb.append(field).append(",");
		switch (type) {
		case DATE:
			sb.append("'%Y-%m-%d'");
			break;
		case TIME:
			sb.append("'%H:%i:%s'");
			break;
		case TIMESTAMP:
			sb.append("'%Y-%m-%d %H:%i:%s'");
			break;
		default:
			throw new IllegalArgumentException();
		}
		sb.append(")");
		return sb.toString();
	}

}
