package com.icitic.core.db.dao;

import java.util.List;

import com.icitic.core.db.dao.function.Function;
import com.icitic.core.db.model.Entity;

public abstract class Condition {

	/**
	 * 添加一个 and的子条件
	 * 
	 * @param cnd
	 * @return
	 */
	public abstract Condition and(Condition cnd);

	/**
	 * 添加一个or的子条件
	 * 
	 * @param cnd
	 * @return
	 */
	public abstract Condition or(Condition cnd);

	public boolean isEmpty() {
		return false;
	}

	public Condition and(String property, Operator op, Object param) {
		return and(new SimpleCondition(property, op, param));
	}

	public Condition and(String property, Object param) {
		return and(property, Operator.Equal, param);
	}

	public Condition and(Function function, Operator op, Object param) {
		return and(new SimpleCondition(function, op, param));
	}

	public Condition or(String property, Operator op, Object param) {
		return or(new SimpleCondition(property, op, param));
	}

	public Condition or(String property, Object param) {
		return or(property, Operator.Equal, param);
	}

	public Condition or(Function function, Operator op, Object param) {
		return or(new SimpleCondition(function, op, param));
	}

	public abstract void toSql(Entity entity, Dao dao, StringBuilder sb, List<Object> params);

	public abstract void toSqlRaw(Dao dao, StringBuilder sb, List<Object> params);

	/**
	 * 建一个简单的条件
	 * 
	 * @param property
	 * @param op
	 * @param param
	 * @return
	 */
	public static Condition make(String property, Operator op, Object param) {
		return new SimpleCondition(property, op, param);
	}

	public static Condition make(String property, Object param) {
		return make(property, Operator.Equal, param);
	}

	public static Condition make(Function function, Operator op, Object param) {
		return new SimpleCondition(function, op, param);
	}

	/**
	 * 建一个空的条件
	 * 
	 * @return
	 */
	public static Condition make() {
		return EmptyCondition.INSTANCE;
	}

}