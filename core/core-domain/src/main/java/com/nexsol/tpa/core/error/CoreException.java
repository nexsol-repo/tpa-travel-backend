package com.nexsol.tpa.core.error;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

	private final CoreErrorType errorType;

	private final Object data;

	public CoreException(CoreErrorType errorType) {

		super(errorType.getMessage());
		this.errorType = errorType;
		this.data = null;
	}

	public CoreException(CoreErrorType errorType, String customMessage) {
		super(customMessage);
		this.errorType = errorType;
		this.data = null;
	}

	public CoreException(CoreErrorType errorType, Object data) {
		super(errorType.getMessage());
		this.errorType = errorType;
		this.data = data;
	}

}