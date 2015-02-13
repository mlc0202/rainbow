package com.icitic.core.util;

import com.icitic.core.model.exception.AppException;

public final class Preconditions {
	private Preconditions() {
	}

	public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs)
			throws AppException {
		if (!expression) {
			throw new AppException(errorMessageTemplate, errorMessageArgs);
		}
	}

	public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs)
			throws AppException {
		if (!expression) {
			throw new AppException(errorMessageTemplate, errorMessageArgs);
		}
	}

	public static <T> T checkNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs)
			throws AppException {
		if (reference == null) {
			throw new AppException(errorMessageTemplate, errorMessageArgs);
		}
		return reference;
	}
}
