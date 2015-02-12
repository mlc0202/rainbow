package com.icitic.core.db.incrementer;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.test.TestUtils;

public class TestTableIncrementer {

    private static Dao dao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        dao = TestUtils.createInMemeoryDao(TestTableIncrementer.class.getResource("test.rdm"));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestUtils.shutdownInMemoryDao(dao);
    }

    @Test
    public void testNextIntValue() {
        Incrementer incrementer = new TableIncrementer(dao, "T_INT_SEQ");
        assertEquals(1, incrementer.nextIntValue());
        assertEquals(2, incrementer.nextIntValue());
        assertEquals(3, incrementer.nextIntValue());
    }

    @Test
    public void testNextLongValue() {
        Incrementer incrementer1 = new TableIncrementer(dao, "T_LONG_SEQ", "XX");
        assertEquals(1, incrementer1.nextIntValue());
        assertEquals(2, incrementer1.nextIntValue());

        Incrementer incrementer2 = new TableIncrementer(dao, "T_LONG_SEQ", "YY");
        assertEquals(1, incrementer2.nextIntValue());
        assertEquals(2, incrementer2.nextIntValue());

        assertEquals(3, incrementer1.nextIntValue());
        assertEquals(3, incrementer2.nextIntValue());
    }

}