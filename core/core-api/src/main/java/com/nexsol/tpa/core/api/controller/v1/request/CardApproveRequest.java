package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardApproveRequest {

    private String polNo;

    private String quotGrpNo;

    private String quotReqNo;

    private String crdNo;

    private String efctPrd;

    private String dporNm;

    private String dporCd;

    private String rcptPrem;
}
