package com.nexsol.tpa.core.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexsol.tpa.core.error.CoreErrorKind;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;
import com.nexsol.tpa.core.support.response.ApiResponse;

@RestControllerAdvice
public class ApiControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<Object>> handleCoreException(CoreException e) {

        CoreErrorType errorType = e.getErrorType();

        logError(errorType, e);

        ApiResponse<Object> apiResponse = ApiResponse.error(errorType, null);

        HttpStatus status = mapKindToHttpStatus(errorType.getKind());

        return new ResponseEntity<>(apiResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);

        ApiResponse<Object> apiResponse = ApiResponse.error(CoreErrorType.NOT_FOUND_DATA, null);

        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logError(CoreErrorType errorType, Exception e) {
        switch (errorType.getLevel()) {
            case ERROR -> log.error("CoreException : {}", e.getMessage(), e);
            case WARN -> log.warn("CoreException : {}", e.getMessage(), e);
            default -> log.info("CoreException : {}", e.getMessage(), e);
        }
    }

    /**
     * 도메인 에러의 Kind를 HTTP Status로 번역
     */
    private HttpStatus mapKindToHttpStatus(CoreErrorKind kind) {
        return switch (kind) {
            case CLIENT_ERROR -> HttpStatus.BAD_REQUEST; // 400
            case SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR; // 500
                // (향후 UNAUTHORIZED, FORBIDDEN 등 Kind가 추가되면 여기만 수정)
        };
    }
}
