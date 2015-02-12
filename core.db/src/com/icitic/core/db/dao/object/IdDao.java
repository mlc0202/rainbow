package com.icitic.core.db.dao.object;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.icitic.core.db.dao.Condition;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.DaoUtils;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.db.dao.NeoBeanObjectMapper;
import com.icitic.core.db.dao.Operator;
import com.icitic.core.db.dao.PageData;
import com.icitic.core.db.dao.Pager;
import com.icitic.core.db.dao.SqlBuilder;
import com.icitic.core.db.dao.Update;
import com.icitic.core.db.dao.converter.Converter;
import com.icitic.core.db.incrementer.Incrementer;
import com.icitic.core.db.incrementer.LongIncrementer;
import com.icitic.core.db.incrementer.MaxIdIncrementer;
import com.icitic.core.db.internal.ObjectNameRule;
import com.icitic.core.db.jdbc.Atom;
import com.icitic.core.db.jdbc.AtomEx;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.object.ObjectManager;
import com.icitic.core.db.object.ObjectType;
import com.icitic.core.db.object.ObjectTypeAdapter;
import com.icitic.core.extension.ExtensionRegistry;
import com.icitic.core.model.IAdaptable;
import com.icitic.core.model.exception.DuplicateCodeException;
import com.icitic.core.model.exception.DuplicateNameException;
import com.icitic.core.model.object.ICodeObject;
import com.icitic.core.model.object.IIdObject;
import com.icitic.core.model.object.INameObject;
import com.icitic.core.model.object.IdObject;
import com.icitic.core.util.ioc.InitializingBean;
import com.icitic.core.util.ioc.Inject;

/**
 * 封装一个具体对象的数据库操作类，该对象实现IIdObject接口。
 * 
 * 派生类必须由Context容器管理
 * 
 * @author lijinghui
 * 
 * @param <I>
 * @param <T>
 */
public class IdDao<I, T extends IIdObject<I>> implements Function<I, T>, IAdaptable, InitializingBean {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Dao dao;

	protected Class<T> clazz;

	protected Class<I> idClazz;

	private NeoBeanObjectMapper<T> mapper;

	private CURDHelper<I, T> curd;

	private Map<String, Converter> converters;

	/**
	 * ID生成器
	 */
	protected Incrementer incrementer;

	/**
	 * 对象管理器
	 */
	protected ObjectManager objectManager;

	/**
	 * 名字翻译规则
	 */
	protected List<ObjectNameRule> nameRules;

	/**
	 * 级联删除子对象
	 */
	protected List<SubEntity> subEntities = ImmutableList.of();

	@Inject
	public void setDao(Dao dao) {
		this.dao = dao;
	}

	public Dao getDao() {
		return dao;
	}

	public void setIncrementer(Incrementer incrementer) {
		this.incrementer = incrementer;
	}

	public Incrementer getIncrementer() {
		return incrementer;
	}

	/**
	 * 如果是代码对象，是否自动生成代码
	 * 
	 * @return
	 */
	protected boolean genCode() {
		return false;
	}

	public final void addCurd(CURDHelper<I, T> curd) {
		if (this.curd == null)
			this.curd = curd;
		else
			this.curd = this.curd.add(curd);
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
	protected IdDao(Class<T> clazz) {
		setClazz(clazz);
	}

	/**
	 * 构造函数，使用默认的Incrementer
	 * 
	 * @param dao
	 * @param clazz
	 *            对象类
	 */
	public IdDao(Dao dao, Class<T> clazz) {
		setDao(dao);
		setClazz(clazz);
		initMapper();
		setIncrementer(createDefaultIncrementer());
	}

	/**
	 * 构造函数，使用指定的Incrementer或传入null不使用
	 * 
	 * @param dao
	 * @param clazz
	 *            对象类
	 */
	public IdDao(Dao dao, Class<T> clazz, Incrementer incrementer) {
		setDao(dao);
		setClazz(clazz);
		initMapper();
		setIncrementer(incrementer);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initMapper();
		if (incrementer == null)
			incrementer = createIncrementer();
	}

	private void initMapper() {
		mapper = new NeoBeanObjectMapper<T>(getEntity(), clazz, converters);
	}

	@SuppressWarnings("unchecked")
	private void setClazz(Class<T> clazz) {
		this.clazz = clazz;
		try {
			Type type = clazz;
			Class<?> tmpClass;
			do {
				tmpClass = (Class<?>) type;
				type = tmpClass.getGenericSuperclass();
			} while (!(type instanceof ParameterizedType));
			ParameterizedType pt = (ParameterizedType) type;
			idClazz = (Class<I>) pt.getActualTypeArguments()[0];
		} catch (Exception e) {
			Throwables.propagate(e);
		}
		checkNotNull(idClazz, "cannot retrieve id class of [%s]", clazz.getSimpleName());
	}

	/**
	 * 添加一个级联删除子对象
	 * 
	 * @param name
	 * @param property
	 * @return
	 */
	public IdDao<I, T> addSubEntity(String name, String property) {
		if (subEntities.isEmpty())
			subEntities = Lists.newLinkedList();
		subEntities.add(new SubEntity(name, property));
		return this;
	}

	/**
	 * 创建对象id自增器。默认根据id类型会自动选择对应的缺省自增器。 派生类重载此函数可以返回自己指定的自增器或返回空表示不需要。
	 * 
	 * @return
	 */
	protected Incrementer createIncrementer() {
		return createDefaultIncrementer();
	}

	/**
	 * 创建缺省的序号发生器
	 * 
	 * @return
	 */
	protected final Incrementer createDefaultIncrementer() {
		if (idClazz == Integer.class)
			return new MaxIdIncrementer(dao, getEntityName());
		else
			return new LongIncrementer();
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
		if (ICodeObject.class.isAssignableFrom(clazz))
			return "code";
		if (INameObject.class.isAssignableFrom(clazz))
			return "name";
		return "id";
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
	 * 返回指定ID的对象
	 * 
	 * @param id
	 * @return
	 */
	public T fetch(I id) {
		return fetch(Condition.make("id", id));
	}

	/**
	 * 返回指定条件的对象
	 * 
	 * @param cnd
	 * @return
	 */
	public T fetch(Condition cnd) {
		T result = dao.queryForObject(SqlBuilder.select().from(getEntityName()).where(cnd), mapper);
		if (result != null) {
			if (curd != null)
				result = curd.fetch(dao, result);
			decorateItem(result);
		}
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
		if (result.isEmpty())
			return result;
		if (curd != null)
			curd.afterQuery(dao, result);
		return decorateList(result);
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
	@SuppressWarnings("unchecked")
	public T insert(final T obj) {
		final NeoBean neo = dao.newNeoBean(getEntityName());
		neo.init(obj, converters);
		dao.transaction(new Atom() {
			@Override
			public void run() {
				if (incrementer != null) {
					Object id;
					if (idClazz == Integer.class)
						id = (Integer) incrementer.nextIntValue();
					else if (idClazz == Long.class)
						id = (Long) incrementer.nextLongValue();
					else
						id = incrementer.nextStringValue();
					neo.setObject("id", id);
					((IdObject<I>) obj).setId((I) id);
				}
				if (obj instanceof ICodeObject && genCode()) {
					CodeGenerator cg = ExtensionRegistry.getExtensionObject(CodeGenerator.class, getEntityName());
					String code = cg.getNextCode(obj);
					((ICodeObject) obj).setCode(code);
					neo.setString("code", code);
				}
				if (curd != null) {
					curd.beforeInsert(dao, obj, neo);
				}
				doInsert(obj, neo);
				if (curd != null)
					curd.afterInsert(dao, obj, neo);

			}
		});
		return fetch((I) neo.getObject("id"));
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
		beforeUpdate(obj, neo);
		dao.transaction(new Atom() {
			@Override
			public void run() {
				if (curd != null)
					curd.beforeUpdate(dao, obj, neo);
				doUpdate(neo);
				if (curd != null)
					curd.afterUpdate(dao, obj, neo);
			}
		});
		afterUpdate(neo);
	}

	protected void beforeUpdate(T obj, NeoBean neo) {
	}

	protected void doUpdate(NeoBean neo) {
		dao.update(neo);
	}

	protected void afterUpdate(NeoBean obj) {
	}

	/**
	 * 按id更新一条记录
	 * 
	 * @param id
	 * @param update
	 * @return
	 */
	public int update(I id, Update update) {
		return update(update, Condition.make("id", id));
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
		afterCndUpdate(atom.getResult());
		return atom.getResult();
	}

	protected int doUpdate(final Update update, final Condition cnd) {
		return dao.execSql(SqlBuilder.update(getEntityName()).set(update).where(cnd));
	}

	protected void afterCndUpdate(int rows) {
	}

	/**
	 * 删除一条记录
	 * 
	 * @param id
	 */
	public void delete(final I id) {
		final NeoBean neo = dao.fetch(getEntityName(), id);
		if (neo == null)
			return;
		beforeDelete(neo);
		dao.transaction(new Atom() {
			@Override
			public void run() {
				if (curd != null)
					curd.beforeDelete(dao, id, neo);
				doDelete(id, neo);
				if (curd != null)
					curd.afterDelete(dao, id, neo);
			}
		});
		afterDelete(neo);
	}

	protected void beforeDelete(NeoBean neo) {
	}

	protected void doDelete(I id, NeoBean neo) {
		dao.execSql(SqlBuilder.delete().from(getEntityName()).where("id", id));
		if (!subEntities.isEmpty()) {
			for (SubEntity sub : subEntities) {
				dao.execSql(SqlBuilder.delete().from(sub.getName()).where(sub.getProperty(), id));
			}
		}
	}

	protected void afterDelete(NeoBean neo) {
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
		afterCndDelete(atom.getResult());
		return atom.getResult();
	}

	protected int doDelete(Condition cnd) {
		if (!subEntities.isEmpty()) {
			SqlBuilder idQuery = SqlBuilder.select("id").from(getEntityName()).where(cnd);
			for (SubEntity sub : subEntities) {
				dao.execSql(SqlBuilder.delete().from(sub.getName()).where(sub.getProperty(), Operator.IN, idQuery));
			}
		}
		return dao.execSql(SqlBuilder.delete().from(getEntityName()).where(cnd));
	}

	protected void afterCndDelete(int rows) {
	}

	@Override
	public T apply(I input) {
		return fetch(input);
	}

	/**
	 * 检查名称是否重复
	 * 
	 * @param obj
	 * @throws DuplicateNameException
	 */
	public void checkDuplicateName(T obj) throws DuplicateNameException {
		if (obj instanceof INameObject) {
			String name = ((INameObject) obj).getName();
			DaoUtils.checkDuplicateName(dao, getEntityName(), obj.getId(), name);
		}
	}

	/**
	 * 检查名称是否重复
	 * 
	 * @param obj
	 * @param cnd
	 * @throws DuplicateNameException
	 */
	public void checkDuplicateName(T obj, Condition cnd) throws DuplicateNameException {
		if (obj instanceof INameObject) {
			String name = ((INameObject) obj).getName();
			DaoUtils.checkDuplicateName(dao, getEntityName(), obj.getId(), name, cnd);
		}
	}

	/**
	 * 检查是否有重复的code
	 * 
	 * @param obj
	 * @throws DuplicateCodeException
	 */
	public void checkDuplicateCode(T obj) throws DuplicateCodeException {
		if (obj instanceof ICodeObject) {
			String code = ((ICodeObject) obj).getCode();
			DaoUtils.checkDuplicateCode(dao, getEntityName(), obj.getId(), code);
		}
	}

	/**
	 * 检查是否有重复的code
	 * 
	 * @param obj
	 * @param cnd
	 * @throws DuplicateCodeException
	 */
	public void checkDuplicateCode(T obj, Condition cnd) throws DuplicateCodeException {
		if (obj instanceof ICodeObject) {
			String code = ((ICodeObject) obj).getCode();
			DaoUtils.checkDuplicateCode(dao, getEntityName(), obj.getId(), code, cnd);
		}
	}

	@Override
	public Object getAdapter(Class<?> adapter) {
		if (adapter == ObjectType.class) {
			return getObjectType();
		}
		return null;
	}

	/**
	 * 返回ObjectType适配对象
	 * 
	 * @return
	 */
	protected ObjectType getObjectType() {
		if (INameObject.class.isAssignableFrom(clazz)) {
			return new ObjectTypeAdapter() {
				@Override
				public String getName() {
					return getEntityName();
				}

				@SuppressWarnings("unchecked")
				@Override
				public String getObjectName(Object key) {
					T obj = fetch((I) key);
					return ((INameObject) obj).getName();
				}

			};
		}
		return null;
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
