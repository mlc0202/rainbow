package com.icitic.core.db.dao.object;

import java.util.List;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.model.object.IIdObject;

public interface CURDHelper<I, T extends IIdObject<I>> {

	void beforeInsert(Dao dao, T obj, NeoBean neo);

	void afterInsert(Dao dao, T obj, NeoBean neo);

	void beforeUpdate(Dao dao, T obj, NeoBean neo);

	void afterUpdate(Dao dao, T obj, NeoBean neo);

	void beforeDelete(Dao dao, I id, NeoBean neo);

	void afterDelete(Dao dao, I id, NeoBean neo);

	T fetch(Dao dao, T obj);

	void afterQuery(Dao dao, List<T> list);

	/**
	 * 增加一个级联的CURD
	 * 
	 * @param curd
	 * @return
	 */
	CURDHelper<I, T> add(CURDHelper<I, T> curd);
}
