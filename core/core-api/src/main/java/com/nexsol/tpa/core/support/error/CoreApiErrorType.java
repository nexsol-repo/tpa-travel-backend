package com.nexsol.tpa.core.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum CoreApiErrorType {

    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, CoreApiErrorCode.T500, "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            LogLevel.ERROR),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, CoreApiErrorCode.T400, "요청이 올바르지 않습니다.", LogLevel.INFO),
    NOT_FOUND_DATA(HttpStatus.BAD_REQUEST, CoreApiErrorCode.T401, "해당 데이터를 찾을 수 없습니다.", LogLevel.ERROR);

    private final HttpStatus status;

    private final CoreApiErrorCode code;

    private final String message;

    private final LogLevel logLevel;

    CoreApiErrorType(HttpStatus status, CoreApiErrorCode code, String message, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }

}
