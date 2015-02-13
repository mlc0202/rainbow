package com.icitic.core.util.ioc;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * 测试IOC代码的正确性
 * 
 * @author lijinghui
 * 
 */
public class TestIOC {

	@Test
	public void testGetBean() {
		long now = System.currentTimeMillis();
		Context container = new Context(ImmutableMap.of( //
				"depend", Bean.singleton(Double.valueOf(1827.99)), //
				"timestamp", Bean.singleton(Long.valueOf(now)), //
				"number", Bean.singleton(Long.valueOf(10)), //
				"tom", Bean.singleton(TestObject.class), //
				"kitty", Bean.prototype(TestObject.class) //
				));
		TestObject object1 = container.getBean("kitty", TestObject.class);
		try {
			Thread.sleep(100); // 如果不这么写，多核的cpu有时候object2会先于object1设置时间
		} catch (InterruptedException e) {
		}
		TestObject object2 = container.getBean("kitty", TestObject.class);
		object2.setNumber(container.getBean("number", Long.class));
		assertNotSame(object1, object2);
		assertEquals(5, object1.getNumber().longValue()); // setNumber没写@Inject不会自动注入 
		assertEquals(10, object2.getNumber().longValue());
		assertEquals(Double.valueOf(1827.99), object1.getDepend());
		assertEquals(now, object1.getTimestamp().longValue());
		assertEquals(object1.getTimestamp(), object2.getTimestamp());

		container.setBean("number", Long.valueOf(20));
		object1 = container.getBean("tom", TestObject.class);
		object2 = (TestObject) container.getBean("tom");
		object2.setNumber(container.getBean("number", Long.class));
		assertSame(object1, object2);
		assertEquals(20, object1.getNumber().longValue());
		assertEquals(now, object1.getTimestamp().longValue());

		object1 = container.getBean(TestObject.class);
		assertNotNull(object1);
	}

	@Test
	public void testGetBeanDepend() {
		Context container = new Context(ImmutableMap.of(
				//
				"name", Bean.singleton(String.valueOf("rainbow")), "email",
				Bean.singleton(String.valueOf("rayboy@rainbow.com")), "kitty", Bean.singleton(TestDepend.class) //
				));
		TestDepend object = container.getBean("kitty", TestDepend.class);
		assertEquals(object.getName(), object.getName(), "rainbow");
		assertEquals(object.getEmail(), object.getEmail(), "rayboy@rainbow.com");
	}

	@Test
	public void testGetBeanNoDepend() {
		Context container = new Context(ImmutableMap.of( //
				"kitty", Bean.prototype(TestDepend.class) //
				));
		try {
			container.getBean("kitty", TestDepend.class);
			fail();
		} catch (BeanInitializationException e) {
		}

		Context container1 = new Context(ImmutableMap.of( //
				"name", Bean.singleton("rainbow"), //
				"email", Bean.singleton("jinghui70@163.com"), //
				"kitty", Bean.prototype(TestDepend.class) //
				));
		TestDepend testDepend = container1.getBean("kitty", TestDepend.class);
		assertEquals("rainbow", testDepend.getName());
		assertEquals("jinghui70@163.com", testDepend.getEmail());
	}

	@Test
	public void testSetBean() {
		long now = System.currentTimeMillis();
		Context container = new Context(ImmutableMap.of( //
				"depend", Bean.singleton(Double.class), //
				"timestamp", Bean.singleton(Long.valueOf(now), Long.class), //
				"main", Bean.prototype(TestObject.class) //
				));
		try {
			container.setBean("depend", Boolean.TRUE);
			fail();
		} catch (BeanNotOfRequiredTypeException e) {
		}
		container.setBean("depend", Double.valueOf(2011.10));
		TestObject main = container.getBean(TestObject.class);
		assertEquals(2011.1, main.getDepend(), 0);
		assertEquals(now, main.getTimestamp().longValue());
	}

	@Test
	public void testParam() {
		long now = System.currentTimeMillis();
		Context container = new Context(ImmutableMap.of( //
				"depend", Bean.singleton(Double.valueOf(1)), //
				"timestamp", Bean.singleton(Long.valueOf(1)), //
				"main", Bean.prototype(TestObject.class).set("timestamp", now).set("depend", 2011.10), //
				"simple", Bean.prototype(TestObject.class) //
				));
		TestObject main = container.getBean(TestObject.class);
		assertEquals(2011.1, main.getDepend(), 0);
		assertEquals(now, main.getTimestamp().longValue());

		TestObject simple = container.getBean("simple", TestObject.class);
		assertEquals(1, simple.getDepend(), 0);
		assertEquals(1, simple.getTimestamp().longValue());
	}
}
