package com.icitic.core.db.dao.object;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.icitic.core.db.dao.Condition;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.db.dao.NeoBeanObjectMapper;
import com.icitic.core.db.dao.PageData;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.SqlBuilder;
import com.icitic.core.db.dao.Update;
import com.icitic.core.db.dao.converter.Converter;
import com.icitic.core.db.internal.ObjectNameRule;
import com.icitic.core.db.jdbc.Atom;
import com.icitic.core.db.jdbc.AtomEx;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.object.ObjectManager;
import com.icitic.core.util.Utils;
import com.icitic.core.util.ioc.InitializingBean;
import com.icitic.core.util.ioc.Inject;

/**
 * 封装一个具体对象的数据库操作类
 * 
 * 派生类必须由Context容器管理
 * 
 * @author lijinghui
 * 
 * @param <I>
 * @param <T>
 */
public class ObjectDao<T> implements InitializingBean {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Dao dao;

	protected Class<T> clazz;

	private NeoBeanObjectMapper<T> mapper;

	private Map<String, Converter> converters;

	private String defaultOrderBy;

	/**
	 * 对象管理器
	 */
	protected ObjectManager objectManager;

	/**
	 * 名字翻译规则
	 */
	protected List<ObjectNameRule> nameRules;

	@Inject
	public void setDao(Dao dao) {
		this.dao = dao;
	}

	public Dao getDao() {
		return dao;
	}

	/**
	 * 添加一个属性转换器
	 * 
	 * @param property
	 * @param converter
	 */
	public final void addConverter(Converter converter, String... properties) {
		if (converters == null) {
			converters = Maps.newHashMap();
			if (mapper != null)
				mapper.setConverters(converters);
		}
		for (String p : properties)
			converters.put(p, converter);
	}

	@Inject
	public void setObjectManager(ObjectManager objectManager) {
		this.objectManager = objectManager;
		if (objectManager != null) {
			nameRules = objectManager.getObjectNameRule(clazz);
			if (nameRules.isEmpty())
				nameRules = null;
		}
	}

	/**
	 * 构造函数,用于作为Bean在容器中生成
	 * 
	 * @param clazz
	 */
	protected ObjectDao(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Entity entity = getEntity();
		mapper = new NeoBeanObjectMapper<T>(entity, clazz, converters);
		if (entity.getKeyCount() > 0) {
			List<String> keys = Utils.transform(getEntity().getKeys(), Utils.toNameFunction);
			defaultOrderBy = Joiner.on(',').join(keys);
		}
	}

	/**
	 * 实体对象名
	 * 
	 * @return
	 */
	protected String getEntityName() {
		return clazz.getSimpleName();
	}

	/**
	 * 实体对象配置
	 * 
	 * @return
	 */
	protected Entity getEntity() {
		return dao.getEntity(getEntityName());
	}

	/**
	 * 返回默认
	 * 
	 * @return
	 */
	public String getDefaultOrderBy() {
		return defaultOrderBy;
	}

	/**
	 * 返回数据库中的全部对象
	 * 
	 * @return
	 */
	public List<T> getAll() {
		return query(null, getDefaultOrderBy());
	}

	/**
	 * 返回数据库中的全部对象
	 * 
	 * @return
	 */
	public List<T> getAll(String orderBy) {
		return query(null, orderBy);
	}

	/**
	 * 分页地返回数据库中的全部对象
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public PageData<T> getAll(int pageNo, int pageSize) {
		return query(null, getDefaultOrderBy(), pageNo, pageSize);
	}

	/**
	 * 分页地返回数据库中的全部对象
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public PageData<T> getAll(String orderBy, int pageNo, int pageSize) {
		return query(null, orderBy, pageNo, pageSize);
	}

	/**
	 * 返回指定主键的对象
	 * 
	 * @param id
	 * @return
	 */
	public T fetch(Object... keyValues) {
		return fetch(dao.keyCnd(getEntityName(), keyValues));
	}

	/**
	 * 返回指定条件的对象
	 * 
	 * @param cnd
	 * @return
	 */
	public T fetch(Condition cnd) {
		T result = dao.queryForObject(SqlBuilder.select().from(getEntityName()).where(cnd), mapper);
		if (result != null)
			decorateItem(result);
		return result;
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
	public List<T> query(Condition cnd) {
		return query(cnd, getDefaultOrderBy());
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
	public List<T> query(Condition cnd, String orderBy) {
		List<T> result = dao
				.queryForList(SqlBuilder.select().from(getEntityName()).where(cnd).orderBy(orderBy), mapper);
		return afterQuery(result);
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
	public PageData<T> query(Condition cnd, int pageNo, int pageSize) {
		return query(cnd, getDefaultOrderBy(), pageNo, pageSize);
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
	public PageData<T> query(Condition cnd, String orderBy, int pageNo, int pageSize) {
		Pager pager = new Pager(pageNo, pageSize);
		PageData<T> result = dao.pageQuery(SqlBuilder.select().from(getEntityName()).where(cnd).orderBy(orderBy),
				mapper, pager);
		result.setData(afterQuery(result.getData()));
		return result;
	}

	private List<T> afterQuery(List<T> result) {
		return Utils.isNullOrEmpty(result) ? result : decorateList(result);
	}

	/**
	 * 返回数据库中的总数
	 * 
	 * @return
	 */
	public int count() {
		return dao.count(getEntityName());
	}

	/**
	 * 返回符合条件的记录数
	 * 
	 * @param cnd
	 * @return
	 */
	public int count(Condition cnd) {
		return dao.count(getEntityName(), cnd);
	}

	/**
	 * 插入一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public void insert(final T obj) {
		final NeoBean neo = dao.newNeoBean(getEntityName());
		neo.init(obj, converters);
		dao.transaction(new Atom() {
			@Override
			public void run() {
				doInsert(obj, neo);
			}
		});
	}

	protected void doInsert(T obj, NeoBean neo) {
		dao.insert(neo);
	}

	/**
	 * 更新一个对象
	 * 
	 * @param obj
	 */
	public void update(final T obj) {
		final NeoBean neo = dao.newNeoBean(getEntityName());
		neo.init(obj, converters);
		dao.transaction(new Atom() {
			@Override
			public void run() {
				doUpdate(neo);
			}
		});
	}

	protected void doUpdate(NeoBean neo) {
		dao.update(neo);
	}

	/**
	 * 按条件更新对象
	 * 
	 * @param update
	 *            更新内容
	 * @param cnd
	 *            更新条件
	 * @return 更新了的记录数
	 */
	public int update(final Update update, final Condition cnd) {
		AtomEx<Integer> atom = new AtomEx<Integer>() {
			@Override
			public void run() throws Throwable {
				result = doUpdate(update, cnd);
			}
		};
		dao.transaction(atom);
		return atom.getResult();
	}

	protected int doUpdate(final Update update, final Condition cnd) {
		return dao.execSql(SqlBuilder.update(getEntityName()).set(update).where(cnd));
	}

	/**
	 * 删除一条记录
	 * 
	 * @param id
	 */
	public void delete(final Object... keyValues) {
		dao.transaction(new Atom() {
			@Override
			public void run() {
				doDelete(keyValues);
			}
		});
	}

	protected void doDelete(Object[] keyValues) {
		dao.delete(getEntityName(), keyValues);
	}

	/**
	 * 按条件删除对象
	 * 
	 * @param cnd
	 * @return 删除的记录数
	 */
	public int delete(final Condition cnd) {
		AtomEx<Integer> atom = new AtomEx<Integer>() {
			@Override
			public void run() throws Throwable {
				result = doDelete(cnd);
			}
		};
		dao.transaction(atom);
		return atom.getResult();
	}

	protected int doDelete(Condition cnd) {
		return dao.execSql(SqlBuilder.delete().from(getEntityName()).where(cnd));
	}

	protected void decorateItem(T obj) {
		if (nameRules != null)
			objectManager.setName(obj, nameRules);
	}

	protected List<T> decorateList(List<T> list) {
		for (T obj : list)
			decorateItem(obj);
		return list;
	}
}
