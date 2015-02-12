package com.icitic.core.db.test;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.DaoImpl;
import com.icitic.core.db.jdbc.DataAccessException;
import com.icitic.core.db.jdbc.JdbcTemplate;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.Model;
import com.icitic.core.util.ioc.Bean;
import com.icitic.core.util.ioc.Context;

public final class TestUtils {

    private TestUtils() {
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
     * 创建一个使用内存数据库的Dao
     * 
     * @param models
     *            数据库模型描述，可以是文件名、File或URL
     * 
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public static Dao createInMemeoryDao(Object... models) throws IOException, JAXBException {
        URL[] urls = new URL[models.length];
        for (int i = 0; i < models.length; i++) {
            Object model = models[i];
            if (model instanceof URL)
                urls[i] = (URL) model;
            else if (model instanceof String) {
                File file = new File((String) model);
                urls[i] = file.toURI().toURL();
            } else if (model instanceof File) {
                urls[i] = ((File) model).toURI().toURL();
            } else
                throw new IllegalArgumentException("bad model param at " + i);
        }
        return createInMemeoryDao(urls);
    }

    public static Dao createInMemeoryDao(URL... modelUrls) throws IOException, JAXBException {
        DataSource dataSource = createInMemeoryDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Entity> entities = new ArrayList<Entity>();

        for (URL url : modelUrls) {
            InputStream is = url.openStream();
            Model model = Model.getXmlBinder().unmarshal(is, true);
            entities.addAll(model.getEntities());
            is = url.openStream();
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

    public static void shutdownInMemoryDao(Dao dao) {
        try {
            dao.execSql("SHUTDOWN");
        } catch (DataAccessException e) {
            // e.printStackTrace();
        }
    }

    public static InputStream getClasspathFile(String file) {
        InputStream is = TestUtils.class.getResourceAsStream(file);
        checkNotNull(is, "file %s not found", file);
        return is;
    }
    
    public static Context createManagerContext() {
    	Map<String, Bean> beans = ImmutableMap.<String, Bean> builder() //
    			.put("cacheManager", Bean.singleton(FakeCacheManager.class)) //
    			.put("objectManager", Bean.singleton(FakeObjectManager.class)) //
    			.build();
    	return new Context(beans);
    }
}
