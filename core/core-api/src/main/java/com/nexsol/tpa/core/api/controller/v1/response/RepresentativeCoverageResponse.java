package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RepresentativeCoverageResponse {

    private String covCd;

    private String covNm;

    private String coverageName;

    private long insdAmt;
}
