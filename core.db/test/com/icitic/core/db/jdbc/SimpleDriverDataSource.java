package com.icitic.core.db.jdbc;

import static com.google.common.base.Preconditions.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import com.google.common.base.Throwables;

/**
 * Simple implementation of the standard JDBC {@link javax.sql.DataSource} interface,
 * configuring a plain old JDBC {@link java.sql.Driver} via bean properties, and returning
 * a new {@link java.sql.Connection} from every <code>getConnection</code> call.
 *
*/
public class SimpleDriverDataSource extends AbstractDriverBasedDataSource {

	private Driver driver;


	/**
	 * Constructor for bean-style configuration.
	 */
	public SimpleDriverDataSource() {
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard Driver parameters.
	 * @param driver the JDBC Driver object
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url) {
		setDriver(driver);
		setUrl(url);
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard Driver parameters.
	 * @param driver the JDBC Driver object
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param username the JDBC username to use for accessing the DriverManager
	 * @param password the JDBC password to use for accessing the DriverManager
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url, String username, String password) {
		setDriver(driver);
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard Driver parameters.
	 * @param driver the JDBC Driver object
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param conProps JDBC connection properties
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public SimpleDriverDataSource(Driver driver, String url, Properties conProps) {
		setDriver(driver);
		setUrl(url);
		setConnectionProperties(conProps);
	}


	/**
	 * Specify the JDBC Driver implementation class to use.
	 * <p>An instance of this Driver class will be created and held
	 * within the SimpleDriverDataSource.
	 * @see #setDriver
	 */
	public void setDriverClass(Class<? extends Driver> driverClass) {
		try {
			this.driver = driverClass.newInstance();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Specify the JDBC Driver instance to use.
	 * <p>This allows for passing in a shared, possibly pre-configured
	 * Driver instance.
	 * @see #setDriverClass
	 */
	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Return the JDBC Driver instance to use.
	 */
	public Driver getDriver() {
		return this.driver;
	}


	@Override
	protected Connection getConnectionFromDriver(Properties props) throws SQLException {
		Driver driver = getDriver();
		String url = getUrl();
		checkNotNull(driver, "Driver must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new JDBC Driver Connection to [{}]", url);
		}
		return driver.connect(url, props);
	}

}
