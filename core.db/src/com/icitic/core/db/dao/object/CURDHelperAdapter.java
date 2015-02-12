package com.icitic.core.db.dao.object;

import java.util.List;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.model.object.IIdObject;

public class CURDHelperAdapter<I, T extends IIdObject<I>> implements CURDHelper<I, T> {

	@Override
	public void beforeInsert(Dao dao, T obj, NeoBean neo) {
	}

	@Override
	public void afterInsert(Dao dao, T obj, NeoBean neo) {
	}

	@Override
	public void beforeUpdate(Dao dao, T obj, NeoBean neo) {
	}

	@Override
	public void afterUpdate(Dao dao, T obj, NeoBean neo) {
	}

	@Override
	public void beforeDelete(Dao dao, I id, NeoBean neo) {
	}

	@Override
	public void afterDelete(Dao dao, I id, NeoBean neo) {
	}

	@Override
	public T fetch(Dao dao, T obj) {
		return obj;
	}

	@Override
	public void afterQuery(Dao dao, List<T> list) {
	}

	@Override
	public CURDHelper<I, T> add(CURDHelper<I, T> curd) {
		if (curd == null)
			return this;
		ComboCURD<I, T> combo = new ComboCURD<I, T>(this);
		combo.add(curd);
		return combo;
	}

}
