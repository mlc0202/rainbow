package com.icitic.core.util.ioc;

public class TestDepend {
	
	private String name;
	
	private String email;

	public String getName() {
		return name;
	}

	@Inject
	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	@Inject
	public void setEmail(String email) {
		this.email = email;
	}

	
}
