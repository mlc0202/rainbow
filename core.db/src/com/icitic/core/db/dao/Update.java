package com.icitic.core.db.dao;

import java.util.List;

import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.Entity;

public final class Update {

	public static final char ADD = '+';
	public static final char SUB = '-';
	public static final char MUL = '*';
	public static final char DIV = '/';

	public static Update make(String property, Object value) {
		return new Update(property, '\0', value, null, null);
	}

	public static Update make(String property, char calc, Object value) {
		return new Update(property, calc, value, null, null);
	}

	private String property;

	private Object value;

	private char calc;

	private Update head;

	private Update next;

	private Update(String property, char calc, Object value, Update head, Update next) {
		this.property = property;
		this.calc = calc;
		this.value = value;
		if (head == null)
			this.head = this;
		else
			this.head = head;
		this.next = next;
	}

	public Update set(String property, Object value) {
		return set(property, '\0', value);
	}

	public Update set(String property, char calc, Object value) {
		Update oldNext = next;
		next = new Update(property, calc, value, this.head, oldNext);
		return next;
	}

	public void toSql(Entity entity, StringBuilder sb, List<Object> params) {
		sb.append(" set ");
		Update u = head;
		while (u != null) {
			if (u != head)
				sb.append(",");
			Column column = entity.getColumn(u.property);
			String fieldName = column.getDbName();
			if (u.calc == '\0') {
				sb.append(fieldName).append("=?");
			} else {
				sb.append(fieldName).append("=").append(fieldName).append(u.calc).append('?');
			}
			params.add(column.toDb(u.value));
			u = u.next;
		}
	}
	
	public void toSqlRaw(StringBuilder sb, List<Object> params) {
		sb.append(" set ");
		Update u = head;
		while (u != null) {
			if (u != head)
				sb.append(",");
			String fieldName = u.property;
			if (u.calc == '\0') {
				sb.append(fieldName).append("=?");
			} else {
				sb.append(fieldName).append("=").append(fieldName).append(u.calc).append('?');
			}
			params.add(u.value);
			u = u.next;
		}
	}
}
