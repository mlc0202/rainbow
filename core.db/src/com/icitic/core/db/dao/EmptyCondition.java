package com.icitic.core.db.dao;

import java.util.List;

import com.icitic.core.db.model.Entity;

public class EmptyCondition extends Condition {

	public static final Condition INSTANCE = new EmptyCondition();

	@Override
	public Condition and(Condition cnd) {
		if (cnd instanceof ComboCondition)
			return new ComboCondition(cnd);
		return cnd;
	}

	@Override
	public Condition or(Condition cnd) {
		return and(cnd);
	}

	@Override
	public void toSql(Entity entity, Dao dao, StringBuilder sb, List<Object> params) {
	}

	@Override
	public void toSqlRaw(Dao dao, StringBuilder sb, List<Object> params) {
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
