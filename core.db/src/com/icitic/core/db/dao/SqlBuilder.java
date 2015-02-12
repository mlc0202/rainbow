package com.icitic.core.db.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.Entity;
import com.icitic.core.util.Utils;

/**
 * 查询创建器
 * 
 * @author lijinghui
 * 
 */
public class SqlBuilder {

	private static Pattern functionPattern = Pattern.compile("(.+)\\((.+)\\)");

	private static Joiner commaJoiner = Joiner.on(",");

	private SqlType type;

	private String[] select;

	private boolean distinct = false;

	private String entityName;

	private Entity entity;

	private Update update;

	private Condition cnd;

	private Iterable<OrderBy> orderBy;

	private List<String> groups;

	private boolean raw = false;

	private int limit = 0;

	private int page = 0;

	/**
	 * 插入用字段
	 */
	private String[] fields;

	/**
	 * 插入值
	 */
	private Object[] values;

	/**
	 * 插入数据的sql
	 */
	private SqlBuilder insertSB;

	public SqlBuilder(SqlType type) {
		this.type = type;
	}

	public SqlBuilder raw() {
		this.raw = true;
		return this;
	}

	public SqlBuilder limit(int limit) {
		this.limit = limit;
		return this;
	}

	public SqlBuilder paging(int pageNo, int pageSize) {
		this.page = pageNo;
		this.limit = pageSize;
		return this;
	}

	public static SqlBuilder select(String... select) {
		SqlBuilder sqlB = new SqlBuilder(SqlType.SELECT);
		sqlB.select = select;
		return sqlB;
	}

	public static SqlBuilder selectDistinct(String... select) {
		SqlBuilder sqlB = new SqlBuilder(SqlType.SELECT);
		sqlB.select = select;
		sqlB.distinct = true;
		return sqlB;
	}

	public SqlType getType() {
		return type;
	}

	public static SqlBuilder delete() {
		return new SqlBuilder(SqlType.DELETE);
	}

	public static SqlBuilder update(String entityName) {
		SqlBuilder sqlB = new SqlBuilder(SqlType.UPDATE);
		sqlB.entityName = entityName;
		return sqlB;
	}

	public static SqlBuilder update(Entity entity) {
		SqlBuilder sqlB = new SqlBuilder(SqlType.UPDATE);
		sqlB.entity = entity;
		return sqlB;
	}

	public static SqlBuilder insertInto(String entityName, String... fields) {
		SqlBuilder sqlB = new SqlBuilder(SqlType.INSERT);
		sqlB.entityName = entityName;
		sqlB.fields = fields;
		return sqlB;
	}

	public SqlBuilder values(Object... values) {
		checkState(SqlType.INSERT == type);
		this.values = values;
		return this;
	}

	public SqlBuilder select(SqlBuilder insertSB) {
		checkState(SqlType.INSERT == type);
		this.insertSB = insertSB;
		return this;
	}

	public String getEntityName() {
		return entityName;
	}

	public SqlBuilder from(String entityName) {
		checkState(SqlType.SELECT == type || SqlType.DELETE == type);
		this.entityName = entityName;
		return this;
	}

	public SqlBuilder from(Entity entity) {
		this.entity = entity;
		return this;
	}

	/**
	 * 设置一个更新项
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public SqlBuilder set(String property, Object value) {
		checkState(SqlType.UPDATE == type);
		if (update == null)
			update = Update.make(property, value);
		else
			update.set(property, value);
		return this;
	}

	/**
	 * 设置一个更新项
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public SqlBuilder set(String property, char calc, Object value) {
		checkState(SqlType.UPDATE == type);
		if (update == null)
			update = Update.make(property, calc, value);
		else
			update.set(property, calc, value);
		return this;
	}

	public SqlBuilder set(Update update) {
		checkState(SqlType.UPDATE == type);
		this.update = update;
		return this;
	}

	/**
	 * 添加第一个条件
	 * 
	 * @param property
	 * @param op
	 * @param param
	 * @return
	 */
	public SqlBuilder where(String property, Operator op, Object param) {
		cnd = Condition.make(property, op, param);
		return this;
	}

	/**
	 * 添加第一个条件
	 * 
	 * @param function
	 * @param op
	 * @param param
	 * @return
	 */
	public SqlBuilder where(com.icitic.core.db.dao.function.Function function, Operator op, Object param) {
		cnd = Condition.make(function, op, param);
		return this;
	}

	/**
	 * 添加第一个相等条件
	 * 
	 * @param property
	 * @param param
	 * @return
	 */
	public SqlBuilder where(String property, Object param) {
		cnd = Condition.make(property, Operator.Equal, param);
		return this;
	}

	/**
	 * 添加第一个条件
	 * 
	 * @param cnd
	 * @return
	 */
	public SqlBuilder where(Condition cnd) {
		this.cnd = cnd;
		return this;
	}

	/**
	 * And一个条件
	 * 
	 * @param cnd
	 * @return
	 */
	public SqlBuilder and(Condition cnd) {
		if (this.cnd == null)
			this.cnd = cnd;
		else
			this.cnd = this.cnd.and(cnd);
		return this;
	}

	/**
	 * And一个条件
	 * 
	 * @param property
	 * @param op
	 * @param param
	 * @return
	 */
	public SqlBuilder and(String property, Operator op, Object param) {
		if (cnd == null)
			cnd = Condition.make(property, op, param);
		else
			cnd = cnd.and(property, op, param);
		return this;
	}

	/**
	 * And一个条件
	 * 
	 * @param function
	 * @param op
	 * @param param
	 * @return
	 */
	public SqlBuilder and(com.icitic.core.db.dao.function.Function function, Operator op, Object param) {
		if (cnd == null)
			cnd = Condition.make(function, op, param);
		else
			cnd = cnd.and(function, op, param);
		return this;
	}

	/**
	 * And一个相等条件
	 * 
	 * @param property
	 * @param param
	 * @return
	 */
	public SqlBuilder and(String property, Object param) {
		return and(property, Operator.Equal, param);
	}

	/**
	 * Or一个条件
	 * 
	 * @param cnd
	 * @return
	 */
	public SqlBuilder or(Condition cnd) {
		checkNotNull(cnd);
		this.cnd = this.cnd.or(cnd);
		return this;
	}

	/**
	 * Or一个条件
	 * 
	 * @param property
	 * @param op
	 * @param param
	 * @return
	 */
	public SqlBuilder or(String property, Operator op, Object param) {
		checkNotNull(cnd);
		cnd = cnd.or(property, op, param);
		return this;
	}

	/**
	 * Or一个条件
	 * 
	 * @param function
	 * @param op
	 * @param param
	 * @return
	 */
	public SqlBuilder or(com.icitic.core.db.dao.function.Function function, Operator op, Object param) {
		checkNotNull(cnd);
		cnd = cnd.or(function, op, param);
		return this;
	}

	/**
	 * Or一个相等条件
	 * 
	 * @param property
	 * @param param
	 * @return
	 */
	public SqlBuilder or(String property, Object param) {
		return or(property, Operator.Equal, param);
	}

	/**
	 * 设置OrderBy项
	 * 
	 * @param input
	 * @return
	 */
	public SqlBuilder orderBy(String input) {
		if (!Utils.isNullOrEmpty(input))
			orderBy = Iterables.transform(Splitter.on(',').split(input), OrderBy.parse);
		return this;
	}

	/**
	 * 添加GroupBy项
	 * 
	 * @param property
	 * @return
	 */
	public SqlBuilder groupBy(String property) {
		if (groups == null)
			groups = new LinkedList<String>();
		groups.add(property);
		return this;
	}

	public Condition getCondition() {
		return cnd;
	}

	public Sql build(Dao dao) {
		StringBuilder sb = new StringBuilder();
		List<Object> params = new ArrayList<Object>();
		switch (type) {
		case SELECT:
			buildSelect(dao, sb, params);
			Sql result = new Sql(sb.toString(), params);
			if (page > 0)
				result.paging(dao, Pager.make(page, limit));
			else if (limit > 0)
				result.limit(dao, limit);
			return result;
		case UPDATE:
			buildUpdate(dao, sb, params);
			break;
		case DELETE:
			buildDelete(dao, sb, params);
			break;
		case INSERT:
			buildInsert(dao, sb, params);
			break;
		}
		return new Sql(sb.toString(), params);
	}

	private void buildSelect(Dao dao, StringBuilder sb, List<Object> params) {
		sb.append("select ");
		if (raw) {
			if (select == null || select.length == 0) {
				sb.append("*");
			} else {
				if (distinct)
					sb.append("DISTINCT ");
				if (select.length == 1)
					sb.append(select[0]);
				else
					commaJoiner.appendTo(sb, select);
			}
			sb.append(" from ").append(entityName);
			if (cnd != null && !cnd.isEmpty()) {
				sb.append(" where ");
				cnd.toSqlRaw(dao, sb, params);
			}
			if (orderBy != null) {
				sb.append(" order by ");
				commaJoiner.appendTo(sb, orderBy);
			}
			if (groups != null) {
				sb.append(" group by ");
				commaJoiner.appendTo(sb, groups);
			}
		} else {
			if (entity == null)
				entity = dao.getEntity(entityName);
			Function<String, String> convertField = convertField(entity);
			if (select == null || select.length == 0) {
				sb.append("*");
			} else {
				if (distinct)
					sb.append("DISTINCT ");
				if (select.length == 1)
					sb.append(convertField.apply(select[0]));
				else
					commaJoiner.appendTo(sb, Iterables.transform(Arrays.asList(select), convertField));
			}
			sb.append(" from ").append(entity.getDbName());
			if (cnd != null && !cnd.isEmpty()) {
				sb.append(" where ");
				cnd.toSql(entity, dao, sb, params);
			}
			if (orderBy != null) {
				sb.append(" order by ");
				commaJoiner.appendTo(sb, Iterables.transform(orderBy, order2db(convertField)));
			}
			if (groups != null) {
				sb.append(" group by ");
				commaJoiner.appendTo(sb, Iterables.transform(groups, field2db(entity)));
			}
		}
	}

	private void buildUpdate(Dao dao, StringBuilder sb, List<Object> params) {
		if (raw) {
			sb.append("update ").append(entityName);
			update.toSqlRaw(sb, params);
			if (cnd != null && !cnd.isEmpty()) {
				sb.append(" where ");
				cnd.toSqlRaw(dao, sb, params);
			}
		} else {
			if (entity == null)
				entity = dao.getEntity(entityName);
			sb.append("update ").append(entity.getDbName());
			update.toSql(entity, sb, params);
			if (cnd != null && !cnd.isEmpty()) {
				sb.append(" where ");
				cnd.toSql(entity, dao, sb, params);
			}
		}
	}

	private void buildDelete(Dao dao, StringBuilder sb, List<Object> params) {
		if (raw) {
			sb.append("delete from ").append(entityName);
			if (cnd != null && !cnd.isEmpty()) {
				sb.append(" where ");
				cnd.toSqlRaw(dao, sb, params);
			}
		} else {
			if (entity == null)
				entity = dao.getEntity(entityName);
			sb.append("delete from ").append(entity.getDbName());
			if (cnd != null && !cnd.isEmpty()) {
				sb.append(" where ");
				cnd.toSql(entity, dao, sb, params);
			}
		}
	}

	private void buildInsert(Dao dao, StringBuilder sb, List<Object> params) {
		if (raw) {
			sb.append("insert into ").append(entityName).append(" ");
			if (fields != null && fields.length > 0) {
				sb.append("(");
				commaJoiner.appendTo(sb, fields);
				sb.append(")");
			}
			if (values != null && values.length > 0) {
				sb.append(" values( ");
				Utils.repeat(sb, "?", ',', values.length);
				sb.append(")");
				params.addAll(Arrays.asList(values));
			} else {
				checkNotNull(insertSB != null);
				insertSB.build(dao);
			}
		} else {
			if (entity == null)
				entity = dao.getEntity(entityName);
			sb.append("insert into ").append(entity.getDbName()).append(" ");

			List<Column> columns;
			if (fields != null && fields.length > 0) {
				columns = Lists.newArrayListWithCapacity(fields.length);
				sb.append("(");
				boolean first = true;
				for (String field : fields) {
					if (first)
						first = false;
					else
						sb.append(',');
					Column column = entity.getColumn(field);
					checkNotNull(column);
					columns.add(column);
					sb.append(column.getDbName());
				}
				sb.append(")");
			} else {
				columns = entity.getColumns();
			}
			if (values != null && values.length > 0) {
				checkArgument(columns.size() == values.length, "insert param count not match");
				sb.append(" values( ");
				Utils.repeat(sb, "?", ',', values.length);
				sb.append(")");
				Iterator<Column> it = columns.iterator();
				for (int i = 0; i < values.length; i++) {
					params.add(it.next().toDb(values[i]));
				}
			} else {
				checkNotNull(insertSB != null);
				Sql sql = insertSB.build(dao);
				sb.append("(").append(sql.getSql()).append(")");
				params.addAll(sql.getParams());
			}
		}
	}

	/**
	 * 把一个元数据的属性字符串转为对应的数据库字段名字符串
	 * 
	 * @param entity
	 *            元数据对象
	 * @return
	 * @exception NullPointerException
	 *                如果表示的字符串不能够找到对应的Column对象
	 */
	private Function<String, String> field2db(final Entity entity) {
		return new Function<String, String>() {
			@Override
			public String apply(String input) {
				Column column = entity.getColumn(input);
				checkNotNull(column);
				return column.getDbName();
			}
		};
	}

	/**
	 * 把一个可能带有
	 * min，max，sum，avg等函数的属性字符串转为对应的数据库字段名字符串。如果表示的字符串不能够找到对应的Column对象，则原样返回
	 * 
	 * @param entity
	 * @return
	 */
	private Function<String, String> convertField(final Entity entity) {
		return new Function<String, String>() {
			@Override
			public String apply(String input) {
				Matcher matcher = functionPattern.matcher(input);
				// 看看是不是 min,max,sum之流
				if (matcher.matches()) {
					Column column = entity.getColumn(matcher.group(2));
					// TODO 检讨一下是不是要原样返回
					return (column == null) ? input : String.format("%s(%s)", matcher.group(1), column.getDbName());
				} else {
					Column column = entity.getColumn(input);
					return (column == null) ? input : column.getDbName();
				}
			}
		};
	}

	private Function<OrderBy, String> order2db(final Function<String, String> convertField) {
		return new Function<OrderBy, String>() {
			@Override
			public String apply(OrderBy input) {
				String field = convertField.apply(input.getProperty());
				return (input.isDesc()) ? field + " DESC " : field;
			}
		};
	}

}