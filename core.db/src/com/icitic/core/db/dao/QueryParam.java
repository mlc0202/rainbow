package com.icitic.core.db.dao;

@Deprecated
public class QueryParam {

	private int page;

	private int limit;

	private Filter[] filter;

	private Sorter[] sorter;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Filter[] getFilter() {
		return filter;
	}

	public void setFilter(Filter[] filter) {
		this.filter = filter;
	}

	public Sorter[] getSorter() {
		return sorter;
	}

	public void setSorter(Sorter[] sorter) {
		this.sorter = sorter;
	}

	public Pager getPager() {
		return Pager.make(page, limit);
	}
}
