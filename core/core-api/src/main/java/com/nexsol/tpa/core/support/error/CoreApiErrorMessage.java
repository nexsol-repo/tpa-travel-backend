package com.nexsol.tpa.core.support.error;

import com.nexsol.tpa.core.error.CoreErrorType;

public record CoreApiErrorMessage(String code, String message, Object data) {

    public CoreApiErrorMessage(CoreErrorType coreErrorType, Object data) {
        this(coreErrorType.getCode().name(), coreErrorType.getMessage(), data);
    }
}