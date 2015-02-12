package com.icitic.core.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.jdbc.DataAccessException;

public class TestOther {

    @Test
    public void testDao() throws Exception {
        Dao dao = TestUtilsOld.createInMemeoryDao();
        try {
            int count = dao.queryForInt("select count(1) from TBL_TEST");
            assertEquals(0, count);
            fail();
        } catch (DataAccessException e) {
        }
    }
}
