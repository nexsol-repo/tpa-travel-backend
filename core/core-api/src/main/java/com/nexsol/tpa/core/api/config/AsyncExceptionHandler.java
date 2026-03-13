package com.nexsol.tpa.core.api.config;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.logging.LogLevel;

import com.nexsol.tpa.core.support.error.CoreApiException;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable e, Method method, @Nullable Object... params) {
        if (e instanceof CoreApiException coreApiException) {
            LogLevel level = coreApiException.getCoreApiErrorType().getLogLevel();

            if (level == LogLevel.ERROR) {
                log.error("CoreApiException : {}", e.getMessage(), e);
            } else if (level == LogLevel.WARN) {
                log.warn("CoreApiException : {}", e.getMessage(), e);
            } else {
                log.info("CoreApiException : {}", e.getMessage(), e);
            }
        } else {
            log.error("Exception : {}", e.getMessage(), e);
        }
    }
}
