package com.icitic.core.db.incrementer;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icitic.core.db.dao.Dao;
import com.icitic.core.db.test.TestUtils;

public class TestMaxIdIncrementer {

	private static Dao dao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = TestUtils.createInMemeoryDao(TestMaxIdIncrementer.class.getResource("test.rdm"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestUtils.shutdownInMemoryDao(dao);
	}

	private MaxIdIncrementer createIncrementer() {
		MaxIdIncrementer incrementer = new MaxIdIncrementer();
		incrementer.setDao(dao);
		return incrementer;
	}

	@Test
	public void testNextIntValue() {
		MaxIdIncrementer incrementer = createIncrementer();
		incrementer.setTblName("T_INT");
		assertEquals(1, incrementer.nextIntValue());
		dao.execSql("insert into T_INT values(4, 'apple')");
		assertEquals(5, incrementer.nextIntValue());

		dao.execSql("delete from T_INT");
		incrementer = createIncrementer();
		incrementer.setEntityName("IntTest");
		assertEquals(1, incrementer.nextIntValue());
		dao.execSql("insert into T_INT values(4, 'apple')");
		assertEquals(5, incrementer.nextIntValue());
	}

	@Test
	public void testNextLongValue() {
		long now = System.currentTimeMillis();
		MaxIdIncrementer incrementer = createIncrementer();
		incrementer.setTblName("T_LONG");
		assertEquals(1, incrementer.nextLongValue());
		dao.execSql("insert into T_LONG values(?, 'apple')", now);
		assertEquals(now + 1, incrementer.nextLongValue());

		dao.execSql("delete from T_LONG");
		incrementer = createIncrementer();
		incrementer.setEntityName("LongTest");
		assertEquals(1, incrementer.nextLongValue());
		dao.execSql("insert into T_LONG values(?, 'apple')", now);
		assertEquals(now + 1, incrementer.nextLongValue());
	}

	@Test
	public void testNextStringValue() {
		dao.execSql("delete from T_INT");
		MaxIdIncrementer incrementer = createIncrementer();
		incrementer.setLength(6);
		incrementer.setTblName("T_INT");
		assertEquals("000001", incrementer.nextStringValue());
		dao.execSql("insert into T_INT values(4, 'apple')");
		assertEquals("000005", incrementer.nextStringValue());

		dao.execSql("delete from T_INT");
		incrementer = createIncrementer();
		incrementer.setLength(3);
		incrementer.setEntityName("IntTest");
		assertEquals("001", incrementer.nextStringValue());
		dao.execSql("insert into T_INT values(4, 'apple')");
		assertEquals("005", incrementer.nextStringValue());
	}

}
