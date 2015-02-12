package com.icitic.core.db.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.icitic.core.db.model.Column;
import com.icitic.core.db.model.ColumnType;
import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.JavaType;

/**
 * 单元测试用的对象
 * 
 * @author lijinghui
 * 
 */
public class DullObject {
	private int id;
	private String name;
	private boolean male;
	private long serial;
	private double weight;
	private BigDecimal salary;
	private Date birth;
	private byte[] code;
	private Color color;
	private Color bgColor;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public long getSerial() {
		return serial;
	}

	public void setSerial(long serial) {
		this.serial = serial;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public byte[] getCode() {
		return code;
	}

	public void setCode(byte[] code) {
		this.code = code;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getBgColor() {
		return bgColor;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public static Entity getEntity() {
		Entity entity = new Entity();
		entity.setCnName("测试实体");
		entity.setDbName("TBL_TEST");
		entity.setName("TestObject");
		List<Column> columns = new ArrayList<Column>();
		entity.setColumns(columns);

		Column column = new Column();
		column.setCnName("编号");
		column.setDbName("ID");
		column.setName("id");
		column.setJavaType(JavaType.INT);
		column.setType(ColumnType.INT);
		column.setKey(true);
		columns.add(column);

		column = new Column();
		column.setCnName("名称");
		column.setDbName("MC");
		column.setName("name");
		column.setJavaType(JavaType.STRING);
		column.setType(ColumnType.VARCHAR);
		column.setLength(10);
		column.setMandatory(true);
		columns.add(column);

		column = new Column();
		column.setCnName("男人");
		column.setDbName("XB");
		column.setName("male");
		column.setJavaType(JavaType.BOOL);
		column.setType(ColumnType.VARCHAR);
		column.setMandatory(true);
		columns.add(column);

		column = new Column();
		column.setCnName("序列号");
		column.setDbName("XLH");
		column.setName("serial");
		column.setJavaType(JavaType.LONG);
		column.setType(ColumnType.LONG);
		column.setMandatory(true);
		columns.add(column);

		column = new Column();
		column.setCnName("体重");
		column.setDbName("TZ");
		column.setName("weight");
		column.setJavaType(JavaType.DOUBLE);
		column.setType(ColumnType.DOUBLE);
		column.setMandatory(true);
		columns.add(column);

		column = new Column();
		column.setCnName("工资");
		column.setDbName("GZ");
		column.setName("salary");
		column.setJavaType(JavaType.BIGDECIMAL);
		column.setType(ColumnType.NUMERIC);
		column.setMandatory(true);
		columns.add(column);
		
		column = new Column();
		column.setCnName("生日");
		column.setDbName("CSRQ");
		column.setName("birth");
		column.setJavaType(JavaType.DATE);
		column.setType(ColumnType.LONG);
		column.setMandatory(true);
		columns.add(column);

		column = new Column();
		column.setCnName("编码");
		column.setDbName("BM");
		column.setName("code");
		column.setJavaType(JavaType.BYTEARRAY);
		column.setType(ColumnType.BLOB);
		column.setMandatory(true);
		columns.add(column);
		
		column = new Column();
		column.setCnName("颜色");
		column.setDbName("YS");
		column.setName("color");
		column.setJavaType(JavaType.STRING);
		column.setType(ColumnType.VARCHAR);
		column.setLength(20);
		columns.add(column);

		column = new Column();
		column.setCnName("背景色");
		column.setDbName("BJS");
		column.setName("bgColor");
		column.setJavaType(JavaType.INT);
		column.setType(ColumnType.SMALLINT);
		columns.add(column);

		entity.afterLoad();
		return entity;
	}

}
