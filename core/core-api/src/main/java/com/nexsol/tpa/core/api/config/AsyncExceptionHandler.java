package com.nexsol.tpa.core.api.config;

import com.nexsol.tpa.core.error.CoreErrorLevel;
import com.nexsol.tpa.core.error.CoreException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable e, Method method, @Nullable Object... params) {
        if (e instanceof CoreException) {
            CoreException coreException = (CoreException) e;
            CoreErrorLevel level = coreException.getErrorType().getLevel();

            switch (level) {
                case ERROR:
                    log.error("CoreException : {}", e.getMessage(), e);
                    break;
                case WARN:
                    log.warn("CoreException : {}", e.getMessage(), e);
                    break;
                default:
                    log.info("CoreException : {}", e.getMessage(), e);
                    break;
            }
        }
        else {
            log.error("Exception : {}", e.getMessage(), e);
        }
    }

}
