package com.icitic.core.db.database;

import static com.google.common.base.Preconditions.checkArgument;
import static com.icitic.core.util.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.Sql;
import com.icitic.core.db.jdbc.Atom;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.util.Utils;

public class DB2 extends AbstractDialect {

	@Override
	public String getTimeSql() {
		return "select current timestamp from sysibm.sysdummy1";
	}

	@Override
	public String wrapLimitSql(String sql, int limit) {
		return String.format("%s fetch first %d rows only", sql, limit);
	}

	@Override
	public String wrapPagedSql(String sql, Pager pager) {
		return String
				.format("select * from (select rownumber() over() as row_next,t.* from (select * from (%s) fetch first %d rows only) as t) as temp where row_next between %d and %d",
						sql, pager.getTo(), pager.getFrom(), pager.getTo());
	}

	@Override
	public String wrapPagedSql(String sql, String select, Pager pager) {
		return String
				.format("select %s from (select rownumber() over() as row_next,t.* from (select * from (%s) fetch first %d rows only) as t) as temp where row_next between %d and %d",
						select, sql, pager.getTo(), pager.getFrom(), pager.getTo());
	}

	@Override
	public String wrapDirtyRead(String sql) {
		return sql + " WITH UR";
	}

	@Override
	public String toDateSql(String field, ColumnType type) {
		switch (type) {
		case DATE:
			return new StringBuilder().append("DATE(").append(field).append(")").toString();
		case TIME:
			return new StringBuilder().append("TIME(").append(field).append(")").toString();
		case TIMESTAMP:
			return new StringBuilder().append("TIMESTAMP(").append(field).append(")").toString();
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toPhysicType(ColumnType type, int length, int precision) {
		switch (type) {
		case SMALLINT:
			return "SMALLINT";
		case INT:
			return "INT";
		case LONG:
			return "BIGINT";
		case DOUBLE:
			return "DOUBLE";
		case NUMERIC:
			if (length == 0)
				return "NUMERIC";
			else
				return String.format("NUMERIC(%d,%d)", length, precision);
		case DATE:
			return "DATE";
		case TIME:
			return "TIME";
		case TIMESTAMP:
			return "TIMESTAMP";
		case CHAR:
		case VARCHAR:
		case CLOB:
		case BLOB:
			return String.format("%s(%d)", type.name(), length);
		case NCHAR:
			return String.format("GRAPHIC(%d)", length);
		case NVARCHAR:
			return String.format("VARGRAPHIC(%d)", length);
		case NCLOB:
			return String.format("DBCLOC(%d)", length);
		default:
			return Utils.NULL_STR;
		}
	}

	@Override
	public Sql findEntityNameSql(String schema, String entityName) {
		Sql result = new Sql(
				"Select tabName,'table' AS entityType from SYSCAT.tables where tabName like ? and owner=? UNION ALL "
						+ "Select viewName,'view' AS entityType from SYSCAT.views where viewName like ? AND owner=?");
		entityName = entityName.toUpperCase();
		result.setParams(ImmutableList.<Object> of(entityName, schema, entityName, schema));
		return result;
	}

	@Override
	public String getProcedureSql(String schema) {
		return String.format("select PROCNAME from syscat.PROCEDURES where PROCSCHEMA='%s'", schema);
	}

	@Override
	public void createTableAs(final String tableName, final Sql sql, final String table_space, String index_space,boolean distribute,String distributeKey,
			final Dao dao) {
		checkNotNull(table_space, "tablespace is null");
		if (Strings.isNullOrEmpty(index_space))
			index_space = table_space;
		final String tmp_index_space = index_space;
		String tmpsql = sql.getSql();
		Object[] params = sql.getParamArray();
		for (int i = 0; i < params.length; i++) {
			int pos = tmpsql.indexOf('?');
			if (pos >= 0) {
				Object p = params[i];
				if (p instanceof String) {
					tmpsql = tmpsql.replaceFirst("[?]", String.format("'%s'", params[i].toString()));
				} else {
					tmpsql = tmpsql.replaceFirst("[?]", params[i].toString());
				}
			}
		}
		final String createSql = tmpsql;
		dao.transaction(new Atom() {
			@Override
			public void run() throws Throwable {
				dao.execSql(String.format(
						"create table %s as (%s) definition only in %s index in %s not logged initially", tableName,
						createSql, table_space, tmp_index_space));
				dao.execSql(String.format("alter table %s activate not logged initially", tableName));
				dao.execSql(String.format("insert into %s (%s)", tableName, sql.getSql()), sql.getParamArray());
			}
		});
	}

	@Override
	public StringBuilder createTableSql(String tableName, List<Column> columns, String table_space, String index_space,boolean distribute,String distributeKey,
			Dao dao) {
		checkNotNull(table_space, "tablespace is null");
		if (Strings.isNullOrEmpty(index_space))
			index_space = table_space;
		StringBuilder sb = super.createTableSql(tableName, columns, table_space, index_space,distribute,distributeKey, dao);
		sb.append(" in ").append(table_space).append(" index in ").append(index_space);
		return sb;
	}

	@Override
	public void function(StringBuilder sb, String funcName, Object[] params) {
		if ("TO_DAYS".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "to_days need 1 params");
			sb.append("days");
			addFuncParam(sb, params);
		} else if ("ADD_DAYS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_days need 2 params");
			addParam(sb, params[0]);
			sb.append("+(");
			addParam(sb, params[1]);
			sb.append(") day");
		} else if ("ADD_YEARS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_years need 2 params");
			addParam(sb, params[0]);
			sb.append("+(");
			addParam(sb, params[1]);
			sb.append(") year");
		} else if ("DATE_TO_CHAR".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "date_to_char need 2 params");
			sb.append("to_char");
			addFuncParam(sb, params);
		} else if ("TO_TIMESTAMP".equals(funcName)) {
			checkArgument(params != null && params.length >= 1, "to_timestamp at least need 1 param");
			sb.append("timestamp(");
			addParam(sb, params[0]);
			sb.append(")");
		} else if ("NOW".equals(funcName)) {
			checkArgument(params == null || params.length == 0, "extra params of now");
			sb.append("CURRENT DATE");
		} else if ("TIMEDIFF".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "timediff need 2 params");
			sb.append("(DAYS(");
			addParam(sb, params[0]);
			sb.append(") - DAYS(");
			addParam(sb, params[1]);
			sb.append(")) * 86400 +(MIDNIGHT_SECONDS(");
			addParam(sb, params[0]);
			sb.append(") - MIDNIGHT_SECONDS(");
			addParam(sb, params[1]);
			sb.append("))");
		} else if ("ISNULL".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "isnull need 2 params");
			sb.append("COALESCE(");
			addParam(sb, params[0]);
			sb.append(",");
			addParam(sb, params[1]);
			sb.append(")");
		} else
			super.function(sb, funcName, params);
	}

	@Override
	public String getColumnInfoSql() {
		return new StringBuilder("select TABSCHEMA AS TABLE_SCHEMA ,TABNAME as ENTITY,COLNAME as CODE")
				.append(",0 as IS_KEY")
				.append(",case TYPENAME when 'INTEGER' then 'INT' when 'BIGINT' then 'LONG' when 'TINYINT' then 'SMALLINT' when 'DECIMAL' then 'NUMERIC' when 'CHARACTER' then 'CHAR' else TYPENAME end as DATA_TYPE")
				.append(",LENGTH").append(",SCALE").append(",COLNO as SORT,remarks as COLUMN_COMMENT")
				.append(" from syscat.columns where Upper(TABNAME)=? and Upper(TABSCHEMA)=?").toString();
	}

	@Override
	public String getCalcTableSizeSql(String schema, String table_name, int pageSize) {
		checkNotNull(schema, "schema is null,please check database.xml.");
		return String.format(
				"select ceil(npages*%d/1024.00) from syscat.tables where tabschema='%s' and  tabname = '%s'",
				pageSize, schema, table_name);
	}

}
