package com.icitic.core.db.database;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.Sql;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;

public class Gbase extends AbstractDialect {

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
		return String.format("select %s from (%s) sybhwcm_gbase limit %d offset %d", select, sql, pager.getLimit(),
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
			sb.append("'HH24:MM:SS'");
			break;
		case TIMESTAMP:
			sb.append("'YYYY-MM-DD HH24:MM:SS'");
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
			return type.name();
		case NUMERIC:
			if (length == 0)
				return "DECIMAL";
			else
				return String.format("DECIMAL(%d,%d)", length, precision);
		case DATE:
		case TIME:
		case TIMESTAMP:
			return type.name();
		case CHAR:
			return String.format("CHAR(%d)", length);
		case NCHAR:
			return String.format("CHAR(%d) CHARACTER SET utf8", length);
		case VARCHAR:
			return String.format("VARCHAR(%d)", length);
		case NVARCHAR:
			return String.format("VARCHAR(%d) CHARACTER SET utf8", length);
		case CLOB:
		case NCLOB: // GBase 有四种TEXT, 我们不用它开发，所以这里就简单的处理了
			return (length == 0) ? "TEXT" : String.format("TEXT(%d)", length);
		case BLOB: // GBase 有四种BLOB, 我们不用它开发，所以这里就简单的处理了
			return (length == 0) ? "BLOB" : String.format("BLOB(%d)", length);
		default:
			return type.name();
		}
	}

	@Override
	public Sql findEntityNameSql(String schema, String entityName) {
		Sql result = new Sql(
				"Select table_name,'table' AS entityType from tables where table_name like ? and table_schema=? UNION ALL "
						+ "Select table_name,'view' AS entityType views where table_name like ? AND table_schema=?");
		entityName = entityName.toUpperCase();
		result.setParams(ImmutableList.<Object> of(entityName, schema, entityName, schema));
		return result;
	}

	@Override
	public String getProcedureSql(String schema) {
		return "select trim(routine_schema)||'.'||trim(routine_name)  from information_schema.routines";
	}

	@Override
	public String getProcedureContent(String procName, Dao dao) {
		Map<String, Object> rs = dao.queryForMap("show create procedure " + procName);
		Object pro = rs.get("CREATE PROCEDURE");
		return pro == null ? null : (String) pro;
	}

	@Override
	public void function(StringBuilder sb, String funcName, Object[] params) {
		if ("DATE_TO_CHAR".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "date_to_char need 2 params");
			sb.append("to_char");
			addFuncParam(sb, params);
		} else if ("ADD_DAYS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_days need 2 params");
			sb.append("DATE_ADD(");
			addParam(sb, params[0]);
			sb.append(", interval ");
			addParam(sb, params[1]);
			sb.append(" day)");
		} else if ("ADD_MONTHS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_months need 2 params");
			sb.append("DATE_ADD(");
			addParam(sb, params[0]);
			sb.append(", interval ");
			addParam(sb, params[1]);
			sb.append(" month)");
		} else if ("ADD_YEARS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_years need 2 params");
			sb.append("DATE_ADD(");
			addParam(sb, params[0]);
			sb.append(", interval ");
			addParam(sb, params[1]);
			sb.append(" year)");
		} else if ("TO_TIMESTAMP".equals(funcName)) {
			checkArgument(params != null && params.length >= 1, "to_timestamp at least need 1 param");
			sb.append("timestamp(");
			addParam(sb, params[0]);
			sb.append(")");
		} else if ("TRUNC".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "trunc need 2 params");
			sb.append("TRUNCATE");
			addFuncParam(sb, params);
		} else if ("NOW".equals(funcName)) {
			checkArgument(params == null || params.length == 0, "extra params of now");
			sb.append("now()");
		} else if ("TIMEDIFF".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "timediff need 2 params");
			sb.append("TIME_TO_SEC(");
			addParam(sb, params[0]);
			sb.append(") - TIME_TO_SEC(");
			addParam(sb, params[1]);
			sb.append(")");
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
	public void createTableAs(String tableName, Sql sql, String table_space, String index_space, boolean distribute,
			String distributeKey, Dao dao) {
		String modify;
		if (distribute) {
			modify = Strings.isNullOrEmpty(distributeKey) ? " nolock NOCOPIES" : String.format("DISTRIBUTED BY ('%s')",
					distributeKey);
		} else {
			modify = "REPLICATED";
		}
		dao.execSql(String.format("create table %s %s as %s ", tableName, modify, sql.getSql()), sql.getParamArray());
	}

	@Override
	public StringBuilder createTableSql(String tableName, List<Column> columns, String table_space, String index_space,
			boolean distribute, String distributeKey, Dao dao) {
		StringBuilder result = super.createTableSql(tableName, columns, table_space, index_space, distribute,
				distributeKey, dao);
		if (distribute) {
			if (Strings.isNullOrEmpty(distributeKey)) {
				result.append(" NOCOPIES ");
			} else {
				result.append(" DISTRIBUTED BY ('").append(distributeKey).append("')");
			}
		} else {
			result.append(" REPLICATED ");
		}
		return result;
	}

	@Override
	public String getColumnInfoSql() {
		return new StringBuilder("select TABLE_SCHEMA ,TABLE_NAME as ENTITY,COLUMN_NAME as CODE")
				.append(",case COLUMN_KEY when 'PRI' then true else false end as IS_KEY")
				.append(",case DATA_TYPE when 'bigint' then 'LONG' when 'tinyint' then 'SMALLINT' when 'decimal' then 'NUMERIC' when 'text' then 'CLOB' when 'longtext' then 'CLOB' when 'longblob' then 'BLOB' when 'datetime' then 'TIMESTAMP' else upper(DATA_TYPE) end as DATA_TYPE")
				.append(",case when CHARACTER_MAXIMUM_LENGTH is null and NUMERIC_PRECISION is null then 0 when CHARACTER_MAXIMUM_LENGTH is null then NUMERIC_PRECISION else CHARACTER_MAXIMUM_LENGTH END as LENGTH")
				.append(",case when NUMERIC_SCALE is null then 0 else NUMERIC_SCALE end as SCALE")
				.append(",ORDINAL_POSITION as SORT,COLUMN_COMMENT")
				.append(" from information_schema.columns where Upper(TABLE_NAME)=? and Upper(TABLE_SCHEMA)=?").toString();
	}

	@Override
	public String getCalcTableSizeSql(String schema, String table_name, int pageSize) {
		return String
				.format("select ceil(table_storage_size/(1024*1024)) from information_schema.cluster_tables where table_schema='%s' and table_name = '%s'",
						schema, table_name);
	}

	@Override
	public String getTableListSql() {
		// TODO Auto-generated method stub
		return null;
	}

}
