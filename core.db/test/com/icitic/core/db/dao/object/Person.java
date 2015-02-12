package com.icitic.core.db.dao.object;

import com.icitic.core.model.object.CodeObject;

public class Person extends CodeObject<Integer> {

	private static final long serialVersionUID = 1L;

	private int dept;

	public int getDept() {
		return dept;
	}

	public void setDept(int dept) {
		this.dept = dept;
	}

}
