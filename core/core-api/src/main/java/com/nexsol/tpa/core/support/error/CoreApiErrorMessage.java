package com.nexsol.tpa.core.support.error;

public record CoreApiErrorMessage(String code, String message, Object data) {

    public CoreApiErrorMessage(CoreApiErrorType errorType, Object data) {
        this(errorType.getCode().name(), errorType.getMessage(), data);
    }
}
