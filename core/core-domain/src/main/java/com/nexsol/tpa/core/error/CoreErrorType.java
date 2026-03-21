package com.nexsol.tpa.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoreErrorType {

    // 공통
    DEFAULT_ERROR(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T500,
            "알 수 없는 오류가 발생했습니다.",
            CoreErrorLevel.ERROR),
    NOT_FOUND_DATA(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T1000,
            "해당 데이터를 찾지 못했습니다.",
            CoreErrorLevel.INFO),
    INVALID_REQUEST(
            CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T1001, "요청이 올바르지 않습니다.", CoreErrorLevel.INFO),

    // 견적
    INVALID_QUOTE_REQUEST(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T2000,
            "견적 요청이 올바르지 않습니다.",
            CoreErrorLevel.INFO),
    QUOTE_PLAN_NOT_FOUND(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T2001,
            "요청한 플랜을 찾을 수 없습니다.",
            CoreErrorLevel.ERROR),
    PREMIUM_CALCULATION_FAILED(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T2002,
            "보험료 산출에 실패했습니다.",
            CoreErrorLevel.ERROR),

    // 계약
    INVALID_CONTRACT_REQUEST(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T3000,
            "계약 요청이 올바르지 않습니다.",
            CoreErrorLevel.INFO),
    CONTRACT_API_FAILED(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T3001,
            "계약 API 호출에 실패했습니다.",
            CoreErrorLevel.ERROR),
    CONTRACT_ALREADY_PAID(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T3002,
            "이미 결제 완료된 계약입니다.",
            CoreErrorLevel.WARN),
    INSURANCE_NOT_FOUND_DATA(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T3003,
            "보험가입데이터를 찾을 수 없습니다.",
            CoreErrorLevel.INFO),
    INSURANCE_MANUAL_CONSULTATION_REQUIRED(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T3004,
            "현재 조건으로는 가입할수 없습니다.",
            CoreErrorLevel.INFO),
    INSURANCE_NOTFOUND_PEOPLE_DATA(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T3005,
            "최소 1명 이상의 피보험자가 필요합니다.",
            CoreErrorLevel.INFO),
    INSURANCE_USER_UNAUTHORIZED(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T3006,
            "해당 청약서에 대한 접근 권한이 없습니다",
            CoreErrorLevel.INFO),

    // 참조데이터
    REFERENCE_API_FAILED(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T5002,
            "참조데이터 API 호출에 실패했습니다.",
            CoreErrorLevel.ERROR),
    REFERENCE_PARSE_FAILED(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T5003,
            "참조데이터 응답 파싱에 실패했습니다.",
            CoreErrorLevel.ERROR),

    // 결제
    PAYMENT_NOT_FOUND_DATA(
            CoreErrorKind.SERVER_ERROR, CoreErrorCode.T5000, "결제 내역이 없습니다.", CoreErrorLevel.INFO),
    PAYMENT_ALREADY_CANCELLED(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T5001,
            "이미 취소된 결제건 입니다.",
            CoreErrorLevel.INFO),

    // 본인인증
    INVALID_AUTH_REQUEST(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T6000,
            "본인인증 요청이 올바르지 않습니다.",
            CoreErrorLevel.INFO),
    AUTH_CONTRACT_NOT_FOUND(
            CoreErrorKind.CLIENT_ERROR,
            CoreErrorCode.T6001,
            "본인인증 대상 계약을 찾을 수 없습니다.",
            CoreErrorLevel.ERROR),
    AUTH_CERTIFICATION_FAILED(
            CoreErrorKind.SERVER_ERROR,
            CoreErrorCode.T6002,
            "본인인증 처리에 실패했습니다.",
            CoreErrorLevel.ERROR);

    private final CoreErrorKind kind;

    private final CoreErrorCode code;

    private final String message;

    private final CoreErrorLevel level;
}
