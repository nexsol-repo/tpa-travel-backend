package com.nexsol.tpa.core.api.meritz.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzCardCancelBody(String gnrAflcoCd, String aflcoDivCd, String bizpeNo, String polNo, String estNo,
        String orgApvNo, // 원 승인번호
        String cncAmt // 취소금액
) {
}
