package com.nexsol.tpa.core.support.error;

import lombok.Getter;

@Getter
public class CoreApiException extends RuntimeException {

    private final CoreApiErrorType coreApiErrorType;

    private final Object data;

    public CoreApiException(CoreApiErrorType coreApiErrorType, Object data) {
        super(coreApiErrorType.getMessage());
        this.coreApiErrorType = coreApiErrorType;
        this.data = data;
    }

    public CoreApiException(CoreApiErrorType coreApiErrorType) {
        // data가 없는 경우 null로 기본 설정
        this(coreApiErrorType, null);
    }

}
