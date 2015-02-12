package com.icitic.core.db.dao;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.icitic.core.db.model.Entity;
import com.icitic.core.model.object.NameObject;

public class TestNeoBean {

	private static byte[] ba = "晚来天欲雪，能饮一杯无".getBytes();
	private BigDecimal bd = new BigDecimal("1279.3456778");
	private Date date = new Date();

	@Test
	public void testSetGet() {
		Entity entity = DullObject.getEntity();

		NeoBean neo = new NeoBean(entity);
		neo.setInt("id", 100);
		neo.setString("name", "小宝");
		neo.setBoolean("male", true);
		neo.setLong("serial", 5);
		neo.setDouble("weight", 78.5);
		neo.setBigDecimal("salary", bd);
		neo.setDate("birth", date);
		neo.setByteArray("code", ba);
		neo.setEnum("color", Color.RED);
		neo.setEnum("bgColor", Color.GREEN);

		checkNeo(neo);
		// 以下代码检查枚举对应的存储类型
		assertEquals("RED", neo.getString("color"));
		assertEquals(Color.GREEN.ordinal(), neo.getInt("bgColor").intValue());
		// 以下代码检查设日期参数为long的情况
		neo.setDate("birth", date.getTime());
		assertEquals(date, neo.getDate("birth"));
		// 以下代码检查bigDecimal设为数字时的情况
		neo.setBigDecimal("salary", 1279.3456778);
		assertEquals(bd, neo.getBigDecimal("salary"));
	}

	@Test
	public void testSetObject() {
		Entity entity = DullObject.getEntity();
		NeoBean neo = new NeoBean(entity);
		Map<String, Object> map = Maps.newHashMap();
		map.put("id", 100);
		map.put("name", "小宝");
		map.put("male", true);
		map.put("serial", 5);
		map.put("weight", 78.5);
		map.put("salary", 1279.3456778);
		map.put("birth", date);
		map.put("code", ba);
		map.put("color", Color.RED);
		map.put("bgColor", Color.GREEN);
		neo.init(map);
		checkNeo(neo);

		DullObject obj = new DullObject();
		obj.setId(100);
		obj.setName("小宝");
		obj.setMale(true);
		obj.setSerial(5);
		obj.setWeight(78.5);
		obj.setSalary(bd);
		obj.setBirth(date);
		obj.setCode(ba);
		obj.setColor(Color.RED);
		obj.setBgColor(Color.GREEN);
		neo = new NeoBean(entity, obj);
		checkNeo(neo);
	}

	private void checkNeo(NeoBean neo) {
		assertEquals(100, neo.getInt("id").intValue());
		assertEquals("小宝", neo.getString("name"));
		assertTrue(neo.getBoolean("male"));
		assertEquals(5, neo.getLong("serial").longValue());
		assertEquals(Double.valueOf(78.5), neo.getDouble("weight"));
		assertEquals(bd, neo.getBigDecimal("salary"));
		assertArrayEquals(ba, neo.getByteArray("code"));
		assertEquals(date, neo.getDate("birth"));
		assertEquals(Color.RED, neo.getEnum("color", Color.class));
		assertEquals(Color.GREEN, neo.getEnum("bgColor", Color.class));
	}

	@Test
	public void testBianShen() {
		Entity entity = DullObject.getEntity();
		NeoBean neo = new NeoBean(entity);
		neo.setInt("id", 100);
		neo.setString("name", "小宝");
		neo.setBoolean("male", true);
		neo.setLong("serial", 5);
		neo.setDouble("weight", 78.5);
		neo.setBigDecimal("salary", bd);
		neo.setDate("birth", date);
		neo.setByteArray("code", ba);
		neo.setEnum("color", Color.RED);
		neo.setEnum("bgColor", Color.GREEN);

		DullObject obj = neo.bianShen(DullObject.class, null);
		assertEquals(100, obj.getId());
		assertEquals("小宝", obj.getName());
		assertTrue(obj.isMale());
		assertEquals(5, obj.getSerial());
		assertEquals(78.5, obj.getWeight(), 0);
		assertEquals(bd, obj.getSalary());
		assertArrayEquals(ba, obj.getCode());
		assertEquals(date, obj.getBirth());
		assertEquals(Color.RED, obj.getColor());
		assertEquals(Color.GREEN, obj.getBgColor());

		// 变身时传递有的属性
		NameObject<?> no = neo.bianShen(NameObject.class, null); 
		assertEquals("小宝", no.getName());
	}

}
