package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoverageAmountResponse {

    private String covCd;

    private String covNm;

    private String coverageName;

    private long insdAmt;

    private String cur;

    private List<CoverageUnitResponse> units;
}
