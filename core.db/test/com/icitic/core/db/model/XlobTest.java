package com.icitic.core.db.model;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.TestXlob;

public class XlobTest {
    @Test
    public void testXlob() throws Exception {
        // Dao dao = TestUtils
        // .createDao("jdbc:db2://192.168.56.107:50000/rainbow", "db2admin",
        // "passw0rd", "testXlob.xml");
        Dao dao = TestUtilsOld.createInMemeoryDao("testXlob.xml");
        TestXlob obj = new TestXlob();

        InputStream is = null;
        DataInputStream di = null;
        try {
            is = TestUtilsOld.getClasspathFile("testXlob.xml");
            di = new DataInputStream(is);
            byte[] data = new byte[is.available()];
            di.read(data);
            obj.setBlob(data);
        } finally {
            try {
                di.close();
            } catch (IOException e) {
            }
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        obj.setClob("CLOB_中文！·￥#·￥%￥%……&");
        dao.insert(obj);

        TestXlob actObj = dao.fetch("TestXlob", TestXlob.class, 0);
        assertEquals(0, actObj.getId());
        byte[] blob1 = obj.getBlob();
        byte[] blob2 = actObj.getBlob();
        assertEquals(blob1.length, blob2.length);
        for (int i = 0; i < blob1.length; i++) {
            assertEquals(blob1[i], blob2[i]);
        }
        assertEquals(obj.getClob(), actObj.getClob());

        // TestUtils.shutdownDao(dao);
        TestUtilsOld.shutdownInMemoryDao(dao);
    }
}
