package com.icitic.core.db.dao.object;

import java.util.List;

import com.google.common.collect.Lists;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.model.object.IIdObject;

public class ComboCURD<I, T extends IIdObject<I>> extends CURDHelperAdapter<I, T> {

	List<CURDHelper<I, T>> chain = Lists.newLinkedList();

	public ComboCURD(CURDHelper<I, T> curd) {
		chain.add(curd);
	}

	@Override
	public void beforeInsert(Dao dao, T obj, NeoBean neo) {
		for (CURDHelper<I, T> curd : chain)
			curd.beforeInsert(dao, obj, neo);
	}

	@Override
	public void afterInsert(Dao dao, T obj, NeoBean neo) {
		for (CURDHelper<I, T> curd : chain)
			curd.afterInsert(dao, obj, neo);
	}

	@Override
	public void beforeUpdate(Dao dao, T obj, NeoBean neo) {
		for (CURDHelper<I, T> curd : chain)
			curd.beforeUpdate(dao, obj, neo);
	}

	@Override
	public void afterUpdate(Dao dao, T obj, NeoBean neo) {
		for (CURDHelper<I, T> curd : chain)
			curd.afterUpdate(dao, obj, neo);
	}

	@Override
	public void beforeDelete(Dao dao, I id, NeoBean neo) {
		for (CURDHelper<I, T> curd : chain)
			curd.beforeDelete(dao, id, neo);
	}

	@Override
	public void afterDelete(Dao dao, I id, NeoBean neo) {
		for (CURDHelper<I, T> curd : chain)
			curd.afterDelete(dao, id, neo);
	}

	@Override
	public T fetch(Dao dao, T obj) {
		for (CURDHelper<I, T> curd : chain)
			obj = curd.fetch(dao, obj);
		return obj;
	}

	@Override
	public void afterQuery(Dao dao, List<T> list) {
		for (CURDHelper<I, T> curd : chain)
			curd.afterQuery(dao, list);
	}

	@Override
	public CURDHelper<I, T> add(CURDHelper<I, T> curd) {
		chain.add(curd);
		return this;
	}

}
