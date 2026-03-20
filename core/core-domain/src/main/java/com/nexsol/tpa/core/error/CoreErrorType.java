package com.nexsol.tpa.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoreErrorType {

	// 공통
	NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T1000, "해당 데이터를 찾지 못했습니다.", CoreErrorLevel.INFO),
	INVALID_REQUEST(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T1001, "요청이 올바르지 않습니다.", CoreErrorLevel.INFO),
	// 보험 계약
	INSURANCE_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T3001, "보험가입데이터를 찾을 수 없습니다.",
			CoreErrorLevel.INFO),
	INSURANCE_MANUAL_CONSULTATION_REQUIRED(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T3002, "현재 조건으로는 가입할수 없습니다.",
			CoreErrorLevel.INFO),
	INSURANCE_NOTFOUND_PEOPLE_DATA(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T3003, "최소 1명 이상의 피보험자가 필요합니다.",
			CoreErrorLevel.INFO),
	INSURANCE_USER_UNAUTHORIZED(CoreErrorKind.CLIENT_ERROR, CoreErrorCode.T3004, "해당 청약서에 대한 접근 권한이 없습니다",
			CoreErrorLevel.INFO),

	// 결제
	PAYMENT_NOT_FOUND_DATA(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T5000, "결제 내역이 없습니다.", CoreErrorLevel.INFO),
	PAYMENT_ALREADY_CANCELLED(CoreErrorKind.SERVER_ERROR, CoreErrorCode.T5001, "이미 취소된 결제건 입니다.", CoreErrorLevel.INFO);

	private final CoreErrorKind kind;

	private final CoreErrorCode code;

	private final String message;

	private final CoreErrorLevel level;

}
