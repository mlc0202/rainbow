package com.icitic.core.model.exception;


public class DuplicateNameException extends AppException {

	private static final long serialVersionUID = 1L;

	public DuplicateNameException(String name) {
        super(String.format("名称【%s】重复", name));
	}
	
}
