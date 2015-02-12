package com.icitic.core.db.dao.object;

import java.util.List;

import com.icitic.core.cache.Cache;
import com.icitic.core.cache.CacheLoader;
import com.icitic.core.db.dao.Condition;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.db.dao.PageData;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.SqlBuilder;
import com.icitic.core.db.dao.Update;
import com.icitic.core.model.object.IIdObject;
import com.icitic.core.util.Utils;

public abstract class IdCacheDao<I, T extends IIdObject<I>> extends CacheObjectDao<I, T> {

	protected Cache<I, T> cache;

	protected IdCacheDao(Class<T> clazz) {
		super(clazz);
	}

	@Override
	protected void createCache() {
		CacheLoader<I, T> loader = new CacheLoader<I, T>() {
			@Override
			public T load(I key) {
				return IdCacheDao.super.fetch(key);
			}
		};
		cache = cacheManager.createCache(getCacheName(), loader, getCacheConfig());
	}

	/**
	 * 返回数据库中的全部对象
	 * 
	 * @return
	 */
	@Override
	public List<T> getAll(String orderBy) {
		return queryAndTran(null, orderBy, null);
	}

	/**
	 * 分页地返回数据库中的全部对象
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Override
	public PageData<T> getAll(String orderBy, int pageNo, int pageSize) {
		Pager pager = new Pager(pageNo, pageSize);
		return pageQueryAndTran(null, orderBy, pager);
	}

	/**
	 * 返回指定ID的对象
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public T fetch(I id) {
		return cache.get(id);
	}

	/**
	 * 查询一组对象
	 * 
	 * @param cnd
	 *            查询条件
	 * @param orderBy
	 *            排序
	 * @return
	 */
	@Override
	public List<T> query(Condition cnd, String orderBy) {
		return queryAndTran(cnd, orderBy, null);
	}

	/**
	 * 分页查询一组对象
	 * 
	 * @param cnd
	 *            查询条件
	 * @param orderBy
	 *            排序
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Override
	public PageData<T> query(Condition cnd, String orderBy, int pageNo, int pageSize) {
		Pager pager = new Pager(pageNo, pageSize);
		return pageQueryAndTran(cnd, orderBy, pager);
	}

	/**
	 * 更新一个对象
	 * 
	 * @param obj
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void afterUpdate(NeoBean obj) {
		cache.remove((I) obj.getObject("id"));
	}

	/**
	 * 按id更新一条记录
	 * 
	 * @param update
	 * @param id
	 * @return
	 */
	@Override
	public int update(I id, Update update) {
		int result = super.update(id, update);
		cache.remove(id);
		return result;
	}

	@Override
	protected void afterCndUpdate(int rows) {
		if (rows > 0)
			cache.removeAll();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void afterDelete(NeoBean neo) {
		cache.remove((I) neo.getObject("id"));
	}

	@Override
	protected void afterCndDelete(int rows) {
		if (rows > 0)
			cache.removeAll();
	}

	/**
	 * 按条件查询，仅查ID，然后从cache中获取对象
	 * 
	 * @param cnd
	 * @param orderBy
	 * @param pager
	 * @return
	 */
	protected List<T> queryAndTran(Condition cnd, String orderBy, Pager pager) {
		List<I> ids = dao.queryForList(SqlBuilder.select("id").from(getEntityName()).where(cnd).orderBy(orderBy),
				idClazz, pager);
		return Utils.transform(ids, cache);
	}

	/**
	 * 按条件查询，仅查ID，然后从cache中获取对象
	 * 
	 * @param cnd
	 * @param orderBy
	 * @param pager
	 * @return
	 */
	protected PageData<T> pageQueryAndTran(Condition cnd, String orderBy, Pager pager) {
		int count = dao.count(getEntityName(), cnd);
		if (count == 0)
			return new PageData<T>();
		else {
			List<T> list = queryAndTran(cnd, orderBy, pager);
			return new PageData<T>(count, list);
		}
	}

}
