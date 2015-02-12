package com.icitic.core.db.dao.function;

import com.icitic.core.db.database.Dialect;
import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;

public class StringFunc implements Function {

	private String property;

	private String name;

	private StringFunc(String name, String property) {
		this.name = name;
		this.property = property;
	}

	@Override
	public void toSql(Dialect dialect, Entity entity, StringBuilder sb) {
		Column column = entity.getColumn(property);
		sb.append(name).append("(").append(column == null ? property : column.getDbName()).append(")");
	}

	@Override
	public ColumnType getType() {
		return ColumnType.VARCHAR;
	}

	public static Function upper(String property) {
		return new StringFunc("UPPER", property);
	}

	public static Function lower(String property) {
		return new StringFunc("LOWER", property);
	}
}
