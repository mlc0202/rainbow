package com.icitic.core.db.dao;

import java.util.LinkedList;
import java.util.List;

import com.icitic.core.db.model.Entity;

public class ComboCondition extends Condition {

	private List<Condition> child = new LinkedList<Condition>();

	ComboCondition(Condition cnd) {
		child.add(cnd);
	}

	@Override
	public Condition and(Condition cnd) {
		if (cnd != null && !cnd.isEmpty()) {
			child.add(new Join(" AND "));
			child.add(cnd);
		}
		return this;
	}

	@Override
	public Condition or(Condition cnd) {
		if (cnd != null && !cnd.isEmpty()) {
			child.add(new Join(" OR "));
			child.add(cnd);
		}
		return this;
	}

	@Override
	public boolean isEmpty() {
		return child.isEmpty();
	}

	@Override
	public void toSql(Entity entity, Dao dao, StringBuilder sb, List<Object> args) {
		for (Condition cnd : child) {
			if (cnd instanceof ComboCondition) {
				sb.append("(");
				cnd.toSql(entity, dao, sb, args);
				sb.append(")");
			} else
				cnd.toSql(entity, dao, sb, args);
		}
	}

	@Override
	public void toSqlRaw(Dao dao, StringBuilder sb, List<Object> params) {
		for (Condition cnd : child) {
			if (cnd instanceof ComboCondition) {
				sb.append("(");
				cnd.toSqlRaw(dao, sb, params);
				sb.append(")");
			} else
				cnd.toSqlRaw(dao, sb, params);
		}
	}

	private static class Join extends Condition {
		private String text;

		Join(String text) {
			this.text = text;
		}

		@Override
		public Condition and(Condition andCnd) {
			return null;
		}

		@Override
		public Condition or(Condition orCnd) {
			return null;
		}

		@Override
		public void toSql(Entity entity, Dao dao, StringBuilder sb, List<Object> params) {
			sb.append(text);
		}

		@Override
		public void toSqlRaw(Dao dao, StringBuilder sb, List<Object> params) {
			sb.append(text);
		}

	}

}
