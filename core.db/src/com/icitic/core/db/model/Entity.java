package com.icitic.core.db.model;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.icitic.core.model.object.INameObject;
import com.icitic.core.util.Utils;

@XmlType(name = "Entity", propOrder = { "name", "dbName", "cnName", "columns", "indexes" })
public class Entity implements INameObject {

	/**
	 * 实体名
	 */
	@XmlElement(required = true)
	private String name;

	/**
	 * 实体的数据库名字
	 */
	@XmlElement(required = true)
	private String dbName;

	/**
	 * 实体的中文名字
	 */
	@XmlElement(required = true)
	private String cnName;

	/**
	 * 实体的属性列表
	 */
	@XmlElementWrapper(name = "columns")
	@XmlElement(name = "column", required = true)
	private List<Column> columns;

    /**
     * 实体的索引列表
     */
    @XmlElementWrapper(name = "indexes")
    @XmlElement(name = "index", required = true)
    private List<Index> indexes;
    
	@XmlTransient
	private Map<String, Column> columnMap;

	@XmlTransient
	private List<Column> keys;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getCnName() {
		return cnName;
	}

	public void setCnName(String cnName) {
		this.cnName = cnName;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public Column getColumn(String columnName) {
		return columnMap.get(columnName);
	}

    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Index> indexes) {
        this.indexes = indexes;
    }

	public void afterLoad() {
		ImmutableMap.Builder<String, Column> mapBuilder = ImmutableMap.builder();
		ImmutableList.Builder<Column> keyBuilder = ImmutableList.builder();

		checkState(!Utils.isNullOrEmpty(columns), "Entity %s has no column", getName());
		for (Column column : columns) {
			checkNotNull(column.getName(), "Entity %s has a null name column", getName());
			mapBuilder.put(column.getName(), column);
			if (column.isKey())
				keyBuilder.add(column);
		}
		columnMap = mapBuilder.build();
		keys = keyBuilder.build();
	}

	public List<Column> getKeys() {
		return keys;
	}

	public int getKeyCount() {
		return keys.size();
	}

}
