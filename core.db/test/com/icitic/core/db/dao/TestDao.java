package com.icitic.core.db.dao;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icitic.core.db.dao.object.Device;
import com.icitic.core.db.dao.object.Person;
import com.icitic.core.db.test.TestUtils;

public class TestDao {

	private static Dao dao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dao = TestUtils.createInMemeoryDao(TestDao.class.getResource("object/test.rdm"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestUtils.shutdownInMemoryDao(dao);
	}

	@Before
	public void setUp() throws Exception {
		dao.getJdbcTemplate().getTransactionManager().beginTransaction();
	}

	@After
	public void tearDown() throws Exception {
		dao.getJdbcTemplate().getTransactionManager().rollback();
	}

	@Test
	// 测试一下有别名的时候有没有问题
	public void testAliasSelect() {
		// 这里借用一下Device作为接收数据的类
		Person person = new Person();
		person.setId(100);
		person.setDept(200);
		person.setName("aaaa");
		dao.insert(person);

		SqlBuilder sb = SqlBuilder.select("id dept", "count(1) id").from("Person");
		Device device = dao.fetch(sb, Device.class);
		assertEquals(Integer.valueOf(1), device.getId());
		assertEquals(100, device.getDept());

	}

}
