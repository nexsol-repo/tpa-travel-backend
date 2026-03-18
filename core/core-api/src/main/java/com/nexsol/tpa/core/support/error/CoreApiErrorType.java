package com.nexsol.tpa.core.support.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum CoreApiErrorType {
    DEFAULT_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            CoreApiErrorCode.T500,
            "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            LogLevel.ERROR),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, CoreApiErrorCode.T400, "요청이 올바르지 않습니다.", LogLevel.INFO),
    NOT_FOUND_DATA(
            HttpStatus.BAD_REQUEST, CoreApiErrorCode.T401, "해당 데이터를 찾을 수 없습니다.", LogLevel.ERROR),

    // 견적
    INVALID_QUOTE_REQUEST(
            HttpStatus.BAD_REQUEST, CoreApiErrorCode.T1000, "견적 요청이 올바르지 않습니다.", LogLevel.INFO),
    QUOTE_PLAN_NOT_FOUND(
            HttpStatus.BAD_REQUEST, CoreApiErrorCode.T1001, "요청한 플랜을 찾을 수 없습니다.", LogLevel.ERROR),
    PREMIUM_CALCULATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            CoreApiErrorCode.T1002,
            "보험료 산출에 실패했습니다.",
            LogLevel.ERROR),

    // 참조데이터
    REFERENCE_API_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            CoreApiErrorCode.T2000,
            "참조데이터 API 호출에 실패했습니다.",
            LogLevel.ERROR),
    REFERENCE_PARSE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            CoreApiErrorCode.T2001,
            "참조데이터 응답 파싱에 실패했습니다.",
            LogLevel.ERROR),

    // 계약
    INVALID_CONTRACT_REQUEST(
            HttpStatus.BAD_REQUEST, CoreApiErrorCode.T3000, "계약 요청이 올바르지 않습니다.", LogLevel.INFO),
    CONTRACT_API_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            CoreApiErrorCode.T3001,
            "계약 API 호출에 실패했습니다.",
            LogLevel.ERROR),
    CONTRACT_ALREADY_PAID(
            HttpStatus.BAD_REQUEST, CoreApiErrorCode.T3002, "이미 결제 완료된 계약입니다.", LogLevel.WARN),

    // 본인인증
    INVALID_AUTH_REQUEST(
            HttpStatus.BAD_REQUEST, CoreApiErrorCode.T5000, "본인인증 요청이 올바르지 않습니다.", LogLevel.INFO),
    AUTH_CONTRACT_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            CoreApiErrorCode.T5001,
            "본인인증 대상 계약을 찾을 수 없습니다.",
            LogLevel.ERROR),
    AUTH_CERTIFICATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            CoreApiErrorCode.T5002,
            "본인인증 처리에 실패했습니다.",
            LogLevel.ERROR);

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
