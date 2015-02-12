package com.icitic.core.db.dao.object;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.common.collect.Lists;
import com.icitic.core.db.dao.Condition;
import com.icitic.core.db.dao.DaoUtils;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.db.dao.Operator;
import com.icitic.core.db.dao.Sql;
import com.icitic.core.db.dao.SqlBuilder;
import com.icitic.core.db.jdbc.Atom;
import com.icitic.core.model.object.INestObject;
import com.icitic.core.model.object.ITreeObject;
import com.icitic.core.model.object.TreeNode;
import com.icitic.core.util.Utils;
import com.icitic.core.util.tree.Tree;
import com.icitic.core.util.tree.TreeUtils;

/**
 * 树形对象的数据库访问工具类,与TreeCacheAllDao的区别就是单个对象缓存
 * 
 * @author lijinghui
 * 
 * @param <T>
 */
public class TreeCacheDao<I, T extends ITreeObject<I>> extends IdCacheDao<I, T> {

	/**
	 * 森林字段,如果一个数据表中存放的是一个森林,这是用来区分树的字段名
	 */
	protected String forestField = null;

	/**
	 * 给作为Bean的派生类用的
	 * 
	 * @param clazz
	 */
	protected TreeCacheDao(Class<T> clazz) {
		this(clazz, null);
	}

	/**
	 * 给作为Bean的派生类用的
	 * 
	 * @param clazz
	 */
	protected TreeCacheDao(Class<T> clazz, String forestField) {
		super(clazz);
		this.forestField = forestField;
	}

	/**
	 * 是否是森林
	 * 
	 * @return
	 */
	public boolean isForest() {
		return forestField != null;
	}

	private Condition getTreeCnd(NeoBean neo) {
		return isForest() ? Condition.make(forestField, neo.getObject(forestField)) : null;
	}

	/**
	 * 返回树根的父节点id值
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected I rootId() {
		if (Integer.class == idClazz)
			return (I) Integer.valueOf(0);
		if (Long.class == idClazz)
			return (I) Long.valueOf(0);
		return null;
	}

	/**
	 * 当数据库保存的不是森林时调用这个函数获得树
	 * 
	 * @return
	 */
	public Tree<I, T> getTree() {
		checkState(isForest() == false);
		List<T> all = getAll();
		if (Utils.isNullOrEmpty(all))
			return null;
		return new Tree<I, T>(all);
	}

	/**
	 * 当数据库保存的是森林，返回指定的树
	 * 
	 * @param forestValue
	 *            树标记值
	 * @return
	 */
	public Tree<I, T> getTree(Object forestValue) {
		checkNotNull(forestValue);
		List<T> all = query(Condition.make(forestField, forestValue));
		if (Utils.isNullOrEmpty(all))
			return null;
		return new Tree<I, T>(all);
	}

	/**
	 * 返回指定的树枝
	 * 
	 * @param id
	 * @return
	 */
	public TreeNode<T> getBranch(I id) {
		if (INestObject.class.isAssignableFrom(clazz)) {
			NeoBean neo = dao.fetch(getEntityName(), id);
			if (neo == null)
				return null;
			List<T> all = query(Condition.make("left", Operator.GreaterEqual, neo.getInt("left"))
					.and("left", Operator.Less, neo.getInt("right")).and(getTreeCnd(neo)));
			Tree<I, T> tree = new Tree<I, T>(all);
			List<TreeNode<T>> firstLevel = tree.getRoots();
			checkState(firstLevel.size() == 1);
			return firstLevel.get(0);
		} else {
			T obj = fetch(id);
			return getBranch(obj);
		}
	}

	public TreeNode<T> getBranch(T obj) {
		TreeNode<T> node = new TreeNode<T>(obj);
		List<T> list = query(Condition.make("pid", obj.getId()));
		if (!list.isEmpty()) {
			for (T child : list)
				node.addChild(getBranch(child));
		}
		return node;
	}

	/**
	 * 返回某个节点的直接子节点
	 * 
	 * @param pid
	 * @return
	 */
	public List<T> getChildren(I pid) {
		return query(Condition.make("pid", pid));
	}

	/**
	 * 返回某个节点层级
	 * 
	 * @param id
	 * @return
	 */
	public int getLayer(I id) {
		T self = fetch(id);
		checkNotNull(self, "id[%d] not exist", id);
		int level = 1;
		while (!self.getPid().equals(rootId())) {
			self = fetch(self.getPid());
			checkNotNull(self, "(%s) id[%d]'s ancestor [%d] not exist", getEntityName(), id, self.getPid());
			level++;
		}
		return level;
	}

	@Override
	protected void doInsert(T obj, NeoBean neo) {
		if (INestObject.class.isAssignableFrom(clazz)) {
			DaoUtils.calcLeftRight(dao, neo, getTreeCnd(neo), rootId());
		}
		super.doInsert(obj, neo);
		cache.removeAll();
	}

	@Override
	protected void beforeUpdate(T obj, NeoBean neo) {
		if (INestObject.class.isAssignableFrom(clazz)) {
			// 确保左右值不被外面污染
			INestObject<I> old = (INestObject<I>) fetch(obj.getId());
			neo.setInt("left", old.getLeft());
			neo.setInt("right", old.getRight());
		}
	}

	@Override
	protected void doDelete(I id, NeoBean neo) {
		if (INestObject.class.isAssignableFrom(clazz)) {
			Condition cnd = getTreeCnd(neo);
			int left = neo.getInt("left");
			int right = neo.getInt("right");
			int width = neo.getInt("right") - neo.getInt("left");
			if (width == 1) {
				super.doDelete(id, neo);
			} else {
				SqlBuilder subIdSql = DaoUtils.subIdSql(getEntityName(), cnd, left, right, true);
				for (SubEntity sub : subEntities) {
					dao.execSql(SqlBuilder.delete().from(sub.getName()).where(sub.getProperty(), Operator.IN, subIdSql));
				}
				dao.execSql(SqlBuilder.delete().from(getEntityName()).where("id", Operator.IN, subIdSql));
			}
			width++;
			dao.execSql(SqlBuilder.update(getEntityName()).set("right", '-', width).where(cnd)
					.and("right", Operator.Greater, right));
			dao.execSql(SqlBuilder.update(getEntityName()).set("left", '-', width).where(cnd)
					.and("left", Operator.Greater, right));
			cache.removeAll();
		} else {
			TreeNode<T> treeNode = getBranch(id);
			if (treeNode == null)
				return;
			if (treeNode.isLeaf())
				super.doDelete(id, neo);
			else {
				Sql sql = SqlBuilder.delete().from(getEntityName()).where("id", 0).build(dao);
				List<I> ids = Lists.reverse(TreeUtils.getIds(treeNode));
				for (I subId : ids) {
					for (SubEntity sub : subEntities) {
						dao.execSql(SqlBuilder.delete().from(sub.getName()).where(sub.getProperty(), subId));
					}
					dao.execSql(sql.getSql(), subId);
				}
			}
		}
	}

	public void move(final I id, final I newPid) {
		dao.transaction(new Atom() {
			@Override
			public void run() throws Throwable {
				doMove(id, newPid);
			}
		});
		afterMove(id, newPid);
	}

	protected void doMove(I id, I newPid) {
		dao.execSql(SqlBuilder.update(getEntityName()).set("pid", newPid).where("id", id));
		if (INestObject.class.isAssignableFrom(clazz)) {
			// TODO 重新计算所有的左右值
		}
	}

	protected void afterMove(I id, I newPid) {
		if (INestObject.class.isAssignableFrom(clazz))
			cache.removeAll();
		else {
			cache.remove(id);
		}
	}

}
