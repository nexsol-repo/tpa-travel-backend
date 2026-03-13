package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractCompletedRequest {

    private Long contractId;

    // 카드정보만 받는다 (기획서 기준)
    private String cardNo; // 카드번호

    private String efctPrd; // 유효기간(YYYYMM)

    private String dporNm; // 예금주명

    private String dporCd; // 예금주코드
}
