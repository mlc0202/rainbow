package com.icitic.core.db.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Iterables;
import com.icitic.core.db.dao.function.Function;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;

public class SimpleCondition extends Condition {

	private String property;

	private Function function;

	private Operator op;

	private Object param;

	public SimpleCondition(String property, Operator op, Object param) {
		this.property = property;
		this.op = op;
		this.param = param;
	}

	public SimpleCondition(Function function, Operator op, Object param) {
		this.function = function;
		this.op = op;
		this.param = param;
	}

	public Condition and(Condition cnd) {
		if (cnd == null || cnd.isEmpty())
			return this;
		return new ComboCondition(this).and(cnd);
	}

	public Condition or(Condition cnd) {
		if (cnd == null || cnd.isEmpty())
			return this;
		return new ComboCondition(this).or(cnd);
	}

	public void toSql(Entity entity, Dao dao, StringBuilder sb, List<Object> params) {
		ColumnType type = null;
		if (function == null) {
			Column column = entity.getColumn(property);
			checkNotNull(column, "entity [%s] doesn't has a column named [%s]", entity.getName(), property);
			sb.append(column.getDbName());
			type = column.getType();
		} else {
			function.toSql(dao.getDatabaseDialect(), entity, sb);
			type = function.getType();
		}

		if (param != null) {
			if (param instanceof Sql) {
				subQuery((Sql) param, sb, params);
				return;
			} else if (param instanceof SqlBuilder) {
				Sql sql = ((SqlBuilder) param).build(dao);
				subQuery(sql, sb, params);
				return;
			}
		}
		normalQuery(type, sb, params);
	}

	private void subQuery(Sql sql, StringBuilder sb, List<Object> params) {
		sb.append(op.getSymbol()).append('(').append(sql.getSql()).append(')');
		params.addAll(sql.getParams());
	}

	private void normalQuery(ColumnType type, StringBuilder sb, List<Object> params) {
		if (op == Operator.IN || op == Operator.NotIn) {
			checkNotNull(param, "param of [%s] should not be null", property);
			sb.append(op.getSymbol()).append(" (");

			Object[] p = null;
			if (param instanceof Iterable<?>) {
				p = Iterables.toArray((Iterable<?>) param, Object.class);
			} else if (param.getClass().isArray())
				p = (Object[]) param;

			for (int i = 0; i < p.length; i++) {
				sb.append(i == 0 ? "?" : ",?");
				params.add(Column.toDb(type, p[i]));
			}
			sb.append(")");
		} else {
			if (param == null) {
				if (op == Operator.Equal)
					sb.append(" is null");
				else if (op == Operator.NotEqual)
					sb.append(" is not null");
				else
					checkNotNull(param, "param of [%s] should not be null", property);
			} else {
				sb.append(op.getSymbol());
				sb.append("?");
				params.add(Column.toDb(type, param));
			}
		}
	}

	public void toSqlRaw(Dao dao, StringBuilder sb, List<Object> params) {
		if (function == null) {
			sb.append(property);
		} else {
			// TODO 现在先不支持function
			throw new RuntimeException("function not support yet");
		}
		if (param != null) {
			if (param instanceof Sql) {
				subQuery((Sql) param, sb, params);
				return;
			} else if (param instanceof SqlBuilder) {
				Sql sql = ((SqlBuilder) param).build(dao);
				subQuery(sql, sb, params);
				return;
			}
		}
		normalQuery(sb, params);
	}

	private void normalQuery(StringBuilder sb, List<Object> params) {
		if (op == Operator.IN || op == Operator.NotIn) {
			checkNotNull(param, "param of [%s] should not be null", property);
			sb.append(op.getSymbol()).append(" (");

			Object[] p = null;
			if (param instanceof Iterable<?>) {
				p = Iterables.toArray((Iterable<?>) param, Object.class);
			} else if (param.getClass().isArray())
				p = (Object[]) param;

			for (int i = 0; i < p.length; i++) {
				sb.append(i == 0 ? "?" : ",?");
				params.add(p[i]);
			}
			sb.append(")");
		} else {
			if (param == null) {
				if (op == Operator.Equal)
					sb.append(" is null");
				else if (op == Operator.NotEqual)
					sb.append(" is not null");
				else
					checkNotNull(param, "param of [%s] should not be null", property);
			} else {
				sb.append(op.getSymbol());
				sb.append("?");
				params.add(param);
			}
		}
	}
}
