package com.icitic.core.db.jdbc;

import com.google.common.base.Throwables;
import com.icitic.core.model.exception.AppException;

/**
 * 事务执行的原子对象
 * 
 * @author lijinghui
 * 
 * @param <T>
 */
public abstract class Atom {

	protected Throwable exception;

	/**
	 * 事务执行代码
	 * 
	 * @return
	 */
	public abstract void run() throws Throwable;

	/**
	 * 发生异常的处理代码,如果事物是嵌套的, 代码中对数据库的改动可能会被回滚
	 * 
	 * @return true 表示继续处理，false 表示已处理完毕，无需继续处理
	 */
	public boolean onError() {
		return true;
	}

	/**
	 * 检查是否发生了AppException
	 * 
	 * @throws AppException
	 */
	public void checkAppException() throws AppException {
		if (exception != null) {
			if (exception instanceof AppException)
				throw (AppException) exception;
			Throwables.propagate(exception);
		}
	}

	public Throwable getException() {
		return exception;
	}

	public boolean failed() {
		return exception != null;
	}
}
