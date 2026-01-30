package com.nexsol.tpa.core.api.meritz.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzCardApproveBody(String gnrAflcoCd, String aflcoDivCd, String bizpeNo, String polNo, String estNo, // 견적번호
                                                                                                                      // (estSave
                                                                                                                      // 결과)
        String crdNo, String efctPrd, String dporNm, String dporCd, String apvAmt // 승인금액
) {
}
