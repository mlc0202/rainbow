package com.icitic.binding.json;

import java.util.Date;

public class Child {

	private String name;

	private Date birth;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public Child() {
	}

	public Child(String name, Date birth) {
		super();
		this.name = name;
		this.birth = birth;
	}

}
