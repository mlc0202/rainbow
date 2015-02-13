package com.icitic.core.model.exception;

public class DuplicateCodeException extends AppException {

    private static final long serialVersionUID = 1L;

    public DuplicateCodeException(String code) {
        super(String.format("代码值【%s】重复", code));
    }

}
