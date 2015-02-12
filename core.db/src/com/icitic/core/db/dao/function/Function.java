package com.icitic.core.db.dao.function;

import com.icitic.core.db.database.Dialect;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;

public interface Function {

	void toSql(Dialect dialect, Entity entity, StringBuilder sb);

	ColumnType getType();

}
