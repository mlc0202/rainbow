package com.icitic.core.db.dao;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.icitic.core.model.exception.DuplicateCodeException;
import com.icitic.core.model.exception.DuplicateNameException;
import com.icitic.core.util.Utils;

public abstract class DaoUtils {

	/**
	 * 检查名称是否重复
	 * 
	 * @param obj
	 * @throws DuplicateNameException
	 */
	public static void checkDuplicateName(Dao dao, String entityName, Object id, String name)
			throws DuplicateNameException {
		checkDuplicateName(dao, entityName, name, Condition.make("id", Operator.NotEqual, id).and("name", name));
	}

	/**
	 * 检查名称是否重复
	 * 
	 * @param obj
	 * @throws DuplicateNameException
	 */
	public static void checkDuplicateName(Dao dao, String entityName, Object id, String name, Condition cnd)
			throws DuplicateNameException {
		checkDuplicateName(dao, entityName, name, Condition.make("id", Operator.NotEqual, id).and("name", name)
				.and(cnd));
	}

	/**
	 * 检查名称是否重复
	 * 
	 * @param obj
	 * @param cnd
	 * @throws DuplicateNameException
	 */
	public static void checkDuplicateName(Dao dao, String entityName, String name, Condition cnd)
			throws DuplicateNameException {
		if (dao.count(entityName, cnd) > 0)
			throw new DuplicateNameException(name);
	}

	/**
	 * 检查代码是否重复
	 * 
	 * @param obj
	 * @throws DuplicateCodeException
	 */
	public static void checkDuplicateCode(Dao dao, String entityName, Object id, String code)
			throws DuplicateCodeException {
		checkDuplicateCode(dao, entityName, code, Condition.make("id", Operator.NotEqual, id).and("code", code));
	}

	/**
	 * 检查代码是否重复
	 * 
	 * @param obj
	 * @throws DuplicateCodeException
	 */
	public static void checkDuplicateCode(Dao dao, String entityName, Object id, String code, Condition cnd)
			throws DuplicateCodeException {
		checkDuplicateCode(dao, entityName, code, Condition.make("id", Operator.NotEqual, id).and("code", code)
				.and(cnd));
	}

	/**
	 * 检查代码是否重复
	 * 
	 * @param obj
	 * @param cnd
	 *            默认为空
	 * @throws DuplicateCodeException
	 */
	public static void checkDuplicateCode(Dao dao, String entityName, String code, Condition cnd)
			throws DuplicateCodeException {
		if (dao.count(entityName, cnd) > 0)
			throw new DuplicateCodeException(code);
	}

	/**
	 * 增加一个nest对象时，计算左右值
	 * 
	 * @param dao
	 * @param neo
	 * @param cnd
	 * @param rootId
	 */
	public static void calcLeftRight(Dao dao, NeoBean neo, Condition cnd, Object rootId) {
		String entityName = neo.getEntity().getName();
		int right;
		if (Objects.equal(neo.getObject("pid"), rootId)) {
			right = dao.queryForInt(SqlBuilder.select("max(right)").from(entityName).where(cnd));
			right++;
		} else {
			right = dao.queryForInt(SqlBuilder.select("right").from(entityName).where("id", neo.getObject("pid")));
			if (right <= 0) {
				String err = String.format("id[%s] has a wrong parent id[%s]", neo.getObject("id"),
						neo.getObject("pid"));
				throw new IllegalArgumentException(err);
			}
			dao.execSql(SqlBuilder.update(entityName).set("left", '+', 2).set("right", '+', 2).where(cnd)
					.and("left", Operator.Greater, right));
			dao.execSql(SqlBuilder.update(entityName).set("right", '+', 2).where(cnd).and("left", Operator.Less, right)
					.and("right", Operator.GreaterEqual, right));
		}
		neo.setInt("left", right);
		neo.setInt("right", right + 1);
	}

	/**
	 * 返回求指定对象所有下级id的sql，需要对象有正确的Left，Right值
	 * 
	 * @param dao
	 * @param obj
	 *            查询对象，已经有正确的Left，Right值了
	 * @param cnd
	 *            额外条件，（如果是森林，可以是树标识条件）
	 * @param withSelf
	 *            是否包含指定对象
	 * @return
	 */
	public static SqlBuilder subIdSql(String entityName, Condition cnd, int left, int right, boolean withSelf) {
		SqlBuilder sb = SqlBuilder.select("id").from(entityName).where(cnd);
		if (withSelf) {
			return sb.and("left", Operator.GreaterEqual, left).and("left", Operator.LessEqual, right);
		} else {
			return sb.and("left", Operator.Greater, left).and("left", Operator.Less, right);
		}
	}

	/**
	 * 返回获得一组节点所有下级id的sql
	 * @param dao
	 * @param entityName
	 * @param ids
	 * @return
	 */
	public static <I> Sql subIdSql(String entityName, List<I> ids) {
		checkArgument(!Utils.isNullOrEmpty(ids));
		StringBuilder sb = new StringBuilder();
		sb.append("select A.ID from ").append(entityName).append(" A,").append(entityName).append(" B")
				.append(" where A.LFT>=B.LFT and A.LFT<=B.RGT and B.ID in (");
		Utils.repeat(sb, "?", ',', ids.size());
		sb.append(")");
		return new Sql(sb.toString(), Collections.<Object>unmodifiableList(ids));
	}
}
