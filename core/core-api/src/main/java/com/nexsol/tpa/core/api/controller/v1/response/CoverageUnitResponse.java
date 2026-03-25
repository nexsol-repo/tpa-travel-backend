package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoverageUnitResponse {

    private String ageBandCode;

    private String ageBandLabel;

    private Integer count;

    private Long insdAmt;

    private Long premSum;
}
