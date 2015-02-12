package com.icitic.core.db.model;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.dao.TestDate;

public class DateTypeTest {

    @Test
    public void testDateType() throws Exception {
        // Dao dao = TestUtils.createInMemeoryDao("testDateTypeColumn.xml");
        Dao dao = TestUtilsOld.createDao("jdbc:db2://192.168.56.107:50000/rainbow", "db2admin", "passw0rd",
            "testDateTypeColumn.xml");
        TestDate obj = new TestDate();

        long dateL = System.currentTimeMillis();

        Date date = new Date(dateL);

        String dateStr = new SimpleDateFormat("yyyyMMdd").format(date);
        String timeStr = new SimpleDateFormat("HHmmss").format(date);

        obj.setDate1(date);
        obj.setDate2(date);
        obj.setDate3(date);
        obj.setDate4(date);
        obj.setDate5(date);
        obj.setDate6(date);
        dao.insert(obj);

        TestDate actObj = dao.fetch("TestDate", TestDate.class, 0);
        assertEquals(0, actObj.getId());
        assertEquals(dateL, actObj.getDate1().getTime());
        assertEquals(dateL, actObj.getDate2().getTime());
        assertEquals(dateStr, new SimpleDateFormat("yyyyMMdd").format(actObj.getDate3()));
        assertEquals(timeStr, new SimpleDateFormat("HHmmss").format(actObj.getDate4()));
        assertEquals(dateL, actObj.getDate5().getTime());
        assertEquals(dateL, actObj.getDate6().getTime());

        // TestUtils.shutdownInMemoryDao(dao);
        TestUtilsOld.shutdownDao(dao);
    }
}
