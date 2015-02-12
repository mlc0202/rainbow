package com.icitic.core.db.dao;

import com.google.common.base.Function;
import com.icitic.core.util.Utils;

public class OrderBy {

	private String property;

	private boolean desc;

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	public OrderBy() {
	}

	public OrderBy(String property, boolean desc) {
		this.property = property;
		this.desc = desc;
	}

	public static Function<String, OrderBy> parse = new Function<String, OrderBy>() {
		@Override
		public OrderBy apply(String input) {
			String[] strs = Utils.split(input, ' ');
			if (strs.length == 1)
				return new OrderBy(input, false);
			if ("DESC".equalsIgnoreCase(strs[1]))
				return new OrderBy(strs[0], true);
			if ("ASC".equalsIgnoreCase(strs[1]))
				return new OrderBy(strs[0], false);
			throw new IllegalArgumentException("invalid order by str: " + input);
		}
	};

	@Override
	public String toString() {
		if (desc)
			return property + " DESC";
		return property;
	}
}
