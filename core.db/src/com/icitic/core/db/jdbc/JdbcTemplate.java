package com.icitic.core.db.jdbc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * This is the central class in the JDBC core package.</b> It simplifies the use
 * of JDBC and helps to avoid common errors.
 */
public class JdbcTemplate implements JdbcOperations {

	/** Logger available to subclasses */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private DataSource dataSource;

	private TransactionManager transactionManager = new TransactionManager();

	/**
	 * Return the DataSource used by this template.
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections
	 * from.
	 * <p>
	 * Note: This will not trigger initialization of the exception translator.
	 * 
	 * @param dataSource
	 *            the JDBC DataSource to obtain connections from
	 */
	public JdbcTemplate(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 获取连接
	 * 
	 */
	public Connection getConnection() {
		try {
			Transaction trans = transactionManager.get();
			Connection conn = null;
			if (trans != null)
				conn = trans.getConnection(dataSource);
			else {
				conn = dataSource.getConnection();
				conn.setAutoCommit(true);
			}
			return conn;
		} catch (Throwable e) {
			throw new DataAccessException("Could not get JDBC Connection", e);
		}
	}

	/**
	 * 释放连接
	 * 
	 * @param ch
	 *            连接持有者
	 */
	public void releaseConnection(Connection con) {
		if (con != null)
			try {
				Transaction trans = transactionManager.get();
				if (trans == null)
					con.close();
			} catch (Throwable e) {
				logger.warn("close connection failed", e);
			}
	}

	public <T> T execute(StatementCallback<T> action) throws DataAccessException {
		Preconditions.checkNotNull(action, "Callback object must not be null");

		Connection con = getConnection();
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			T result = action.doInStatement(stmt);
			handleWarnings(stmt);
			return result;
		} catch (SQLException ex) {
			JdbcUtils.closeStatement(stmt);
			stmt = null;
			releaseConnection(con);
			con = null;
			throw new DataAccessException(getSql(action) + "\r" + ex.toString(), ex);
		} finally {
			JdbcUtils.closeStatement(stmt);
			releaseConnection(con);
		}
	}

	public void execute(final String sql) throws DataAccessException {
		logger.debug("Executing SQL statement [{}]", sql);
		class ExecuteStatementCallback implements StatementCallback<Object>, SqlProvider {
			public Object doInStatement(Statement stmt) throws SQLException {
				stmt.execute(sql);
				return null;
			}

			public String getSql() {
				return sql;
			}
		}
		execute(new ExecuteStatementCallback());
	}

	public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws DataAccessException {
		Preconditions.checkNotNull(sql, "SQL must not be null");
		Preconditions.checkNotNull(rse, "ResultSetExtractor must not be null");
		if (logger.isDebugEnabled() && JdbcUtils.filterSqlSrc()) {
			logger.debug("Executing SQL query [{}]", sql);
		}
		class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
			public T doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					return rse.extractData(rs);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
			}

			public String getSql() {
				return sql;
			}
		}
		return execute(new QueryStatementCallback());
	}

	public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
		query(sql, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, new RowMapperResultSetExtractor<T>(rowMapper));
	}

	public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		List<T> results = query(sql, rowMapper);
		return requiredSingleResult(results);
	}

	public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
		return queryForObject(sql, new SingleColumnRowMapper<T>(requiredType));
	}

	public long queryForLong(String sql) throws DataAccessException {
		Number number = queryForObject(sql, Long.class);
		return (number != null ? number.longValue() : 0);
	}

	public int queryForInt(String sql) throws DataAccessException {
		Number number = queryForObject(sql, Integer.class);
		return (number != null ? number.intValue() : 0);
	}

	public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException {
		return query(sql, new SingleColumnRowMapper<T>(elementType));
	}

	public int update(final String sql) throws DataAccessException {
		Preconditions.checkNotNull(sql, "SQL must not be null");
		if (logger.isDebugEnabled() && JdbcUtils.filterSqlSrc()) {
			logger.debug("Executing SQL update [{}]", sql);
		}
		class UpdateStatementCallback implements StatementCallback<Integer>, SqlProvider {
			public Integer doInStatement(Statement stmt) throws SQLException {
				int rows = stmt.executeUpdate(sql);
				if (logger.isDebugEnabled() && JdbcUtils.filterSqlSrc()) {
					logger.debug("SQL update affected {} rows", rows);
				}
				return rows;
			}

			public String getSql() {
				return sql;
			}
		}
		return execute(new UpdateStatementCallback());
	}

	public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
		Preconditions.checkNotNull(psc, "PreparedStatementCreator must not be null");
		Preconditions.checkNotNull(action, "Callback object must not be null");
		if (logger.isDebugEnabled() && JdbcUtils.filterSqlSrc()) {
			logger.debug("Executing prepared SQL statement [{}]", getSql(psc));
		}
		Connection con = getConnection();
		PreparedStatement ps = null;
		try {
			ps = psc.createPreparedStatement(con);
			T result = action.doInPreparedStatement(ps);
			handleWarnings(ps);
			return result;
		} catch (SQLException ex) {
			String sql = getSql(psc);
			psc = null;
			JdbcUtils.closeStatement(ps);
			ps = null;
			releaseConnection(con);
			con = null;
			throw new DataAccessException(sql + "\r" + ex.toString(), ex);
		} finally {
			JdbcUtils.closeStatement(ps);
			releaseConnection(con);
		}
	}

	public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
		return execute(new SimplePreparedStatementCreator(sql), action);
	}

	public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse)
			throws DataAccessException {
		Preconditions.checkNotNull(rse, "ResultSetExtractor must not be null");
		return execute(psc, new PreparedStatementCallback<T>() {
			public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ResultSet rs = null;
				try {
					if (pss != null) {
						pss.setValues(ps);
					}
					rs = ps.executeQuery();
					return rse.extractData(rs);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
		});
	}

	public <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(psc, null, rse);
	}

	public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(new SimplePreparedStatementCreator(sql), pss, rse);
	}

	public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException {
		return query(sql, newArgPreparedStatementSetter(args), rse);
	}

	public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException {
		query(psc, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException {
		query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException {
		query(sql, newArgPreparedStatementSetter(args), rch);
	}

	public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper));
	}

	public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
		List<T> results = query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
		return requiredSingleResult(results);
	}

	public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
		return queryForObject(sql, args, new SingleColumnRowMapper<T>(requiredType));
	}

	public long queryForLong(String sql, Object... args) throws DataAccessException {
		Number number = queryForObject(sql, args, Long.class);
		return (number != null ? number.longValue() : 0);
	}

	public int queryForInt(String sql, Object... args) throws DataAccessException {
		Number number = queryForObject(sql, args, Integer.class);
		return (number != null ? number.intValue() : 0);
	}

	public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
		return query(sql, args, new SingleColumnRowMapper<T>(elementType));
	}

	protected int update(final PreparedStatementCreator psc, final PreparedStatementSetter pss)
			throws DataAccessException {
		if (logger.isDebugEnabled() && JdbcUtils.filterSqlSrc()) {
			logger.debug("Executing prepared SQL update[{}]", getSql(psc));
		}
		return execute(psc, new PreparedStatementCallback<Integer>() {
			public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
				if (pss != null) {
					pss.setValues(ps);
				}
				int rows = ps.executeUpdate();
				if (logger.isDebugEnabled() && JdbcUtils.filterSqlSrc()) {
					logger.debug("SQL update affected {} rows", rows);
				}
				return rows;
			}
		});
	}

	public int update(String sql, Object... args) throws DataAccessException {
		return update(new SimplePreparedStatementCreator(sql), newArgPreparedStatementSetter(args));
	}

	// -------------------------------------------------------------------------
	// Implementation hooks and helper methods
	// -------------------------------------------------------------------------

	/**
	 * Throw a SQLException if we're not ignoring warnings, else log the
	 * warnings (at debug level).
	 * 
	 * @param stmt
	 *            the current JDBC statement
	 * @throws SQLException
	 *             if not ignoring warnings
	 * @see java.sql.SQLWarning
	 */
	private void handleWarnings(Statement stmt) throws SQLException {
		if (logger.isDebugEnabled()) {
			SQLWarning warningToLog = stmt.getWarnings();
			while (warningToLog != null) {
				logger.debug("SQLWarning ignored: SQL state '{}', error code '{}', message [{}]",
						warningToLog.getSQLState(), warningToLog.getErrorCode(), warningToLog.getMessage());
				warningToLog = warningToLog.getNextWarning();
			}
		}
	}

	/**
	 * Create a new ArgPreparedStatementSetter using the args passed in. This
	 * method allows the creation to be overridden by sub-classes.
	 * 
	 * @param args
	 *            object array woth arguments
	 * @return the new ArgPreparedStatementSetter
	 */
	private ArgPreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
		return new ArgPreparedStatementSetter(args);
	}

	/**
	 * Determine SQL from potential provider object.
	 * 
	 * @param sqlProvider
	 *            object that's potentially a SqlProvider
	 * @return the SQL string, or <code>null</code>
	 * @see SqlProvider
	 */
	private static String getSql(Object sqlProvider) {
		if (sqlProvider instanceof SqlProvider) {
			return ((SqlProvider) sqlProvider).getSql();
		} else {
			return null;
		}
	}

	/**
	 * Simple adapter for PreparedStatementCreator, allowing to use a plain SQL
	 * statement.
	 */
	private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

		private final String sql;

		public SimplePreparedStatementCreator(String sql) {
			Preconditions.checkNotNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql);
		}

		public String getSql() {
			return this.sql;
		}
	}

	/**
	 * Adapter to enable use of a RowCallbackHandler inside a
	 * ResultSetExtractor.
	 * <p>
	 * Uses a regular ResultSet, so we have to be careful when using it: We
	 * don't use it for navigating since this could lead to unpredictable
	 * consequences.
	 */
	private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Object> {

		private final RowCallbackHandler rch;

		public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch) {
			this.rch = rch;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			while (rs.next()) {
				this.rch.processRow(rs);
			}
			return null;
		}
	}

	private static <T> T requiredSingleResult(Collection<T> results) {
		int size = (results == null ? 0 : results.size());
		if (size == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}

	// -------------------------------------------------------------------------
	// Methods dealing with callable statements
	// -------------------------------------------------------------------------

	public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
		checkNotNull(csc, "CallableStatementCreator must not be null");
		checkNotNull(action, "Callback object must not be null");
		if (logger.isDebugEnabled()) {
			String sql = getSql(csc);
			logger.debug("Calling stored procedure" + (sql != null ? " [" + sql + "]" : ""));
		}

		Connection con = getConnection();
		CallableStatement cs = null;
		try {
			cs = csc.createCallableStatement(con);
			T result = action.doInCallableStatement(cs);
			handleWarnings(cs);
			return result;
		} catch (SQLException ex) {
			String sql = getSql(csc);
			csc = null;
			JdbcUtils.closeStatement(cs);
			cs = null;
			releaseConnection(con);
			con = null;
			throw new DataAccessException(sql + "\r" + ex.toString(), ex);
		} finally {
			JdbcUtils.closeStatement(cs);
			releaseConnection(con);
		}
	}

	public Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters)
			throws DataAccessException {
		final List<SqlParameter> updateCountParameters = new ArrayList<SqlParameter>();
		final List<SqlParameter> resultSetParameters = new ArrayList<SqlParameter>();
		final List<SqlParameter> callParameters = new ArrayList<SqlParameter>();
		for (SqlParameter parameter : declaredParameters) {
			if (parameter.isResultsParameter()) {
				if (parameter instanceof SqlReturnResultSet) {
					resultSetParameters.add(parameter);
				} else {
					updateCountParameters.add(parameter);
				}
			} else {
				callParameters.add(parameter);
			}
		}
		return execute(csc, new CallableStatementCallback<Map<String, Object>>() {
			public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
				boolean retVal = cs.execute();
				int updateCount = cs.getUpdateCount();
				if (logger.isDebugEnabled()) {
					logger.debug("CallableStatement.execute() returned '" + retVal + "'");
					logger.debug("CallableStatement.getUpdateCount() returned " + updateCount);
				}
				Map<String, Object> returnedResults = new LinkedHashMap<String, Object>();
				if (retVal || updateCount != -1) {
					returnedResults.putAll(extractReturnedResults(cs, updateCountParameters, resultSetParameters,
							updateCount));
				}
				returnedResults.putAll(extractOutputParameters(cs, callParameters));
				return returnedResults;
			}
		});
	}

	/**
	 * Extract returned ResultSets from the completed stored procedure.
	 * 
	 * @param cs
	 *            JDBC wrapper for the stored procedure
	 * @param updateCountParameters
	 *            Parameter list of declared update count parameters for the
	 *            stored procedure
	 * @param resultSetParameters
	 *            Parameter list of declared resturn resultSet parameters for
	 *            the stored procedure
	 * @return Map that contains returned results
	 */
	protected Map<String, Object> extractReturnedResults(CallableStatement cs,
			List<SqlParameter> updateCountParameters, List<SqlParameter> resultSetParameters, int updateCount)
			throws SQLException {

		Map<String, Object> returnedResults = new HashMap<String, Object>();
		int rsIndex = 0;
		int updateIndex = 0;
		boolean moreResults;
		do {
			if (updateCount == -1) {
				if (resultSetParameters != null && resultSetParameters.size() > rsIndex) {
					SqlReturnResultSet declaredRsParam = (SqlReturnResultSet) resultSetParameters.get(rsIndex);
					returnedResults.putAll(processResultSet(cs.getResultSet(), declaredRsParam));
					rsIndex++;
				}
			} else {
				if (updateCountParameters != null && updateCountParameters.size() > updateIndex) {
					SqlReturnUpdateCount ucParam = (SqlReturnUpdateCount) updateCountParameters.get(updateIndex);
					String declaredUcName = ucParam.getName();
					returnedResults.put(declaredUcName, updateCount);
					updateIndex++;
				}
			}
			moreResults = cs.getMoreResults();
			updateCount = cs.getUpdateCount();
			if (logger.isDebugEnabled()) {
				logger.debug("CallableStatement.getUpdateCount() returned " + updateCount);
			}
		} while (moreResults || updateCount != -1);
		return returnedResults;
	}

	/**
	 * Extract output parameters from the completed stored procedure.
	 * 
	 * @param cs
	 *            JDBC wrapper for the stored procedure
	 * @param parameters
	 *            parameter list for the stored procedure
	 * @return Map that contains returned results
	 */
	protected Map<String, Object> extractOutputParameters(CallableStatement cs, List<SqlParameter> parameters)
			throws SQLException {

		Map<String, Object> returnedResults = new HashMap<String, Object>();
		int sqlColIndex = 1;
		for (SqlParameter param : parameters) {
			if (param instanceof SqlOutParameter) {
				SqlOutParameter outParam = (SqlOutParameter) param;
				if (outParam.isReturnTypeSupported()) {
					Object out = outParam.getSqlReturnType().getTypeValue(cs, sqlColIndex, outParam.getSqlType(),
							outParam.getTypeName());
					returnedResults.put(outParam.getName(), out);
				} else {
					Object out = cs.getObject(sqlColIndex);
					if (out instanceof ResultSet) {
						if (outParam.isResultSetSupported()) {
							returnedResults.putAll(processResultSet((ResultSet) out, outParam));
						} else {
							String rsName = outParam.getName();
							SqlReturnResultSet rsParam = new SqlReturnResultSet(rsName, new ColumnMapRowMapper());
							returnedResults.putAll(processResultSet(cs.getResultSet(), rsParam));
							logger.info("Added default SqlReturnResultSet parameter named " + rsName);
						}
					} else {
						returnedResults.put(outParam.getName(), out);
					}
				}
			}
			if (!(param.isResultsParameter())) {
				sqlColIndex++;
			}
		}
		return returnedResults;
	}

	/**
	 * Process the given ResultSet from a stored procedure.
	 * 
	 * @param rs
	 *            the ResultSet to process
	 * @param param
	 *            the corresponding stored procedure parameter
	 * @return Map that contains returned results
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> processResultSet(ResultSet rs, ResultSetSupportingSqlParameter param)
			throws SQLException {
		if (rs == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> returnedResults = new HashMap<String, Object>();
		try {
			ResultSet rsToUse = rs;
			if (param.getRowMapper() != null) {
				RowMapper<?> rowMapper = param.getRowMapper();
				Object result = (new RowMapperResultSetExtractor(rowMapper)).extractData(rsToUse);
				returnedResults.put(param.getName(), result);
			} else if (param.getRowCallbackHandler() != null) {
				RowCallbackHandler rch = param.getRowCallbackHandler();
				(new RowCallbackHandlerResultSetExtractor(rch)).extractData(rsToUse);
				returnedResults.put(param.getName(), "ResultSet returned from stored procedure was processed");
			} else if (param.getResultSetExtractor() != null) {
				Object result = param.getResultSetExtractor().extractData(rsToUse);
				returnedResults.put(param.getName(), result);
			}
		} finally {
			JdbcUtils.closeResultSet(rs);
		}
		return returnedResults;
	}
}
