package com.icitic.core.db.dao;

import java.util.List;

import com.icitic.core.util.Utils;

/**
 * 封装了一个Sql的内容对象
 * 
 * @author lijinghui
 * 
 */
public class Sql {

	private String sql;

	private List<Object> params;

	public Sql() {
	}

	public Sql(String sql) {
		this.sql = sql;
	}

	public Sql(String sql, List<Object> params) {
		this.sql = sql;
		this.params = params;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	public Object[] getParamArray() {
		if (Utils.isNullOrEmpty(params))
			return Utils.NULL_ARRAY;
		else
			return params.toArray();
	}

	/**
	 * 取前几行
	 * 
	 * @param dao
	 * @param limit
	 * @return
	 */
	public Sql limit(Dao dao, int limit) {
		if (limit > 0)
			setSql(dao.getDatabaseDialect().wrapLimitSql(getSql(), limit));
		return this;
	}

	/**
	 * 分页
	 * 
	 * @param dao
	 * @param pager
	 * @return
	 */
	public Sql paging(Dao dao, Pager pager) {
		if (pager != null)
			setSql(dao.getDatabaseDialect().wrapPagedSql(getSql(), pager));
		return this;
	}

	/**
	 * 分页
	 * 
	 * @param dao
	 * @param pager
	 * @return
	 */
	public Sql paging(Dao dao, int pageNo, int pageSize) {
		return paging(dao, Pager.make(pageNo, pageSize));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(sql);
		sb.append("\n\r");
		int i = 1;
		if (!Utils.isNullOrEmpty(params)) {
			sb.append("params:\n\r");
			for (Object param : params) {
				sb.append("[").append(i++).append("] ").append(param.toString()).append("\n\r");
			}
		}
		return sb.toString();
	}

}
