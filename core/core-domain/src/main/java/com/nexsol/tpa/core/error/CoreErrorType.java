package com.nexsol.tpa.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoreErrorType {

    NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T1000, "해당 데이터를 찾지 못했습니다.", CoreErrorLevel.INFO),
    // Auth User
    USER_NOT_FOUND(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T1001, "해당 유저를 찾을 수 없습니다.", CoreErrorLevel.INFO),

    USER_EXIST_DATA(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T1003, "해당 유저가 존재합니다.", CoreErrorLevel.INFO),
    INVALID_INPUT(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T1004, "잘못된 입력값입니다.", CoreErrorLevel.INFO),
    TOKEN_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T1005, "존재하지 않는 refresh token 입니다.",
            CoreErrorLevel.INFO),
    AUTH_UNAUTHORIZED(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T1006, "만료된 refresh token 입니다.", CoreErrorLevel.INFO),

    // EmailVerification
    EMAIL_VERIFIED_OVERTIME(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T2001, "인증 시간이 만료되었습니다.", CoreErrorLevel.INFO),
    EMAIL_VERIFIED_INVALID(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T2002, "인증 코드가 일치하지 않습니다.", CoreErrorLevel.INFO),
    EMAIL_VERIFIED_REPEAT(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T2003, "잠시 후 다시 시도해주세요.", CoreErrorLevel.INFO),
    EMAIL_VERIFIED_AUTH(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T2004, "이메일 인증이 완료되지 않았습니다.", CoreErrorLevel.INFO),
    EMAIL_VERIFIED_EMPTY(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T2005, "인증 요청 내역이 없습니다.", CoreErrorLevel.INFO),

    // Insurance
    INSURANCE_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T3001, "보험가입데이터를 찾을 수 없습니다.",
            CoreErrorLevel.INFO),
    INSURANCE_MANUAL_CONSULTATION_REQUIRED(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T3002, "현재 조건으로는 가입할수 없습니다.",
            CoreErrorLevel.INFO),
    INSURANCE_RATE_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T3003, "요율 정보가 없습니다.", CoreErrorLevel.INFO),
    INSURANCE_USER_UNAUTHORIZED(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T3004, "해당 청약서에 대한 접근 권한이 없습니다",
            CoreErrorLevel.INFO),
    INSURANCE_NOT_COMPLETED(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T3005, "가입이 완료된 보험만 사고접수가 가능합니다.",
            CoreErrorLevel.INFO),
    INSURANCE_DUPLICATE_PLANT_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T3006, "이미 등록된 발전소입니다.",
            CoreErrorLevel.INFO),

    // FileUpload
    FILE_UPLOAD_VALIDATION_KEY(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T4000, "파일 키는 필수 입니다.", CoreErrorLevel.INFO),
    FILE_UPLOAD_VALIDATION_CONTENT(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T4001, "모든 서류는 PDF 형식이어야 합니다.",
            CoreErrorLevel.INFO),
    FILE_UPLOAD_VALIDATION_IMAGE(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T4002, "서명은 이미지 파일만 가능합니다.",
            CoreErrorLevel.INFO),
    FILE_UPLOAD_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T4003, "파일이 존재하지 않습니다.", CoreErrorLevel.INFO),
    FILE_UPLOAD_UNAUTHORIZED(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T4004, "잘못된 접근이거나 권한이 없는 파일입니다.",
            CoreErrorLevel.INFO),

    PAYMENT_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T5000, "결제 내역이 없습니다.", CoreErrorLevel.INFO),
    PAYMENT_ALREADY_CANCELLED(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T5001, "이미 취소된 결제건 입니다.", CoreErrorLevel.INFO);

    private final CoreErrorKind kind;

    private final CoreErrorCode code;

    private final String message;

    private final CoreErrorLevel level;

}
