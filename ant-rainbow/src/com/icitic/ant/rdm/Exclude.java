package com.icitic.ant.rdm;

/**
 * 输出DDL时要排除的实体
 * 
 * @author lijinghui
 *
 */
public class Exclude {

	/**
	 * 实体所在模型
	 */
	private String model;
	
	/**
	 * 实体dbName
	 */
	private String entity;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}
	
}
