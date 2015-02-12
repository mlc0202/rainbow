package com.icitic.core.db.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.h2.jdbcx.JdbcDataSource;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.DaoImpl;
import com.icitic.core.db.jdbc.DataAccessException;
import com.icitic.core.db.jdbc.JdbcTemplate;
import com.icitic.core.db.jdbc.SimpleDriverDataSource;

final class TestUtilsOld {

    private TestUtilsOld() {
    }

    /**
     * 将Model文件转为DB2建表语句
     * 
     * @param is
     * @return
     */
    public static String db2(InputStream is) {
        return genarate("db2.xsl", is);
    }

    /**
     * 将Model文件转为Oracle建表语句
     * 
     * @param is
     * @return
     */
    public static String oracle(InputStream is) {
        return genarate("oracle.xsl", is);
    }

    /**
     * 将Model文件转为H2建表语句
     * 
     * @param is
     * @return
     */
    public static String h2(InputStream is) {
        return genarate("h2.xsl", is);
    }

    private static String genarate(String xslt, InputStream is) {
        InputStream xsltStream = getClasspathFile(xslt);
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer(new StreamSource(xsltStream));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);
            t.transform(new StreamSource(is), result);
            return new String(baos.toByteArray(), Charsets.UTF_8);
        } catch (TransformerException e) {
            throw Throwables.propagate(e);
        } finally {
            try {
				Closeables.close(xsltStream, true);
			} catch (IOException e) {
			}
        }
    }

    /**
     * 创建H2内存数据库
     * 
     * @return
     */
    public static DataSource createInMemeoryDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    /**
     * 创建数据源
     * 
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static DataSource createDataSource(String url, String userName, String password)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return new SimpleDriverDataSource(getDriver(url), url, userName, password);
    }

    private static Driver getDriver(String url) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        if (url.startsWith("jdbc:h2:")) {
            return (Driver) Class.forName("org.h2.Driver").newInstance();
        }
        if (url.startsWith("jdbc:db2:")) {
            return (Driver) Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
        }
        if (url.startsWith("jdbc:oracle:")) {
            return (Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        }
        return null;
    }

    /**
     * 创建一个使用内存数据库的Dao
     * 
     * @param modelFiles
     * @return
     * @throws IOException
     * @throws JAXBException
     * @throws Exception
     */
    public static Dao createInMemeoryDao(String... modelFiles) throws IOException, JAXBException {
        DataSource dataSource = createInMemeoryDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Entity> entities = new ArrayList<Entity>();

        for (String modelFile : modelFiles) {
            InputStream is = new FileInputStream(modelFile);
            Model model = Model.getXmlBinder().unmarshal(is, true);
            entities.addAll(model.getEntities());
            is = new FileInputStream(modelFile);
            try {
                String sql = h2(is);
                jdbcTemplate.update(sql);
            } finally {
                Closeables.close(is, true);
            }
        }
        Model model = new Model();
        model.setEntities(entities);
        model.afterLoad();
        return new DaoImpl(dataSource, model.getEntityMap());
    }

    /**
     * 创建一个Dao
     * 
     * @param modelFiles
     * @return
     * @throws IOException
     * @throws JAXBException
     * @throws SQLException
     * @throws Exception
     */
    public static Dao createDao(DataSource dataSource, String... modelFiles) throws IOException, JAXBException,
            SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Entity> entities = new ArrayList<Entity>();

        for (String modelFile : modelFiles) {
            InputStream is = getClasspathFile(modelFile);
            Model model = Model.getXmlBinder().unmarshal(is, true);
            entities.addAll(model.getEntities());
            is = getClasspathFile(modelFile);

            try {
                String sql = getInitSql(dataSource, is);
                String[] sqlArray = sql.split(";");
                for (String sqlStr : sqlArray) {
                    if (!sqlStr.trim().isEmpty()) {
                        try {
                            jdbcTemplate.update(sqlStr);
                        } catch (Exception e) {
                        }
                    }
                }
            } finally {
                Closeables.close(is, true);
            }
        }
        Model model = new Model();
        model.setEntities(entities);
        model.afterLoad();
        return new DaoImpl(dataSource, model.getEntityMap());
    }

    /**
     * 创建一个Dao
     * 
     * @param dataSource
     * @param modelFiles
     * @return
     * @throws IOException
     * @throws JAXBException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Dao createDao(String url, String userName, String password, String... modelFiles) throws IOException,
            JAXBException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        DataSource ds = createDataSource(url, userName, password);
        return createDao(ds, modelFiles);
    }

    private static String getInitSql(DataSource ds, InputStream is) throws SQLException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            String pnm = meta.getDatabaseProductName();
            String ver = meta.getDatabaseProductVersion();

            String dbName = String.format("%s::RAINBOW_JDBC::%s", pnm, ver).toLowerCase();
            if (dbName.indexOf("db2") >= 0) {
                return db2(is);
            }
            if (dbName.indexOf("oracle") >= 0) {
                return oracle(is);
            }
            if (dbName.indexOf("h2") >= 0) {
                return h2(is);
            }
            throw new IllegalArgumentException("Can not support database '" + pnm + " " + ver + "'");
        } finally {
            if (null != conn)
                try {
                    conn.close();
                } catch (SQLException e) {
                }
        }
    }

    public static void shutdownInMemoryDao(Dao dao) {
        try {
            dao.execSql("SHUTDOWN");
        } catch (DataAccessException e) {
            // e.printStackTrace();
        }
    }

    public static InputStream getClasspathFile(String file) {
        InputStream is = TestUtilsOld.class.getResourceAsStream(file);
        checkNotNull(is, "file %s not found", file);
        return is;
    }

    public static void shutdownDao(Dao dao) {
        // TODO Auto-generated method stub
    }
}
