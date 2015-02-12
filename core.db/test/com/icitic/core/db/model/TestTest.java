package com.icitic.core.db.model;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.icitic.core.db.dao.Color;
import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.NeoBean;
import com.icitic.core.db.dao.DullObject;

public class TestTest {

    public void testCreateModelXml() throws Exception {
        Model model = new Model();
        model.setEntities(Lists.newArrayList(DullObject.getEntity()));
        model.afterLoad();

        FileOutputStream os = new FileOutputStream("testModel.xml");
        Model.getXmlBinder().marshal(model, os);
        os.close();
    }

    @Test
    public void testDao() throws Exception {
        Dao dao = TestUtilsOld.createInMemeoryDao("testModel.xml");
        NeoBean neo = dao.newNeoBean("TestObject");
        neo.setInt("id", 1);
        neo.setString("name", "小宝");
        neo.setEnum("color", Color.BLUE);
        neo.setEnum("bgColor", Color.GREEN);
        dao.insert(neo);

        DullObject obj = dao.fetch("TestObject", DullObject.class, 1);
        assertEquals(1, obj.getId());
        assertEquals("小宝", obj.getName());
        assertEquals(Color.BLUE, obj.getColor());
        assertEquals(Color.GREEN, obj.getBgColor());
        
        TestUtilsOld.shutdownInMemoryDao(dao);
    }
}
