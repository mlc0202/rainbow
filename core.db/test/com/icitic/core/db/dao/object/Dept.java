package com.icitic.core.db.dao.object;

import com.icitic.core.model.object.NestObject;

public class Dept extends NestObject<Integer> {
	
	private static final long serialVersionUID = 1L;
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
