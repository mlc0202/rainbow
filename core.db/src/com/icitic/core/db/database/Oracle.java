package com.icitic.core.db.database;

import static com.google.common.base.Preconditions.checkArgument;
import static com.icitic.core.util.Preconditions.checkArgument;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.Sql;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.util.Utils;

public class Oracle extends AbstractDialect {

	public static final ImmutableMap<String, ColumnType> PHYSIC_TO_LOGIC = new ImmutableMap.Builder<String, ColumnType>()
			.put("DATE", ColumnType.DATE) //
			.put("TIME", ColumnType.TIME) //
			.put("TIMESTAMP", ColumnType.TIMESTAMP) //
			.put("CHARACTER", ColumnType.CHAR) //
			.put("CHAR", ColumnType.CHAR) //
			.put("VARCHAR2", ColumnType.VARCHAR) //
			.put("NCHAR", ColumnType.NCHAR) //
			.put("NVARCHAR2", ColumnType.NVARCHAR) //
			.put("CLOB", ColumnType.CLOB) //
			.put("NCLOB", ColumnType.NCLOB) //
			.put("BLOB", ColumnType.BLOB) //
			.build();

	@Override
	public String getTimeSql() {
		return "select sysdate from dual";
	}

	@Override
	public String wrapLimitSql(String sql, int limit) {
		String sqlPage = partPageSql(sql, limit);
		return String.format("select * from (%s)", sqlPage);
	}

	@Override
	public String wrapPagedSql(String sql, Pager pager) {
		String sqlPage = partPageSql(sql, pager.getTo());
		return String.format("select * from (select rownum rownum1,A.* from (%s) A) where rownum1 >=%d", sqlPage,
				pager.getFrom());
	}

	@Override
	public String wrapPagedSql(String sql, String select, Pager pager) {
		String sqlPage = partPageSql(sql, pager.getTo());
		return String.format("select %s from (select rownum rownum1,A.* from (%s) A) where rownum1 >=%d", select,
				sqlPage, pager.getFrom());
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
			sb.append("'yyyy-MM-dd'");
			break;
		case TIME:
			sb.append("'HH24:mm:ss'");
			break;
		case TIMESTAMP:
			sb.append("'yyyy-MM-dd HH24:mm:ss'");
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
			return String.format("NUMBER(%d)", 5);
		case INT:
			return String.format("NUMBER(%d)", 10);
		case LONG:
			return String.format("NUMBER(%d)", 19);
		case DOUBLE:
			return "NUMBER";
		case NUMERIC:
			return (length == 0) ? "NUMBER" : String.format("NUMBER(%d,%d)", length, precision);
		case DATE:
		case TIME:
		case TIMESTAMP:
			return "DATE";
		case CHAR:
			return String.format("CHAR(%d)", length);
		case VARCHAR:
			return String.format("VARCHAR2(%d)", length);
		case NCHAR:
			return String.format("NCHAR(%d)", length);
		case NVARCHAR:
			return String.format("NVARCHAR2(%d)", length);
		case CLOB:
			return String.format("CLOB(%d)", length);
		case BLOB:
			return String.format("BLOB(%d)", length);
		case NCLOB:
			return String.format("NCLOB(%d)", length);
		default:
			return Utils.NULL_STR;
		}
	}

	@Override
	public Sql findEntityNameSql(String schema, String entityName) {
		Sql result = new Sql(
				"SELECT table_name, 'table' AS entityType FROM DBA_TABLES WHERE table_name LIKE ? AND owner = ? UNION ALL "
						+ "SELECT view_name, 'view' AS entityType FROM DBA_VIEWS WHERE view_name LIKE ? AND owner=?");
		entityName = entityName.toUpperCase();
		result.setParams(ImmutableList.<Object> of(entityName, schema, entityName, schema));
		return result;
	}

	private String parseCols(String sql) {
		String sqlTemp = sql;
		if (sql.toUpperCase().indexOf(" FROM ") >= 0)
			sqlTemp = sql.substring(0, sql.toUpperCase().indexOf(" FROM "));
		String regex = "(select)(.+)";
		String cols = getMatchedString(regex, sqlTemp);
		if (Strings.isNullOrEmpty(cols))
			return null;
		else
			return cols.trim();
	}

	private String parseTables(String sql) {
		String sqlTemp = sql;
		if (sql.toUpperCase().indexOf(" WHERE ") >= 0)
			sqlTemp = sql.substring(0, sql.toUpperCase().indexOf(" WHERE "));
		String regex = "";

		if (isContains(sqlTemp, "\\s+where\\s+")) {
			regex = "( from )(.+)( where )";
		} else {
			regex = "( from )(.+)($)";
		}

		String tables = getMatchedString(regex, sqlTemp);
		if (Strings.isNullOrEmpty(tables))
			return null;
		else
			return tables.trim();
	}

	private String parseConditions(String sql, String regex) {
		String conditions = getMatchedString(regex, sql);
		return conditions.trim();
	}

	private String parseGroupCols(String sql, String order) {
		String groupCols = sql;
		if (isContains(groupCols, "group\\s+by")) {
			while (isContains(groupCols, "group\\s+by")) {
				String regex = "";
				if (Strings.isNullOrEmpty(order)) {
					regex = "(group\\s+by)(.+)($)";
				} else {
					regex = "(group\\s+by)(.+)(order\\s+by)";
				}

				groupCols = getMatchedString(regex, groupCols);
			}
			if (Strings.isNullOrEmpty(groupCols))
				return null;
			groupCols = groupCols.trim();
			String left[] = groupCols.split("\\(");
			int leftNum = left.length - 1;
			if (groupCols.substring(groupCols.length() - 1).equals("("))
				leftNum++;
			String right[] = groupCols.split("\\)");
			int rightNum = right.length - 1;
			if (groupCols.substring(groupCols.length() - 1).equals(")"))
				rightNum++;
			if (Strings.isNullOrEmpty(groupCols)
					|| ((groupCols.indexOf(")") >= 0) && ((leftNum != rightNum) || ((groupCols.indexOf("(") <= 0) || (groupCols
							.indexOf("(") > groupCols.indexOf(")"))))))
				return null;
			return groupCols;
		} else
			return null;
	}

	private String parseOrderCols(String sql) {
		String orderCols = sql;
		if (isContains(orderCols, "order\\s+by")) {
			while (isContains(orderCols, "order\\s+by")) {
				String regex = "";

				if (isContains(orderCols, "order\\s+by")) {
					regex = "(order\\s+by)(.+)($)";
				}

				orderCols = getMatchedString(regex, orderCols);
			}
			if (Strings.isNullOrEmpty(orderCols))
				return null;
			orderCols = orderCols.trim();
			String left[] = orderCols.split("\\(");
			int leftNum = left.length - 1;
			if (orderCols.substring(orderCols.length() - 1).equals("("))
				leftNum++;
			String right[] = orderCols.split("\\)");
			int rightNum = right.length - 1;
			if (orderCols.substring(orderCols.length() - 1).equals(")"))
				rightNum++;
			if (Strings.isNullOrEmpty(orderCols)
					|| ((orderCols.indexOf(")") >= 0) && ((leftNum != rightNum) || ((orderCols.indexOf("(") <= 0) || (orderCols
							.indexOf("(") > orderCols.indexOf(")"))))))
				return null;
			return orderCols;
		} else
			return null;
	}

	private String getMatchedString(String regex, String text) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			return matcher.group(2);
		}

		return null;
	}

	private boolean isContains(String lineText, String word) {
		Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(lineText);
		return matcher.find();
	}

	// 重新分解sql
	private String partPageSql(String sql, int limit) {
		sql = sql.replace('\n', ' ').replace('\r', ' ');
		String cols = parseCols(sql);
		String tables = parseTables(sql);
		String orderBy = parseOrderCols(sql);
		String groupBy = parseGroupCols(sql, orderBy);
		// 复杂的表不做
		if ((Strings.isNullOrEmpty(tables)) || (tables.toUpperCase().indexOf("SELECT ") >= 0)
				&& (tables.toUpperCase().indexOf(" FROM ") >= 0) || (!Strings.isNullOrEmpty(orderBy))
				|| (!Strings.isNullOrEmpty(groupBy))) {
			return String.format("select * from (%s) where rownum <=%d ", sql, limit);
		}
		String conditions = null;
		String regex = "";
		if (isContains(sql, "\\s+where\\s+")) {
			if (groupBy != null) {
				regex = "(where)(.+)(group\\s+by)";
			} else if (orderBy != null) {
				regex = "(where)(.+)(order\\s+by)";
			} else {
				regex = "(where)(.+)($)";
			}
			conditions = parseConditions(sql, regex);
		}
		if (Strings.isNullOrEmpty(conditions))
			conditions = "1=1";
		if (Strings.isNullOrEmpty(groupBy))
			groupBy = "";
		else
			groupBy = String.format("group by %s", groupBy);
		if (Strings.isNullOrEmpty(orderBy))
			orderBy = "";
		else
			orderBy = String.format("order by %s", orderBy);
		return String.format("select %s from %s where (%s) and rownum <=%d %s %s", cols, tables, conditions, limit,
				groupBy, orderBy);
	}

	@Override
	public String getProcedureSql(String schema) {
		return String.format("select object_name  from ALL_OBJECTS where owner='%s' and object_type='PROCEDURE'",
				schema);
	}

	@Override
	public void function(StringBuilder sb, String funcName, Object[] params) {
		if ("YEAR".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "year need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(",'yyyy'))");
		} else if ("MONTH".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "month need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(",'mm'))");
		} else if ("DAY".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "day need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(",'dd'))");
		} else if ("HOUR".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "hour need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(",'hh24'))");
		} else if ("MINUTE".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "minute need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(",'mi'))");
		} else if ("SECOND".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "second need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(",'ss'))");
		} else if ("DATE".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "date need 1 params");
			sb.append("to_date(to_char(");
			addParam(sb, params[0]);
			sb.append(",'yyyy-mm-dd'),'yyyy-mm-dd')");
		} else if ("TIME".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "time need 1 params");
			sb.append("to_date(to_char(");
			addParam(sb, params[0]);
			sb.append(",'hh24:mi:ss'),'hh24:mi:ss')");
		} else if ("TIMEDIFF".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "timediff need 2 params");
			sb.append("ROUND(TO_NUMBER(");
			addParam(sb, params[0]);
			sb.append(" - ");
			addParam(sb, params[1]);
			sb.append(") * 24 * 60 * 60)");
		} else if ("ADD_DAYS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_days need 2 params");
			addParam(sb, params[0]);
			sb.append("+ (");
			addParam(sb, params[1]);
			sb.append(")");
		} else if ("ADD_YEARS".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "add_years need 2 params");
			sb.append("(ADD_MONTHS(");
			addParam(sb, params[0]);
			sb.append(", 12*(");
			addParam(sb, params[1]);
			sb.append(")))");
		} else if ("TO_DAYS".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "to_days need 1 params");
			sb.append("trunc(");
			addParam(sb, params[0]);
			sb.append("-to_date('0001-01-01','yyyy-mm-dd'))");
		} else if ("DATE_TO_CHAR".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "date_to_char need 2 params");
			sb.append("to_char");
			addFuncParam(sb, params);
		} else if ("DAYNAME".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "dayname need 1 params");
			sb.append("to_char(");
			addParam(sb, params[0]);
			sb.append(",'dy')");
		} else if ("NOW".equals(funcName)) {
			checkArgument(params == null || params.length == 0, "extra params of now");
			sb.append("sysdate");
		} else if ("ISNULL".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "isnull need 2 params");
			sb.append("NVL(");
			addParam(sb, params[0]);
			sb.append(",");
			addParam(sb, params[1]);
			sb.append(")");
		} else if ("LEFT".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "isnull need 2 params");
			sb.append("SUBSTR(");
			addParam(sb, params[0]);
			sb.append(",1,");
			addParam(sb, params[1]);
			sb.append(")");
		} else if ("RIGHT".equals(funcName)) {
			checkArgument(params != null && params.length == 2, "isnull need 2 params");
			sb.append("SUBSTR(");
			addParam(sb, params[0]);
			sb.append(",case when length(");
			addParam(sb, params[0]);
			sb.append(") > ");
			addParam(sb, params[1]);
			sb.append(" then -");
			addParam(sb, params[1]);
			sb.append(" else -length(");
			addParam(sb, params[0]);
			sb.append(") end ,");
			addParam(sb, params[1]);
			sb.append(")");
		} else if ("WEEK".equals(funcName)) {
			checkArgument(params != null && params.length == 1, "week need 1 params");
			sb.append("to_number(to_char(");
			addParam(sb, params[0]);
			sb.append(" ,'ww'))");
		} else
			super.function(sb, funcName, params);
	}

	@Override
	public void createTableAs(String tableName, Sql sql, String table_space, String index_space,boolean distribute,String distributeKey, Dao dao) {
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
		if (Strings.isNullOrEmpty(table_space))
			dao.execSql(String.format("create table %s as %s ", tableName, tmpsql));
		else
			dao.execSql(String.format("create table %s tablespace %s as %s ", tableName, table_space, tmpsql));
	}

	@Override
	public StringBuilder createTableSql(String tableName, List<Column> columns, String table_space, String index_space,boolean distribute,String distributeKey,
			Dao dao) {
		if (Strings.isNullOrEmpty(table_space))
			return super.createTableSql(tableName, columns, table_space, index_space,distribute,distributeKey, dao);
		else {
			StringBuilder sb = super.createTableSql(tableName, columns, table_space, index_space,distribute,distributeKey, dao);
			sb.append(" tablespace ").append(table_space);
			return sb;
		}
	}

	@Override
	public String getColumnInfoSql() {
		return new StringBuilder("select owner AS TABLE_SCHEMA ,table_name as ENTITY,column_name as CODE")
				.append(",0 as IS_KEY")
				.append(",case DATA_TYPE when 'FLOAT' then 'NUMERIC' when 'NUMBER' then 'NUMERIC' when 'VARCHAR2' then 'VARCHAR' when 'NVARCHAR2' then 'NVARCHAR' when 'TIMESTAMP(6)' then 'TIMESTAMP' else DATA_TYPE end as DATA_TYPE")
				.append(",DATA_LENGTH as LENGTH")
				.append(",DATA_SCALE as SCALE")
				.append(",column_id as SORT,comments as COLUMN_COMMENT")
				.append(" from  (select t.* from all_tab_columns  t where Upper(t.table_name)=? and Upper(t.owner)=?) tcols,")
				.append("(select table_name table_name_c, comments,t.column_name column_name_c from user_col_comments t  group by table_name,comments,column_name) tcols_coment")
				.append(" where tcols.table_name = tcols_coment.table_name_c and tcols_coment.column_name_c = tcols.column_name ")
				.toString();
	}

	@Override
	public String getCalcTableSizeSql(String schema, String table_name, int pageSize) {
		return String.format(
				"select ceil(sum(bytes)/(1024.00*1024.00)) from dba_segments where owner='%s' and segment_name='%s'",
				schema, table_name);
	}

}
