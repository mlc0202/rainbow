package com.icitic.core.db.dao;

/**
 * 查询操作符
 * 
 * @author lijinghui
 * 
 */
public enum Operator {
	Like(" like "),
	NotLike(" not like "),
	Equal("="),
	NotEqual("<>"),
	Greater(">"),
	Less("<"),
	GreaterEqual(">="),
	LessEqual("<="),
	IN(" in "),
	NotIn(" not in ");

	private String symbol;

	Operator(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	/**
	 * 将字符串作为查询操作符解析
	 * 
	 * @param str
	 * @return
	 */
	public static Operator parse(String str) {
		for (Operator op : Operator.class.getEnumConstants()) {
			if (op.getSymbol().equals(str))
				return op;
		}
		return null;
	}

}
