package com.nexsol.tpa.core.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.core.support.response.ApiResponse;

@RestControllerAdvice
public class ApiControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(CoreApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleCoreApiException(CoreApiException e) {
        CoreApiErrorType errorType = e.getCoreApiErrorType();

        logError(errorType, e);

        ApiResponse<Object> apiResponse = ApiResponse.error(errorType, e.getData());

        return new ResponseEntity<>(apiResponse, errorType.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);

        CoreApiErrorType errorType = CoreApiErrorType.DEFAULT_ERROR;
        ApiResponse<Object> apiResponse = ApiResponse.error(errorType);

        return new ResponseEntity<>(apiResponse, errorType.getStatus());
    }

    private void logError(CoreApiErrorType errorType, Exception e) {
        LogLevel level = errorType.getLogLevel();
        if (level == LogLevel.ERROR) {
            log.error("CoreApiException : {}", e.getMessage(), e);
        } else if (level == LogLevel.WARN) {
            log.warn("CoreApiException : {}", e.getMessage(), e);
        } else {
            log.info("CoreApiException : {}", e.getMessage(), e);
        }
    }
}
