package com.icitic.core.db.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.google.common.collect.ImmutableMap;
import com.icitic.core.db.jdbc.DataAccessException;
import com.icitic.core.util.ioc.Bean;
import com.icitic.core.util.ioc.Context;

/**
 * 数据库方言管理器
 * 
 * @author lijinghui
 * 
 */
public final class DialectManager {

	private DialectManager() {
	}

	private static Context context = new Context(ImmutableMap.<String, Bean> builder()
			.put("DB2", Bean.singleton(DB2.class)) //
			.put("ORACLE", Bean.singleton(Oracle.class)) //
			.put("MSSQL", Bean.singleton(SqlServer.class)) //
			.put("H2", Bean.singleton(H2.class)) //
			.put("MySql", Bean.singleton(Mysql.class)) //
			.put("GBASE", Bean.singleton(Gbase.class)) //
			.put("Postgres", Bean.singleton(Postgres.class)).build());

	/**
	 * 根据数据库元数据返回数据库方言对象
	 * 
	 * @param meta
	 * @return
	 * @throws SQLException
	 */
	public static Dialect getDialect(DatabaseMetaData meta) throws SQLException {
		String proName = meta.getDatabaseProductName().toLowerCase();
		if (proName.startsWith("db2")) {
			return context.getBean("DB2", Dialect.class);
		} else if (proName.startsWith("oracle")) {
			return context.getBean("ORACLE", Dialect.class);
		} else if (proName.startsWith("microsoft")) {
			return context.getBean("MSSQL", Dialect.class);
		} else if ("h2".equals(proName)) {
			return context.getBean("H2", Dialect.class);
		} else if (proName.startsWith("mysql")) {
			return context.getBean("MySql", Dialect.class);
		} else if (proName.startsWith("postgres")) {
			return context.getBean("Postgres", Dialect.class);
		} else if (proName.startsWith("gbase")) {
			return context.getBean("GBASE", Dialect.class);
		} else {
			throw new DataAccessException(proName + " not support");
		}
	}

	/**
	 * 返回特定的数据库方言
	 * 
	 * @param database
	 * @return
	 */
	public static Dialect getDialect(String database) {
		return context.getBean(database, Dialect.class);
	}

}
