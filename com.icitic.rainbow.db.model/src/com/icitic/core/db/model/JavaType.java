package com.icitic.core.db.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "JavaType")
@XmlEnum
public enum JavaType {

	INT(Integer.class), // 支持数据类型： SMALLINT, INT
	BOOL(Boolean.class), // 支持数据类型： SMALLINT, INT, CHAR, VARCHAR
	LONG(Long.class), // 支持数据类型： SMALLINT - LONG
	DOUBLE(Double.class), // 支持数据类型： SMALLINT- NUMERIC
	BIGDECIMAL(BigDecimal.class), // 支持数据类型： SMALLINT - NUMERIC
	STRING(String.class), // 支持数据类型： CHAR - NCLOB
	DATE(Date.class), // 支持数据类型： LONG, DATE - VARCHAR
	BYTEARRAY(byte[].class); // 支持数据类型： BLOB
	
	private Class<?> clazz;
	
	public Class<?> getJavaClass() {
		return clazz;
	}
	
	JavaType(Class<?> clazz) {
		this.clazz = clazz;
	}
}
