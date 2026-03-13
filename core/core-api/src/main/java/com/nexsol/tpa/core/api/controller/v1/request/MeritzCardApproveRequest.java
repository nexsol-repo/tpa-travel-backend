package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeritzCardApproveRequest {

    private String polNo;

    private String estNo;

    private String crdNo;

    private String efctPrd;

    private String dporNm;

    private String dporCd;

    private String apvAmt;
}
