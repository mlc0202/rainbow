package com.icitic.core.db.dao;

/**
 * Sql零件，可以输出为sql的一部分
 * 
 * @author lijinghui
 * 
 */
public interface SqlPart {

	void toSql(StringBuilder sb);

}
