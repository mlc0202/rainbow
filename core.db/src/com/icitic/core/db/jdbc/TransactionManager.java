package com.icitic.core.db.jdbc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.common.base.Throwables;

/**
 * 简单的事务管理器，不支持嵌套事务
 * 
 * @author Administrator
 * 
 */
public class TransactionManager {

	private ThreadLocal<Transaction> trans = new ThreadLocal<Transaction>();

	public TransactionManager() {
	}

	/**
	 * @return 当前线程的事务，如果没有事务，返回 null
	 */
	public Transaction get() {
		return trans.get();
	}

	public Transaction beginTransaction(int level) {
		Transaction tran = trans.get();
		if (tran == null) {
			tran = new Transaction();
			tran.setLevel(level);
			trans.set(tran);
		}
		tran.beginNestTranscation();
		return tran;
	}

	public void beginTransaction() {
		beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
	}

	@Deprecated
	public void commit() throws SQLException {
		Transaction tran = trans.get();
		checkNotNull(tran, "extra commit call, no transaction founded");
		tran.commit();
		if (tran.getCount() == 0)
			trans.set(null);
	}

	public void rollback() {
		Transaction tran = trans.get();
		checkNotNull(tran, "extra rollback call, no transaction founded");
		tran.rollback();
		if (tran.getCount() == 0)
			trans.set(null);
	}

	public void transaction(int level, Atom atom) {
		Transaction tran = beginTransaction(level);
		try {
			atom.run();
			tran.commit();
			if (tran.getCount() == 0)
				trans.set(null);
		} catch (Throwable e) {
			tran.rollback();
			if (tran.getCount() == 0)
				trans.set(null);
			atom.exception = e;
			if (atom.onError())
				throw Throwables.propagate(e);
		}
	}
}
